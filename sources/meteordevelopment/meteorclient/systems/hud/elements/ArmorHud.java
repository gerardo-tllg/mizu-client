package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_1799;
import net.minecraft.class_1802;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/ArmorHud.class */
public class ArmorHud extends HudElement {
    public static final HudElementInfo<ArmorHud> INFO = new HudElementInfo<>(Hud.GROUP, "armor", "Displays your armor.", ArmorHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgDurability;
    private final SettingGroup sgBackground;
    private final Setting<Orientation> orientation;
    private final Setting<Boolean> flipOrder;
    private final Setting<Double> scale;
    private final Setting<Integer> border;
    private final Setting<Durability> durability;
    private final Setting<SettingColor> durabilityColor;
    private final Setting<Boolean> durabilityShadow;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/ArmorHud$Durability.class */
    public enum Durability {
        None,
        Bar,
        Total,
        Percentage
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/ArmorHud$Orientation.class */
    public enum Orientation {
        Horizontal,
        Vertical
    }

    public ArmorHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgDurability = this.settings.createGroup("Durability");
        this.sgBackground = this.settings.createGroup("Background");
        this.orientation = this.sgGeneral.add(new EnumSetting.Builder().name("orientation").description("How to display armor.").defaultValue(Orientation.Horizontal).onChanged(val -> {
            calculateSize();
        }).build());
        this.flipOrder = this.sgGeneral.add(new BoolSetting.Builder().name("flip-order").description("Flips the order of armor items.").defaultValue(true).build());
        this.scale = this.sgGeneral.add(new DoubleSetting.Builder().name("scale").description("The scale.").defaultValue(2.0d).onChanged(aDouble -> {
            calculateSize();
        }).min(1.0d).sliderRange(1.0d, 5.0d).build());
        this.border = this.sgGeneral.add(new IntSetting.Builder().name("border").description("How much space to add around the element.").defaultValue(0).onChanged(integer -> {
            calculateSize();
        }).build());
        this.durability = this.sgDurability.add(new EnumSetting.Builder().name("durability").description("How to display armor durability.").defaultValue(Durability.Bar).onChanged(durability1 -> {
            calculateSize();
        }).build());
        this.durabilityColor = this.sgDurability.add(new ColorSetting.Builder().name("durability-color").description("Color of the text.").visible(() -> {
            return this.durability.get() == Durability.Total || this.durability.get() == Durability.Percentage;
        }).defaultValue(new SettingColor()).build());
        this.durabilityShadow = this.sgDurability.add(new BoolSetting.Builder().name("durability-shadow").description("Text shadow.").visible(() -> {
            return this.durability.get() == Durability.Total || this.durability.get() == Durability.Percentage;
        }).defaultValue(true).build());
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
        switch (this.orientation.get()) {
            case Horizontal:
                setSize((16.0d * this.scale.get().doubleValue() * 4.0d) + 8.0d, 16.0d * this.scale.get().doubleValue());
                break;
            case Vertical:
                setSize(16.0d * this.scale.get().doubleValue(), (16.0d * this.scale.get().doubleValue() * 4.0d) + 8.0d);
                break;
        }
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        double armorX;
        double armorY;
        String string;
        double armorX2;
        double armorY2;
        double x = this.x;
        double y = this.y;
        int slot = this.flipOrder.get().booleanValue() ? 3 : 0;
        for (int position = 0; position < 4; position++) {
            class_1799 itemStack = getItem(slot);
            if (this.orientation.get() == Orientation.Vertical) {
                armorX = x;
                armorY = y + (((double) (position * 18)) * this.scale.get().doubleValue());
            } else {
                armorX = x + (((double) (position * 18)) * this.scale.get().doubleValue());
                armorY = y;
            }
            renderer.item(itemStack, (int) armorX, (int) armorY, this.scale.get().floatValue(), itemStack.method_7963() && this.durability.get() == Durability.Bar);
            if (itemStack.method_7963() && !isInEditor() && this.durability.get() != Durability.Bar && this.durability.get() != Durability.None) {
                switch (this.durability.get().ordinal()) {
                    case 2:
                        string = Integer.toString(itemStack.method_7936() - itemStack.method_7919());
                        break;
                    case 3:
                        string = Integer.toString(Math.round(((itemStack.method_7936() - itemStack.method_7919()) * 100.0f) / itemStack.method_7936()));
                        break;
                    default:
                        string = "err";
                        break;
                }
                String message = string;
                double messageWidth = renderer.textWidth(message);
                if (this.orientation.get() == Orientation.Vertical) {
                    armorX2 = (x + (8.0d * this.scale.get().doubleValue())) - (messageWidth / 2.0d);
                    armorY2 = y + (((double) (18 * position)) * this.scale.get().doubleValue()) + ((18.0d * this.scale.get().doubleValue()) - renderer.textHeight());
                } else {
                    armorX2 = ((x + (((double) (18 * position)) * this.scale.get().doubleValue())) + (8.0d * this.scale.get().doubleValue())) - (messageWidth / 2.0d);
                    armorY2 = y + (((double) getHeight()) - renderer.textHeight());
                }
                renderer.text(message, armorX2, armorY2, this.durabilityColor.get(), this.durabilityShadow.get().booleanValue());
            }
            slot = this.flipOrder.get().booleanValue() ? slot - 1 : slot + 1;
        }
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), this.backgroundColor.get());
        }
    }

    private class_1799 getItem(int i) {
        if (isInEditor()) {
            switch (i) {
                case 1:
                    return class_1802.field_22029.method_7854();
                case 2:
                    return class_1802.field_22028.method_7854();
                case 3:
                    return class_1802.field_22027.method_7854();
                default:
                    return class_1802.field_22030.method_7854();
            }
        }
        return (class_1799) MeteorClient.mc.field_1724.method_31548().meteor$getArmor().get(i);
    }
}
