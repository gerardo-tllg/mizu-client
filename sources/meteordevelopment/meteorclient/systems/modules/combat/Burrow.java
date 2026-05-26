package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2199;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2828;
import net.minecraft.class_2879;
import net.minecraft.class_3965;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/Burrow.class */
public class Burrow extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Block> block;
    private final Setting<Boolean> instant;
    private final Setting<Boolean> automatic;
    private final Setting<Double> triggerHeight;
    private final Setting<Double> rubberbandHeight;
    private final Setting<Double> timer;
    private final Setting<Boolean> onlyInHole;
    private final Setting<Boolean> center;
    private final Setting<Boolean> rotate;
    private final class_2338.class_2339 blockPos;
    private boolean shouldBurrow;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/Burrow$Block.class */
    public enum Block {
        EChest,
        Obsidian,
        Anvil,
        Held
    }

    public Burrow() {
        super(Categories.Combat, "burrow", "Attempts to clip you into a block.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.block = this.sgGeneral.add(new EnumSetting.Builder().name("block-to-use").description("The block to use for Burrow.").defaultValue(Block.EChest).build());
        this.instant = this.sgGeneral.add(new BoolSetting.Builder().name("instant").description("Jumps with packets rather than vanilla jump.").defaultValue(true).build());
        this.automatic = this.sgGeneral.add(new BoolSetting.Builder().name("automatic").description("Automatically burrows on activate rather than waiting for jump.").defaultValue(true).build());
        this.triggerHeight = this.sgGeneral.add(new DoubleSetting.Builder().name("trigger-height").description("How high you have to jump before a rubberband is triggered.").defaultValue(1.12d).range(0.01d, 1.4d).sliderRange(0.01d, 1.4d).build());
        this.rubberbandHeight = this.sgGeneral.add(new DoubleSetting.Builder().name("rubberband-height").description("How far to attempt to cause rubberband.").defaultValue(12.0d).sliderMin(-30.0d).sliderMax(30.0d).build());
        this.timer = this.sgGeneral.add(new DoubleSetting.Builder().name("timer").description("Timer override.").defaultValue(1.0d).min(0.01d).sliderRange(0.01d, 10.0d).build());
        this.onlyInHole = this.sgGeneral.add(new BoolSetting.Builder().name("only-in-holes").description("Stops you from burrowing when not in a hole.").defaultValue(false).build());
        this.center = this.sgGeneral.add(new BoolSetting.Builder().name("center").description("Centers you to the middle of the block before burrowing.").defaultValue(true).build());
        this.rotate = this.sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Faces the block you place server-side.").defaultValue(true).build());
        this.blockPos = new class_2338.class_2339();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        if (!this.mc.field_1687.method_8320(this.mc.field_1724.method_24515()).method_45474()) {
            error("Already burrowed, disabling.", new Object[0]);
            toggle();
            return;
        }
        if (!PlayerUtils.isInHole(false) && this.onlyInHole.get().booleanValue()) {
            error("Not in a hole, disabling.", new Object[0]);
            toggle();
            return;
        }
        if (!checkHead()) {
            error("Not enough headroom to burrow, disabling.", new Object[0]);
            toggle();
            return;
        }
        FindItemResult result = getItem();
        if (!result.isHotbar() && !result.isOffhand()) {
            error("No burrow block found, disabling.", new Object[0]);
            toggle();
            return;
        }
        this.blockPos.method_10101(this.mc.field_1724.method_24515());
        ((Timer) Modules.get().get(Timer.class)).setOverride(this.timer.get().doubleValue());
        this.shouldBurrow = false;
        if (this.automatic.get().booleanValue()) {
            if (!this.instant.get().booleanValue()) {
                this.mc.field_1724.method_6043();
                return;
            } else {
                this.shouldBurrow = true;
                return;
            }
        }
        info("Waiting for manual jump.", new Object[0]);
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        ((Timer) Modules.get().get(Timer.class)).setOverride(1.0d);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.instant.get().booleanValue()) {
            this.shouldBurrow = this.mc.field_1724.method_23318() > ((double) this.blockPos.method_10264()) + this.triggerHeight.get().doubleValue();
        }
        if (!this.shouldBurrow && this.instant.get().booleanValue()) {
            this.blockPos.method_10101(this.mc.field_1724.method_24515());
        }
        if (this.shouldBurrow) {
            if (this.rotate.get().booleanValue()) {
                Rotations.rotate(Rotations.getYaw(this.mc.field_1724.method_24515()), Rotations.getPitch(this.mc.field_1724.method_24515()), 50, this::burrow);
            } else {
                burrow();
            }
            toggle();
        }
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (this.instant.get().booleanValue() && !this.shouldBurrow) {
            if (event.action == KeyAction.Press && this.mc.field_1690.field_1903.method_1417(event.key, 0)) {
                this.shouldBurrow = true;
            }
            this.blockPos.method_10101(this.mc.field_1724.method_24515());
        }
    }

    private void burrow() {
        if (this.center.get().booleanValue()) {
            PlayerUtils.centerPlayer();
        }
        if (this.instant.get().booleanValue()) {
            this.mc.field_1724.field_3944.method_52787(new class_2828.class_2829(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318() + 0.4d, this.mc.field_1724.method_23321(), false, this.mc.field_1724.field_5976));
            this.mc.field_1724.field_3944.method_52787(new class_2828.class_2829(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318() + 0.75d, this.mc.field_1724.method_23321(), false, this.mc.field_1724.field_5976));
            this.mc.field_1724.field_3944.method_52787(new class_2828.class_2829(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318() + 1.01d, this.mc.field_1724.method_23321(), false, this.mc.field_1724.field_5976));
            this.mc.field_1724.field_3944.method_52787(new class_2828.class_2829(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318() + 1.15d, this.mc.field_1724.method_23321(), false, this.mc.field_1724.field_5976));
        }
        FindItemResult block = getItem();
        if (this.mc.field_1724.method_31548().method_5438(block.slot()).method_7909() instanceof class_1747) {
            InvUtils.swap(block.slot(), true);
            this.mc.field_1761.method_2896(this.mc.field_1724, class_1268.field_5808, new class_3965(Utils.vec3d(this.blockPos), class_2350.field_11036, this.blockPos, false));
            this.mc.field_1724.field_3944.method_52787(new class_2879(class_1268.field_5808));
            InvUtils.swapBack();
            if (this.instant.get().booleanValue()) {
                this.mc.field_1724.field_3944.method_52787(new class_2828.class_2829(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318() + this.rubberbandHeight.get().doubleValue(), this.mc.field_1724.method_23321(), false, this.mc.field_1724.field_5976));
            } else {
                this.mc.field_1724.method_30634(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318() + this.rubberbandHeight.get().doubleValue(), this.mc.field_1724.method_23321());
            }
        }
    }

    private FindItemResult getItem() {
        switch (this.block.get()) {
            case EChest:
                return InvUtils.findInHotbar(class_1802.field_8466);
            case Obsidian:
            default:
                return InvUtils.findInHotbar(class_1802.field_8281, class_1802.field_22421);
            case Anvil:
                return InvUtils.findInHotbar((Predicate<class_1799>) itemStack -> {
                    return class_2248.method_9503(itemStack.method_7909()) instanceof class_2199;
                });
            case Held:
                return new FindItemResult(this.mc.field_1724.method_31548().field_7545, this.mc.field_1724.method_6047().method_7947());
        }
    }

    private boolean checkHead() {
        class_2680 blockState1 = this.mc.field_1687.method_8320(this.blockPos.method_10102(this.mc.field_1724.method_23317() + 0.3d, this.mc.field_1724.method_23318() + 2.3d, this.mc.field_1724.method_23321() + 0.3d));
        class_2680 blockState2 = this.mc.field_1687.method_8320(this.blockPos.method_10102(this.mc.field_1724.method_23317() + 0.3d, this.mc.field_1724.method_23318() + 2.3d, this.mc.field_1724.method_23321() - 0.3d));
        class_2680 blockState3 = this.mc.field_1687.method_8320(this.blockPos.method_10102(this.mc.field_1724.method_23317() - 0.3d, this.mc.field_1724.method_23318() + 2.3d, this.mc.field_1724.method_23321() - 0.3d));
        class_2680 blockState4 = this.mc.field_1687.method_8320(this.blockPos.method_10102(this.mc.field_1724.method_23317() - 0.3d, this.mc.field_1724.method_23318() + 2.3d, this.mc.field_1724.method_23321() + 0.3d));
        boolean air1 = blockState1.method_45474();
        boolean air2 = blockState2.method_45474();
        boolean air3 = blockState3.method_45474();
        boolean air4 = blockState4.method_45474();
        return air1 && air2 && air3 && air4;
    }
}
