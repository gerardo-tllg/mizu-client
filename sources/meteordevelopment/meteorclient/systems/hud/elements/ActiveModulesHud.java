package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/ActiveModulesHud.class */
public class ActiveModulesHud extends HudElement {
    public static final HudElementInfo<ActiveModulesHud> INFO = new HudElementInfo<>(Hud.GROUP, "active-modules", "Displays your active modules.", ActiveModulesHud::new);
    private static final Color WHITE = new Color();
    private final SettingGroup sgGeneral;
    private final Setting<List<Module>> shownModules;
    private final Setting<Sort> sort;
    private final Setting<Boolean> activeInfo;
    private final Setting<SettingColor> moduleInfoColor;
    private final Setting<ColorMode> colorMode;
    private final Setting<SettingColor> flatColor;
    private final Setting<Boolean> shadow;
    private final Setting<Alignment> alignment;
    private final Setting<Boolean> outlines;
    private final Setting<Integer> outlineWidth;
    private final Setting<Boolean> customScale;
    private final Setting<Double> scale;
    private final Setting<Double> rainbowSpeed;
    private final Setting<Double> rainbowSpread;
    private final Setting<Double> rainbowSaturation;
    private final Setting<Double> rainbowBrightness;
    private final List<Module> modules;
    private final Color rainbow;
    private double rainbowHue1;
    private double rainbowHue2;
    private double prevX;
    private double prevTextLength;
    private Color prevColor;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/ActiveModulesHud$ColorMode.class */
    public enum ColorMode {
        Flat,
        Random,
        Rainbow
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/ActiveModulesHud$Sort.class */
    public enum Sort {
        Alphabetical,
        Biggest,
        Smallest
    }

