/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.hunting;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.fabricmc.loader.api.FabricLoader;

import java.util.*;

/**
 * PortalScanner — classifies nether portal frames by construction origin.
 *
 * OW-origin:       4×5 outer frame with all 4 corners filled (14 obsidian).
 * Nether-origin:   minimum frame, corners absent (10 obsidian, corners air/other).
 * AutoPortal-built: minimum frame, possibly floating (no ground below base).
 *
 * Detects active portals (NETHER_PORTAL blocks) and inspects the surrounding
 * obsidian frame to classify them. Works on chunk load so no tick overhead.
 */
public class PortalScanner extends Module {

    // ── Setting groups ───────────────────────────────────────────────────────
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender  = settings.createGroup("Render");
    private final SettingGroup sgAlert   = settings.createGroup("Alert");

    // ── General ──────────────────────────────────────────────────────────────
    private final Setting<Integer> scanRadius = sgGeneral.add(new IntSetting.Builder()
        .name("scan-radius")
        .description("Chunk radius to accept for scanning on load.")
        .defaultValue(5)
        .min(1)
        .sliderRange(1, 10)
        .build()
    );

    private final Setting<Boolean> highlightOW = sgGeneral.add(new BoolSetting.Builder()
        .name("highlight-ow-portals")
        .description("Render OW-origin portals (14-obsidian full frame).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> highlightNether = sgGeneral.add(new BoolSetting.Builder()
        .name("highlight-nether-portals")
        .description("Render Nether-origin portals (10-obsidian minimum frame).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> netherOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("nether-only")
        .description("Only scan chunks while in the Nether dimension.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> saveToWaypoints = sgGeneral.add(new BoolSetting.Builder()
        .name("save-to-waypoints")
        .description("Create Xaero's Minimap waypoints at detected portals (requires Xaero's Minimap).")
        .defaultValue(false)
        .visible(() -> FabricLoader.getInstance().isModLoaded("xaerominimap"))
        .build()
    );

    // ── Render ───────────────────────────────────────────────────────────────
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> owSideColor = sgRender.add(new ColorSetting.Builder()
        .name("ow-portal-color")
        .description("Fill color for OW-origin portals.")
        .defaultValue(new SettingColor(0, 160, 255, 40))
        .build()
    );

    private final Setting<SettingColor> owLineColor = sgRender.add(new ColorSetting.Builder()
        .name("ow-portal-line-color")
        .description("Outline color for OW-origin portals.")
        .defaultValue(new SettingColor(0, 160, 255, 255))
        .build()
    );

    private final Setting<SettingColor> netherSideColor = sgRender.add(new ColorSetting.Builder()
        .name("nether-portal-color")
        .description("Fill color for Nether-origin portals.")
        .defaultValue(new SettingColor(255, 60, 0, 40))
        .build()
    );

    private final Setting<SettingColor> netherLineColor = sgRender.add(new ColorSetting.Builder()
        .name("nether-portal-line-color")
        .description("Outline color for Nether-origin portals.")
        .defaultValue(new SettingColor(255, 60, 0, 255))
        .build()
    );

    // ── Alert ────────────────────────────────────────────────────────────────
    private final Setting<String> webhookLink = sgAlert.add(new StringSetting.Builder()
        .name("webhook-link")
        .description("Discord webhook URL. Leave blank to disable.")
        .defaultValue("")
        .build()
    );

    // ── Internal data ─────────────────────────────────────────────────────────
    public enum PortalType { OW_ORIGIN, NETHER_ORIGIN, AUTOPORTAL }

    private static class PortalEntry {
        final BlockPos       frameMin;  // bottom-left corner of the outer obsidian frame
        final BlockPos       frameMax;  // top-right corner of the outer obsidian frame
        final Direction.Axis axis;
        final PortalType     type;
        final boolean        floating;

        PortalEntry(BlockPos min, BlockPos max, Direction.Axis axis, PortalType type, boolean floating) {
            this.frameMin = min;
            this.frameMax = max;
            this.axis     = axis;
            this.type     = type;
            this.floating = floating;
        }
    }

    /** Root portal block → entry, to deduplicate portals in the same frame. */
    private final Map<BlockPos, PortalEntry> detectedPortals = new LinkedHashMap<>();
    /** Positions of portal interior blocks already processed, to skip re-scanning the same frame. */
    private final Set<BlockPos> processedPortalBlocks = new HashSet<>();

    public PortalScanner() {
        super(Categories.Hunting,
            "portal-scanner",
            "Scans loaded chunks for nether portals and classifies them by construction origin.");
    }

    @Override
    public void onActivate() {
        detectedPortals.clear();
        processedPortalBlocks.clear();
    }

    @Override
    public void onDeactivate() {
        detectedPortals.clear();
        processedPortalBlocks.clear();
    }

    // ── Chunk scan ────────────────────────────────────────────────────────────

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (netherOnly.get() && !mc.world.getRegistryKey().equals(World.NETHER)) return;

        int playerChunkX = mc.player.getBlockPos().getX() >> 4;
        int playerChunkZ = mc.player.getBlockPos().getZ() >> 4;
        Chunk chunk = event.chunk();

        if (Math.abs(chunk.getPos().x - playerChunkX) > scanRadius.get() ||
            Math.abs(chunk.getPos().z - playerChunkZ) > scanRadius.get()) return;

        scanChunkForPortals(chunk);
    }

    private void scanChunkForPortals(Chunk chunk) {
        int startX  = chunk.getPos().getStartX();
        int startZ  = chunk.getPos().getStartZ();
        int bottomY = mc.world.getBottomY();
        int topY    = mc.world.getBottomY() + mc.world.getHeight();

        for (int x = startX; x <= chunk.getPos().getEndX(); x++) {
            for (int z = startZ; z <= chunk.getPos().getEndZ(); z++) {
                for (int y = bottomY; y < topY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = chunk.getBlockState(pos);

                    if (state.getBlock() != Blocks.NETHER_PORTAL) continue;
                    if (processedPortalBlocks.contains(pos.toImmutable())) continue;

                    analyzePortal(pos, state);
                }
            }
        }
    }

    /**
     * Starting from one portal block, flood-fills connected NETHER_PORTAL blocks in the
     * portal plane, then inspects the surrounding obsidian frame.
     */
    private void analyzePortal(BlockPos startPos, BlockState startState) {
        if (mc.world == null) return;

        // Portal AXIS: X = portal interior spans the X direction (frame columns at min/max Z)
        //              Z = portal interior spans the Z direction (frame columns at min/max X)
        Direction.Axis axis;
        if (startState.contains(Properties.HORIZONTAL_AXIS)) {
            axis = startState.get(Properties.HORIZONTAL_AXIS);
        } else {
            axis = Direction.Axis.X; // fallback
        }

        // ── BFS over connected portal blocks ──────────────────────────────────
        Set<BlockPos> portalBlocks = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos cur = queue.poll();
            if (!portalBlocks.add(cur)) continue;

            // Portal blocks connect up/down and along the non-axis horizontal direction
            BlockPos[] neighbours = {
                cur.up(), cur.down(),
                axis == Direction.Axis.X ? cur.east() : cur.north(),
                axis == Direction.Axis.X ? cur.west() : cur.south()
            };
            for (BlockPos nb : neighbours) {
                if (portalBlocks.contains(nb)) continue;
                BlockState nbState = mc.world.getBlockState(nb);
                if (nbState.getBlock() == Blocks.NETHER_PORTAL) queue.add(nb);
            }
        }

        // Mark all of these so we don't re-process
        for (BlockPos p : portalBlocks) processedPortalBlocks.add(p.toImmutable());

        // ── Bounding box of portal interior ───────────────────────────────────
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos p : portalBlocks) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
            if (p.getZ() < minZ) minZ = p.getZ();
            if (p.getZ() > maxZ) maxZ = p.getZ();
        }

