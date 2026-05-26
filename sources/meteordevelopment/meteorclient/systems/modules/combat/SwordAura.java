package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.List;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.managers.SwapManager;
import meteordevelopment.meteorclient.systems.managers.TargetManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2824;
import net.minecraft.class_2828;
import net.minecraft.class_3532;
import net.minecraft.class_465;
import net.minecraft.class_5134;
import net.minecraft.class_634;
import net.minecraft.class_9285;
import net.minecraft.class_9334;
import org.apache.commons.lang3.mutable.MutableDouble;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/SwordAura.class */
public class SwordAura extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Boolean> silentSwapOverrideDelay;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> snapRotation;
    private final Setting<Boolean> swordPull;
    private final Setting<Boolean> tpsSync;
    private final Setting<Boolean> forcePauseEat;
    private final Setting<Boolean> pauseInAir;
    private final Setting<Boolean> pauseInventoryOepn;
    private final Setting<CritMode> critMode;
    private final Setting<Boolean> awaitJumpCrit;
    private final Setting<Boolean> wallCritsPauseOnMove;
    private final Setting<Boolean> wallCritsOnlyOnSword;
    private final Setting<Boolean> onlyCritWhenFullyPhased;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Double> fadeTime;
    private final TargetManager targetManager;
    private long lastAttackTime;
    private List<class_1297> targets;
    private class_1297 lastAttackedEntity;
    private int targetIndex;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/SwordAura$CritMode.class */
    public enum CritMode {
        None,
        OldWall,
        OldAlways
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/SwordAura$SwitchMode.class */
    public enum SwitchMode {
        None,
        SilentHotbar,
        SilentSwap,
        Auto
    }

    public SwordAura() {
        super(Categories.Combat, "sword-aura", "Automatically attacks entities with your sword");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.silentSwapOverrideDelay = this.sgGeneral.add(new BoolSetting.Builder().name("silent-swap-override-delay").description("Whether or not to use the held items delay when attacking with silent swap").defaultValue(true).visible(() -> {
            return MeteorClient.SWAP.getItemSwapMode() != SwapManager.SwapMode.None;
        }).build());
        this.rotate = this.sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Whether or not to rotate to the entity to attack it.").defaultValue(true).build());
        this.snapRotation = this.sgGeneral.add(new BoolSetting.Builder().name("snap-rotate").description("Instantly rotates to the targeted entity.").defaultValue(true).visible(() -> {
            return this.rotate.get().booleanValue();
        }).build());
        this.swordPull = this.sgGeneral.add(new BoolSetting.Builder().name("sword-pull").description("Pulls the target towards you").defaultValue(true).build());
        this.tpsSync = this.sgGeneral.add(new BoolSetting.Builder().name("tps-sync").description("Adjusts attack speed to match the server's TPS.").defaultValue(true).build());
        this.forcePauseEat = this.sgGeneral.add(new BoolSetting.Builder().name("force-pause-on-eat").description("Does not attack while using an item.").defaultValue(false).build());
        this.pauseInAir = this.sgGeneral.add(new BoolSetting.Builder().name("pause-in-air").description("Does not attack while rising during a jump.").defaultValue(false).build());
        this.pauseInventoryOepn = this.sgGeneral.add(new BoolSetting.Builder().name("pause-on-inventory").description("Does not attack when the inventory is open. Disabling this may cause unhappiness.").defaultValue(true).build());
        this.critMode = this.sgGeneral.add(new EnumSetting.Builder().name("crit-mode").description("The mode to use for critical hits.").defaultValue(CritMode.None).build());
        this.awaitJumpCrit = this.sgGeneral.add(new BoolSetting.Builder().name("await-jump-crit").description("Waits until you are falling after a jump to perform a vanilla critical hit.").defaultValue(true).build());
        this.wallCritsPauseOnMove = this.sgGeneral.add(new BoolSetting.Builder().name("wall-crits-pause-on-move").description("Only for Old modes. Pauses crits when moving. (Redundant, packet crits only work when still).").defaultValue(true).visible(() -> {
            return this.critMode.get() == CritMode.OldWall || this.critMode.get() == CritMode.OldAlways;
        }).build());
        this.wallCritsOnlyOnSword = this.sgGeneral.add(new BoolSetting.Builder().name("wall-crits-only-on-sword").description("Only for Old modes. Only crits when you swapped to the sword.").defaultValue(true).visible(() -> {
            return this.critMode.get() == CritMode.OldWall || this.critMode.get() == CritMode.OldAlways;
        }).build());
        this.onlyCritWhenFullyPhased = this.sgGeneral.add(new BoolSetting.Builder().name("only-crit-when-fully-phased").description("Only perform OldWall crits if your head is also phased (fully phased).").defaultValue(true).visible(() -> {
            return this.critMode.get() == CritMode.OldWall;
        }).build());
        this.render = this.sgRender.add(new BoolSetting.Builder().name("render").description("Whether or not to render attacks").defaultValue(false).build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).visible(() -> {
            return this.render.get().booleanValue();
        }).build());
        this.sideColor = this.sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color of the rendering.").defaultValue(new SettingColor(Opcode.IF_ICMPNE, 0, 225, 35)).visible(() -> {
            return this.shapeMode.get().sides();
        }).build());
        this.lineColor = this.sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color of the rendering.").defaultValue(new SettingColor(255, 255, 255, 50)).visible(() -> {
            return this.render.get().booleanValue() && this.shapeMode.get().lines();
        }).build());
        this.fadeTime = this.sgRender.add(new DoubleSetting.Builder().name("fade-time").description("How long to fade the bounding box render.").min(0.0d).sliderMax(2.0d).defaultValue(0.8d).build());
        this.targetManager = new TargetManager(this, true);
        this.lastAttackTime = 0L;
        this.targets = null;
        this.lastAttackedEntity = null;
        this.targetIndex = 0;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.mc.field_1724.method_29504() || this.mc.field_1724.method_7325()) {
            return;
        }
        if (this.forcePauseEat.get().booleanValue() && this.mc.field_1724.method_6115() && this.mc.field_1724.method_6058() == class_1268.field_5808) {
            return;
        }
        if (this.pauseInventoryOepn.get().booleanValue() && (this.mc.field_1755 instanceof class_465)) {
            return;
        }
        FindItemResult result = MeteorClient.SWAP.getSlot(class_1802.field_22022);
        if (!result.found()) {
            result = MeteorClient.SWAP.getSlot(class_1802.field_8802);
        }
        if (result.found()) {
            this.targets = this.targetManager.getEntityTargets();
            if (this.targets.isEmpty()) {
                return;
            }
            class_1297 target = this.targets.get(this.targetIndex % this.targets.size());
            int delayCheckSlot = result.slot();
            if (this.silentSwapOverrideDelay.get().booleanValue()) {
                delayCheckSlot = this.mc.field_1724.method_31548().field_7545;
            }
            if (delayCheck(delayCheckSlot)) {
                boolean isFalling = (this.mc.field_1724.method_24828() || this.mc.field_1724.method_18798().field_1351 >= -0.1d || this.mc.field_1724.method_5799() || this.mc.field_1724.method_5771() || this.mc.field_1724.method_5715() || this.mc.field_1724.method_6101()) ? false : true;
                boolean isMoving = this.mc.field_1724.field_3913.field_54155.comp_3159() || this.mc.field_1724.field_3913.field_54155.comp_3160() || this.mc.field_1724.field_3913.field_54155.comp_3161() || this.mc.field_1724.field_3913.field_54155.comp_3162();
                boolean isStandingOnGround = (!this.mc.field_1724.method_24828() || isMoving || this.mc.field_1724.method_5799() || this.mc.field_1724.method_5771()) ? false : true;
                CritMode currentCritMode = this.critMode.get();
                boolean awaitingCrit = this.awaitJumpCrit.get().booleanValue();
                if (!this.pauseInAir.get().booleanValue() || this.mc.field_1724.method_24828()) {
                    if (!awaitingCrit || this.mc.field_1724.method_24828() || isFalling || this.mc.field_1724.method_5715()) {
                        boolean sendPackets = isStandingOnGround && currentCritMode != CritMode.None;
                        if (this.rotate.get().booleanValue() || !this.mc.field_1724.method_5715()) {
                            class_243 point = getClosestPointOnBox(target.method_5829(), this.mc.field_1724.method_33571());
                            if (this.snapRotation.get().booleanValue() || !this.mc.field_1724.method_5715()) {
                                MeteorClient.ROTATION.snapAt(point);
                            }
                            MeteorClient.ROTATION.requestRotation(point, 9.0d);
                            if (!MeteorClient.ROTATION.lookingAt(target.method_5829())) {
                                return;
                            }
                        }
                        boolean isHolding = result.isMainHand();
                        if (MeteorClient.SWAP.beginSwap(result, true)) {
                            attack(target, !isHolding, sendPackets);
                            MeteorClient.SWAP.endSwap(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!this.render.get().booleanValue() || this.lastAttackedEntity == null || this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        double secondsSinceAttack = (System.currentTimeMillis() - this.lastAttackTime) / 1000.0d;
        if (secondsSinceAttack > this.fadeTime.get().doubleValue()) {
            return;
        }
        double alpha = 1.0d - (secondsSinceAttack / this.fadeTime.get().doubleValue());
        double x = class_3532.method_16436(event.tickDelta, this.lastAttackedEntity.field_6038, this.lastAttackedEntity.method_23317()) - this.lastAttackedEntity.method_23317();
        double y = class_3532.method_16436(event.tickDelta, this.lastAttackedEntity.field_5971, this.lastAttackedEntity.method_23318()) - this.lastAttackedEntity.method_23318();
        double z = class_3532.method_16436(event.tickDelta, this.lastAttackedEntity.field_5989, this.lastAttackedEntity.method_23321()) - this.lastAttackedEntity.method_23321();
        class_238 box = this.lastAttackedEntity.method_5829();
        event.renderer.box(x + box.field_1323, y + box.field_1322, z + box.field_1321, x + box.field_1320, y + box.field_1325, z + box.field_1324, this.sideColor.get().copy().a((int) (((double) this.sideColor.get().a) * alpha)), this.lineColor.get().copy().a((int) (((double) this.lineColor.get().a) * alpha)), this.shapeMode.get(), 0);
    }

    public void attack(class_1297 target, boolean didSwap, boolean sendPackets) {
        class_243 pos;
        float legitYaw;
        float legitPitch;
        if (this.mc.field_1724 == null || this.mc.method_1562() == null) {
            return;
        }
        class_634 net2 = this.mc.method_1562();
        if (RotationManager.lastGround) {
            pos = new class_243(MeteorClient.ROTATION.lastX, MeteorClient.ROTATION.lastY, MeteorClient.ROTATION.lastZ);
            legitYaw = MeteorClient.ROTATION.lastYaw;
            legitPitch = MeteorClient.ROTATION.lastPitch;
        } else {
            pos = this.mc.field_1724.method_19538();
            legitYaw = MeteorClient.ROTATION.rotationYaw;
            legitPitch = MeteorClient.ROTATION.rotationPitch;
        }
        boolean willSendCrits = false;
        if (sendPackets) {
            boolean pulling = this.mc.field_1724.field_3913.field_54155.comp_3159() || this.mc.field_1724.field_3913.field_54155.comp_3160() || this.mc.field_1724.field_3913.field_54155.comp_3161() || this.mc.field_1724.field_3913.field_54155.comp_3162();
            if ((!this.wallCritsPauseOnMove.get().booleanValue() || !pulling) && (!this.wallCritsOnlyOnSword.get().booleanValue() || !didSwap)) {
                switch (this.critMode.get().ordinal()) {
                    case 1:
                        if (PlayerUtils.isPlayerPhased() && (!this.onlyCritWhenFullyPhased.get().booleanValue() || isHeadPhased())) {
                            willSendCrits = true;
                        }
                        break;
                    case 2:
                        willSendCrits = true;
                        break;
                }
            }
        }
        boolean pulling2 = this.swordPull.get().booleanValue();
        if (willSendCrits) {
            pulling2 = false;
        }
        if (pulling2) {
            float spoofYaw = (legitYaw + 180.0f) % 360.0f;
            net2.method_52787(new class_2828.class_2830(pos.field_1352, pos.field_1351, pos.field_1350, legitYaw, legitPitch, this.mc.field_1724.method_24828(), this.mc.field_1724.field_5976));
            net2.method_52787(new class_2828.class_2830(pos.field_1352, pos.field_1351, pos.field_1350, spoofYaw, legitPitch, this.mc.field_1724.method_24828(), this.mc.field_1724.field_5976));
        } else if (willSendCrits) {
            sendCrits(didSwap);
        }
        net2.method_52787(class_2824.method_34206(target, this.mc.field_1724.method_5715()));
        this.mc.field_1724.method_6104(class_1268.field_5808);
        this.lastAttackedEntity = target;
        this.lastAttackTime = System.currentTimeMillis();
        this.targetIndex++;
    }

    private boolean delayCheck(int slot) {
        if (this.mc.field_1724 == null) {
            return false;
        }
        class_1661 inventory = this.mc.field_1724.method_31548();
        class_1799 itemStack = inventory.method_5438(slot);
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
        if (this.tpsSync.get().booleanValue()) {
            double tps = ((double) TickRate.INSTANCE.getTickRate()) * 0.95d;
            if (tps < 19.5d) {
                double tpsFactor = tps / 20.0d;
                attackCooldownTicks /= tpsFactor;
            }
        }
        long currentTime = System.currentTimeMillis();
        return ((double) (currentTime - this.lastAttackTime)) / 50.0d > attackCooldownTicks;
    }

    private void sendCrits(boolean didSwap) {
        class_243 pos;
        float packetYaw;
        float packetPitch;
        if (this.mc.field_1724 == null || this.mc.method_1562() == null) {
            return;
        }
        class_634 net2 = this.mc.method_1562();
        if (RotationManager.lastGround) {
            pos = new class_243(MeteorClient.ROTATION.lastX, MeteorClient.ROTATION.lastY, MeteorClient.ROTATION.lastZ);
            packetYaw = MeteorClient.ROTATION.lastYaw;
            packetPitch = MeteorClient.ROTATION.lastPitch;
        } else {
            pos = this.mc.field_1724.method_19538();
            packetYaw = this.mc.field_1724.method_36454();
            packetPitch = this.mc.field_1724.method_36455();
        }
        boolean isMoving = this.mc.field_1724.field_3913.field_54155.comp_3159() || this.mc.field_1724.field_3913.field_54155.comp_3160() || this.mc.field_1724.field_3913.field_54155.comp_3161() || this.mc.field_1724.field_3913.field_54155.comp_3162();
        if (this.wallCritsPauseOnMove.get().booleanValue() && isMoving) {
        }
        if (!this.wallCritsOnlyOnSword.get().booleanValue() || !didSwap) {
            switch (this.critMode.get().ordinal()) {
                case 1:
                    if (PlayerUtils.isPlayerPhased()) {
                        if (!this.onlyCritWhenFullyPhased.get().booleanValue() || isHeadPhased()) {
                            net2.method_52787(new class_2828.class_2830(pos.field_1352, pos.field_1351, pos.field_1350, packetYaw, packetPitch, true, this.mc.field_1724.field_5976));
                            net2.method_52787(new class_2828.class_2830(pos.field_1352, pos.field_1351 + 0.0625d, pos.field_1350, packetYaw, packetPitch, false, this.mc.field_1724.field_5976));
                            net2.method_52787(new class_2828.class_2830(pos.field_1352, pos.field_1351 + 0.045d, pos.field_1350, packetYaw, packetPitch, false, this.mc.field_1724.field_5976));
                        }
                    }
                    break;
                case 2:
                    net2.method_52787(new class_2828.class_2830(pos.field_1352, pos.field_1351, pos.field_1350, packetYaw, packetPitch, true, this.mc.field_1724.field_5976));
                    net2.method_52787(new class_2828.class_2830(pos.field_1352, pos.field_1351 + 0.0625d, pos.field_1350, packetYaw, packetPitch, false, this.mc.field_1724.field_5976));
                    net2.method_52787(new class_2828.class_2830(pos.field_1352, pos.field_1351 + 0.045d, pos.field_1350, packetYaw, packetPitch, false, this.mc.field_1724.field_5976));
                    break;
            }
        }
    }

    private boolean isHeadPhased() {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return false;
        }
        class_243 eyePos = this.mc.field_1724.method_33571();
        class_2338 pos = new class_2338(class_3532.method_15357(eyePos.field_1352), class_3532.method_15357(eyePos.field_1351), class_3532.method_15357(eyePos.field_1350));
        return !this.mc.field_1687.method_8320(pos).method_26215();
    }

    public class_243 getClosestPointOnBox(class_238 box, class_243 point) {
        if (this.mc.field_1724 == null) {
            return class_243.field_1353;
        }
        double x = Math.max(box.field_1323, Math.min(point.field_1352, box.field_1320));
        double y = Math.max(box.field_1322, Math.min(point.field_1351, box.field_1325));
        double z = Math.max(box.field_1321, Math.min(point.field_1350, box.field_1324));
        return new class_243(x, y, z);
    }
}