    public ActiveModulesHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.shownModules = this.sgGeneral.add(new ModuleListSetting.Builder().name("visible-modules").description("Which modules to show in the list").build());
        this.sort = this.sgGeneral.add(new EnumSetting.Builder().name("sort").description("How to sort active modules.").defaultValue(Sort.Biggest).build());
        this.activeInfo = this.sgGeneral.add(new BoolSetting.Builder().name("additional-info").description("Shows additional info from the module next to the name in the active modules list.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgGeneral;
        ColorSetting.Builder builderDefaultValue = new ColorSetting.Builder().name("module-info-color").description("Color of module info text.").defaultValue(new SettingColor(Opcode.DRETURN, Opcode.DRETURN, Opcode.DRETURN));
        Setting<Boolean> setting = this.activeInfo;
        Objects.requireNonNull(setting);
        this.moduleInfoColor = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        this.colorMode = this.sgGeneral.add(new EnumSetting.Builder().name("color-mode").description("What color to use for active modules.").defaultValue(ColorMode.Rainbow).build());
        this.flatColor = this.sgGeneral.add(new ColorSetting.Builder().name("flat-color").description("Color for flat color mode.").defaultValue(new SettingColor(225, 25, 25)).visible(() -> {
            return this.colorMode.get() == ColorMode.Flat;
        }).build());
        this.shadow = this.sgGeneral.add(new BoolSetting.Builder().name("shadow").description("Renders shadow behind text.").defaultValue(true).build());
        this.alignment = this.sgGeneral.add(new EnumSetting.Builder().name("alignment").description("Horizontal alignment.").defaultValue(Alignment.Auto).build());
        this.outlines = this.sgGeneral.add(new BoolSetting.Builder().name("outlines").description("Whether or not to render outlines").defaultValue(false).build());
        SettingGroup settingGroup2 = this.sgGeneral;
        IntSetting.Builder builderSliderMin = new IntSetting.Builder().name("outline-width").description("Outline width").defaultValue(2).min(1).sliderMin(1);
        Setting<Boolean> setting2 = this.outlines;
        Objects.requireNonNull(setting2);
        this.outlineWidth = settingGroup2.add(builderSliderMin.visible(setting2::get).build());
        this.customScale = this.sgGeneral.add(new BoolSetting.Builder().name("custom-scale").description("Applies custom text scale rather than the global one.").defaultValue(false).build());
        SettingGroup settingGroup3 = this.sgGeneral;
        DoubleSetting.Builder builderDescription = new DoubleSetting.Builder().name("scale").description("Custom scale.");
        Setting<Boolean> setting3 = this.customScale;
        Objects.requireNonNull(setting3);
        this.scale = settingGroup3.add(builderDescription.visible(setting3::get).defaultValue(1.0d).min(0.5d).sliderRange(0.5d, 3.0d).build());
        this.rainbowSpeed = this.sgGeneral.add(new DoubleSetting.Builder().name("rainbow-speed").description("Rainbow speed of rainbow color mode.").defaultValue(0.05d).sliderMin(0.01d).sliderMax(0.2d).decimalPlaces(4).visible(() -> {
            return this.colorMode.get() == ColorMode.Rainbow;
        }).build());
        this.rainbowSpread = this.sgGeneral.add(new DoubleSetting.Builder().name("rainbow-spread").description("Rainbow spread of rainbow color mode.").defaultValue(0.01d).sliderMin(0.001d).sliderMax(0.05d).decimalPlaces(4).visible(() -> {
            return this.colorMode.get() == ColorMode.Rainbow;
        }).build());
        this.rainbowSaturation = this.sgGeneral.add(new DoubleSetting.Builder().name("rainbow-saturation").defaultValue(1.0d).sliderRange(0.0d, 1.0d).visible(() -> {
            return this.colorMode.get() == ColorMode.Rainbow;
        }).build());
        this.rainbowBrightness = this.sgGeneral.add(new DoubleSetting.Builder().name("rainbow-brightness").defaultValue(1.0d).sliderRange(0.0d, 1.0d).visible(() -> {
            return this.colorMode.get() == ColorMode.Rainbow;
        }).build());
        this.modules = new ArrayList();
        this.rainbow = new Color(255, 255, 255);
        this.prevColor = new Color();
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void tick(HudRenderer renderer) {
        this.modules.clear();
        for (Module module : Modules.get().getActive()) {
            if (this.shownModules.get().contains(module)) {
                this.modules.add(module);
            }
        }
        if (this.modules.isEmpty()) {
            if (isInEditor()) {
                setSize(renderer.textWidth("Active Modules", this.shadow.get().booleanValue(), getScale()), renderer.textHeight(this.shadow.get().booleanValue(), getScale()));
                return;
            }
            return;
        }
        this.modules.sort((e1, e2) -> {
            switch (this.sort.get()) {
                case Alphabetical:
                    return e1.title.compareTo(e2.title);
                case Biggest:
                    return Double.compare(getModuleWidth(renderer, e2), getModuleWidth(renderer, e1));
                case Smallest:
                    return Double.compare(getModuleWidth(renderer, e1), getModuleWidth(renderer, e2));
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
        });
        double width = 0.0d;
        double height = 0.0d;
        for (int i = 0; i < this.modules.size(); i++) {
            width = Math.max(width, getModuleWidth(renderer, this.modules.get(i)));
            height += renderer.textHeight(this.shadow.get().booleanValue(), getScale());
            if (i > 0) {
                height += 2.0d;
            }
        }
        setSize(width, height);
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        double x = this.x;
        double y = this.y;
        if (this.modules.isEmpty()) {
            if (isInEditor()) {
                renderer.text("Active Modules", x, y, WHITE, this.shadow.get().booleanValue(), getScale());
                return;
            }
            return;
        }
        this.rainbowHue1 += this.rainbowSpeed.get().doubleValue() * renderer.delta;
        if (this.rainbowHue1 > 1.0d) {
            this.rainbowHue1 -= 1.0d;
        } else if (this.rainbowHue1 < -1.0d) {
            this.rainbowHue1 += 1.0d;
        }
        this.rainbowHue2 = this.rainbowHue1;
        this.prevX = x;
        for (int i = 0; i < this.modules.size(); i++) {
            double offset = alignX(getModuleWidth(renderer, this.modules.get(i)), this.alignment.get());
            renderModule(renderer, this.modules, i, x + offset, y);
            this.prevX = x + offset;
            y += 2.0d + renderer.textHeight(this.shadow.get().booleanValue(), getScale());
        }
    }

    private void renderModule(HudRenderer renderer, List<Module> modules, int index, double x, double y) {
        String info;
        Module module = modules.get(index);
        Color color = this.flatColor.get();
        switch (this.colorMode.get().ordinal()) {
            case 1:
                color = module.color;
                break;
            case 2:
                this.rainbowHue2 += this.rainbowSpread.get().doubleValue();
                int c = java.awt.Color.HSBtoRGB((float) this.rainbowHue2, this.rainbowSaturation.get().floatValue(), this.rainbowBrightness.get().floatValue());
                this.rainbow.r = Color.toRGBAR(c);
                this.rainbow.g = Color.toRGBAG(c);
                this.rainbow.b = Color.toRGBAB(c);
                color = this.rainbow;
                break;
        }
        renderer.text(module.title, x, y, color, this.shadow.get().booleanValue(), getScale());
        double emptySpace = renderer.textWidth(" ", this.shadow.get().booleanValue(), getScale());
        double textHeight = renderer.textHeight(this.shadow.get().booleanValue(), getScale());
        double textLength = renderer.textWidth(module.title, this.shadow.get().booleanValue(), getScale());
        if (this.activeInfo.get().booleanValue() && (info = module.getInfoString()) != null) {
            renderer.text(info, x + emptySpace + textLength, y, this.moduleInfoColor.get(), this.shadow.get().booleanValue(), getScale());
            textLength += emptySpace + renderer.textWidth(info, this.shadow.get().booleanValue(), getScale());
        }
        if (this.outlines.get().booleanValue()) {
            if (index == 0) {
                renderer.quad((x - 2.0d) - ((double) this.outlineWidth.get().intValue()), y - 2.0d, this.outlineWidth.get().intValue(), textHeight + 4.0d, this.prevColor, this.prevColor, color, color);
                renderer.quad(x + textLength + 2.0d, y - 2.0d, this.outlineWidth.get().intValue(), textHeight + 4.0d, this.prevColor, this.prevColor, color, color);
                renderer.quad((x - 2.0d) - ((double) this.outlineWidth.get().intValue()), (y - 2.0d) - ((double) this.outlineWidth.get().intValue()), textLength + 4.0d + ((double) (this.outlineWidth.get().intValue() * 2)), this.outlineWidth.get().intValue(), this.prevColor, this.prevColor, color, color);
                if (index == modules.size() - 1) {
                    renderer.quad((x - 2.0d) - ((double) this.outlineWidth.get().intValue()), y + textHeight + 2.0d, textLength + 4.0d + ((double) (this.outlineWidth.get().intValue() * 2)), this.outlineWidth.get().intValue(), this.prevColor, this.prevColor, color, color);
                }
            } else if (index == modules.size() - 1) {
                renderer.quad((x - 2.0d) - ((double) this.outlineWidth.get().intValue()), y, this.outlineWidth.get().intValue(), textHeight + 2.0d + ((double) this.outlineWidth.get().intValue()), this.prevColor, this.prevColor, color, color);
                renderer.quad(x + textLength + 2.0d, y, this.outlineWidth.get().intValue(), textHeight + 2.0d + ((double) this.outlineWidth.get().intValue()), this.prevColor, this.prevColor, color, color);
                renderer.quad((x - 2.0d) - ((double) this.outlineWidth.get().intValue()), y + textHeight + 2.0d, textLength + 4.0d + ((double) (this.outlineWidth.get().intValue() * 2)), this.outlineWidth.get().intValue(), this.prevColor, this.prevColor, color, color);
            }
            if (index > 0) {
                if (index < modules.size() - 1) {
                    renderer.quad((x - 2.0d) - ((double) this.outlineWidth.get().intValue()), y, this.outlineWidth.get().intValue(), textHeight + 2.0d, this.prevColor, this.prevColor, color, color);
                    renderer.quad(x + textLength + 2.0d, y, this.outlineWidth.get().intValue(), textHeight + 2.0d, this.prevColor, this.prevColor, color, color);
                }
                renderer.quad((Math.min(this.prevX, x) - 2.0d) - ((double) this.outlineWidth.get().intValue()), Math.max(this.prevX, x) == x ? y : y - ((double) this.outlineWidth.get().intValue()), (Math.max(this.prevX, x) - 2.0d) - ((Math.min(this.prevX, x) - 2.0d) - ((double) this.outlineWidth.get().intValue())), this.outlineWidth.get().intValue(), this.prevColor, this.prevColor, color, color);
                renderer.quad(Math.min(this.prevX + this.prevTextLength, x + textLength) + 2.0d, Math.min(this.prevX + this.prevTextLength, x + textLength) == x + textLength ? y : y - ((double) this.outlineWidth.get().intValue()), ((Math.max(this.prevX + this.prevTextLength, x + textLength) + 2.0d) + ((double) this.outlineWidth.get().intValue())) - (Math.min(this.prevX + this.prevTextLength, x + textLength) + 2.0d), this.outlineWidth.get().intValue(), this.prevColor, this.prevColor, color, color);
            }
        }
        this.prevTextLength = textLength;
        this.prevColor = color;
    }

    private double getModuleWidth(HudRenderer renderer, Module module) {
        String info;
        double width = renderer.textWidth(module.title, this.shadow.get().booleanValue(), getScale());
        if (this.activeInfo.get().booleanValue() && (info = module.getInfoString()) != null) {
            width += renderer.textWidth(" ", this.shadow.get().booleanValue(), getScale()) + renderer.textWidth(info, this.shadow.get().booleanValue(), getScale());
        }
        return width;
    }

    private double getScale() {
        if (this.customScale.get().booleanValue()) {
            return this.scale.get().doubleValue();
        }
        return -1.0d;
    }
}
