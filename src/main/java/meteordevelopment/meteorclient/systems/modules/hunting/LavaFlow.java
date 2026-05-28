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
import net.minecraft.client.render.Camera;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.Map;

/**
 * LavaFlow — Nether portal skip detector.
 *
 * On Paper/Folia servers fluid ticks are region-gated: lava only advances
 * when a player is actively loading the region. Any flowing lava whose
 * level indicates it travelled >= flowThreshold blocks is therefore strong
 * evidence that a real player loaded those chunks. This module detects
 * that pattern on chunk load and alerts via render / Discord webhook.
 */
public class LavaFlow extends Module {

    // ── Setting groups ───────────────────────────────────────────────────────
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender   = settings.createGroup("Render");
    private final SettingGroup sgAlert    = settings.createGroup("Alert");

    // ── General ──────────────────────────────────────────────────────────────
    private final Setting<Integer> flowThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("flow-threshold")
        .description("Minimum lava flow distance (blocks from source) to trigger detection.")
        .defaultValue(5)
        .min(3)
        .sliderRange(3, 7)
        .build()
    );

    private final Setting<Integer> scanRadius = sgGeneral.add(new IntSetting.Builder()
        .name("scan-radius")
        .description("Chunk radius around the player to accept incoming chunks for scanning.")
        .defaultValue(3)
        .min(1)
        .sliderRange(1, 8)
        .build()
    );

    private final Setting<Boolean> netherOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("nether-only")
        .description("Only scan chunks while in the Nether dimension.")
        .defaultValue(true)
        .build()
    );

    // ── Render ───────────────────────────────────────────────────────────────
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Show a highlight box over detected flow blocks.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> flowSideColor = sgRender.add(new ColorSetting.Builder()
        .name("flow-color")
        .description("Fill color for detected lava flow blocks.")
        .defaultValue(new SettingColor(255, 80, 0, 60))
        .build()
    );

    private final Setting<SettingColor> flowLineColor = sgRender.add(new ColorSetting.Builder()
        .name("flow-line-color")
        .description("Outline color for detected lava flow blocks.")
        .defaultValue(new SettingColor(255, 80, 0, 255))
        .build()
    );

    // ── Alert ────────────────────────────────────────────────────────────────
    private final Setting<String> webhookLink = sgAlert.add(new StringSetting.Builder()
        .name("webhook-link")
        .description("Discord webhook URL. Leave blank to disable.")
        .defaultValue("")
        .build()
    );

    private final Setting<Integer> alertCooldown = sgAlert.add(new IntSetting.Builder()
        .name("alert-cooldown")
        .description("Minimum seconds between duplicate alerts for the same chunk.")
        .defaultValue(30)
        .min(5)
        .sliderRange(5, 120)
        .build()
    );

    // ── State ─────────────────────────────────────────────────────────────────
    /** BlockPos of the furthest-flowing lava block found → last alert timestamp (ms). */
    private final Map<BlockPos, Long>    alertTimestamps = new HashMap<>();
    /** BlockPos → flow distance, kept across chunk loads for rendering. */
    private final Map<BlockPos, Integer> detectedFlows   = new HashMap<>();

    public LavaFlow() {
        super(Categories.Hunting,
            "lava-flow",
            "Detects lava flows from player activity — portal skip detection (Folia/Paper aware).");
    }

    @Override
    public void onActivate() {
        alertTimestamps.clear();
        detectedFlows.clear();
    }

    @Override
    public void onDeactivate() {
        alertTimestamps.clear();
        detectedFlows.clear();
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

        scanChunk(chunk);
    }

    /**
     * Single-pass scan over all blocks in the chunk.
     * Flowing lava FluidState level: 7 = 1 block from source, 1 = 7 blocks from source.
     * Flow distance = 8 - level.
     *
     * On Folia/Paper, fluid ticks are region-local — lava only advances when a player
     * is loading that region, so any flow >= threshold is credible evidence of player activity.
     */
    private void scanChunk(Chunk chunk) {
        int maxFlowDist = 0;
        BlockPos maxFlowPos = null;

        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();
        int endX   = chunk.getPos().getEndX();
        int endZ   = chunk.getPos().getEndZ();
        int bottomY = mc.world.getBottomY();
        int topY    = mc.world.getBottomY() + mc.world.getHeight();

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                for (int y = bottomY; y < topY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    FluidState fs = chunk.getFluidState(pos);

                    // FLOWING_LAVA (not source) — level 1..7
                    if (fs.getFluid() == Fluids.FLOWING_LAVA) {
                        int flowDist = 8 - fs.getLevel(); // 1 = adjacent, 7 = max range
                        if (flowDist > maxFlowDist) {
                            maxFlowDist = flowDist;
                            maxFlowPos  = pos.toImmutable();
                        }
                    }
                }
            }
        }

        if (maxFlowPos != null && maxFlowDist >= flowThreshold.get()) {
            handleDetection(maxFlowPos, maxFlowDist);
        }
    }

    private void handleDetection(BlockPos pos, int flowDist) {
        // Always track for rendering
        detectedFlows.put(pos, flowDist);

        long now      = System.currentTimeMillis();
        long lastSent = alertTimestamps.getOrDefault(pos, 0L);
        if (now - lastSent < alertCooldown.get() * 1_000L) return;
        alertTimestamps.put(pos, now);

        String coords    = pos.getX() + " " + pos.getY() + " " + pos.getZ();
        String playerName = (mc.player != null) ? mc.player.getGameProfile().getName() : "Unknown";

        info("LavaFlow: distance §c" + flowDist + "§r at " + coords);

        if (!webhookLink.get().isEmpty()) {
            new Thread(() -> HuntingUtils.sendWebhook(
                webhookLink.get(),
                "LavaFlow: Portal Skip Evidence",
                "Flow distance **" + flowDist + "** (threshold: " + flowThreshold.get() + ")\\nAt: `" + coords + "`",
                null,
                playerName
            )).start();
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get() || mc.player == null) return;
        Camera camera = mc.gameRenderer.getCamera();
        double camX = camera.getPos().x;
        double camY = camera.getPos().y;
        double camZ = camera.getPos().z;

        for (BlockPos pos : detectedFlows.keySet()) {
            double ox = pos.getX() - camX;
            double oy = pos.getY() - camY;
            double oz = pos.getZ() - camZ;
            event.renderer.box(ox, oy, oz, ox + 1, oy + 1, oz + 1,
                flowSideColor.get(), flowLineColor.get(), shapeMode.get(), 0);
        }
    }
}
