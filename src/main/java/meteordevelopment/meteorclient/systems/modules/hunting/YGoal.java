/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.hunting;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/**
 * YGoal — gets the player to a target Y level using the best available method.
 *
 * Baritone mode: paths / mines to GoalYLevel (underground or descent).
 * Rocket  mode: elytra + firework rockets (open-air ascent).
 * Auto:         picks Baritone if underground or descending, Rockets otherwise.
 */
public class YGoal extends Module {

    // ── Enums ──────────────────────────────────────────────────────────────────
    public enum AscentMode { Auto, Baritone, Rockets }

    private enum ActiveMode  { BARITONE, ROCKETS }
    private enum LaunchState { GROUNDED, JUMPING, GLIDING }

    // ── Setting groups ───────────────────────────────────────────────────────
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSafety  = settings.createGroup("Safety");

    // ── General ──────────────────────────────────────────────────────────────
    private final Setting<Integer> targetY = sgGeneral.add(new IntSetting.Builder()
        .name("target-y")
        .description("Target Y level to reach.")
        .defaultValue(200)
        .range(-64, 320)
        .sliderRange(-64, 320)
        .build()
    );

    private final Setting<AscentMode> ascentMode = sgGeneral.add(new EnumSetting.Builder<AscentMode>()
        .name("ascent-mode")
        .description("Auto picks Baritone when underground or descending; Rockets for open-air ascent.")
        .defaultValue(AscentMode.Auto)
        .build()
    );

