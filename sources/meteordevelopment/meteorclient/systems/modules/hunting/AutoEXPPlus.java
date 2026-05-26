package meteordevelopment.meteorclient.systems.modules.hunting;

import java.util.Objects;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1893;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/AutoEXPPlus.class */
public class AutoEXPPlus extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private final Setting<Boolean> replenish;
    private final Setting<Integer> slot;
    private final Setting<Integer> minThreshold;
    private final Setting<Integer> maxThreshold;
    private final Setting<Boolean> ignoreElytra;
    private int repairingI;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/AutoEXPPlus$Mode.class */
    public enum Mode {
        Armor,
        Hands,
        Both
    }

    public AutoEXPPlus() {
        super(Categories.Hunting, "auto-exp-plus", "Automatically repairs your armor and tools in pvp.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").description("Which items to repair.").defaultValue(Mode.Both).build());
        this.replenish = this.sgGeneral.add(new BoolSetting.Builder().name("replenish").description("Automatically replenishes exp into a selected hotbar slot.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgGeneral;
        IntSetting.Builder builderDescription = new IntSetting.Builder().name("exp-slot").description("The slot to replenish exp into.");
        Setting<Boolean> setting = this.replenish;
        Objects.requireNonNull(setting);
        this.slot = settingGroup.add(builderDescription.visible(setting::get).defaultValue(6).range(1, 9).sliderRange(1, 9).build());
        this.minThreshold = this.sgGeneral.add(new IntSetting.Builder().name("min-threshold").description("The minimum durability percentage that an item needs to fall to, to be repaired.").defaultValue(30).range(1, 100).sliderRange(1, 100).build());
        this.maxThreshold = this.sgGeneral.add(new IntSetting.Builder().name("max-threshold").description("The maximum durability percentage to repair items to.").defaultValue(80).range(1, 100).sliderRange(1, 100).build());
        this.ignoreElytra = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-elytra").description("Ignore elytra when repairing.").defaultValue(false).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.repairingI = -1;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null) {
            return;
        }
        if (this.repairingI == -1) {
            if (this.mode.get() != Mode.Hands) {
                int i = 0;
                while (true) {
                    if (i >= 4) {
                        break;
                    }
                    class_1799 armorStack = this.mc.field_1724.method_31548().method_5438(36 + i);
                    if ((this.ignoreElytra.get().booleanValue() && armorStack.method_7909() == class_1802.field_8833) || !needsRepair(armorStack, this.minThreshold.get().intValue())) {
                        i++;
                    } else {
                        this.repairingI = 36 + i;
                        break;
                    }
                }
            }
            if (this.mode.get() != Mode.Armor && this.repairingI == -1) {
                class_1268[] class_1268VarArrValues = class_1268.values();
                int length = class_1268VarArrValues.length;
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        break;
                    }
                    class_1268 hand = class_1268VarArrValues[i2];
                    if (!needsRepair(this.mc.field_1724.method_5998(hand), this.minThreshold.get().intValue())) {
                        i2++;
                    } else {
                        this.repairingI = hand == class_1268.field_5808 ? this.mc.field_1724.method_31548().field_7545 : 45;
                    }
                }
            }
        }
        if (this.repairingI != -1) {
            if (!needsRepair(this.mc.field_1724.method_31548().method_5438(this.repairingI), this.maxThreshold.get().intValue())) {
                this.repairingI = -1;
                return;
            }
            FindItemResult exp = InvUtils.find(class_1802.field_8287);
            if (exp.found()) {
                if (!exp.isHotbar() && !exp.isOffhand()) {
                    if (!this.replenish.get().booleanValue()) {
                        return;
                    } else {
                        InvUtils.move().from(exp.slot()).toHotbar(this.slot.get().intValue() - 1);
                    }
                }
                Rotations.rotate(this.mc.field_1724.method_36454(), 90.0d, () -> {
                    if (exp.getHand() != null) {
                        this.mc.field_1761.method_2919(this.mc.field_1724, exp.getHand());
                        return;
                    }
                    InvUtils.swap(exp.slot(), true);
                    this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5808);
                    InvUtils.swapBack();
                });
            }
        }
    }

    private boolean needsRepair(class_1799 itemStack, double threshold) {
        return !itemStack.method_7960() && Utils.hasEnchantments(itemStack, class_1893.field_9101) && (((double) (itemStack.method_7936() - itemStack.method_7919())) / ((double) itemStack.method_7936())) * 100.0d <= threshold;
    }
}
