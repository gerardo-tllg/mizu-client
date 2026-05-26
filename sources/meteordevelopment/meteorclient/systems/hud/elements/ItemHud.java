package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.Objects;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/ItemHud.class */
public class ItemHud extends HudElement {
    public static final HudElementInfo<ItemHud> INFO = new HudElementInfo<>(Hud.GROUP, "item", "Displays the item count.", ItemHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgBackground;
    private final Setting<class_1792> item;
    private final Setting<NoneMode> noneMode;
    private final Setting<Double> scale;
    private final Setting<Integer> border;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;

    private ItemHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgBackground = this.settings.createGroup("Background");
        this.item = this.sgGeneral.add(new ItemSetting.Builder().name("item").description("Item to display").defaultValue(class_1802.field_8288).build());
        this.noneMode = this.sgGeneral.add(new EnumSetting.Builder().name("none-mode").description("How to render the item when you don't have the specified item in your inventory.").defaultValue(NoneMode.HideCount).build());
        this.scale = this.sgGeneral.add(new DoubleSetting.Builder().name("scale").description("Scale of the item.").defaultValue(2.0d).onChanged(aDouble -> {
            calculateSize();
        }).min(1.0d).sliderRange(1.0d, 4.0d).build());
        this.border = this.sgGeneral.add(new IntSetting.Builder().name("border").description("How much space to add around the element.").defaultValue(0).onChanged(integer -> {
            calculateSize();
        }).build());
        this.background = this.sgBackground.add(new BoolSetting.Builder().name("background").description("Displays background.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgBackground;
        ColorSetting.Builder builderDescription = new ColorSetting.Builder().name("background-color").description("Color used for the background.");
        Setting<Boolean> setting = this.background;
        Objects.requireNonNull(setting);
        this.backgroundColor = settingGroup.add(builderDescription.visible(setting::get).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        calculateSize();
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void setSize(double width, double height) {
        super.setSize(width + ((double) (this.border.get().intValue() * 2)), height + ((double) (this.border.get().intValue() * 2)));
    }

    private void calculateSize() {
        setSize(17.0d * this.scale.get().doubleValue(), 17.0d * this.scale.get().doubleValue());
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        class_1799 itemStack = new class_1799(this.item.get(), InvUtils.find(this.item.get()).count());
        if (this.noneMode.get() == NoneMode.HideItem && itemStack.method_7960()) {
            if (isInEditor()) {
                renderer.line(this.x, this.y, this.x + getWidth(), this.y + getHeight(), Color.GRAY);
                renderer.line(this.x, this.y + getHeight(), this.x + getWidth(), this.y, Color.GRAY);
            }
        } else {
            renderer.post(() -> {
                double x = this.x + this.border.get().intValue();
                double y = this.y + this.border.get().intValue();
                render(renderer, itemStack, (int) x, (int) y);
            });
        }
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), this.backgroundColor.get());
        }
    }

    private void render(HudRenderer renderer, class_1799 itemStack, int x, int y) {
        if (this.noneMode.get() == NoneMode.HideItem) {
            renderer.item(itemStack, x, y, this.scale.get().floatValue(), true);
            return;
        }
        String countOverride = null;
        boolean resetToZero = false;
        if (itemStack.method_7960()) {
            if (this.noneMode.get() == NoneMode.ShowCount) {
                countOverride = "0";
            }
            itemStack.method_7939(1);
            resetToZero = true;
        }
        renderer.item(itemStack, x, y, this.scale.get().floatValue(), true, countOverride);
        if (resetToZero) {
            itemStack.method_7939(0);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/ItemHud$NoneMode.class */
    public enum NoneMode {
        HideItem,
        HideCount,
        ShowCount;

        /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
        @Override // java.lang.Enum
        public String toString() throws MatchException {
            switch (this) {
                case HideItem:
                    return "Hide Item";
                case HideCount:
                    return "Hide Count";
                case ShowCount:
                    return "Show Count";
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
        }
    }
}