        // ── Frame positions (outer ring around bounding box) ──────────────────
        // For standard 2-wide interior, minX==maxX (Z-axis) or minZ==maxZ (X-axis)
        // Frame columns and rows are just outside the bounding box.
        // We enumerate all 10 non-corner + 4 corner positions.

        List<BlockPos> nonCornerFrame = new ArrayList<>();
        List<BlockPos> cornerFrame    = new ArrayList<>();

        if (axis == Direction.Axis.X) {
            // Portal extends along X, frame columns at z-1 and z+1
            int fz = minZ; // all portal blocks share the same Z
            // Bottom row (non-corner)
            for (int x = minX; x <= maxX; x++)
                nonCornerFrame.add(new BlockPos(x, minY - 1, fz));
            // Top row (non-corner)
            for (int x = minX; x <= maxX; x++)
                nonCornerFrame.add(new BlockPos(x, maxY + 1, fz));
            // Left column (non-corner, excluding bottom/top overlap)
            for (int y = minY; y <= maxY; y++)
                nonCornerFrame.add(new BlockPos(minX - 1, y, fz));
            // Right column (non-corner)
            for (int y = minY; y <= maxY; y++)
                nonCornerFrame.add(new BlockPos(maxX + 1, y, fz));
            // Corners
            cornerFrame.add(new BlockPos(minX - 1, minY - 1, fz));
            cornerFrame.add(new BlockPos(maxX + 1, minY - 1, fz));
            cornerFrame.add(new BlockPos(minX - 1, maxY + 1, fz));
            cornerFrame.add(new BlockPos(maxX + 1, maxY + 1, fz));
        } else {
            // Portal extends along Z, frame columns at x-1 and x+1
            int fx = minX;
            // Bottom row (non-corner)
            for (int z = minZ; z <= maxZ; z++)
                nonCornerFrame.add(new BlockPos(fx, minY - 1, z));
            // Top row (non-corner)
            for (int z = minZ; z <= maxZ; z++)
                nonCornerFrame.add(new BlockPos(fx, maxY + 1, z));
            // Left column (non-corner)
            for (int y = minY; y <= maxY; y++)
                nonCornerFrame.add(new BlockPos(fx, y, minZ - 1));
            // Right column (non-corner)
            for (int y = minY; y <= maxY; y++)
                nonCornerFrame.add(new BlockPos(fx, y, maxZ + 1));
            // Corners
            cornerFrame.add(new BlockPos(fx, minY - 1, minZ - 1));
            cornerFrame.add(new BlockPos(fx, minY - 1, maxZ + 1));
            cornerFrame.add(new BlockPos(fx, maxY + 1, minZ - 1));
            cornerFrame.add(new BlockPos(fx, maxY + 1, maxZ + 1));
        }

