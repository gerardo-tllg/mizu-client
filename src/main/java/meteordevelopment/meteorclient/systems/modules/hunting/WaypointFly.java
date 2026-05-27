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
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * WaypointFly — sequentially flies to every Xaero temporary waypoint in list order,
 * deleting each on arrival before advancing to the next.
 *
 * Overworld: yaw-lock toward waypoint + Pitch40Util or AFKVanillaFly for thrust.
 * Nether:    Baritone elytra GoalXZ (falls back to yaw-lock if Baritone absent).
 *
 * Requires Xaero's Minimap. Baritone is optional (nether only).
 */
public class WaypointFly extends Module {

    // ── Enums ─────────────────────────────────────────────────────────────────
    public enum OWFlightMode { Pitch40, AFKVanillaFly }

    // ── Setting groups ───────────────────────────────────────────────────────
    private final SettingGroup sgGeneral  = settings.getDefaultGroup();
    private final SettingGroup sgFlight   = settings.createGroup("Flight");
    private final SettingGroup sgSafety   = settings.createGroup("Safety");

    // ── General ──────────────────────────────────────────────────────────────
    private final Setting<Double> arrivalRadius = sgGeneral.add(new DoubleSetting.Builder()
        .name("arrival-radius")
        .description("Distance in blocks to consider a waypoint reached.")
        .defaultValue(20.0)
        .min(5.0)
        .sliderRange(5.0, 100.0)
        .build()
    );

    // ── Flight ────────────────────────────────────────────────────────────────
    private final Setting<OWFlightMode> owFlightMode = sgFlight.add(new EnumSetting.Builder<OWFlightMode>()
        .name("ow-flight-mode")
        .description("Flight module to use in the Overworld (Pitch40Util or AFKVanillaFly).")
        .defaultValue(OWFlightMode.Pitch40)
        .build()
    );

    private final Setting<Integer> minAltOW = sgFlight.add(new IntSetting.Builder()
        .name("min-altitude-ow")
        .description("Minimum Y level before starting yaw guidance in the Overworld.")
        .defaultValue(200)
        .sliderRange(50, 320)
        .build()
    );

    private final Setting<Integer> minAltNether = sgFlight.add(new IntSetting.Builder()
        .name("min-altitude-nether")
        .description("Minimum Y level before starting guidance in the Nether.")
        .defaultValue(100)
        .sliderRange(30, 255)
        .build()
    );

    // ── Safety ────────────────────────────────────────────────────────────────
    private final Setting<Boolean> pauseOnPlayer = sgSafety.add(new BoolSetting.Builder()
        .name("pause-on-player")
        .description("Pause flight and yaw guidance when a player enters render distance.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> resumeDelay = sgSafety.add(new IntSetting.Builder()
        .name("resume-delay")
        .description("Seconds to wait after no players are visible before resuming.")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 60)
        .visible(pauseOnPlayer::get)
        .build()
    );

    // ── State ─────────────────────────────────────────────────────────────────
    /** Snapshot of (x, z, name) taken at activation — processed in order. */
    private static class WpTarget {
        final int    x, z;
        final String name;
        /** Reference to the live Xaero waypoint object for deletion on arrival. */
        final xaero.common.minimap.waypoints.Waypoint xaeroRef;

        WpTarget(int x, int z, String name, xaero.common.minimap.waypoints.Waypoint ref) {
            this.x = x; this.z = z; this.name = name; this.xaeroRef = ref;
        }
    }

    private final List<WpTarget> waypoints = new ArrayList<>();
    private int  currentIndex   = 0;
    private boolean paused      = false;
    private long lastPlayerSeen = 0L;

    // Baritone state (nether only)
    private boolean baritoneActive  = false;
    private int     baritoneRetryTick = 0;
    private static final int BARITONE_RETRY_INTERVAL = 100;

