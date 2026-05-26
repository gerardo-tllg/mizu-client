package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.MovementFix;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.utils.entity.ProjectileEntitySimulator;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1533;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2824;
import net.minecraft.class_2846;
import net.minecraft.class_2886;
import net.minecraft.class_3532;
import net.minecraft.class_3966;
import net.minecraft.class_408;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/PearlPhase.class */
public class PearlPhase extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Keybind> phaseBind;
    private final Setting<RotateMode> rotateMode;
    private final Setting<Boolean> burrow;
    private final Setting<Integer> burrowDelay;
    private final Setting<Boolean> antiPearlFail;
    private final Setting<Boolean> antiPearlFailStrict;
    private final Setting<Boolean> rephase;
    private final Setting<Integer> scaffoldBreakDelay;
    private boolean active;
    private boolean keyUnpressed;
    private boolean blockPlaced;
    private int burrowTimer;
    private final ProjectileEntitySimulator simulator;
    private int scaffoldBreakTimer;
    private boolean scaffoldCleared;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/PearlPhase$RotateMode.class */
    public enum RotateMode {
        Movement,
        Instant,
        DelayedInstant,
        DelayedInstantWebOnly
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/PearlPhase$SwitchMode.class */
    public enum SwitchMode {
        SilentHotbar,
        SilentSwap
    }

    public PearlPhase() {
        super(Categories.Combat, "pearl-phase", "Phases into walls using pearls");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.phaseBind = this.sgGeneral.add(new KeybindSetting.Builder().name("key-bind").description("Phase on keybind press").build());
        this.rotateMode = this.sgGeneral.add(new EnumSetting.Builder().name("rotate-mode").description("Which method of rotating should be used.").defaultValue(RotateMode.DelayedInstantWebOnly).build());
        this.burrow = this.sgGeneral.add(new BoolSetting.Builder().name("burrow").description("Places a block where you phase.").defaultValue(true).build());
        this.burrowDelay = this.sgGeneral.add(new IntSetting.Builder().name("burrow-delay").description("Delay in ticks after placing the block before phasing.").defaultValue(0).min(0).max(10).sliderMin(0).sliderMax(10).build());
        this.antiPearlFail = this.sgGeneral.add(new BoolSetting.Builder().name("anti-pearl-fail").description("Hits entities below you when you phase.").defaultValue(true).build());
        this.antiPearlFailStrict = this.sgGeneral.add(new BoolSetting.Builder().name("anti-pearl-fail-strict").description("Waits for the entity to disappear before phasing.").defaultValue(false).build());
        this.rephase = this.sgGeneral.add(new BoolSetting.Builder().name("rephase").description("Automatically rephase when the keybind is held down.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgGeneral;
        IntSetting.Builder builderSliderMax = new IntSetting.Builder().name("scaffold-break-delay").description("Delay in ticks before throwing the pearl after breaking scaffolds.").defaultValue(1).min(0).max(20).sliderMin(0).sliderMax(10);
        Setting<Boolean> setting = this.antiPearlFail;
        Objects.requireNonNull(setting);
        this.scaffoldBreakDelay = settingGroup.add(builderSliderMax.visible(setting::get).build());
        this.active = false;
        this.keyUnpressed = false;
        this.blockPlaced = false;
        this.burrowTimer = 0;
        this.simulator = new ProjectileEntitySimulator();
        this.scaffoldBreakTimer = 0;
        this.scaffoldCleared = false;
    }

    private void activate() {
        this.active = true;
        this.blockPlaced = false;
        this.burrowTimer = 0;
        this.scaffoldBreakTimer = 0;
        this.scaffoldCleared = false;
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            deactivate(false);
        } else {
            update();
        }
    }

    private void deactivate(boolean phased) {
        this.active = false;
        this.blockPlaced = false;
        this.burrowTimer = 0;
        this.scaffoldBreakTimer = 0;
        this.scaffoldCleared = false;
        if (phased) {
            info("Phased", new Object[0]);
        }
    }

    private void update() {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            deactivate(false);
            return;
        }
        if (!this.active) {
            return;
        }
        class_238 boundingBox = this.mc.field_1724.method_5829().method_1002(0.05d, 0.1d, 0.05d);
        double feetY = this.mc.field_1724.method_23318();
        class_238 feetBox = new class_238(boundingBox.field_1323, feetY, boundingBox.field_1321, boundingBox.field_1320, feetY + 0.1d, boundingBox.field_1324);
        if (class_2338.method_29715(feetBox).anyMatch(blockPos -> {
            return this.mc.field_1687.method_8320(blockPos).method_26212(this.mc.field_1687, blockPos);
        })) {
            deactivate(false);
        }
        if (!MeteorClient.SWAP.canSwap(class_1802.field_8634)) {
            deactivate(false);
        } else if (this.mc.field_1724.method_7357().method_7904(class_1802.field_8634.method_7854())) {
            deactivate(false);
        } else if (this.mc.field_1690.field_1832.method_1434()) {
            deactivate(false);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.active) {
        }
        if (this.burrow.get().booleanValue() && !this.blockPlaced) {
            placeBlock();
            this.blockPlaced = true;
            this.burrowTimer = this.burrowDelay.get().intValue();
            return;
        }
        if (this.burrowTimer > 0) {
            this.burrowTimer--;
            return;
        }
        if (this.scaffoldBreakTimer > 0) {
            this.scaffoldBreakTimer--;
            if (this.scaffoldBreakTimer == 0) {
                this.scaffoldCleared = true;
                return;
            }
            return;
        }
        if (this.antiPearlFailStrict.get().booleanValue() && this.mc.field_1687.method_8320(this.mc.field_1724.method_24515()).method_27852(class_2246.field_16492)) {
            return;
        }
        class_243 targetPos = calculateTargetPos();
        float[] angle = MeteorClient.ROTATION.getRotation(targetPos);
        switch (this.rotateMode.get()) {
            case Movement:
                MeteorClient.ROTATION.requestRotation(targetPos, 1000.0d);
                if (MeteorClient.ROTATION.lookingAt(class_238.method_30048(targetPos, 0.05d, 0.05d, 0.05d))) {
                    throwPearl(angle[0], angle[1]);
                }
                break;
            case Instant:
                if (this.mc.field_1724.method_24828()) {
                    MeteorClient.ROTATION.snapAt(targetPos);
                    throwPearl(angle[0], angle[1]);
                }
                break;
            case DelayedInstant:
                MeteorClient.ROTATION.requestRotation(targetPos, 1000.0d);
                if (MeteorClient.ROTATION.lookingAt(class_238.method_30048(targetPos, 0.05d, 0.05d, 0.05d))) {
                    MeteorClient.ROTATION.snapAt(targetPos);
                    throwPearl(angle[0], angle[1]);
                }
                break;
            case DelayedInstantWebOnly:
                MeteorClient.ROTATION.requestRotation(targetPos, 1000.0d);
                if (MeteorClient.ROTATION.lookingAt(class_238.method_30048(targetPos, 0.05d, 0.05d, 0.05d))) {
                    if (MovementFix.inWebs) {
                        MeteorClient.ROTATION.snapAt(targetPos);
                    }
                    throwPearl(angle[0], angle[1]);
                }
                break;
        }
    }

    private void placeBlock() {
        class_243 targetPos = calculateTargetPos();
        class_238 newHitbox = this.mc.field_1724.method_5829().method_989(targetPos.field_1352 - this.mc.field_1724.method_23317(), 0.0d, targetPos.field_1350 - this.mc.field_1724.method_23321()).method_1014(0.05d);
        List<class_2338> placePoses = new ArrayList<>();
        int minX = (int) Math.floor(newHitbox.field_1323);
        int maxX = (int) Math.floor(newHitbox.field_1320);
        int minZ = (int) Math.floor(newHitbox.field_1321);
        int maxZ = (int) Math.floor(newHitbox.field_1324);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                class_2338 feetPos = new class_2338(x, this.mc.field_1724.method_24515().method_10264(), z);
                placePoses.add(feetPos);
            }
        }
        if (MeteorClient.BLOCK.beginPlacement(placePoses, class_1802.field_8281)) {
            placePoses.forEach(blockPos -> {
                MeteorClient.BLOCK.placeBlock(class_1802.field_8281, blockPos);
            });
            MeteorClient.BLOCK.endPlacement();
        }
    }

    private void throwPearl(float yaw, float pitch) {
        if (this.antiPearlFail.get().booleanValue()) {
            class_3966 enderPearlHitResult = getEnderPearlHitResult();
            if (enderPearlHitResult != null && enderPearlHitResult.method_17783() == class_239.class_240.field_1331) {
                class_1297 hitEntity = enderPearlHitResult.method_17782();
                if ((hitEntity instanceof class_1511) || (hitEntity instanceof class_1533)) {
                    MeteorClient.ROTATION.requestRotation(hitEntity.method_19538(), 11.0d);
                    if (!MeteorClient.ROTATION.lookingAt(hitEntity.method_5829()) && RotationManager.lastGround) {
                        MeteorClient.ROTATION.snapAt(hitEntity.method_19538());
                    }
                    if (MeteorClient.ROTATION.lookingAt(hitEntity.method_5829())) {
                        this.mc.method_1562().method_52787(class_2824.method_34206(hitEntity, this.mc.field_1724.method_5715()));
                    }
                }
                if (this.antiPearlFailStrict.get().booleanValue() && hitEntity != null) {
                    return;
                }
            }
            if (this.mc.field_1687.method_8320(this.mc.field_1724.method_24515()).method_27852(class_2246.field_16492) && !this.scaffoldCleared) {
                SilentMine silentMine = (SilentMine) Modules.get().get(SilentMine.class);
                if (silentMine.isActive()) {
                    silentMine.silentBreakBlock(this.mc.field_1724.method_24515(), class_2350.field_11036, 100.0d);
                } else {
                    int sequence = this.mc.field_1687.meteor$getAndIncrementSequence();
                    this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12968, this.mc.field_1724.method_24515(), class_2350.field_11036, sequence));
                    this.mc.method_1562().method_52787(new class_2846(class_2846.class_2847.field_12973, this.mc.field_1724.method_24515(), class_2350.field_11036, sequence + 1));
                }
                this.scaffoldBreakTimer = this.scaffoldBreakDelay.get().intValue();
                return;
            }
        }
        if (MeteorClient.SWAP.beginSwap(class_1802.field_8634, true)) {
            this.mc.method_1562().method_52787(new class_2886(class_1268.field_5808, this.mc.field_1687.meteor$getAndIncrementSequence(), yaw, pitch));
            deactivate(true);
            MeteorClient.SWAP.endSwap(true);
        }
    }

    @EventHandler(priority = 220)
    private void onRender(Render3DEvent event) {
        if (!this.phaseBind.get().isPressed()) {
            this.keyUnpressed = true;
        }
        if (this.phaseBind.get().isPressed() && this.keyUnpressed && !(this.mc.field_1755 instanceof class_408)) {
            activate();
            this.keyUnpressed = false;
        }
        if (this.rephase.get().booleanValue() && this.phaseBind.get().isPressed() && !this.active) {
            activate();
        }
        update();
    }

    private class_239 getEnderPearlHitResult() {
        if (!this.simulator.set(this.mc.field_1724, class_1802.field_8634.method_7854(), 0.0d, false, 1.0f)) {
            return null;
        }
        for (int i = 0; i < 256; i++) {
            class_239 result = this.simulator.tick();
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private class_243 calculateTargetPos() {
        double playerX = this.mc.field_1724.method_23317();
        double playerZ = this.mc.field_1724.method_23321();
        double x = playerX + class_3532.method_15350(toClosest(playerX, Math.floor(playerX) + 0.241660973353061d, Math.floor(playerX) + 0.7853981633974483d) - playerX, -0.2d, 0.2d);
        double z = playerZ + class_3532.method_15350(toClosest(playerZ, Math.floor(playerZ) + 0.241660973353061d, Math.floor(playerZ) + 0.7853981633974483d) - playerZ, -0.2d, 0.2d);
        double yOffset = this.mc.field_1724.method_20448() ? -0.1d : -0.5d;
        return new class_243(x, this.mc.field_1724.method_23318() + yOffset, z);
    }

    private double toClosest(double num, double min, double max) {
        double dmin = num - min;
        double dmax = max - num;
        if (dmax > dmin) {
            return min;
        }
        return max;
    }
}
