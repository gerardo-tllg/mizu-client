package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.Objects;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.TickRate;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/LagNotifierHud.class */
public class LagNotifierHud extends HudElement {
    public static final HudElementInfo<LagNotifierHud> INFO = new HudElementInfo<>(Hud.GROUP, "lag-notifier", "Displays if the server is lagging in ticks.", LagNotifierHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    private final Setting<Boolean> shadow;
    private final Setting<SettingColor> textColor;
    private final Setting<SettingColor> color1;
    private final Setting<SettingColor> color2;
    private final Setting<SettingColor> color3;
    private final Setting<Integer> border;
    private final Setting<Boolean> customScale;
    private final Setting<Double> scale;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;

    public LagNotifierHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.shadow = this.sgGeneral.add(new BoolSetting.Builder().name("shadow").description("Text shadow.").defaultValue(true).build());
        this.textColor = this.sgGeneral.add(new ColorSetting.Builder().name("text-color").description("A.").defaultValue(new SettingColor()).build());
        this.color1 = this.sgGeneral.add(new ColorSetting.Builder().name("color-1").description("First color.").defaultValue(new SettingColor(255, 255, 5)).build());
        this.color2 = this.sgGeneral.add(new ColorSetting.Builder().name("color-2").description("Second color.").defaultValue(new SettingColor(235, Opcode.IFLE, 52)).build());
        this.color3 = this.sgGeneral.add(new ColorSetting.Builder().name("color-3").description("Third color.").defaultValue(new SettingColor(225, 45, 45)).build());
        this.border = this.sgGeneral.add(new IntSetting.Builder().name("border").description("How much space to add around the element.").defaultValue(0).build());
        this.customScale = this.sgScale.add(new BoolSetting.Builder().name("custom-scale").description("Applies custom text scale rather than the global one.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgScale;
        DoubleSetting.Builder builderDescription = new DoubleSetting.Builder().name("scale").description("Custom scale.");
        Setting<Boolean> setting = this.customScale;
        Objects.requireNonNull(setting);
        this.scale = settingGroup.add(builderDescription.visible(setting::get).defaultValue(1.0d).min(0.5d).sliderRange(0.5d, 3.0d).build());
        this.background = this.sgBackground.add(new BoolSetting.Builder().name("background").description("Displays background.").defaultValue(false).build());
        SettingGroup settingGroup2 = this.sgBackground;
        ColorSetting.Builder builderDescription2 = new ColorSetting.Builder().name("background-color").description("Color used for the background.");
        Setting<Boolean> setting2 = this.background;
        Objects.requireNonNull(setting2);
        this.backgroundColor = settingGroup2.add(builderDescription2.visible(setting2::get).defaultValue(new SettingColor(25, 25, 25, 50)).build());
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void setSize(double width, double height) {
        super.setSize(width + ((double) (this.border.get().intValue() * 2)), height + ((double) (this.border.get().intValue() * 2)));
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        Color color;
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), this.backgroundColor.get());
        }
        if (isInEditor()) {
            render(renderer, "4.3", this.color3.get());
            return;
        }
        float timeSinceLastTick = TickRate.INSTANCE.getTimeSinceLastTick();
        if (timeSinceLastTick >= 1.0f) {
            if (timeSinceLastTick > 10.0f) {
                color = this.color3.get();
            } else {
                color = timeSinceLastTick > 3.0f ? this.color2.get() : this.color1.get();
            }
            render(renderer, String.format("%.1f", Float.valueOf(timeSinceLastTick)), color);
        }
    }

    private void render(HudRenderer renderer, String right, Color rightColor) {
        double x = this.x + this.border.get().intValue();
        double y = this.y + this.border.get().intValue();
        double x2 = renderer.text("Time since last tick ", x, y, this.textColor.get(), this.shadow.get().booleanValue(), getScale());
        setSize(renderer.text(right, x2, y, rightColor, this.shadow.get().booleanValue(), getScale()) - x, renderer.textHeight(this.shadow.get().booleanValue(), getScale()));
    }

    private double getScale() {
        if (this.customScale.get().booleanValue()) {
            return this.scale.get().doubleValue();
        }
        return -1.0d;
    }
}