    private final Setting<Integer> rocketThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("rocket-threshold")
        .description("Minimum firework rockets required to activate Rocket mode.")
        .defaultValue(5)
        .min(1)
        .sliderRange(1, 64)
        .build()
    );

    private final Setting<Boolean> stopOnArrival = sgGeneral.add(new BoolSetting.Builder()
        .name("stop-on-arrival")
        .description("Disable module when target Y is reached.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> arrivalTolerance = sgGeneral.add(new IntSetting.Builder()
        .name("arrival-tolerance")
        .description("Y distance (blocks) that counts as arrived.")
        .defaultValue(3)
        .min(1)
        .sliderRange(1, 20)
        .build()
    );

    private final Setting<Boolean> alertOnArrival = sgGeneral.add(new BoolSetting.Builder()
        .name("alert-on-arrival")
        .description("Play a sound when the target Y is reached.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> alertOnLava = sgGeneral.add(new BoolSetting.Builder()
        .name("alert-on-lava")
        .description("Alert and stop when lava is detected above the player during Baritone ascent.")
        .defaultValue(true)
        .build()
    );

    // ── Safety ────────────────────────────────────────────────────────────────
    private final Setting<Boolean> disableOnPlayer = sgSafety.add(new BoolSetting.Builder()
        .name("disable-on-player")
        .description("Disable module if a player enters render distance.")
        .defaultValue(true)
        .build()
    );

    // ── State ──────────────────────────────────────────────────────────────────
    private ActiveMode  activeMode  = ActiveMode.BARITONE;
    private LaunchState launchState = LaunchState.GROUNDED;
    private boolean     baritoneStarted    = false;
    private int         rocketTick         = 0;
    private int         jumpTick           = 0;
    private int         baritoneRetryTick  = 0;

    private static final int ROCKET_FIRE_INTERVAL  = 5;   // ticks between rocket uses while gliding
    private static final int JUMP_TIMEOUT_TICKS    = 40;  // ticks before giving up on elytra activation
    private static final int BARITONE_RETRY_INTERVAL = 80; // ticks between Baritone restart attempts

    public YGoal() {
        super(Categories.Hunting,
            "y-goal",
            "Gets to a target Y level. Baritone for underground or descent, elytra+rockets for open-air ascent.");
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) { toggle(); return; }

        // Reset state
        baritoneStarted   = false;
        launchState       = LaunchState.GROUNDED;
        rocketTick        = 0;
        jumpTick          = 0;
        baritoneRetryTick = 0;

        int    target   = targetY.get();
        double currentY = mc.player.getY();

        if (Math.abs(currentY - target) <= arrivalTolerance.get()) {
            info("Already at target Y §b" + target + "§r.");
            toggle();
            return;
        }

        boolean goingUp     = target > currentY;
        boolean underground = isUnderground();
        boolean useRockets;

        if (ascentMode.get() == AscentMode.Rockets) {
            useRockets = true;
        } else if (ascentMode.get() == AscentMode.Baritone) {
            useRockets = false;
        } else { // Auto
            useRockets = goingUp && !underground;
        }

        // Validate rocket prerequisites
        if (useRockets) {
            if (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
                warning("No elytra in chest slot — falling back to Baritone.");
                useRockets = false;
            } else {
                int rockets = countRockets();
                if (rockets < rocketThreshold.get()) {
                    warning("Only §c" + rockets + "§r rockets (need §b"
                        + rocketThreshold.get() + "§r) — falling back to Baritone.");
                    useRockets = false;
                }
            }
        }

        if (useRockets) {
            activeMode  = ActiveMode.ROCKETS;
            launchState = mc.player.isGliding() ? LaunchState.GLIDING : LaunchState.GROUNDED;
            info("§aRocket mode§r → Y §b" + target + "§r. Rockets: §e" + countRockets() + "§r.");
        } else {
            if (!BaritoneUtils.IS_AVAILABLE) {
                error("Baritone is not installed. Install Baritone or switch mode to Rockets.");
                toggle();
                return;
            }
            activeMode = ActiveMode.BARITONE;
            startBaritone();
        }
    }

    @Override
    public void onDeactivate() {
        if (baritoneStarted) cancelBaritone();
        baritoneStarted   = false;
        launchState       = LaunchState.GROUNDED;
        rocketTick        = 0;
        jumpTick          = 0;
        baritoneRetryTick = 0;
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        // Player safety check
        if (disableOnPlayer.get() && hasPlayerNearby()) {
            info("§cPlayer detected — disabling.");
            toggle();
            return;
        }

        double currentY = mc.player.getY();
        int    target   = targetY.get();

        // Arrival check
        if (Math.abs(currentY - target) <= arrivalTolerance.get()) {
            if (alertOnArrival.get()) playSound();
            info("§aArrived at Y §b" + target + "§r (current: §e" + (int) currentY + "§r).");
            if (stopOnArrival.get()) { toggle(); return; }
            return;
        }

        if (activeMode == ActiveMode.BARITONE) {
            tickBaritone(currentY, target);
        } else {
            tickRockets(currentY, target);
        }
    }

    // ── Baritone logic ─────────────────────────────────────────────────────────

    private void startBaritone() {
        if (!BaritoneUtils.IS_AVAILABLE || mc.player == null) return;
        try {
            Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
            Object provider   = apiClass.getMethod("getProvider").invoke(null);
            Object baritone   = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Class<?> goalIface = Class.forName("baritone.api.pathing.goals.Goal");

            // Try GoalYLevel first; fall back to GoalBlock at player XZ
            Object goal;
            try {
                Class<?> goalYLevel = Class.forName("baritone.api.pathing.goals.GoalYLevel");
                goal = goalYLevel.getConstructor(int.class).newInstance(targetY.get());
            } catch (ClassNotFoundException ex) {
                Class<?> goalBlock = Class.forName("baritone.api.pathing.goals.GoalBlock");
                goal = goalBlock.getConstructor(int.class, int.class, int.class)
                    .newInstance((int) mc.player.getX(), targetY.get(), (int) mc.player.getZ());
            }

            Object cgp = baritone.getClass().getMethod("getCustomGoalProcess").invoke(baritone);
            cgp.getClass()
                .getMethod("setGoalAndPath", goalIface)
                .invoke(cgp, goal);

            baritoneStarted   = true;
            baritoneRetryTick = 0;
            info("§aBaritone mode§r → Y §b" + targetY.get()
                + "§r (current: §e" + (int) mc.player.getY() + "§r).");
        } catch (Exception e) {
            error("Failed to start Baritone: " + e.getMessage());
            toggle();
        }
    }

    private void tickBaritone(double currentY, int target) {
        // Lava above alert (ascending only)
        if (alertOnLava.get() && target > currentY && hasLavaAbove()) {
            warning("§cLava detected above — stopping. Resolve lava before re-enabling.");
            cancelBaritone();
            toggle();
            return;
        }

        // Periodic restart if Baritone has stopped pathing
        baritoneRetryTick++;
        if (baritoneRetryTick >= BARITONE_RETRY_INTERVAL) {
            baritoneRetryTick = 0;
            if (!isBaritonePathing()) startBaritone();
        }
    }

    private void cancelBaritone() {
        if (!BaritoneUtils.IS_AVAILABLE || !baritoneStarted) return;
        try {
            Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
            Object provider   = apiClass.getMethod("getProvider").invoke(null);
            Object baritone   = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object cmdMgr     = baritone.getClass().getMethod("getCommandManager").invoke(baritone);
            cmdMgr.getClass().getMethod("execute", String.class).invoke(cmdMgr, "cancel");
        } catch (Exception ignored) {}
        baritoneStarted = false;
    }

    private boolean isBaritonePathing() {
        if (!BaritoneUtils.IS_AVAILABLE) return false;
        try {
            Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
            Object provider   = apiClass.getMethod("getProvider").invoke(null);
            Object baritone   = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object pb         = baritone.getClass().getMethod("getPathingBehavior").invoke(baritone);
            return (Boolean) pb.getClass().getMethod("isPathing").invoke(pb);
        } catch (Exception e) {
            return false;
        }
    }

    // ── Rocket logic ───────────────────────────────────────────────────────────

    private void tickRockets(double currentY, int target) {
        // Rocket depletion — fall back to Baritone
        if (countRockets() <= 0) {
            warning("Rockets depleted. Switching to Baritone.");
            activeMode = ActiveMode.BARITONE;
            if (!BaritoneUtils.IS_AVAILABLE) { toggle(); return; }
            startBaritone();
            return;
        }

        // Tilt the player upward while ascending so rockets provide lift
        if (target > currentY && mc.player.isGliding()) {
            mc.player.setPitch(-45.0f);
        }

        switch (launchState) {
            case GROUNDED -> {
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                    jumpTick = 0;
                } else {
                    // Airborne but not gliding yet
                    launchState = LaunchState.JUMPING;
                    jumpTick    = 0;
                }
            }
            case JUMPING -> {
                if (mc.player.isGliding()) {
                    launchState = LaunchState.GLIDING;
                    rocketTick  = 0;
                } else if (!mc.player.isOnGround()) {
                    // Press jump key each tick while airborne to trigger elytra activation
                    Input.setKeyState(mc.options.jumpKey, true);
                    jumpTick++;
                    if (jumpTick > JUMP_TIMEOUT_TICKS) {
                        warning("Could not activate elytra glide. Is elytra equipped in chest slot?");
                        Input.setKeyState(mc.options.jumpKey, false);
                        launchState = LaunchState.GROUNDED;
                        jumpTick    = 0;
                    }
                } else {
                    // Landed before gliding — retry
                    Input.setKeyState(mc.options.jumpKey, false);
                    launchState = LaunchState.GROUNDED;
                    jumpTick    = 0;
                }
            }
            case GLIDING -> {
                // Release jump key if still held
                Input.setKeyState(mc.options.jumpKey, false);

                if (!mc.player.isGliding()) {
                    // Lost glide
                    launchState = LaunchState.GROUNDED;
                    return;
                }
                rocketTick++;
                if (rocketTick >= ROCKET_FIRE_INTERVAL) {
                    rocketTick = 0;
                    fireRocket();
                }
            }
        }
    }

    /**
     * Uses a firework rocket: checks offhand first, then hotbar slots 0–8.
     * Temporarily swaps hotbar selection to fire, then restores it.
     */
    private void fireRocket() {
        if (mc.player == null || mc.interactionManager == null) return;

        // Offhand
        if (mc.player.getOffHandStack().getItem() == Items.FIREWORK_ROCKET) {
            mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
            return;
        }

        // Hotbar
        int rocketSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.FIREWORK_ROCKET) {
                rocketSlot = i;
                break;
            }
        }
        if (rocketSlot == -1) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = rocketSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prev;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns true if there is a solid block anywhere in the 10 blocks directly
     * above the player's head — indicates an underground or enclosed position.
     */
    private boolean isUnderground() {
        if (mc.player == null || mc.world == null) return false;
        // Start two blocks above feet (one above head)
        BlockPos base = mc.player.getBlockPos().up(2);
        for (int i = 0; i < 10; i++) {
            BlockPos pos = base.up(i);
            if (mc.world.getBlockState(pos).isSolidBlock(mc.world, pos)) return true;
        }
        return false;
    }

    /**
     * Returns true if any lava block exists in the column directly above
     * the player up to targetY. Used to warn before Baritone mines into lava.
     */
    private boolean hasLavaAbove() {
        if (mc.player == null || mc.world == null) return false;
        int worldTop = mc.world.getBottomY() + mc.world.getHeight() - 1;
        int startY   = (int) mc.player.getY() + 1;
        int endY     = Math.min(targetY.get(), worldTop);
        if (endY <= startY) return false;

        int x = mc.player.getBlockPos().getX();
        int z = mc.player.getBlockPos().getZ();
        for (int y = startY; y <= endY; y++) {
            if (mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.LAVA) return true;
        }
        return false;
    }

    /** Counts all firework rockets across the hotbar, main inventory, and offhand. */
    private int countRockets() {
        if (mc.player == null) return 0;
        int count = 0;
        // Slots 0–35: hotbar (0–8) + main inventory (9–35)
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.FIREWORK_ROCKET) count += stack.getCount();
        }
        if (mc.player.getOffHandStack().getItem() == Items.FIREWORK_ROCKET)
            count += mc.player.getOffHandStack().getCount();
        return count;
    }

    private boolean hasPlayerNearby() {
        if (mc.world == null || mc.player == null) return false;
        Box box = mc.player.getBoundingBox().expand(128);
        for (PlayerEntity p : mc.world.getEntitiesByClass(PlayerEntity.class, box, e -> e != mc.player)) {
            return true;
        }
        return false;
    }

    private void playSound() {
        if (mc.player == null || mc.world == null) return;
        mc.world.playSoundFromEntity(
            mc.player, mc.player,
            SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
            SoundCategory.BLOCKS, 2.0f, 1.0f
        );
    }

    // ── Info string ───────────────────────────────────────────────────────────

    @Override
    public String getInfoString() {
        if (mc.player == null) return null;
        int cur    = (int) mc.player.getY();
        int target = targetY.get();
        String modeStr = (activeMode == ActiveMode.BARITONE) ? "§aBari§r" : "§cRkt§r";
        return cur + "→" + target + " " + modeStr;
    }
}