        // Count obsidian in non-corner and corner positions
        int nonCornerObs = 0;
        for (BlockPos p : nonCornerFrame)
            if (mc.world.getBlockState(p).getBlock() == Blocks.OBSIDIAN) nonCornerObs++;

        int cornerObs = 0;
        for (BlockPos p : cornerFrame)
            if (mc.world.getBlockState(p).getBlock() == Blocks.OBSIDIAN) cornerObs++;

        // Need at least a minimum valid frame (all 10 non-corner positions filled)
        if (nonCornerObs < nonCornerFrame.size()) return;

        // Classify
        PortalType type;
        if (cornerObs == 4) {
            type = PortalType.OW_ORIGIN;
        } else {
            // Check if floating (no solid block below any bottom-frame block)
            boolean floating = isFloating(nonCornerFrame, axis, minY);
            type = floating ? PortalType.AUTOPORTAL : PortalType.NETHER_ORIGIN;
        }

        // Outer frame bounding box for rendering
        BlockPos frameMin, frameMax;
        if (axis == Direction.Axis.X) {
            int fz = minZ;
            frameMin = new BlockPos(minX - 1, minY - 1, fz);
            frameMax = new BlockPos(maxX + 1, maxY + 1, fz);
        } else {
            int fx = minX;
            frameMin = new BlockPos(fx, minY - 1, minZ - 1);
            frameMax = new BlockPos(fx, maxY + 1, maxZ + 1);
        }

        BlockPos key = startPos.toImmutable();
        if (detectedPortals.containsKey(key)) return; // already registered

        PortalEntry entry = new PortalEntry(frameMin.toImmutable(), frameMax.toImmutable(), axis, type,
            type == PortalType.AUTOPORTAL);
        detectedPortals.put(key, entry);

        String coords = startPos.getX() + " " + startPos.getY() + " " + startPos.getZ();
        String label  = portalTypeLabel(type, entry.floating);
        info("Portal detected [§b" + label + "§r] at " + coords);

        // Webhook
        if (!webhookLink.get().isEmpty()) {
            final int totalObs = nonCornerObs + cornerObs;
            String playerName = mc.player != null ? mc.player.getGameProfile().getName() : "Unknown";
            new Thread(() -> HuntingUtils.sendWebhook(
                webhookLink.get(),
                "PortalScanner: " + label,
                "Type: **" + label + "**\\nCoords: `" + coords + "`\\n"
                    + "Frame obsidian: " + totalObs + "/14",
                null,
                playerName
            )).start();
        }

