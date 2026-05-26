package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.List;
import java.util.Objects;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.TargetManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2848;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/MaceDive.class */
public class MaceDive extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgFirework;
    private final SettingGroup sgRender;
    private final Setting<Double> height;
    private final Setting<Double> swapDistance;
    private final Setting<Integer> chestplateHoldTicks;
    private final Setting<Double> angle;
    private final Setting<Double> detectionRange;
    private final Setting<Boolean> pauseOnEat;
    private final Setting<Boolean> freelook;
    private final Setting<Integer> rocketLeadTicks;
    private final Setting<Boolean> autoBoost;
    private final Setting<Integer> fireworkCooldown;
    private final Setting<Double> velocityThreshold;
    private final Setting<Boolean> renderTarget;
    private final Setting<SettingColor> targetColor;
    private final Setting<SettingColor> targetLineColor;
    private final TargetManager targetManager;
    private Phase phase;
    private class_1297 target;
    private int ticksSinceFirework;
    private int diveCount;
    private int recoverTick;
    private int groundedTick;
    private boolean didSwapChest;
    private boolean elytraSwapSent;
    private int launchRocketsFired;
    private boolean diveRocketFired;
    private int climbTick;
    private int diveTick;
    private int consecutiveTimeouts;
    private boolean freelookWasActive;
    private FreeLook.Mode freelookPrevMode;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/MaceDive$Phase.class */
    private enum Phase {
        SEARCHING,
        CLIMBING,
        DIVING,
        RECOVERING,
        GROUNDED
    }

    public MaceDive() {
        super(Categories.Combat, "mace-dive", "Flight controller for mace diving. Use with KillAura/MaceAura for attacks.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgFirework = this.settings.createGroup("Firework");
        this.sgRender = this.settings.createGroup("Render");
        this.height = this.sgGeneral.add(new DoubleSetting.Builder().name("height").description("Height above target to climb to.").defaultValue(20.0d).min(5.0d).sliderRange(5.0d, 60.0d).build());
        this.swapDistance = this.sgGeneral.add(new DoubleSetting.Builder().name("swap-distance").description("3D distance from target to swap elytra to chestplate. Set to 0 to disable swap entirely.").defaultValue(3.5d).min(0.0d).sliderRange(0.0d, 8.0d).build());
        this.chestplateHoldTicks = this.sgGeneral.add(new IntSetting.Builder().name("chestplate-hold-ticks").description("Ticks to keep the chestplate on before re-equipping elytra. Higher = more time for MaceAura to attack, lower = faster bounce.").defaultValue(2).min(1).sliderRange(1, 5).build());
        this.angle = this.sgGeneral.add(new DoubleSetting.Builder().name("angle").description("Approach angle. 90 = straight above, lower = offset to the side.").defaultValue(84.0d).min(30.0d).sliderRange(30.0d, 90.0d).build());
        this.detectionRange = this.sgGeneral.add(new DoubleSetting.Builder().name("detection-range").description("Max horizontal range to detect targets.").defaultValue(64.0d).min(10.0d).sliderRange(10.0d, 128.0d).build());
        this.pauseOnEat = this.sgGeneral.add(new BoolSetting.Builder().name("pause-on-eat").description("Pauses while using an item.").defaultValue(true).build());
        this.freelook = this.sgGeneral.add(new BoolSetting.Builder().name("freelook").description("Hides your rotation changes visually using FreeLook camera mode. Steering still works server-side.").defaultValue(false).build());
        this.rocketLeadTicks = this.sgFirework.add(new IntSetting.Builder().name("rocket-lead-ticks").description("Fire a rocket this many ticks before the chestplate swap to pre-cache velocity for instant relaunch.").defaultValue(5).min(0).sliderRange(0, 15).build());
        this.autoBoost = this.sgFirework.add(new BoolSetting.Builder().name("auto-boost").description("Automatically use firework rockets.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgFirework;
        IntSetting.Builder builderSliderRange = new IntSetting.Builder().name("firework-cooldown").description("Minimum ticks between firework uses.").defaultValue(10).min(1).sliderRange(1, 40);
        Setting<Boolean> setting = this.autoBoost;
        Objects.requireNonNull(setting);
        this.fireworkCooldown = settingGroup.add(builderSliderRange.visible(setting::get).build());
        SettingGroup settingGroup2 = this.sgFirework;
        DoubleSetting.Builder builderSliderRange2 = new DoubleSetting.Builder().name("velocity-threshold").description("Use firework when speed is below this value.").defaultValue(2.0d).min(0.1d).sliderRange(0.1d, 5.0d);
        Setting<Boolean> setting2 = this.autoBoost;
        Objects.requireNonNull(setting2);
        this.velocityThreshold = settingGroup2.add(builderSliderRange2.visible(setting2::get).build());
        this.renderTarget = this.sgRender.add(new BoolSetting.Builder().name("render-target").description("Render the target bounding box.").defaultValue(true).build());
        this.targetColor = this.sgRender.add(new ColorSetting.Builder().name("target-color").defaultValue(new SettingColor(255, 100, 0, 50)).build());
        this.targetLineColor = this.sgRender.add(new ColorSetting.Builder().name("target-line-color").defaultValue(new SettingColor(255, 100, 0, Opcode.FCMPG)).build());
        this.targetManager = new TargetManager(this, true);
        this.phase = Phase.SEARCHING;
        this.target = null;
        this.ticksSinceFirework = 0;
        this.diveCount = 0;
        this.recoverTick = 0;
        this.groundedTick = 0;
        this.didSwapChest = false;
        this.elytraSwapSent = false;
        this.launchRocketsFired = 0;
        this.diveRocketFired = false;
        this.climbTick = 0;
        this.diveTick = 0;
        this.consecutiveTimeouts = 0;
        this.freelookWasActive = false;
        this.freelookPrevMode = null;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.phase = Phase.SEARCHING;
        this.target = null;
        this.ticksSinceFirework = 0;
        this.diveCount = 0;
        this.recoverTick = 0;
        this.groundedTick = 0;
        this.didSwapChest = false;
        this.elytraSwapSent = false;
        if (this.freelook.get().booleanValue()) {
            FreeLook fl = (FreeLook) Modules.get().get(FreeLook.class);
            this.freelookWasActive = fl.isActive();
            this.freelookPrevMode = fl.mode.get();
            fl.cameraYaw = this.mc.field_1724.method_36454();
            fl.cameraPitch = this.mc.field_1724.method_36455();
            fl.mode.set(FreeLook.Mode.Camera);
            if (!fl.isActive()) {
                fl.toggle();
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        this.target = null;
        if (this.freelook.get().booleanValue()) {
            FreeLook fl = (FreeLook) Modules.get().get(FreeLook.class);
            if (!this.freelookWasActive && fl.isActive()) {
                fl.toggle();
            } else if (this.freelookWasActive && this.freelookPrevMode != null) {
                fl.mode.set(this.freelookPrevMode);
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return this.target != null ? EntityUtils.getName(this.target) + " [" + this.phase.name() + "] x" + this.diveCount : this.phase.name();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || !this.mc.field_1724.method_5805() || this.mc.field_1724.method_7325()) {
            return;
        }
        try {
            restoreAfterRocket();
            if (this.pauseOnEat.get().booleanValue() && this.mc.field_1724.method_6115()) {
                return;
            }
            this.ticksSinceFirework++;
            switch (this.phase) {
                case SEARCHING:
                    tickSearching();
                    break;
                case CLIMBING:
                    tickClimbing();
                    break;
                case DIVING:
                    tickDiving();
                    break;
                case RECOVERING:
                    tickRecovering();
                    break;
                case GROUNDED:
                    tickGrounded();
                    break;
            }
            if (this.autoBoost.get().booleanValue() && this.mc.field_1724 != null && this.mc.field_1724.method_6128() && this.phase == Phase.CLIMBING && this.mc.field_1724.method_18798().method_1033() < this.velocityThreshold.get().doubleValue() && this.ticksSinceFirework >= this.fireworkCooldown.get().intValue()) {
                info("AUTOBOOST: vel=%.2f ticksSince=%d phase=%s", Double.valueOf(this.mc.field_1724.method_18798().method_1033()), Integer.valueOf(this.ticksSinceFirework), this.phase);
                fireRocket();
            }
        } catch (NullPointerException e) {
        }
    }

    private void tickSearching() {
        this.target = findTarget();
        if (this.target == null) {
            return;
        }
        if (this.mc.field_1724.method_24828()) {
            info("SEARCH→GROUNDED: standing start", new Object[0]);
            this.phase = Phase.GROUNDED;
            this.groundedTick = 0;
            this.elytraSwapSent = false;
            return;
        }
        ensureGliding();
        if (this.mc.field_1724.method_6128()) {
            this.phase = Phase.CLIMBING;
            this.climbTick = 0;
        }
    }

    private class_243 getClimbPosition() {
        double angleRad = Math.toRadians(this.angle.get().doubleValue());
        double horizOff = this.angle.get().doubleValue() >= 89.5d ? 0.0d : this.height.get().doubleValue() / Math.tan(angleRad);
        double dx = this.mc.field_1724.method_23317() - this.target.method_23317();
        double dz = this.mc.field_1724.method_23321() - this.target.method_23321();
        double dist = Math.sqrt((dx * dx) + (dz * dz));
        double nx = dist > 0.1d ? dx / dist : 1.0d;
        double nz = dist > 0.1d ? dz / dist : 0.0d;
        return new class_243(this.target.method_23317() + (nx * horizOff), this.target.method_23318() + this.height.get().doubleValue(), this.target.method_23321() + (nz * horizOff));
    }

    private void tickClimbing() {
        if (!validateTarget()) {
            this.phase = Phase.SEARCHING;
            return;
        }
        this.climbTick++;
        if (this.climbTick > 100) {
            this.consecutiveTimeouts++;
            if (this.consecutiveTimeouts >= 2) {
                info("CLIMB→OFF: %d consecutive timeouts, target unreachable — disabling", Integer.valueOf(this.consecutiveTimeouts));
                this.consecutiveTimeouts = 0;
                toggle();
                return;
            } else {
                info("CLIMB→SEARCH: timeout after %d ticks (%d/2), re-evaluating target", Integer.valueOf(this.climbTick), Integer.valueOf(this.consecutiveTimeouts));
                this.target = null;
                this.phase = Phase.SEARCHING;
                this.climbTick = 0;
                return;
            }
        }
        if (this.mc.field_1724.method_24828() && this.mc.field_1724.method_18798().method_1033() < 0.5d) {
            info("CLIMB→GROUNDED: knocked onto block | vel=%.2f", Double.valueOf(this.mc.field_1724.method_18798().method_1033()));
            this.phase = Phase.GROUNDED;
            this.groundedTick = 0;
            this.elytraSwapSent = false;
            this.climbTick = 0;
            return;
        }
        ensureGliding();
        class_243 climbPos = getClimbPosition();
        double distToClimb = climbPos.method_1022(this.mc.field_1724.method_19538());
        boolean atClimbPos = distToClimb < 3.0d && this.mc.field_1724.method_23318() >= climbPos.field_1351 - 1.0d;
        boolean overHeight = this.mc.field_1724.method_23318() > climbPos.field_1351 + 5.0d;
        if (atClimbPos || overHeight) {
            Object[] objArr = new Object[3];
            objArr[0] = Double.valueOf(this.mc.field_1724.method_23318() - this.target.method_23318());
            objArr[1] = Double.valueOf(this.mc.field_1724.method_18798().method_1033());
            objArr[2] = overHeight ? " (height overshoot)" : "";
            info("CLIMB→DIVE: at height %.1f vel=%.2f%s", objArr);
            this.phase = Phase.DIVING;
            this.didSwapChest = false;
            this.diveRocketFired = false;
            this.diveTick = 0;
            this.climbTick = 0;
            this.consecutiveTimeouts = 0;
            return;
        }
        this.mc.field_1724.method_36456(getYawToTarget(climbPos));
        this.mc.field_1724.method_36457(this.mc.field_1724.method_23318() < climbPos.field_1351 ? -60.0f : -10.0f);
    }

    private void tickDiving() {
        if (!validateTarget()) {
            this.phase = Phase.SEARCHING;
            return;
        }
        this.diveTick++;
        class_243 point = closestPointOnBox(this.target.method_5829(), this.mc.field_1724.method_33571());
        double distToHitbox = point.method_1022(this.mc.field_1724.method_33571());
        class_243 targetCenter = this.target.method_19538().method_1031(0.0d, ((double) this.target.method_17682()) / 2.0d, 0.0d);
        this.mc.field_1724.method_36456(getYawToTarget(targetCenter));
        this.mc.field_1724.method_36457(getPitchToTarget(targetCenter));
        if (this.swapDistance.get().doubleValue() <= 0.0d || distToHitbox > this.swapDistance.get().doubleValue()) {
            ensureGliding();
        }
        double closingSpeed = this.mc.field_1724.method_18798().method_1033();
        if (this.swapDistance.get().doubleValue() > 0.0d && !this.diveRocketFired && this.rocketLeadTicks.get().intValue() > 0 && closingSpeed > 0.01d && distToHitbox > this.swapDistance.get().doubleValue()) {
            double ticksToSwap = (distToHitbox - this.swapDistance.get().doubleValue()) / closingSpeed;
            if (ticksToSwap <= this.rocketLeadTicks.get().intValue()) {
                info("DIVE: pre-swap rocket | dist=%.1f vel=%.2f ticksToSwap=%.1f", Double.valueOf(distToHitbox), Double.valueOf(closingSpeed), Double.valueOf(ticksToSwap));
                fireRocket();
                this.diveRocketFired = true;
            }
        }
        if (this.swapDistance.get().doubleValue() > 0.0d && distToHitbox <= this.swapDistance.get().doubleValue() * 2.0d && !this.didSwapChest) {
            info("DIVE APPROACH: dist=%.1f threshold=%.1f vel=%.2f overshoot=%.1f", Double.valueOf(distToHitbox), this.swapDistance.get(), Double.valueOf(this.mc.field_1724.method_18798().method_1033()), Double.valueOf(this.swapDistance.get().doubleValue() - distToHitbox));
        }
        if (this.swapDistance.get().doubleValue() > 0.0d && distToHitbox <= this.swapDistance.get().doubleValue() && !this.didSwapChest) {
            String chestBefore = this.mc.field_1724.method_6118(class_1304.field_6174).method_7909().toString();
            boolean swapFired = true;
            if (this.mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833)) {
                swapFired = PlayerUtils.silentSwapEquipChestplate();
            }
            String chestAfter = this.mc.field_1724.method_6118(class_1304.field_6174).method_7909().toString();
            info("DIVE SWAP: chest [%s→%s] fired=%s dist=%.1f vel=%.2f", chestBefore, chestAfter, Boolean.valueOf(swapFired), Double.valueOf(distToHitbox), Double.valueOf(this.mc.field_1724.method_18798().method_1033()));
            if (swapFired) {
                this.didSwapChest = true;
                this.diveRocketFired = false;
                this.diveCount++;
                this.recoverTick = 0;
                this.elytraSwapSent = false;
                this.phase = Phase.RECOVERING;
                return;
            }
            return;
        }
        if (this.swapDistance.get().doubleValue() > 0.0d || distToHitbox > 3.5d) {
            if (this.mc.field_1724.method_23318() < this.target.method_23318() - 3.0d) {
                this.phase = Phase.CLIMBING;
                this.didSwapChest = false;
                this.diveRocketFired = false;
                this.climbTick = 0;
                return;
            }
            return;
        }
        this.diveCount++;
        class_243 climbPos = getClimbPosition();
        this.mc.field_1724.method_36456(getYawToTarget(climbPos));
        this.mc.field_1724.method_36457(-60.0f);
        this.phase = Phase.CLIMBING;
        this.didSwapChest = false;
        this.diveRocketFired = false;
        this.climbTick = 0;
    }

    private void tickRecovering() {
        this.recoverTick++;
        String chestItem = this.mc.field_1724.method_6118(class_1304.field_6174).method_7909().toString();
        info("RECOVER[%d]: chest=%s gliding=%s onGround=%s vel=%.2f velY=%.2f", Integer.valueOf(this.recoverTick), chestItem, Boolean.valueOf(this.mc.field_1724.method_6128()), Boolean.valueOf(this.mc.field_1724.method_24828()), Double.valueOf(this.mc.field_1724.method_18798().method_1033()), Double.valueOf(this.mc.field_1724.method_18798().field_1351));
        class_243 climbPos = getClimbPosition();
        this.mc.field_1724.method_36456(getYawToTarget(climbPos));
        this.mc.field_1724.method_36457(-90.0f);
        if (this.recoverTick >= this.chestplateHoldTicks.get().intValue() && !this.mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833) && (!this.elytraSwapSent || this.recoverTick % 3 == 0)) {
            info("RECOVER[%d]: sending elytra re-equip | onGround=%s vel=%.2f", Integer.valueOf(this.recoverTick), Boolean.valueOf(this.mc.field_1724.method_24828()), Double.valueOf(this.mc.field_1724.method_18798().method_1033()));
            PlayerUtils.silentSwapEquipElytra();
            this.elytraSwapSent = true;
        }
        if (this.recoverTick > 60) {
            info("RECOVER→GROUNDED: safety timeout at tick %d", Integer.valueOf(this.recoverTick));
            this.phase = Phase.GROUNDED;
            this.groundedTick = 0;
            this.elytraSwapSent = false;
            this.didSwapChest = false;
            return;
        }
        if (this.mc.field_1724.method_24828()) {
            info("RECOVER→GROUNDED: landed at tick %d vel=%.2f elytra=%s", Integer.valueOf(this.recoverTick), Double.valueOf(this.mc.field_1724.method_18798().method_1033()), Boolean.valueOf(this.mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833)));
            this.phase = Phase.GROUNDED;
            this.groundedTick = 0;
            this.didSwapChest = false;
            return;
        }
        if (!this.mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833) || this.recoverTick < this.chestplateHoldTicks.get().intValue()) {
            return;
        }
        this.elytraSwapSent = false;
        info("RECOVER→CLIMB: bounce at tick %d | deploy+rocket same tick | vel=%.2f gliding=%s", Integer.valueOf(this.recoverTick), Double.valueOf(this.mc.field_1724.method_18798().method_1033()), Boolean.valueOf(this.mc.field_1724.method_6128()));
        this.mc.method_1562().method_52787(new class_2848(this.mc.field_1724, class_2848.class_2849.field_12982));
        fireRocket();
        this.ticksSinceFirework = this.fireworkCooldown.get().intValue();
        this.phase = Phase.CLIMBING;
        this.didSwapChest = false;
        this.climbTick = 0;
    }

    private void tickGrounded() {
        this.groundedTick++;
        if (!this.mc.field_1724.method_24828() && this.mc.field_1724.method_6128() && this.groundedTick <= 2) {
            info("GROUNDED→CLIMB: bounce-through (already gliding) vel=%.2f", Double.valueOf(this.mc.field_1724.method_18798().method_1033()));
            fireRocket();
            this.ticksSinceFirework = this.fireworkCooldown.get().intValue();
            this.phase = Phase.CLIMBING;
            this.climbTick = 0;
            this.launchRocketsFired = 0;
            this.elytraSwapSent = false;
            this.didSwapChest = false;
            return;
        }
        class_243 climbPos = getClimbPosition();
        this.mc.field_1724.method_36456(getYawToTarget(climbPos));
        this.mc.field_1724.method_36457(-60.0f);
        if (!this.mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833)) {
            if (!this.elytraSwapSent || this.groundedTick % 3 == 0) {
                info("GROUNDED[%d]: equipping elytra", Integer.valueOf(this.groundedTick));
                PlayerUtils.silentSwapEquipElytra();
                this.elytraSwapSent = true;
                return;
            }
            return;
        }
        if (this.elytraSwapSent) {
            this.elytraSwapSent = false;
            this.launchRocketsFired = 0;
            this.groundedTick = 0;
        }
        if (this.mc.field_1724.method_24828()) {
            this.mc.field_1724.method_6043();
            return;
        }
        if (!this.mc.field_1724.method_6128()) {
            if (!this.mc.field_1724.method_24828() && this.mc.field_1724.method_18798().field_1351 < 0.0d) {
                this.mc.method_1562().method_52787(new class_2848(this.mc.field_1724, class_2848.class_2849.field_12982));
                return;
            }
            return;
        }
        info("GROUNDED→CLIMB: vel=%.2f", Double.valueOf(this.mc.field_1724.method_18798().method_1033()));
        this.ticksSinceFirework = this.fireworkCooldown.get().intValue();
        this.phase = Phase.CLIMBING;
        this.groundedTick = 0;
        this.climbTick = 0;
        this.launchRocketsFired = 0;
        this.elytraSwapSent = false;
        this.didSwapChest = false;
    }

    private boolean fireRocket() {
        if (this.mc.field_1724.method_6079().method_31574(class_1802.field_8639)) {
            this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5810);
            this.ticksSinceFirework = 0;
            info("ROCKET: fired (offhand) | phase=%s vel=%.2f", this.phase, Double.valueOf(this.mc.field_1724.method_18798().method_1033()));
            return true;
        }
        if (MeteorClient.SWAP.beginSwap(class_1802.field_8639, true)) {
            this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5808);
            MeteorClient.SWAP.endSwap(true);
            this.ticksSinceFirework = 0;
            info("ROCKET: fired (swap) | phase=%s vel=%.2f", this.phase, Double.valueOf(this.mc.field_1724.method_18798().method_1033()));
            return true;
        }
        if (this.ticksSinceFirework > 20) {
            info("ROCKET: unsticking SwapManager (ticksSince=%d)", Integer.valueOf(this.ticksSinceFirework));
            MeteorClient.SWAP.endSwap(true);
            if (MeteorClient.SWAP.beginSwap(class_1802.field_8639, true)) {
                this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5808);
                MeteorClient.SWAP.endSwap(true);
                this.ticksSinceFirework = 0;
                info("ROCKET: fired (unstuck) | phase=%s vel=%.2f", this.phase, Double.valueOf(this.mc.field_1724.method_18798().method_1033()));
                return true;
            }
        }
        info("ROCKET: FAILED | phase=%s gliding=%s onGround=%s vel=%.2f ticksSince=%d", this.phase, Boolean.valueOf(this.mc.field_1724.method_6128()), Boolean.valueOf(this.mc.field_1724.method_24828()), Double.valueOf(this.mc.field_1724.method_18798().method_1033()), Integer.valueOf(this.ticksSinceFirework));
        return false;
    }

    private void restoreAfterRocket() {
    }

    private void ensureGliding() {
        if (!this.mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833)) {
            PlayerUtils.silentSwapEquipElytra();
        }
        if (!this.mc.field_1724.method_6128() && !this.mc.field_1724.method_24828()) {
            this.mc.method_1562().method_52787(new class_2848(this.mc.field_1724, class_2848.class_2849.field_12982));
        }
    }

    private class_1297 findTarget() {
        List<class_1297> targets = this.targetManager.getEntityTargets();
        class_1297 best = null;
        double bestDist = Double.MAX_VALUE;
        class_243 pos = this.mc.field_1724.method_19538();
        for (class_1297 e : targets) {
            if (e instanceof class_1657) {
                double dx = e.method_23317() - pos.field_1352;
                double dz = e.method_23321() - pos.field_1350;
                double d = Math.sqrt((dx * dx) + (dz * dz));
                if (d <= this.detectionRange.get().doubleValue() && d < bestDist) {
                    best = e;
                    bestDist = d;
                }
            }
        }
        return best;
    }

    private boolean validateTarget() {
        if (this.target == null || !this.target.method_5805() || this.target.method_31481()) {
            return false;
        }
        double dx = this.target.method_23317() - this.mc.field_1724.method_23317();
        double dz = this.target.method_23321() - this.mc.field_1724.method_23321();
        return Math.sqrt((dx * dx) + (dz * dz)) <= this.detectionRange.get().doubleValue() * 1.5d;
    }

    private float getYawToTarget(class_243 t) {
        return ((float) Math.toDegrees(Math.atan2(t.field_1350 - this.mc.field_1724.method_23321(), t.field_1352 - this.mc.field_1724.method_23317()))) - 90.0f;
    }

    private float getPitchToTarget(class_243 t) {
        double dx = t.field_1352 - this.mc.field_1724.method_23317();
        double dy = t.field_1351 - this.mc.field_1724.method_23320();
        double dz = t.field_1350 - this.mc.field_1724.method_23321();
        return (float) (-Math.toDegrees(Math.atan2(dy, Math.sqrt((dx * dx) + (dz * dz)))));
    }

    private static class_243 closestPointOnBox(class_238 box, class_243 p) {
        return new class_243(Math.max(box.field_1323, Math.min(p.field_1352, box.field_1320)), Math.max(box.field_1322, Math.min(p.field_1351, box.field_1325)), Math.max(box.field_1321, Math.min(p.field_1350, box.field_1324)));
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || !this.renderTarget.get().booleanValue() || this.target == null || !this.target.method_5805()) {
            return;
        }
        double x = class_3532.method_16436(event.tickDelta, this.target.field_6038, this.target.method_23317()) - this.target.method_23317();
        double y = class_3532.method_16436(event.tickDelta, this.target.field_5971, this.target.method_23318()) - this.target.method_23318();
        double z = class_3532.method_16436(event.tickDelta, this.target.field_5989, this.target.method_23321()) - this.target.method_23321();
        class_238 box = this.target.method_5829();
        event.renderer.box(x + box.field_1323, y + box.field_1322, z + box.field_1321, x + box.field_1320, y + box.field_1325, z + box.field_1324, this.targetColor.get(), this.targetLineColor.get(), ShapeMode.Both, 0);
        event.renderer.line(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318() + 1.0d, this.mc.field_1724.method_23321(), this.target.method_23317(), this.target.method_23318() + (((double) this.target.method_17682()) / 2.0d), this.target.method_23321(), this.phase == Phase.DIVING ? this.targetLineColor.get() : this.targetColor.get());
    }
}
