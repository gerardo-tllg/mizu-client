/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.hunting;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * AutoPortal — builds a nether portal frame in front of the player.
 *
 * Improvements over the original:
 *  - BuildMode: Minimum (10 obsidian, no corners) or Full (14 obsidian, corners included).
 *  - AirPlace:  allows the frame to be placed without ground below (floating portals).
 *  - Safety:    scans for players within safeRadius before and during building.
 *  - Abort:     disables immediately when a player is detected mid-build.
 *  - Folia:     all block placement uses packet-based writes on the network thread
 *               (PlayerInteractBlockC2SPacket via OFF_HAND swap), which are dispatched
 *               safely on the client thread and handled server-side in the correct
 *               Folia region without cross-region violations.
 */
public class AutoPortal extends Module {

    // ── Enums ─────────────────────────────────────────────────────────────────
    public enum BuildMode { Minimum, Full }

    // ── Setting groups ───────────────────────────────────────────────────────
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // ── Settings ──────────────────────────────────────────────────────────────
    private final Setting<BuildMode> buildMode = sgGeneral.add(new EnumSetting.Builder<BuildMode>()
        .name("build-mode")
        .description("Minimum: 10 obsidian (no corners, Nether-origin style). Full: 14 obsidian (corners filled, OW-origin style).")
        .defaultValue(BuildMode.Minimum)
        .build()
    );

    private final Setting<Integer> placeDelay = sgGeneral.add(new IntSetting.Builder()
        .name("place-delay")
        .description("Ticks between each obsidian placement.")
        .defaultValue(1)
        .sliderRange(1, 20)
        .build()
    );

