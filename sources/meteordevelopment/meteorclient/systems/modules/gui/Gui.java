package meteordevelopment.meteorclient.systems.modules.gui;

import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/gui/Gui.class */
public class Gui extends Module {
    private final SettingGroup sgScale;
    private final SettingGroup sgAppearance;
    private final SettingGroup sgAnimation;
    private final SettingGroup sgTheme;
    public final Setting<Double> scale;
    public final Setting<Double> textScale;
    public final Setting<Font> font;
    public final Setting<Boolean> blur;
    public final Setting<Boolean> textShadow;
    public final Setting<Boolean> animation;
    public final Setting<AnimDirection> animDirection;
    public final Setting<SettingColor> animColor;
    public final Setting<SettingColor> primary;
    public final Setting<SettingColor> secondary;
    public final Setting<SettingColor> textColor;
    public final Setting<SettingColor> textSecondary;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/gui/Gui$AnimDirection.class */
    public enum AnimDirection {
        LTR,
        RTL
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/gui/Gui$Font.class */
    public enum Font {
        Default,
        Uniform,
        Comfortaa,
        Inter,
        Lexend
    }

    public Gui() {
        super(Categories.Gui, "gui", "Controls the appearance of the new ClickGui.");
        this.sgScale = this.settings.createGroup("Scale");
        this.sgAppearance = this.settings.createGroup("Appearance");
        this.sgAnimation = this.settings.createGroup("Animation");
        this.sgTheme = this.settings.createGroup("Theme");
        this.scale = this.sgScale.add(new DoubleSetting.Builder().name("scale").description("Overall GUI box size.").defaultValue(0.8d).min(0.4d).max(2.0d).sliderMin(0.4d).sliderMax(2.0d).decimalPlaces(2).onChanged(v -> {
            FontManager.get().setGuiScale(v.floatValue());
        }).build());
        this.textScale = this.sgScale.add(new DoubleSetting.Builder().name("text-scale").description("Text size within the GUI.").defaultValue(0.5d).min(0.3d).max(1.5d).sliderMin(0.3d).sliderMax(1.5d).decimalPlaces(2).onChanged(v2 -> {
            FontManager.get().setScale(v2.floatValue());
        }).build());
        this.font = this.sgAppearance.add(new EnumSetting.Builder().name("font").description("Font used by the GUI.").defaultValue(Font.Default).onChanged(v3 -> {
            FontManager.get().setFont(v3.name());
        }).build());
        this.blur = this.sgAppearance.add(new BoolSetting.Builder().name("blur").description("Blur the background when the GUI is open.").defaultValue(true).onChanged(v4 -> {
            FontManager.get().setBlur(v4.booleanValue());
        }).build());
        this.textShadow = this.sgAppearance.add(new BoolSetting.Builder().name("text-shadow").description("Draw a drop shadow behind text.").defaultValue(true).onChanged(v5 -> {
            FontManager.get().setTextShadow(v5.booleanValue());
        }).build());
        this.animation = this.sgAnimation.add(new BoolSetting.Builder().name("animation").description("Border trace animation on GUI open.").defaultValue(true).onChanged(v6 -> {
            FontManager.get().setAnimation(v6.booleanValue());
        }).build());
        this.animDirection = this.sgAnimation.add(new EnumSetting.Builder().name("anim-direction").description("Direction the border trace travels.").defaultValue(AnimDirection.RTL).onChanged(v7 -> {
            FontManager.get().setAnimationLTR(v7 == AnimDirection.LTR);
        }).build());
        this.animColor = this.sgAnimation.add(new ColorSetting.Builder().name("anim-color").description("Color of the border trace.").defaultValue(new SettingColor(48, 48, 48, 220)).onChanged(c -> {
            FontManager.get().setAnimationColor(packRGB(c));
        }).build());
        this.primary = this.sgTheme.add(new ColorSetting.Builder().name("primary").description("Line and border color.").defaultValue(new SettingColor(0, 0, 0, 255)).onChanged(c2 -> {
            FontManager.get().setPrimaryColor(packRGB(c2));
        }).build());
        this.secondary = this.sgTheme.add(new ColorSetting.Builder().name("secondary").description("Header fill and highlight color.").defaultValue(new SettingColor(48, 48, 48, 255)).onChanged(c3 -> {
            FontManager.get().setSecondaryColor(packRGB(c3));
        }).build());
        this.textColor = this.sgTheme.add(new ColorSetting.Builder().name("text-color").description("Primary text color (active modules, headers).").defaultValue(new SettingColor(240, 240, 250, 255)).onChanged(c4 -> {
            FontManager.get().setTextColor(packRGB(c4));
        }).build());
        this.textSecondary = this.sgTheme.add(new ColorSetting.Builder().name("text-secondary").description("Secondary text color (inactive modules).").defaultValue(new SettingColor(Opcode.F2L, Opcode.F2L, Opcode.IF_ACMPEQ, 255)).onChanged(c5 -> {
            FontManager.get().setTextSecondary(packRGB(c5));
        }).build());
        applyToFontManager();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        applyToFontManager();
    }

    public void applyToFontManager() {
        FontManager fm = FontManager.get();
        fm.setGuiScale(this.scale.get().floatValue());
        fm.setScale(this.textScale.get().floatValue());
        fm.setFont(this.font.get().name());
        fm.setBlur(this.blur.get().booleanValue());
        fm.setTextShadow(this.textShadow.get().booleanValue());
        fm.setAnimation(this.animation.get().booleanValue());
        fm.setAnimationLTR(this.animDirection.get() == AnimDirection.LTR);
        fm.setAnimationColor(packRGB(this.animColor.get()));
        fm.setPrimaryColor(packRGB(this.primary.get()));
        fm.setSecondaryColor(packRGB(this.secondary.get()));
        fm.setTextColor(packRGB(this.textColor.get()));
        fm.setTextSecondary(packRGB(this.textSecondary.get()));
    }

    private static int packRGB(SettingColor c) {
        return ((c.a & 255) << 24) | ((c.r & 255) << 16) | ((c.g & 255) << 8) | (c.b & 255);
    }
}