        // Xaero waypoints
        if (saveToWaypoints.get() && FabricLoader.getInstance().isModLoaded("xaerominimap")) {
            tryAddWaypoint(startPos, type);
        }
    }

    /** Returns true if there is no solid block directly below the bottom frame row. */
    private boolean isFloating(List<BlockPos> nonCornerFrame, Direction.Axis axis, int minY) {
        if (mc.world == null) return false;
        // Bottom row positions are those with y == minY - 1
        for (BlockPos p : nonCornerFrame) {
            if (p.getY() == minY - 1) {
                BlockState below = mc.world.getBlockState(p.down());
                if (below.isSolidBlock(mc.world, p.down())) return false;
            }
        }
        return true;
    }

    private static String portalTypeLabel(PortalType type, boolean floating) {
        return switch (type) {
            case OW_ORIGIN     -> "OW-Origin";
            case NETHER_ORIGIN -> "Nether-Origin";
            case AUTOPORTAL    -> floating ? "AutoPortal (Floating)" : "AutoPortal";
        };
    }

    /** Attempts to add a Xaero waypoint at the portal position. */
    private void tryAddWaypoint(BlockPos pos, PortalType type) {
        try {
            xaero.hud.minimap.module.MinimapSession session =
                xaero.hud.minimap.BuiltInHudModules.MINIMAP.getCurrentSession();
            if (session == null) return;

            xaero.hud.minimap.world.MinimapWorld world = session.getWorldManager().getCurrentWorld();
            if (world == null) return;

            xaero.hud.minimap.waypoint.set.WaypointSet waypointSet =
                world.getCurrentWaypointSet();
            if (waypointSet == null) return;

            int color = switch (type) {
                case OW_ORIGIN     -> 0;  // blue index
                case NETHER_ORIGIN -> 4;  // red index
                case AUTOPORTAL    -> 3;  // orange index
            };

            char symbol = switch (type) {
                case OW_ORIGIN     -> 'P';
                case NETHER_ORIGIN -> 'N';
                case AUTOPORTAL    -> 'A';
            };

            xaero.common.minimap.waypoints.Waypoint waypoint =
                new xaero.common.minimap.waypoints.Waypoint(
                    pos.getX(), pos.getY(), pos.getZ(),
                    portalTypeLabel(type, type == PortalType.AUTOPORTAL),
                    String.valueOf(symbol),
                    color, 0, false
                );
            waypointSet.add(waypoint);
            xaero.map.mods.SupportMods.xaeroMinimap.requestWaypointsRefresh();
        } catch (Throwable ignored) {
            // Xaero API may differ between versions; fail silently
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        for (PortalEntry entry : detectedPortals.values()) {
            boolean isOW     = entry.type == PortalType.OW_ORIGIN;
            boolean isNether = entry.type == PortalType.NETHER_ORIGIN || entry.type == PortalType.AUTOPORTAL;

            if (isOW && !highlightOW.get()) continue;
            if (isNether && !highlightNether.get()) continue;

            SettingColor side = isOW ? owSideColor.get() : netherSideColor.get();
            SettingColor line = isOW ? owLineColor.get()  : netherLineColor.get();

            // Build an axis-aligned box around the outer frame
            Box box = buildFrameBox(entry);
            if (box != null) event.renderer.box(box, side, line, shapeMode.get(), 0);
        }
    }

    private Box buildFrameBox(PortalEntry e) {
        if (e.axis == Direction.Axis.X) {
            // Portal in the XY plane: thin box (1 block deep in Z)
            return new Box(
                e.frameMin.getX(),     e.frameMin.getY(),     e.frameMin.getZ(),
                e.frameMax.getX() + 1, e.frameMax.getY() + 1, e.frameMax.getZ() + 1
            );
        } else {
            // Portal in the ZY plane: thin box (1 block deep in X)
            return new Box(
                e.frameMin.getX(),     e.frameMin.getY(),     e.frameMin.getZ(),
                e.frameMax.getX() + 1, e.frameMax.getY() + 1, e.frameMax.getZ() + 1
            );
        }
    }
}