    public WaypointFly() {
        super(Categories.Hunting,
            "waypoint-fly",
            "Flies to each Xaero temporary waypoint in order, deleting them on arrival. Requires Xaero Minimap.");
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onActivate() {
        waypoints.clear();
        currentIndex   = 0;
        paused         = false;
        baritoneActive = false;

        if (mc.player == null || mc.world == null) { toggle(); return; }

        if (!loadWaypoints()) {
            error("No temporary waypoints found in Xaero. Add some first.");
            toggle();
            return;
        }

        info("Loaded §b" + waypoints.size() + "§r temporary waypoint(s). Starting flight.");
        startLeg();
    }

    @Override
    public void onDeactivate() {
        stopFlightModule();
        cancelBaritone();
        waypoints.clear();
        currentIndex   = 0;
        paused         = false;
        baritoneActive = false;
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        if (currentIndex >= waypoints.size()) {
            info("All waypoints reached. Disabling WaypointFly.");
            toggle();
            return;
        }

        // ── Player safety pause ────────────────────────────────────────────
        if (pauseOnPlayer.get()) {
            boolean playerNearby = hasPlayerNearby();
            if (playerNearby) {
                lastPlayerSeen = System.currentTimeMillis();
                if (!paused) {
                    paused = true;
                    info("Player detected — flight paused.");
                    stopFlightModule();
                    cancelBaritone();
                }
                return; // stay paused
            } else if (paused) {
                long elapsed = System.currentTimeMillis() - lastPlayerSeen;
                if (elapsed < resumeDelay.get() * 1_000L) return; // still waiting
                paused = false;
                info("Resuming flight.");
                startLeg();
            }
        }

        WpTarget current = waypoints.get(currentIndex);
        boolean inNether = mc.world.getRegistryKey().equals(World.NETHER);
        int minAlt = inNether ? minAltNether.get() : minAltOW.get();

        // ── Altitude gate ──────────────────────────────────────────────────
        if (mc.player.getY() < minAlt) {
            // Waiting to reach minimum altitude before guiding horizontally
            return;
        }

        // ── Distance check ─────────────────────────────────────────────────
        double dx   = mc.player.getX() - current.x;
        double dz   = mc.player.getZ() - current.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist <= arrivalRadius.get()) {
            info("Arrived at §b" + current.name + "§r. Deleting waypoint.");
            deleteWaypoint(current.xaeroRef);
            currentIndex++;
            if (currentIndex >= waypoints.size()) {
                info("All waypoints reached. Disabling WaypointFly.");
                toggle();
                return;
            }
            startLeg(); // begin next leg
            return;
        }

        // ── Guidance ───────────────────────────────────────────────────────
        if (inNether) {
            tickNetherGuidance(current, dist);
        } else {
            tickOverworldGuidance(current);
        }
    }

    // ── Per-leg setup ─────────────────────────────────────────────────────────

    private void startLeg() {
        if (mc.player == null || mc.world == null || currentIndex >= waypoints.size()) return;

        WpTarget wp = waypoints.get(currentIndex);
        boolean inNether = mc.world.getRegistryKey().equals(World.NETHER);

        info("Flying to waypoint §b" + wp.name + "§r at " + wp.x + ", " + wp.z + ".");

        if (inNether) {
            baritoneActive = false;
            startBaritone(wp);
        } else {
            enableFlightModule();
        }
    }

    // ── Overworld guidance ────────────────────────────────────────────────────

    private void tickOverworldGuidance(WpTarget target) {
        if (mc.player == null) return;
        // Smooth yaw toward target XZ
        float targetYaw = (float) Rotations.getYaw(new Vec3d(target.x, mc.player.getY(), target.z));
        mc.player.setYaw(HuntingUtils.smoothRotation(mc.player.getYaw(), targetYaw, 0.08));
    }

    private void enableFlightModule() {
        if (owFlightMode.get() == OWFlightMode.Pitch40) {
            Pitch40Util p40 = Modules.get().get(Pitch40Util.class);
            if (p40 != null && !p40.isActive()) p40.toggle();
        } else {
            AFKVanillaFly afk = Modules.get().get(AFKVanillaFly.class);
            if (afk != null && !afk.isActive()) afk.toggle();
        }
    }

    private void stopFlightModule() {
        Pitch40Util p40 = Modules.get().get(Pitch40Util.class);
        if (p40 != null && p40.isActive()) p40.toggle();
        AFKVanillaFly afk = Modules.get().get(AFKVanillaFly.class);
        if (afk != null && afk.isActive()) afk.toggle();
    }

    // ── Nether guidance (Baritone) ────────────────────────────────────────────

    private void tickNetherGuidance(WpTarget target, double dist) {
        if (!BaritoneUtils.IS_AVAILABLE) {
            // Fallback: yaw-lock only
            tickOverworldGuidance(target);
            return;
        }

        baritoneRetryTick++;

        // Retry if Baritone stopped pathing
        if (!baritoneActive || baritoneRetryTick >= BARITONE_RETRY_INTERVAL) {
            baritoneRetryTick = 0;
            if (!isBaritonePathing()) {
                startBaritone(target);
            }
        }
    }

    private void startBaritone(WpTarget target) {
        if (!BaritoneUtils.IS_AVAILABLE || mc.player == null) {
            // No Baritone — fall back to yaw lock
            enableFlightModule();
            return;
        }
        try {
            Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");

            // Accept elytra terms automatically
            Object settings = apiClass.getMethod("getSettings").invoke(null);
            Object elytraTerms = settings.getClass().getField("elytraTermsAccepted").get(settings);
            elytraTerms.getClass().getField("value").setBoolean(elytraTerms, true);

            // Set GoalXZ
            Class<?> goalClass = Class.forName("baritone.api.pathing.goals.GoalXZ");
            Object goal = goalClass.getConstructor(int.class, int.class)
                .newInstance(target.x, target.z);

            Object provider = apiClass.getMethod("getProvider").invoke(null);
            Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object customGoal = baritone.getClass().getMethod("getCustomGoalProcess").invoke(baritone);
            customGoal.getClass()
                .getMethod("setGoalAndPath", Class.forName("baritone.api.pathing.goals.Goal"))
                .invoke(customGoal, goal);

            // Start elytra
            Object cmdMgr = baritone.getClass().getMethod("getCommandManager").invoke(baritone);
            cmdMgr.getClass().getMethod("execute", String.class).invoke(cmdMgr, "elytra");

            baritoneActive    = true;
            baritoneRetryTick = 0;
        } catch (Exception e) {
            warning("Baritone start failed: " + e.getMessage() + " — falling back to yaw-lock.");
            enableFlightModule();
        }
    }

