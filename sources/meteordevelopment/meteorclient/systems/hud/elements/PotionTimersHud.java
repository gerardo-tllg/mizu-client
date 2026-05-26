package meteordevelopment.meteorclient.systems.hud.elements;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StatusEffectListSetting;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_1291;
import net.minecraft.class_1292;
import net.minecraft.class_1293;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/PotionTimersHud.class */
public class PotionTimersHud extends HudElement {
    public static final HudElementInfo<PotionTimersHud> INFO = new HudElementInfo<>(Hud.GROUP, "potion-timers", "Displays active potion effects with timers.", PotionTimersHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    private final Setting<List<class_1291>> hiddenEffects;
    private final Setting<Boolean> showAmbient;
    private final Setting<ColorMode> colorMode;
    private final Setting<SettingColor> flatColor;
    private final Setting<Double> rainbowSpeed;
    private final Setting<Double> rainbowSpread;
    private final Setting<Double> rainbowSaturation;
    private final Setting<Double> rainbowBrightness;
    private final Setting<Boolean> shadow;
    private final Setting<Alignment> alignment;
    private final Setting<Integer> border;
    private final Setting<Boolean> customScale;
    private final Setting<Double> scale;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;
    private final List<Pair<class_1293, String>> texts;
    private double rainbowHue;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/PotionTimersHud$ColorMode.class */
    public enum ColorMode {
        Effect,
        Flat,
        Rainbow
    }

    public PotionTimersHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.hiddenEffects = this.sgGeneral.add(new StatusEffectListSetting.Builder().name("hidden-effects").description("Which effects not to show in the list.").build());
        this.showAmbient = this.sgGeneral.add(new BoolSetting.Builder().name("show-ambient").description("Whether to show ambient effects like from beacons and conduits.").defaultValue(true).build());
        this.colorMode = this.sgGeneral.add(new EnumSetting.Builder().name("color-mode").description("What color to use for effects.").defaultValue(ColorMode.Effect).build());
        this.flatColor = this.sgGeneral.add(new ColorSetting.Builder().name("flat-color").description("Color for flat color mode.").defaultValue(new SettingColor(225, 25, 25)).visible(() -> {
            return this.colorMode.get() == ColorMode.Flat;
        }).build());
        this.rainbowSpeed = this.sgGeneral.add(new DoubleSetting.Builder().name("rainbow-speed").description("Rainbow speed of rainbow color mode.").defaultValue(0.05d).sliderMin(0.01d).sliderMax(0.2d).decimalPlaces(4).visible(() -> {
            return this.colorMode.get() == ColorMode.Rainbow;
        }).build());
        this.rainbowSpread = this.sgGeneral.add(new DoubleSetting.Builder().name("rainbow-spread").description("Rainbow spread of rainbow color mode.").defaultValue(0.01d).sliderMin(0.001d).sliderMax(0.05d).decimalPlaces(4).visible(() -> {
            return this.colorMode.get() == ColorMode.Rainbow;
        }).build());
        this.rainbowSaturation = this.sgGeneral.add(new DoubleSetting.Builder().name("rainbow-saturation").description("Saturation of rainbow color mode.").defaultValue(1.0d).sliderRange(0.0d, 1.0d).visible(() -> {
            return this.colorMode.get() == ColorMode.Rainbow;
        }).build());
        this.rainbowBrightness = this.sgGeneral.add(new DoubleSetting.Builder().name("rainbow-brightness").description("Brightness of rainbow color mode.").defaultValue(1.0d).sliderRange(0.0d, 1.0d).visible(() -> {
            return this.colorMode.get() == ColorMode.Rainbow;
        }).build());
        this.shadow = this.sgGeneral.add(new BoolSetting.Builder().name("shadow").description("Renders shadow behind text.").defaultValue(true).build());
        this.alignment = this.sgGeneral.add(new EnumSetting.Builder().name("alignment").description("Horizontal alignment.").defaultValue(Alignment.Auto).build());
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
        this.texts = new ArrayList();
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void setSize(double width, double height) {
        super.setSize(width + ((double) (this.border.get().intValue() * 2)), height + ((double) (this.border.get().intValue() * 2)));
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    protected double alignX(double width, Alignment alignment) {
        return this.box.alignX(getWidth() - (this.border.get().intValue() * 2), width, alignment);
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void tick(HudRenderer renderer) {
        if (MeteorClient.mc.field_1724 == null || (isInEditor() && hasNoVisibleEffects())) {
            setSize(renderer.textWidth("Potion Timers 0:00", this.shadow.get().booleanValue(), getScale()), renderer.textHeight(this.shadow.get().booleanValue(), getScale()));
            return;
        }
        double width = 0.0d;
        double height = 0.0d;
        this.texts.clear();
        for (class_1293 statusEffectInstance : MeteorClient.mc.field_1724.method_6026()) {
            if (!this.hiddenEffects.get().contains(statusEffectInstance.method_5579().comp_349()) && (this.showAmbient.get().booleanValue() || !statusEffectInstance.method_5591())) {
                String text = getString(statusEffectInstance);
                this.texts.add(new ObjectObjectImmutablePair(statusEffectInstance, text));
                width = Math.max(width, renderer.textWidth(text, this.shadow.get().booleanValue(), getScale()));
                height += renderer.textHeight(this.shadow.get().booleanValue(), getScale());
            }
        }
        setSize(width, height);
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) throws MatchException {
        SettingColor color;
        double x = this.x + this.border.get().intValue();
        double y = this.y + this.border.get().intValue();
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), this.backgroundColor.get());
        }
        if (MeteorClient.mc.field_1724 == null || (isInEditor() && hasNoVisibleEffects())) {
            renderer.text("Potion Timers 0:00", x, y, Color.WHITE, this.shadow.get().booleanValue(), getScale());
            return;
        }
        this.rainbowHue += this.rainbowSpeed.get().doubleValue() * renderer.delta;
        if (this.rainbowHue > 1.0d) {
            this.rainbowHue -= 1.0d;
        } else if (this.rainbowHue < -1.0d) {
            this.rainbowHue += 1.0d;
        }
        double localRainbowHue = this.rainbowHue;
        for (Pair<class_1293, String> potionEffectEntry : this.texts) {
            switch (this.colorMode.get()) {
                case Effect:
                    int c = ((class_1291) ((class_1293) potionEffectEntry.left()).method_5579().comp_349()).method_5556();
                    color = new Color(c).a(255);
                    break;
                case Flat:
                    this.flatColor.get().update();
                    color = this.flatColor.get();
                    break;
                case Rainbow:
                    localRainbowHue += this.rainbowSpread.get().doubleValue();
                    int c2 = java.awt.Color.HSBtoRGB((float) localRainbowHue, this.rainbowSaturation.get().floatValue(), this.rainbowBrightness.get().floatValue());
                    color = new Color(c2);
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
            Color color2 = color;
            String text = (String) potionEffectEntry.right();
            renderer.text(text, x + alignX(renderer.textWidth(text, this.shadow.get().booleanValue(), getScale()), this.alignment.get()), y, color2, this.shadow.get().booleanValue(), getScale());
            y += renderer.textHeight(this.shadow.get().booleanValue(), getScale());
        }
    }

    private String getString(class_1293 statusEffectInstance) {
        return String.format("%s %d (%s)", Names.get((class_1291) statusEffectInstance.method_5579().comp_349()), Integer.valueOf(statusEffectInstance.method_5578() + 1), class_1292.method_5577(statusEffectInstance, 1.0f, MeteorClient.mc.field_1687.method_54719().method_54748()).getString());
    }

    private double getScale() {
        if (this.customScale.get().booleanValue()) {
            return this.scale.get().doubleValue();
        }
        return -1.0d;
    }

    private boolean hasNoVisibleEffects() {
        for (class_1293 statusEffectInstance : MeteorClient.mc.field_1724.method_6026()) {
            if (!this.hiddenEffects.get().contains(statusEffectInstance.method_5579().comp_349()) && (this.showAmbient.get().booleanValue() || !statusEffectInstance.method_5591())) {
                return false;
            }
        }
        return true;
    }
}
