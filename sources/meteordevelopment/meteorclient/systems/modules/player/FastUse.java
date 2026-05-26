package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/FastUse.class */
public class FastUse extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private final Setting<List<class_1792>> items;
    private final Setting<Boolean> blocks;
    private final Setting<Integer> cooldown;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/FastUse$Mode.class */
    public enum Mode {
        All,
        Some
    }

    public FastUse() {
        super(Categories.Player, "fast-use", "Allows you to use items at very high speeds.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").description("Which items to fast use.").defaultValue(Mode.All).build());
        this.items = this.sgGeneral.add(new ItemListSetting.Builder().name("items").description("Which items should fast place work on in \"Some\" mode.").visible(() -> {
            return this.mode.get() == Mode.Some;
        }).build());
        this.blocks = this.sgGeneral.add(new BoolSetting.Builder().name("blocks").description("Fast-places blocks if the mode is \"Some\" mode.").visible(() -> {
            return this.mode.get() == Mode.Some;
        }).defaultValue(false).build());
        this.cooldown = this.sgGeneral.add(new IntSetting.Builder().name("cooldown").description("Fast-use cooldown in ticks.").defaultValue(0).min(0).sliderMax(4).build());
    }

    public int getItemUseCooldown(class_1799 itemStack) {
        if (this.mode.get() == Mode.All || shouldWorkSome(itemStack)) {
            return this.cooldown.get().intValue();
        }
        return 4;
    }

    private boolean shouldWorkSome(class_1799 itemStack) {
        return (this.blocks.get().booleanValue() && (itemStack.method_7909() instanceof class_1747)) || this.items.get().contains(itemStack.method_7909());
    }
}