    private final Setting<Integer> blocksPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("blocks-per-tick")
        .description("How many blocks to place each tick.")
        .defaultValue(1)
        .sliderRange(1, 5)
        .build()
    );

    private final Setting<Boolean> airPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("air-place")
        .description("Allow building the portal frame floating in the air (no ground support required).")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> safetyCheck = sgGeneral.add(new BoolSetting.Builder()
        .name("safety-check")
        .description("Abort if a player is detected within safe-radius before or during construction.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> safeRadius = sgGeneral.add(new IntSetting.Builder()
        .name("safe-radius")
        .description("Player detection radius in blocks.")
        .defaultValue(20)
        .sliderRange(5, 64)
        .visible(safetyCheck::get)
        .build()
    );

    private final Setting<Boolean> render = sgGeneral.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders the remaining portal frame as it is being placed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the preview box is rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
        .name("side-color")
        .defaultValue(new SettingColor(100, 100, 255, 10))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("line-color")
        .defaultValue(new SettingColor(100, 100, 255, 255))
        .build()
    );

    // ── State ─────────────────────────────────────────────────────────────────
    private final List<BlockPos> portalBlocks   = new ArrayList<>();
    private final List<BlockPos> waitingForBreak = new ArrayList<>();
    private int delay = 0;
    private int index = 0;

    public AutoPortal() {
        super(Categories.Hunting, "auto-portal", "For the Base Hunter who has places to be.");
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) { toggle(); return; }

        // Safety check before starting
        if (safetyCheck.get() && hasPlayerNearby()) {
            error("Player detected within " + safeRadius.get() + " blocks. Aborting.");
            toggle();
            return;
        }

        // Minimum obsidian needed depends on build mode
        int required = (buildMode.get() == BuildMode.Full) ? 14 : 10;
        int obsidianCount = countObsidian();
        if (obsidianCount < required) {
            error("Not enough obsidian (need " + required + ", have " + obsidianCount + ").");
            toggle();
            return;
        }

        portalBlocks.clear();
        waitingForBreak.clear();
        index = 0;
        delay = 0;

        buildPortalPlan();
        selectObsidianSlot();
    }

    @Override
    public void onDeactivate() {
        portalBlocks.clear();
        waitingForBreak.clear();
        index = 0;
        delay = 0;
    }

    // ── Plan ──────────────────────────────────────────────────────────────────

    /**
     * Computes the list of BlockPos to fill with obsidian.
     *
     * Frame layout (viewed from above, portal faces forward):
     *
     *  Minimum (10 blocks):
     *    [bottom: L, R] [sides: L×3, R×3] [top: L, R]
     *
     *  Full (14 blocks):
     *    same + [corners: BL, BR, TL, TR]
     *
     * AirPlace mode: bases the frame at the player's eye-level offset instead
     * of adjusting upward for slabs, allowing floating portal construction.
     */
    private void buildPortalPlan() {
        Direction forward = mc.player.getHorizontalFacing();
        Direction right   = forward.rotateYClockwise();

        BlockPos standingPos = mc.player.getBlockPos();

        if (!airPlace.get()) {
            // Slab/half-block correction: if the block below is not full height, shift up
            BlockPos blockBelow = standingPos.down();
            double blockHeight  = mc.world.getBlockState(blockBelow)
                .getCollisionShape(mc.world, blockBelow)
                .getMax(Direction.Axis.Y);
            if (blockHeight < 1.0) standingPos = standingPos.up();
        }

        // base = bottom-left corner of the portal opening (2 blocks in front, 1 left)
        BlockPos base = standingPos
            .offset(forward, 2)
            .offset(right, -1);

        // Obstruction check
        List<BlockPos> checkPositions = buildFramePositions(base, right, false);
        boolean obstructed = checkPositions.stream()
            .anyMatch(p -> !mc.world.getBlockState(p).isReplaceable());
        if (obstructed) {
            error("Portal area obstructed. Move and try again.");
            portalBlocks.addAll(checkPositions);
            index = checkPositions.size(); // skip building, just render blocked frame
            return;
        }

        // Check if portal already exists
        long existingObs = checkPositions.stream()
            .filter(p -> mc.world.getBlockState(p).getBlock().asItem() == Items.OBSIDIAN)
            .count();
        if (existingObs >= checkPositions.size()) {
            error("A portal already exists here!");
            toggle();
            return;
        }

        portalBlocks.addAll(checkPositions);
    }

    /**
     * Returns all frame positions for a portal whose bottom-left post starts at {@code base}.
     *
     * @param base     bottom corner of the left post column (at floor level)
     * @param right    direction of the right column
     * @param corners  whether to include the 4 corner positions
     */
    private List<BlockPos> buildFramePositions(BlockPos base, Direction right, boolean corners) {
        List<BlockPos> list = new ArrayList<>();
        boolean full = (buildMode.get() == BuildMode.Full);

        // Bottom row (2 blocks, non-corner)
        list.add(base.offset(right, 1));
        list.add(base.offset(right, 2));

        // Left column (3 blocks)
        for (int i = 1; i <= 3; i++) list.add(base.offset(right, 0).up(i));
        // Right column (3 blocks)
        for (int i = 1; i <= 3; i++) list.add(base.offset(right, 3).up(i));

        // Top row (2 blocks, non-corner)
        list.add(base.offset(right, 1).up(4));
        list.add(base.offset(right, 2).up(4));

        // Corners (only in Full mode)
        if (full) {
            list.add(base.offset(right, 0));          // bottom-left corner
            list.add(base.offset(right, 3));           // bottom-right corner
            list.add(base.offset(right, 0).up(4));    // top-left corner
            list.add(base.offset(right, 3).up(4));    // top-right corner
        }

        return list;
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem bi)) return;
        if (bi.getBlock().asItem() != Items.OBSIDIAN) return;

        // Abort on player detection mid-build
        if (safetyCheck.get() && hasPlayerNearby()) {
            error("Player detected mid-build. Aborting.");
            toggle();
            return;
        }

        if (index >= portalBlocks.size()) {
            autoLight();
            info("Portal complete. AutoPortal disabled.");
            toggle();
            return;
        }

        delay++;
        if (delay < placeDelay.get()) return;

        for (int i = 0; i < blocksPerTick.get() && index < portalBlocks.size(); i++, index++) {
            BlockPos pos = portalBlocks.get(index);

            if (!mc.world.getBlockState(pos).isReplaceable()) {
                // Block in the way — try to break it if it's not already obsidian
                if (mc.world.getBlockState(pos).getBlock().asItem() != Items.OBSIDIAN) {
                    if (!waitingForBreak.contains(pos) && mc.interactionManager != null) {
                        mc.interactionManager.attackBlock(pos, Direction.UP);
                        mc.player.swingHand(Hand.MAIN_HAND);
                        waitingForBreak.add(pos);
                    }
                    index--; // retry this position next tick
                    return;
                }
                // It's already obsidian — count it and continue
                waitingForBreak.remove(pos);
                continue;
            }

            waitingForBreak.remove(pos);
            placeBlock(pos);
        }

        delay = 0;
    }

    // ── Placement ─────────────────────────────────────────────────────────────

    /**
     * Sends a block placement via the OFF_HAND swap trick.
     * This sends three packets: swap main→offhand, interact, swap back.
     * All three go through {@code mc.player.networkHandler.sendPacket()}, which
     * is called on the main client thread. On a Folia server, the server handles
     * each packet in the owning region's tick — no cross-region API calls are made
     * from the client side.
     */
    private void placeBlock(BlockPos pos) {
        BlockHitResult bhr      = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
        int            sequence = getSequence();

        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(
            Hand.OFF_HAND, bhr, sequence));
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    /** Attempts to light the portal using Flint and Steel from the hotbar. */
    private void autoLight() {
        if (mc.player == null || portalBlocks.isEmpty()) return;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.FLINT_AND_STEEL) {
                mc.player.getInventory().selectedSlot = i;
                // Light the interior: one block above the bottom-left of the frame
                BlockPos firePos = portalBlocks.get(0).up(1);
                BlockHitResult fireHit = new BlockHitResult(Vec3d.ofCenter(firePos), Direction.UP, firePos, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, fireHit);
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns true if any non-self player is within safeRadius blocks. */
    private boolean hasPlayerNearby() {
        if (mc.world == null || mc.player == null) return false;
        double r = safeRadius.get();
        Box searchBox = mc.player.getBoundingBox().expand(r);
        for (PlayerEntity p : mc.world.getEntitiesByClass(PlayerEntity.class, searchBox, e -> e != mc.player)) {
            if (mc.player.distanceTo(p) <= r) return true;
        }
        return false;
    }

    private int countObsidian() {
        if (mc.player == null) return 0;
        int count = 0;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.OBSIDIAN)
                count += mc.player.getInventory().getStack(i).getCount();
        }
        return count;
    }

    private void selectObsidianSlot() {
        if (mc.player == null) return;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.OBSIDIAN) {
                mc.player.getInventory().selectedSlot = i;
                return;
            }
        }
    }

    private int getSequence() {
        if (mc.world == null) return 0;
        return ((meteordevelopment.meteorclient.mixininterface.IClientWorld) mc.world)
            .meteor$getAndIncrementSequence();
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get()) return;
        for (int i = index; i < portalBlocks.size(); i++) {
            event.renderer.box(portalBlocks.get(i), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
    }
}
