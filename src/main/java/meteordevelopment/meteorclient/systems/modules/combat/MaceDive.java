package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.systems.managers.TargetManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class MaceDive extends Module {

    private final SettingGroup sgGeneral  = settings.getDefaultGroup();
    private final SettingGroup sgFirework = settings.createGroup("Firework");
    private final SettingGroup sgRender   = settings.createGroup("Render");

    // ── Settings ────────────────────────────────────────────────────────────────

    private final Setting<Double> height = sgGeneral.add(new DoubleSetting.Builder()
        .name("height")
        .description("Height above target to climb to.")
        .defaultValue(20.0)
        .min(5.0)
        .sliderRange(5.0, 60.0)
        .build());

    private final Setting<Double> swapDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("swap-distance")
        .description("3D distance from target to swap elytra to chestplate. Set to 0 to disable swap entirely.")
        .defaultValue(3.5)
        .min(0.0)
        .sliderRange(0.0, 8.0)
        .build());

    private final Setting<Integer> chestplateHoldTicks = sgGeneral.add(new IntSetting.Builder()
        .name("chestplate-hold-ticks")
        .description("Ticks to keep the chestplate on before re-equipping elytra. Higher = more time for MaceAura to attack, lower = faster bounce.")
        .defaultValue(2)
        .min(1)
        .sliderRange(1, 5)
        .build());

    private final Setting<Double> angle = sgGeneral.add(new DoubleSetting.Builder()
        .name("angle")
        .description("Approach angle. 90 = straight above, lower = offset to the side.")
        .defaultValue(84.0)
        .min(30.0)
        .sliderRange(30.0, 90.0)
        .build());

    private final Setting<Double> detectionRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("detection-range")
        .description("Max horizontal range to detect targets.")
        .defaultValue(64.0)
        .min(10.0)
        .sliderRange(10.0, 128.0)
        .build());

    private final Setting<Boolean> pauseOnEat = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-on-eat")
        .description("Pauses while using an item.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> freelook = sgGeneral.add(new BoolSetting.Builder()
        .name("freelook")
        .description("Hides your rotation changes visually using FreeLook camera mode. Steering still works server-side.")
        .defaultValue(false)
        .build());

    private final Setting<Integer> rocketLeadTicks = sgFirework.add(new IntSetting.Builder()
        .name("rocket-lead-ticks")
        .description("Fire a rocket this many ticks before the chestplate swap to pre-cache velocity for instant relaunch.")
        .defaultValue(5)
        .min(0)
        .sliderRange(0, 15)
        .build());

    private final Setting<Boolean> autoBoost = sgFirework.add(new BoolSetting.Builder()
        .name("auto-boost")
        .description("Automatically use firework rockets.")
        .defaultValue(true)
        .build());

    private final Setting<Integer> fireworkCooldown = sgFirework.add(new IntSetting.Builder()
        .name("firework-cooldown")
        .description("Minimum ticks between firework uses.")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 40)
        .visible(autoBoost::get)
        .build());

    private final Setting<Double> velocityThreshold = sgFirework.add(new DoubleSetting.Builder()
        .name("velocity-threshold")
        .description("Use firework when speed is below this value.")
        .defaultValue(2.0)
        .min(0.1)
        .sliderRange(0.1, 5.0)
        .visible(autoBoost::get)
        .build());

    private final Setting<Boolean> renderTarget = sgRender.add(new BoolSetting.Builder()
        .name("render-target")
        .description("Render the target bounding box.")
        .defaultValue(true)
        .build());

    private final Setting<SettingColor> targetColor = sgRender.add(new ColorSetting.Builder()
        .name("target-color")
        .defaultValue(new SettingColor(255, 100, 0, 50))
        .build());

    private final Setting<SettingColor> targetLineColor = sgRender.add(new ColorSetting.Builder()
        .name("target-line-color")
        .defaultValue(new SettingColor(255, 100, 0, 150))
        .build());

    private final TargetManager targetManager = new TargetManager(this, true);

    // ── State ────────────────────────────────────────────────────────────────────

    private enum Phase { SEARCHING, CLIMBING, DIVING, RECOVERING, GROUNDED }

    private Phase phase = Phase.SEARCHING;
    private Entity target = null;
    private int ticksSinceFirework = 0;
    private int diveCount = 0;
    private int recoverTick = 0;
    private int groundedTick = 0;          // tracks ticks spent on ground / in air during relaunch
    private boolean didSwapChest = false; // prevents double swap
    private boolean elytraSwapSent = false;  // prevents spamming silentSwapEquipElytra every tick
    private int launchRocketsFired = 0; // counts rockets fired during launch (0 → fire, 1 → fire again, 2 → done)
    private boolean diveRocketFired = false;   // true once the pre-swap rocket has been fired mid-dive
    private int climbTick = 0;                 // ticks spent in CLIMBING; resets on phase entry
    private int diveTick = 0;                  // ticks spent in current DIVING pass
    private int consecutiveTimeouts = 0;       // consecutive climb timeouts on same target

    // FreeLook integration
    private boolean freelookWasActive = false;
    private FreeLook.Mode freelookPrevMode = null;

    public MaceDive() {
        super(Categories.Combat, "mace-dive", "Flight controller for mace diving. Use with KillAura/MaceAura for attacks.");
    }

    @Override
    public void onActivate() {
        phase = Phase.SEARCHING;
        target = null;
        ticksSinceFirework = 0;
        diveCount = 0;
        recoverTick = 0;
        groundedTick = 0;
        didSwapChest = false;
        elytraSwapSent = false;

        if (freelook.get()) {
            FreeLook fl = Modules.get().get(FreeLook.class);
            freelookWasActive = fl.isActive();
            freelookPrevMode = fl.mode.get();

            // Seed camera angles to current look so there is no jump when we activate
            fl.cameraYaw   = mc.player.getYaw();
            fl.cameraPitch = mc.player.getPitch();

            // Force Camera mode so the mixin locks the visual camera independently
            fl.mode.set(FreeLook.Mode.Camera);
            if (!fl.isActive()) fl.toggle();
        }
    }

    @Override
    public void onDeactivate() {
        target = null;

        // Restore FreeLook to whatever state it was in before MaceDive activated
        if (freelook.get()) {
            FreeLook fl = Modules.get().get(FreeLook.class);
            if (!freelookWasActive && fl.isActive()) {
                fl.toggle();
            } else if (freelookWasActive && freelookPrevMode != null) {
                fl.mode.set(freelookPrevMode);
            }
        }
    }

    @Override
    public String getInfoString() {
        if (target != null) return EntityUtils.getName(target) + " [" + phase.name() + "] x" + diveCount;
        return phase.name();
    }



    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || !mc.player.isAlive() || mc.player.isSpectator()) return;

        try {
            // Always restore slot first — the server needs one full tick to see the rocket
            // slot before we swap back. Runs unconditionally before pauseOnEat bail-out.
            restoreAfterRocket();

            if (pauseOnEat.get() && mc.player.isUsingItem()) return;
            ticksSinceFirework++;

            switch (phase) {
                case SEARCHING  -> tickSearching();
                case CLIMBING   -> tickClimbing();
                case DIVING     -> tickDiving();
                case RECOVERING -> tickRecovering();
                case GROUNDED   -> tickGrounded();
            }

            // Global boost AFTER phase logic — so gliding state is current.
            // Skip during DIVING (we're descending intentionally, rockets fight the dive)
            // and RECOVERING (chestplate is on, not gliding yet).
            if (autoBoost.get() && mc.player != null && mc.player.isGliding()
                && phase == Phase.CLIMBING
                && mc.player.getVelocity().length() < velocityThreshold.get()
                && ticksSinceFirework >= fireworkCooldown.get()) {
                info("AUTOBOOST: vel=%.2f ticksSince=%d phase=%s", mc.player.getVelocity().length(), ticksSinceFirework, phase);
                fireRocket();
            }
        } catch (NullPointerException e) {
            // mc.player can go null mid-tick during disconnect/respawn/dimension change.
            // Swallow it and let the next tick's null check handle the state.
        }
    }

    // ── SEARCHING ────────────────────────────────────────────────────────────────

    private void tickSearching() {
        target = findTarget();
        if (target == null) return;

        if (mc.player.isOnGround()) {
            info("SEARCH→GROUNDED: standing start");
            phase = Phase.GROUNDED;
            groundedTick = 0;
            elytraSwapSent = false;
            return;
        }

        ensureGliding();
        if (mc.player.isGliding()) { phase = Phase.CLIMBING; climbTick = 0; }
    }

    // ── CLIMBING ─────────────────────────────────────────────────────────────────

    private Vec3d getClimbPosition() {
        double angleRad = Math.toRadians(angle.get());
        double horizOff = (angle.get() >= 89.5) ? 0 : height.get() / Math.tan(angleRad);

        double dx = mc.player.getX() - target.getX();
        double dz = mc.player.getZ() - target.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        double nx = dist > 0.1 ? dx / dist : 1;
        double nz = dist > 0.1 ? dz / dist : 0;

        return new Vec3d(
            target.getX() + nx * horizOff,
            target.getY() + height.get(),
            target.getZ() + nz * horizOff
        );
    }

    private void tickClimbing() {
        if (!validateTarget()) { phase = Phase.SEARCHING; return; }
        climbTick++;

        // Climbing timeout: if we've been climbing for 5+ seconds without reaching
        // the dive position, the target probably moved out of reach.
        if (climbTick > 100) {
            consecutiveTimeouts++;
            if (consecutiveTimeouts >= 2) {
                info("CLIMB→OFF: %d consecutive timeouts, target unreachable — disabling", consecutiveTimeouts);
                consecutiveTimeouts = 0;
                toggle();
                return;
            }
            info("CLIMB→SEARCH: timeout after %d ticks (%d/2), re-evaluating target", climbTick, consecutiveTimeouts);
            target = null;
            phase = Phase.SEARCHING;
            climbTick = 0;
            return;
        }

        if (mc.player.isOnGround() && mc.player.getVelocity().length() < 0.5) {
            info("CLIMB→GROUNDED: knocked onto block | vel=%.2f", mc.player.getVelocity().length());
            phase = Phase.GROUNDED;
            groundedTick = 0;
            elytraSwapSent = false;
            climbTick = 0;
            return;
        }

        ensureGliding();

        Vec3d climbPos = getClimbPosition();
        double distToClimb = climbPos.distanceTo(mc.player.getPos());

        // At climb position → dive. Also force dive if significantly above the
        // climb height — prevents infinite spiraling when horizontally offset.
        boolean atClimbPos = distToClimb < 3.0 && mc.player.getY() >= climbPos.y - 1.0;
        boolean overHeight = mc.player.getY() > climbPos.y + 5.0;
        if (atClimbPos || overHeight) {
            info("CLIMB→DIVE: at height %.1f vel=%.2f%s", mc.player.getY() - target.getY(), mc.player.getVelocity().length(), overHeight ? " (height overshoot)" : "");
            phase = Phase.DIVING;
            didSwapChest = false;
            diveRocketFired = false;
            diveTick = 0;
            climbTick = 0;
            consecutiveTimeouts = 0;
            return;
        }

        mc.player.setYaw(getYawToTarget(climbPos));
        mc.player.setPitch(mc.player.getY() < climbPos.y ? -60.0f : -10.0f);
    }

    // ── DIVING ───────────────────────────────────────────────────────────────────

    private void tickDiving() {
        if (!validateTarget()) { phase = Phase.SEARCHING; return; }
        diveTick++;

        // Use closestPointOnBox for distance calculation (accurate for swap trigger)
        Vec3d point = closestPointOnBox(target.getBoundingBox(), mc.player.getEyePos());
        double distToHitbox = point.distanceTo(mc.player.getEyePos());

        // Steer toward target CENTER — closestPointOnBox causes yaw swings when
        // directly above because the closest point jumps between bounding box edges.
        Vec3d targetCenter = target.getPos().add(0, target.getHeight() / 2.0, 0);
        mc.player.setYaw(getYawToTarget(targetCenter));
        mc.player.setPitch(getPitchToTarget(targetCenter));

        // Only keep gliding if we're outside the swap distance.
        // Inside the window ensureGliding() would fight the chestplate swap.
        if (swapDistance.get() <= 0 || distToHitbox > swapDistance.get()) {
            ensureGliding();
        }

        // Pre-cache rocket velocity: estimate ticks until chestplate swap using
        // the player's current velocity as closing speed. This is more stable than
        // computing from position deltas which oscillate as the player steers.
        double closingSpeed = mc.player.getVelocity().length();
        if (swapDistance.get() > 0 && !diveRocketFired
            && rocketLeadTicks.get() > 0
            && closingSpeed > 0.01
            && distToHitbox > swapDistance.get()) {
            double ticksToSwap = (distToHitbox - swapDistance.get()) / closingSpeed;
            if (ticksToSwap <= rocketLeadTicks.get()) {
                info("DIVE: pre-swap rocket | dist=%.1f vel=%.2f ticksToSwap=%.1f", distToHitbox, closingSpeed, ticksToSwap);
                fireRocket();
                diveRocketFired = true;
            }
        }

        // Debug: log approach distance when within 2x swap distance
        if (swapDistance.get() > 0 && distToHitbox <= swapDistance.get() * 2 && !didSwapChest) {
            info("DIVE APPROACH: dist=%.1f threshold=%.1f vel=%.2f overshoot=%.1f",
                distToHitbox, swapDistance.get(), mc.player.getVelocity().length(),
                swapDistance.get() - distToHitbox);
        }

        if (swapDistance.get() > 0 && distToHitbox <= swapDistance.get() && !didSwapChest) {
            String chestBefore = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().toString();
            boolean swapFired = true;
            if (mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
                swapFired = PlayerUtils.silentSwapEquipChestplate();
            }
            String chestAfter = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().toString();
            info("DIVE SWAP: chest [%s→%s] fired=%s dist=%.1f vel=%.2f", chestBefore, chestAfter, swapFired, distToHitbox, mc.player.getVelocity().length());
            if (swapFired) {
                didSwapChest = true;
                diveRocketFired = false;
                diveCount++;
                recoverTick = 0;
                elytraSwapSent = false;
                phase = Phase.RECOVERING;
            }
        } else if (swapDistance.get() <= 0 && distToHitbox <= 3.5) {
            // No swap mode — just swoop past, aura attacks while gliding
            diveCount++;
            Vec3d climbPos = getClimbPosition();
            mc.player.setYaw(getYawToTarget(climbPos));
            mc.player.setPitch(-60.0f);
            phase = Phase.CLIMBING;
            didSwapChest = false;
            diveRocketFired = false;
            climbTick = 0;
        } else if (mc.player.getY() < target.getY() - 3.0) {
            // Below target → abort
            phase = Phase.CLIMBING;
            didSwapChest = false;
            diveRocketFired = false;
            climbTick = 0;
        }
    }

    // ── RECOVERING ───────────────────────────────────────────────────────────────

    private void tickRecovering() {
        recoverTick++;

        String chestItem = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().toString();
        info("RECOVER[%d]: chest=%s gliding=%s onGround=%s vel=%.2f velY=%.2f",
            recoverTick, chestItem, mc.player.isGliding(), mc.player.isOnGround(),
            mc.player.getVelocity().length(), mc.player.getVelocity().y);

        // Pitch up immediately
        Vec3d climbPos = getClimbPosition();
        mc.player.setYaw(getYawToTarget(climbPos));
        mc.player.setPitch(-90.0f);

        // Re-equip elytra after giving MaceAura time with the chestplate.
        if (recoverTick >= chestplateHoldTicks.get() && !mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
            if (!elytraSwapSent || recoverTick % 3 == 0) {
                info("RECOVER[%d]: sending elytra re-equip | onGround=%s vel=%.2f", recoverTick, mc.player.isOnGround(), mc.player.getVelocity().length());
                PlayerUtils.silentSwapEquipElytra();
                elytraSwapSent = true;
            }
        }

        // Safety escape: if we've been recovering for too long something is genuinely
        // wrong (server rejected the equip, item disappeared, etc.). Fall through to
        // GROUNDED so the normal ground-relaunch path can sort it out.
        if (recoverTick > 60) {
            info("RECOVER→GROUNDED: safety timeout at tick %d", recoverTick);
            phase = Phase.GROUNDED;
            groundedTick = 0;
            elytraSwapSent = false;
            didSwapChest = false;
            return;
        }

        // Landed → grounded recovery. Not a cold start: the pre-swap rocket's
        // cached velocity should carry the relaunch.
        if (mc.player.isOnGround()) {
            info("RECOVER→GROUNDED: landed at tick %d vel=%.2f elytra=%s", recoverTick, mc.player.getVelocity().length(), mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA));
            phase = Phase.GROUNDED;
            groundedTick = 0;
            // Keep elytraSwapSent as-is — tickGrounded reads it to know whether the
            // equip is already done or still pending.
            didSwapChest = false;
            return;
        }

        // Still airborne and elytra not yet confirmed — keep waiting.
        // Also gate on chestplateHoldTicks: if elytra appears BEFORE the hold period,
        // the chestplate swap was reversed (server rejected / another module swapped back).
        // Don't deploy into a falling player — that causes spinning climbs.
        if (!mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) || recoverTick < chestplateHoldTicks.get()) return;
        elytraSwapSent = false;

        // Elytra confirmed mid-air — deploy + rocket + climb all on the same tick.
        info("RECOVER→CLIMB: bounce at tick %d | deploy+rocket same tick | vel=%.2f gliding=%s", recoverTick, mc.player.getVelocity().length(), mc.player.isGliding());
        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        fireRocket();
        // Override the counter so the autoboost (which runs right after phase logic
        // on this same tick) fires immediately instead of waiting a full cooldown.
        ticksSinceFirework = fireworkCooldown.get();
        phase = Phase.CLIMBING;
        didSwapChest = false;
        climbTick = 0;
    }

    // ── GROUNDED ─────────────────────────────────────────────────────────────────

    private void tickGrounded() {
        groundedTick++;

        // Quick exit: if the player touched the ground but immediately bounced and
        // is already gliding, skip the whole takeoff sequence — they never really
        // landed. Fire a rocket and go straight to CLIMBING.
        if (!mc.player.isOnGround() && mc.player.isGliding() && groundedTick <= 2) {
            info("GROUNDED→CLIMB: bounce-through (already gliding) vel=%.2f", mc.player.getVelocity().length());
            fireRocket();
            ticksSinceFirework = fireworkCooldown.get();
            phase = Phase.CLIMBING;
            climbTick = 0;
            launchRocketsFired = 0;
            elytraSwapSent = false;
            didSwapChest = false;
            return;
        }

        // Always point upward toward climb position
        Vec3d climbPos = getClimbPosition();
        mc.player.setYaw(getYawToTarget(climbPos));
        mc.player.setPitch(-60.0f);

        // Step 1: Equip elytra if not already done during RECOVERING.
        if (!mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
            if (!elytraSwapSent || groundedTick % 3 == 0) {
                info("GROUNDED[%d]: equipping elytra", groundedTick);
                PlayerUtils.silentSwapEquipElytra();
                elytraSwapSent = true;
            }
            return;
        }
        if (elytraSwapSent) {
            elytraSwapSent = false;
            launchRocketsFired = 0;
            groundedTick = 0;
        }

        // Step 2: Jump when on ground.
        if (mc.player.isOnGround()) {
            mc.player.jump();
            return;
        }

        // Step 3: Deploy elytra once falling. Only send START_FALL_FLYING when
        // velocity.y < 0 so the server accepts the deploy.
        // DON'T fire rockets here — the server needs 1-2 ticks to confirm the glide
        // before rocket boosts actually apply. Autoboost handles it in CLIMBING.
        if (!mc.player.isGliding()) {
            if (!mc.player.isOnGround() && mc.player.getVelocity().y < 0) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
            return;
        }

        // Step 4: Gliding confirmed — transition to CLIMBING immediately.
        // Set ticksSinceFirework high so autoboost fires on this same tick.
        info("GROUNDED→CLIMB: vel=%.2f", mc.player.getVelocity().length());
        ticksSinceFirework = fireworkCooldown.get();
        phase = Phase.CLIMBING;
        groundedTick = 0;
        climbTick = 0;
        launchRocketsFired = 0;
        elytraSwapSent = false;
        didSwapChest = false;
    }

    // ── Firework ─────────────────────────────────────────────────────────────────



    private boolean fireRocket() {
        // Off-hand rocket: always works, no slot fiddling needed.
        if (mc.player.getOffHandStack().isOf(Items.FIREWORK_ROCKET)) {
            mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
            ticksSinceFirework = 0;
            info("ROCKET: fired (offhand) | phase=%s vel=%.2f", phase, mc.player.getVelocity().length());
            return true;
        }

        // Route through SwapManager so the swap respects AntiCheatConfig swapMode
        // (None / Auto / SilentHotbar / SilentSwap) just like every other module does.
        if (MeteorClient.SWAP.beginSwap(Items.FIREWORK_ROCKET, true)) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            MeteorClient.SWAP.endSwap(true);
            ticksSinceFirework = 0;
            info("ROCKET: fired (swap) | phase=%s vel=%.2f", phase, mc.player.getVelocity().length());
            return true;
        }

        // SwapManager might be stuck (another module called beginSwap without endSwap).
        // Force-clear the instant swap state and retry once.
        if (ticksSinceFirework > 20) {
            info("ROCKET: unsticking SwapManager (ticksSince=%d)", ticksSinceFirework);
            MeteorClient.SWAP.endSwap(true);
            if (MeteorClient.SWAP.beginSwap(Items.FIREWORK_ROCKET, true)) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                MeteorClient.SWAP.endSwap(true);
                ticksSinceFirework = 0;
                info("ROCKET: fired (unstuck) | phase=%s vel=%.2f", phase, mc.player.getVelocity().length());
                return true;
            }
        }

        info("ROCKET: FAILED | phase=%s gliding=%s onGround=%s vel=%.2f ticksSince=%d",
            phase, mc.player.isGliding(), mc.player.isOnGround(),
            mc.player.getVelocity().length(), ticksSinceFirework);
        return false;
    }

    private void restoreAfterRocket() {
        // Swap lifecycle is fully handled by SwapManager; nothing to do here.
    }

    // ── Elytra ───────────────────────────────────────────────────────────────────

    private void ensureGliding() {
        if (!mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
            PlayerUtils.silentSwapEquipElytra();
        }
        if (!mc.player.isGliding() && !mc.player.isOnGround()) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    // ── Targeting ────────────────────────────────────────────────────────────────

    private Entity findTarget() {
        List<Entity> targets = targetManager.getEntityTargets();
        Entity best = null;
        double bestDist = Double.MAX_VALUE;
        Vec3d pos = mc.player.getPos();

        for (Entity e : targets) {
            if (!(e instanceof PlayerEntity)) continue;
            double dx = e.getX() - pos.x;
            double dz = e.getZ() - pos.z;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > detectionRange.get()) continue;
            if (d < bestDist) { best = e; bestDist = d; }
        }
        return best;
    }

    private boolean validateTarget() {
        if (target == null || !target.isAlive() || target.isRemoved()) return false;
        double dx = target.getX() - mc.player.getX();
        double dz = target.getZ() - mc.player.getZ();
        return Math.sqrt(dx * dx + dz * dz) <= detectionRange.get() * 1.5;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private float getYawToTarget(Vec3d t) {
        return (float) Math.toDegrees(Math.atan2(t.z - mc.player.getZ(), t.x - mc.player.getX())) - 90.0f;
    }

    private float getPitchToTarget(Vec3d t) {
        double dx = t.x - mc.player.getX();
        double dy = t.y - mc.player.getEyeY();
        double dz = t.z - mc.player.getZ();
        return (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
    }

    private static Vec3d closestPointOnBox(Box box, Vec3d p) {
        return new Vec3d(
            Math.max(box.minX, Math.min(p.x, box.maxX)),
            Math.max(box.minY, Math.min(p.y, box.maxY)),
            Math.max(box.minZ, Math.min(p.z, box.maxZ))
        );
    }

    // ── Rendering ────────────────────────────────────────────────────────────────

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (!renderTarget.get() || target == null || !target.isAlive()) return;

        double x = MathHelper.lerp(event.tickDelta, target.lastRenderX, target.getX()) - target.getX();
        double y = MathHelper.lerp(event.tickDelta, target.lastRenderY, target.getY()) - target.getY();
        double z = MathHelper.lerp(event.tickDelta, target.lastRenderZ, target.getZ()) - target.getZ();
        Box box = target.getBoundingBox();

        event.renderer.box(
            x + box.minX, y + box.minY, z + box.minZ,
            x + box.maxX, y + box.maxY, z + box.maxZ,
            targetColor.get(), targetLineColor.get(),
            ShapeMode.Both, 0
        );

        event.renderer.line(
            mc.player.getX(), mc.player.getY() + 1.0, mc.player.getZ(),
            target.getX(), target.getY() + target.getHeight() / 2.0, target.getZ(),
            (phase == Phase.DIVING) ? targetLineColor.get() : targetColor.get()
        );
    }
}
