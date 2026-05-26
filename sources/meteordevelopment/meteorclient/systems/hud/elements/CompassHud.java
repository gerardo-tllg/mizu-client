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
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/CompassHud.class */
public class CompassHud extends HudElement {
    public static final HudElementInfo<CompassHud> INFO = new HudElementInfo<>(Hud.GROUP, "compass", "Displays a compass.", CompassHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgTextScale;
    private final SettingGroup sgBackground;
    private final Setting<Mode> mode;
    private final Setting<Double> scale;
    private final Setting<SettingColor> colorNorth;
    private final Setting<SettingColor> colorOther;
    private final Setting<Boolean> shadow;
    private final Setting<Integer> border;
    private final Setting<Boolean> customTextScale;
    private final Setting<Double> textScale;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/CompassHud$Mode.class */
    public enum Mode {
        Direction,
        Axis
    }

    public CompassHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgTextScale = this.settings.createGroup("Text Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("type").description("Which type of direction information to show.").defaultValue(Mode.Axis).build());
        this.scale = this.sgGeneral.add(new DoubleSetting.Builder().name("scale").description("The scale.").defaultValue(1.0d).min(1.0d).sliderRange(1.0d, 5.0d).onChanged(aDouble -> {
            calculateSize();
        }).build());
        this.colorNorth = this.sgGeneral.add(new ColorSetting.Builder().name("color-north").description("Color of north.").defaultValue(new SettingColor(225, 45, 45)).build());
        this.colorOther = this.sgGeneral.add(new ColorSetting.Builder().name("color-north").description("Color of other directions.").defaultValue(new SettingColor()).build());
        this.shadow = this.sgGeneral.add(new BoolSetting.Builder().name("shadow").description("Text shadow.").defaultValue(false).build());
        this.border = this.sgGeneral.add(new IntSetting.Builder().name("border").description("How much space to add around the element.").defaultValue(0).onChanged(integer -> {
            calculateSize();
        }).build());
        this.customTextScale = this.sgTextScale.add(new BoolSetting.Builder().name("custom-text-scale").description("Applies custom text scale rather than the global one.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgTextScale;
        DoubleSetting.Builder builderDescription = new DoubleSetting.Builder().name("text-scale").description("Custom text scale.");
        Setting<Boolean> setting = this.customTextScale;
        Objects.requireNonNull(setting);
        this.textScale = settingGroup.add(builderDescription.visible(setting::get).defaultValue(1.0d).min(0.5d).sliderRange(0.5d, 3.0d).build());
        this.background = this.sgBackground.add(new BoolSetting.Builder().name("background").description("Displays background.").defaultValue(false).build());
        SettingGroup settingGroup2 = this.sgBackground;
        ColorSetting.Builder builderDescription2 = new ColorSetting.Builder().name("background-color").description("Color used for the background.");
        Setting<Boolean> setting2 = this.background;
        Objects.requireNonNull(setting2);
        this.backgroundColor = settingGroup2.add(builderDescription2.visible(setting2::get).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        calculateSize();
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void setSize(double width, double height) {
        super.setSize(width + ((double) (this.border.get().intValue() * 2)), height + ((double) (this.border.get().intValue() * 2)));
    }

    private void calculateSize() {
        setSize(100.0d * this.scale.get().doubleValue(), 100.0d * this.scale.get().doubleValue());
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        double x = ((double) this.x) + (((double) getWidth()) / 2.0d);
        double y = ((double) this.y) + (((double) getHeight()) / 2.0d);
        double pitch = isInEditor() ? 120.0d : class_3532.method_15363(MeteorClient.mc.field_1724.method_36455() + 30.0f, -90.0f, 90.0f);
        double pitch2 = Math.toRadians(pitch);
        double yaw = Math.toRadians(isInEditor() ? 180.0d : class_3532.method_15393(MeteorClient.mc.field_1724.method_36454()));
        Direction[] directionArrValues = Direction.values();
        int length = directionArrValues.length;
        for (int i = 0; i < length; i++) {
            Direction direction = directionArrValues[i];
            String axis = this.mode.get() == Mode.Axis ? direction.getAxis() : direction.name();
            renderer.text(axis, (x + getX(direction, yaw)) - (renderer.textWidth(axis, this.shadow.get().booleanValue(), getTextScale()) / 2.0d), (y + getY(direction, yaw, pitch2)) - (renderer.textHeight(this.shadow.get().booleanValue(), getTextScale()) / 2.0d), direction == Direction.N ? this.colorNorth.get() : this.colorOther.get(), this.shadow.get().booleanValue(), getTextScale());
        }
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), this.backgroundColor.get());
        }
    }

    private double getX(Direction direction, double yaw) {
        return Math.sin(getPos(direction, yaw)) * this.scale.get().doubleValue() * 40.0d;
    }

    private double getY(Direction direction, double yaw, double pitch) {
        return Math.cos(getPos(direction, yaw)) * Math.sin(pitch) * this.scale.get().doubleValue() * 40.0d;
    }

    private double getPos(Direction direction, double yaw) {
        return yaw + ((((double) direction.ordinal()) * 3.141592653589793d) / 2.0d);
    }

    private double getTextScale() {
        if (this.customTextScale.get().booleanValue()) {
            return this.textScale.get().doubleValue();
        }
        return -1.0d;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/CompassHud$Direction.class */
    private enum Direction {
        N("Z-"),
        W("X-"),
        S("Z+"),
        E("X+");

        private final String axis;

        Direction(String axis) {
            this.axis = axis;
        }

        public String getAxis() {
            return this.axis;
        }
    }
}