    private void cancelBaritone() {
        if (!BaritoneUtils.IS_AVAILABLE || !baritoneActive) return;
        try {
            Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
            Object provider = apiClass.getMethod("getProvider").invoke(null);
            Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object cmdMgr   = baritone.getClass().getMethod("getCommandManager").invoke(baritone);
            cmdMgr.getClass().getMethod("execute", String.class).invoke(cmdMgr, "cancel");
        } catch (Exception ignored) {}
        baritoneActive = false;
    }

    private boolean isBaritonePathing() {
        if (!BaritoneUtils.IS_AVAILABLE) return false;
        try {
            Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
            Object provider = apiClass.getMethod("getProvider").invoke(null);
            Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object pathBehavior = baritone.getClass().getMethod("getPathingBehavior").invoke(baritone);
            return (Boolean) pathBehavior.getClass().getMethod("isPathing").invoke(pathBehavior);
        } catch (Exception e) {
            return false;
        }
    }

    // ── Xaero waypoint helpers ────────────────────────────────────────────────

    /**
     * Loads all temporary waypoints from the current Xaero WaypointSet in list order
     * (which reflects creation order for Xaero's ArrayList-backed sets).
     */
    private boolean loadWaypoints() {
        try {
            xaero.hud.minimap.module.MinimapSession session =
                xaero.hud.minimap.BuiltInHudModules.MINIMAP.getCurrentSession();
            if (session == null) return false;

            xaero.hud.minimap.world.MinimapWorld world =
                session.getWorldManager().getCurrentWorld();
            if (world == null) return false;

            xaero.hud.minimap.waypoint.set.WaypointSet set = world.getCurrentWaypointSet();
            if (set == null) return false;

            for (xaero.common.minimap.waypoints.Waypoint wp : set.getWaypoints()) {
                if (!wp.isTemporary()) continue;
                waypoints.add(new WpTarget(wp.getX(), wp.getZ(), wp.getName(), wp));
            }
            return !waypoints.isEmpty();
        } catch (Throwable e) {
            warning("Failed to read Xaero waypoints: " + e.getMessage());
            return false;
        }
    }

    /** Removes a waypoint from the live Xaero set and saves. */
    private void deleteWaypoint(xaero.common.minimap.waypoints.Waypoint wp) {
        try {
            xaero.hud.minimap.module.MinimapSession session =
                xaero.hud.minimap.BuiltInHudModules.MINIMAP.getCurrentSession();
            if (session == null) return;

            xaero.hud.minimap.world.MinimapWorld world =
                session.getWorldManager().getCurrentWorld();
            if (world == null) return;

            xaero.hud.minimap.waypoint.set.WaypointSet set = world.getCurrentWaypointSet();
            if (set == null) return;

            set.remove(wp);
            session.getWorldManagerIO().saveWorld(world);
            xaero.map.mods.SupportMods.xaeroMinimap.requestWaypointsRefresh();
        } catch (Throwable e) {
            warning("Failed to delete waypoint \"" + wp.getName() + "\": " + e.getMessage());
        }
    }

    // ── Player detection ──────────────────────────────────────────────────────

    private boolean hasPlayerNearby() {
        if (mc.world == null || mc.player == null) return false;
        Box box = mc.player.getBoundingBox().expand(128);
        for (PlayerEntity p : mc.world.getEntitiesByClass(PlayerEntity.class, box, e -> e != mc.player)) {
            return true;
        }
        return false;
    }

    // ── Info string ───────────────────────────────────────────────────────────

    @Override
    public String getInfoString() {
        if (mc.player == null || waypoints.isEmpty()) return null;
        if (currentIndex >= waypoints.size()) return "Done";

        WpTarget cur = waypoints.get(currentIndex);
        double dx   = mc.player.getX() - cur.x;
        double dz   = mc.player.getZ() - cur.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        String distStr = dist >= 1000.0
            ? String.format("%.1fk", dist / 1000.0)
            : String.format("%.0f", dist);

        String pausedTag = paused ? " §c[PAUSED]§r" : "";
        return "§b" + cur.name + "§r " + distStr + "b" + pausedTag;
    }
}
