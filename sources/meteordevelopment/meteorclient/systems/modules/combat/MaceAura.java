package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.TargetManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1887;
import net.minecraft.class_1893;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2824;
import net.minecraft.class_5134;
import net.minecraft.class_5321;
import net.minecraft.class_7923;
import net.minecraft.class_9285;
import net.minecraft.class_9334;
import net.minecraft.class_9362;
import org.apache.commons.lang3.mutable.MutableDouble;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/MaceAura.class */
public class MaceAura extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> range;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> snapRotation;
    private final Setting<Boolean> silentSwapOverrideDelay;
    private final Setting<Boolean> chestSwapOnApproach;
    private final Setting<Double> swapRange;
    private final Setting<Boolean> breachSwap;
    private final Setting<Boolean> flipFlop;
    private static final class_5321<class_1887> BREACH_KEY = class_1893.field_50158;
    private long lastSwordAttackTime;
    private int flipFlopCount;
    private boolean useDensityNext;
    private SwapState swapState;
    private final TargetManager targetManager;
    private final SettingGroup sgGrim;
    private final Setting<Boolean> grimSafe;
    private final Setting<Boolean> enablePrediction;
    private final Setting<Boolean> adjustForFlying;
    private final Setting<Double> maxRotStep;
    private final Setting<Double> predictionMs;
    private final Setting<Double> elytraPredictionScale;
    private final Setting<Double> flyingRangeBonus;
    private final Setting<Double> aimYOffset;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/MaceAura$SwapState.class */
    private enum SwapState {
        NONE,
        SWORD_PENDING,
        MACE2_PENDING
    }

    private boolean isMace(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (stack.method_7909() instanceof class_9362) {
            return true;
        }
        try {
            if (stack.method_31574(class_1802.field_49814)) {
                return true;
            }
        } catch (Throwable th) {
        }
        try {
            String id = class_7923.field_41178.method_10221(stack.method_7909()).toString().toLowerCase();
            if (id.contains("mace")) {
                return true;
            }
        } catch (Throwable th2) {
        }
        String name = stack.method_7964().getString().toLowerCase().trim();
        return name.contains("mace");
    }

    public MaceAura() {
        super(Categories.Combat, "mace-aura", "Automatically attacks targets with a mace using vanilla delays.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.range = this.sgGeneral.add(new DoubleSetting.Builder().name("range").description("Attack range.").defaultValue(3.0d).min(1.0d).sliderMax(6.0d).build());
        this.rotate = this.sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Rotate to face the target before attacking.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgGeneral;
        BoolSetting.Builder builderDefaultValue = new BoolSetting.Builder().name("snap-rotate").description("Instantly rotate to target when in range.").defaultValue(true);
        Setting<Boolean> setting = this.rotate;
        Objects.requireNonNull(setting);
        this.snapRotation = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        this.silentSwapOverrideDelay = this.sgGeneral.add(new BoolSetting.Builder().name("silent-swap-override-delay").description("Use held-item delay when silent swapping to mace.").defaultValue(true).build());
        this.chestSwapOnApproach = this.sgGeneral.add(new BoolSetting.Builder().name("chest-swap-on-approach").description("If wearing elytra, swap to chestplate when a target is within range (not hit range).").defaultValue(true).build());
        SettingGroup settingGroup2 = this.sgGeneral;
        DoubleSetting.Builder builderSliderMax = new DoubleSetting.Builder().name("swap-range").description("Range to trigger chestplate swap when wearing elytra.").defaultValue(6.0d).min(1.0d).sliderMax(12.0d);
        Setting<Boolean> setting2 = this.chestSwapOnApproach;
        Objects.requireNonNull(setting2);
        this.swapRange = settingGroup2.add(builderSliderMax.visible(setting2::get).build());
        this.breachSwap = this.sgGeneral.add(new BoolSetting.Builder().name("breach-swap").description("Hit with a breach mace, swap to sword and hit, then swap back and hit again.").defaultValue(false).build());
        this.flipFlop = this.sgGeneral.add(new BoolSetting.Builder().name("flip-flop").description("Alternate between breach and density maces after each successful hit.").defaultValue(false).build());
        this.lastSwordAttackTime = 0L;
        this.flipFlopCount = 0;
        this.useDensityNext = false;
        this.swapState = SwapState.NONE;
        this.targetManager = new TargetManager(this, true);
        this.sgGrim = this.settings.createGroup("2b2t / Grim");
        this.grimSafe = this.sgGrim.add(new BoolSetting.Builder().name("grim-safe").description("Clamp rotation and avoid sketchy behavior for Grim v3.").defaultValue(true).build());
        this.enablePrediction = this.sgGrim.add(new BoolSetting.Builder().name("enable-prediction").description("Predict target movement when aiming.").defaultValue(true).build());
        this.adjustForFlying = this.sgGrim.add(new BoolSetting.Builder().name("adjust-for-flying").description("Adjust range and prediction for flying targets.").defaultValue(true).build());
        SettingGroup settingGroup3 = this.sgGrim;
        DoubleSetting.Builder builderSliderMax2 = new DoubleSetting.Builder().name("max-rot-step").description("Max degrees per tick to rotate when grim-safe.").defaultValue(35.0d).min(5.0d).sliderMax(90.0d);
        Setting<Boolean> setting3 = this.grimSafe;
        Objects.requireNonNull(setting3);
        this.maxRotStep = settingGroup3.add(builderSliderMax2.visible(setting3::get).build());
        SettingGroup settingGroup4 = this.sgGrim;
        DoubleSetting.Builder builderSliderMax3 = new DoubleSetting.Builder().name("prediction-ms").description("Lead prediction in milliseconds.").defaultValue(120.0d).min(0.0d).sliderMax(300.0d);
        Setting<Boolean> setting4 = this.enablePrediction;
        Objects.requireNonNull(setting4);
        this.predictionMs = settingGroup4.add(builderSliderMax3.visible(setting4::get).build());
        this.elytraPredictionScale = this.sgGrim.add(new DoubleSetting.Builder().name("elytra-predict-scale").description("Extra prediction scale when target is flying.").defaultValue(1.4d).min(1.0d).sliderMax(2.5d).visible(() -> {
            return this.enablePrediction.get().booleanValue() && this.adjustForFlying.get().booleanValue();
        }).build());
        SettingGroup settingGroup5 = this.sgGrim;
        DoubleSetting.Builder builderSliderMax4 = new DoubleSetting.Builder().name("flying-range-bonus").description("Extra acquisition range when target is flying.").defaultValue(0.3d).min(0.0d).sliderMax(1.0d);
        Setting<Boolean> setting5 = this.adjustForFlying;
        Objects.requireNonNull(setting5);
        this.flyingRangeBonus = settingGroup5.add(builderSliderMax4.visible(setting5::get).build());
        this.aimYOffset = this.sgGrim.add(new DoubleSetting.Builder().name("aim-y-offset").description("Vertical aim offset for flying targets.").defaultValue(-0.2d).min(-1.0d).sliderMax(1.0d).visible(() -> {
            return this.enablePrediction.get().booleanValue() && this.adjustForFlying.get().booleanValue();
        }).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        if (!this.flipFlop.get().booleanValue() || this.flipFlopCount <= 0) {
            return null;
        }
        return "FF:" + this.flipFlopCount;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null || this.mc.field_1724.method_7325() || !this.mc.field_1724.method_5805() || this.mc.field_1724.method_6115()) {
            return;
        }
        FindItemResult weapon = findWeapon();
        if (weapon.found()) {
            List<class_1297> targets = this.targetManager.getEntityTargets();
            if (targets.isEmpty()) {
                return;
            }
            class_1297 best = null;
            double bestDist = Double.MAX_VALUE;
            class_243 eyes = this.mc.field_1724.method_33571();
            for (class_1297 e : targets) {
                double acqRange = this.range.get().doubleValue();
                if (this.adjustForFlying.get().booleanValue() && isEntityFlying(e)) {
                    acqRange += this.flyingRangeBonus.get().doubleValue();
                }
                double d = closestPointOnBox(e.method_5829(), eyes).method_1022(eyes);
                if (d <= acqRange && d < bestDist) {
                    best = e;
                    bestDist = d;
                }
            }
            if (best == null) {
                return;
            }
            if (this.chestSwapOnApproach.get().booleanValue() && this.mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833)) {
                double swapDist = closestPointOnBox(best.method_5829(), eyes).method_1022(eyes);
                if (swapDist <= this.swapRange.get().doubleValue()) {
                    ChestSwap chestSwap = (ChestSwap) Modules.get().get(ChestSwap.class);
                    if (chestSwap != null) {
                        PlayerUtils.silentSwapEquipChestplate();
                    }
                }
            }
            if (this.rotate.get().booleanValue()) {
                class_243 point = this.enablePrediction.get().booleanValue() ? predictedAimPoint(best, eyes) : closestPointOnBox(best.method_5829(), eyes);
                if (this.grimSafe.get().booleanValue()) {
                    float[] tgt = MeteorClient.ROTATION.getRotation(point);
                    float curYaw = this.mc.field_1724.method_36454();
                    float curPitch = this.mc.field_1724.method_36455();
                    float nextYaw = clampAngle(curYaw, tgt[0], this.maxRotStep.get().floatValue());
                    float nextPitch = clampAngle(curPitch, tgt[1], this.maxRotStep.get().floatValue());
                    MeteorClient.ROTATION.requestRotation(nextYaw, nextPitch, 9.0d);
                } else {
                    if (this.snapRotation.get().booleanValue()) {
                        MeteorClient.ROTATION.snapAt(point);
                    }
                    MeteorClient.ROTATION.requestRotation(point, 9.0d);
                }
                if (!MeteorClient.ROTATION.lookingAt(best.method_5829())) {
                    return;
                }
            }
            if (this.breachSwap.get().booleanValue()) {
                handleBreachSwap(best);
                return;
            }
            if (this.flipFlop.get().booleanValue()) {
                FindItemResult densityMace = findWeaponWithEnchant(class_1893.field_50157);
                FindItemResult breachMace = findWeaponWithEnchant(BREACH_KEY);
                FindItemResult chosen = this.useDensityNext ? densityMace : breachMace;
                if (!chosen.found()) {
                    chosen = this.useDensityNext ? breachMace : densityMace;
                }
                if (!chosen.found()) {
                    chosen = weapon;
                }
                int delayCheckSlot = chosen.slot();
                if (this.silentSwapOverrideDelay.get().booleanValue()) {
                    delayCheckSlot = this.mc.field_1724.method_31548().field_7545;
                }
                if (delayReady(delayCheckSlot)) {
                    boolean isHolding = chosen.isMainHand();
                    if (MeteorClient.SWAP.beginSwap(chosen, true)) {
                        attack(best, !isHolding);
                        MeteorClient.SWAP.endSwap(true);
                        this.useDensityNext = !this.useDensityNext;
                        this.flipFlopCount++;
                        return;
                    }
                    return;
                }
                return;
            }
            int delayCheckSlot2 = weapon.slot();
            if (this.silentSwapOverrideDelay.get().booleanValue()) {
                delayCheckSlot2 = this.mc.field_1724.method_31548().field_7545;
            }
            if (delayReady(delayCheckSlot2)) {
                boolean isHolding2 = weapon.isMainHand();
                if (MeteorClient.SWAP.beginSwap(weapon, true)) {
                    attack(best, !isHolding2);
                    MeteorClient.SWAP.endSwap(true);
                }
            }
        }
    }

    private boolean delayReady(int slotForCooldown) {
        return this.mc.field_1724.method_7261(0.0f) >= 1.0f;
    }

    private void attack(class_1297 target, boolean didSwap) {
        this.mc.method_1562().method_52787(class_2824.method_34206(target, this.mc.field_1724.method_5715()));
        this.mc.field_1724.method_6104(class_1268.field_5808);
    }

    private void handleBreachSwap(class_1297 target) {
        if (this.swapState == SwapState.NONE) {
            FindItemResult breachMace = findWeaponWithEnchant(BREACH_KEY);
            if (breachMace.found()) {
                int delayCheckSlot = breachMace.slot();
                if (this.silentSwapOverrideDelay.get().booleanValue()) {
                    delayCheckSlot = this.mc.field_1724.method_31548().field_7545;
                }
                if (delayReady(delayCheckSlot)) {
                    boolean isHolding = breachMace.isMainHand();
                    if (MeteorClient.SWAP.beginSwap(breachMace, true)) {
                        attack(target, !isHolding);
                        MeteorClient.SWAP.endSwap(true);
                        this.swapState = SwapState.SWORD_PENDING;
                        return;
                    }
                    return;
                }
                return;
            }
            return;
        }
        if (this.swapState == SwapState.SWORD_PENDING) {
            FindItemResult sword = findSword();
            if (!sword.found()) {
                this.swapState = SwapState.NONE;
                return;
            }
            if (swordDelayReady(sword.slot())) {
                sword.isMainHand();
                if (MeteorClient.SWAP.beginSwap(sword, true)) {
                    this.mc.method_1562().method_52787(class_2824.method_34206(target, this.mc.field_1724.method_5715()));
                    this.mc.field_1724.method_6104(class_1268.field_5808);
                    this.lastSwordAttackTime = System.currentTimeMillis();
                    MeteorClient.SWAP.endSwap(true);
                    this.swapState = SwapState.MACE2_PENDING;
                    return;
                }
                return;
            }
            return;
        }
        if (this.swapState == SwapState.MACE2_PENDING) {
            FindItemResult breachMace2 = findWeaponWithEnchant(BREACH_KEY);
            if (!breachMace2.found()) {
                this.swapState = SwapState.NONE;
                return;
            }
            int delayCheckSlot2 = breachMace2.slot();
            if (this.silentSwapOverrideDelay.get().booleanValue()) {
                delayCheckSlot2 = this.mc.field_1724.method_31548().field_7545;
            }
            if (delayReady(delayCheckSlot2)) {
                boolean isHolding2 = breachMace2.isMainHand();
                if (MeteorClient.SWAP.beginSwap(breachMace2, true)) {
                    attack(target, !isHolding2);
                    MeteorClient.SWAP.endSwap(true);
                    this.swapState = SwapState.NONE;
                }
            }
        }
    }

    private boolean swordDelayReady(int slot) {
        class_1799 itemStack = this.mc.field_1724.method_31548().method_5438(slot);
        MutableDouble attackSpeed = new MutableDouble(this.mc.field_1724.method_45326(class_5134.field_23723));
        class_9285 attributeModifiers = (class_9285) itemStack.method_58694(class_9334.field_49636);
        if (attributeModifiers != null) {
            attributeModifiers.method_57482(class_1304.field_6173, (entry, modifier) -> {
                if (entry == class_5134.field_23723) {
                    attackSpeed.add(modifier.comp_2449());
                }
            });
        }
        double attackCooldownTicks = (1.0d / attackSpeed.getValue().doubleValue()) * 20.0d;
        long currentTime = System.currentTimeMillis();
        return ((double) (currentTime - this.lastSwordAttackTime)) / 50.0d > attackCooldownTicks;
    }

    private FindItemResult findWeaponWithEnchant(class_5321<class_1887> enchantKey) {
        for (int slot = 0; slot < 36; slot++) {
            class_1799 stack = this.mc.field_1724.method_31548().method_5438(slot);
            if (isMace(stack) && Utils.getEnchantmentLevel(stack, enchantKey) > 0) {
                return new FindItemResult(slot, stack.method_7947());
            }
        }
        class_1799 off = this.mc.field_1724.method_6079();
        if (isMace(off) && Utils.getEnchantmentLevel(off, enchantKey) > 0) {
            return new FindItemResult(45, off.method_7947());
        }
        return new FindItemResult(-1, 0);
    }

    private FindItemResult findWeapon() {
        for (int slot = 0; slot < 36; slot++) {
            class_1799 stack = this.mc.field_1724.method_31548().method_5438(slot);
            if (isMace(stack)) {
                return new FindItemResult(slot, stack.method_7947());
            }
        }
        class_1799 off = this.mc.field_1724.method_6079();
        if (isMace(off)) {
            return new FindItemResult(45, off.method_7947());
        }
        return new FindItemResult(-1, 0);
    }

    private FindItemResult findSword() {
        FindItemResult res = MeteorClient.SWAP.getSlot(class_1802.field_22022);
        if (!res.found()) {
            res = MeteorClient.SWAP.getSlot(class_1802.field_8802);
        }
        return res;
    }

    private static class_243 closestPointOnBox(class_238 box, class_243 point) {
        double x = Math.max(box.field_1323, Math.min(point.field_1352, box.field_1320));
        double y = Math.max(box.field_1322, Math.min(point.field_1351, box.field_1325));
        double z = Math.max(box.field_1321, Math.min(point.field_1350, box.field_1324));
        return new class_243(x, y, z);
    }

    private boolean isEntityFlying(class_1297 e) {
        return e.method_18798().field_1351 < -0.1d || e.method_18798().field_1351 > 0.1d || e.field_6017 > 1.5d;
    }

    private class_243 predictedAimPoint(class_1297 target, class_243 eyes) {
        class_243 base = closestPointOnBox(target.method_5829(), eyes);
        double ms = this.predictionMs.get().doubleValue();
        class_243 vel = target.method_18798();
        if (this.adjustForFlying.get().booleanValue() && isEntityFlying(target)) {
            vel = vel.method_1021(this.elytraPredictionScale.get().doubleValue());
        }
        class_243 lead = vel.method_1021(ms / 1000.0d);
        return base.method_1031(lead.field_1352, lead.field_1351 + this.aimYOffset.get().doubleValue(), lead.field_1350);
    }

    private float clampAngle(float cur, float target, float maxStep) {
        float diff = wrapDegrees(target - cur);
        return Math.abs(diff) <= maxStep ? target : cur + Math.copySign(maxStep, diff);
    }

    private float wrapDegrees(float f) {
        float f2 = f % 360.0f;
        if (f2 >= 180.0f) {
            f2 -= 360.0f;
        }
        if (f2 < -180.0f) {
            f2 += 360.0f;
        }
        return f2;
    }
}
