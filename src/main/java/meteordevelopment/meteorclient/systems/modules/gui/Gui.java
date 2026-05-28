/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.gui;

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

/**
 * Drives the new ClickGui's runtime appearance. Every setting change here
 * is forwarded to {@link FontManager} immediately, and settings persist
 * through Meteor's own NBT save/load flow.
 *
 * Locked on: this module's {@code setActive} isn't really meaningful — it
 * exists as a module so its settings get the standard Meteor save path,
 * and so users can find it where they expect (in the Gui category of the
 * clickgui itself).
 */
public class Gui extends Module {
    public enum Font {
        Default, Uniform, Comfortaa, Inter, Lexend
    }

    public enum AnimDirection {
        LTR, RTL
    }

    private final SettingGroup sgScale = settings.createGroup("Scale");
    private final SettingGroup sgAppearance = settings.createGroup("Appearance");
    private final SettingGroup sgAnimation = settings.createGroup("Animation");
    private final SettingGroup sgTheme = settings.createGroup("Theme");

    // --- Scale group ---
    public final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Overall GUI box size.")
        .defaultValue(0.8)
        .min(0.4).max(2.0)
        .sliderMin(0.4).sliderMax(2.0)
        .decimalPlaces(2)
        .onChanged(v -> FontManager.get().setGuiScale(v.floatValue()))
        .build()
    );

    public final Setting<Double> textScale = sgScale.add(new DoubleSetting.Builder()
        .name("text-scale")
        .description("Text size within the GUI.")
        .defaultValue(0.5)
        .min(0.3).max(1.5)
        .sliderMin(0.3).sliderMax(1.5)
        .decimalPlaces(2)
        .onChanged(v -> FontManager.get().setScale(v.floatValue()))
        .build()
    );

    // --- Appearance group ---
    public final Setting<Font> font = sgAppearance.add(new EnumSetting.Builder<Font>()
        .name("font")
        .description("Font used by the GUI.")
        .defaultValue(Font.Default)
        .onChanged(v -> FontManager.get().setFont(v.name()))
        .build()
    );

    public final Setting<Boolean> blur = sgAppearance.add(new BoolSetting.Builder()
        .name("blur")
        .description("Blur the background when the GUI is open.")
        .defaultValue(true)
        .onChanged(v -> FontManager.get().setBlur(v))
        .build()
    );

    public final Setting<Boolean> textShadow = sgAppearance.add(new BoolSetting.Builder()
        .name("text-shadow")
        .description("Draw a drop shadow behind text.")
        .defaultValue(true)
        .onChanged(v -> FontManager.get().setTextShadow(v))
        .build()
    );

    // --- Animation group ---
    public final Setting<Boolean> animation = sgAnimation.add(new BoolSetting.Builder()
        .name("animation")
        .description("Border trace animation on GUI open.")
        .defaultValue(true)
        .onChanged(v -> FontManager.get().setAnimation(v))
        .build()
    );

    public final Setting<AnimDirection> animDirection = sgAnimation.add(new EnumSetting.Builder<AnimDirection>()
        .name("anim-direction")
        .description("Direction the border trace travels.")
        .defaultValue(AnimDirection.RTL)
        .onChanged(v -> FontManager.get().setAnimationLTR(v == AnimDirection.LTR))
        .build()
    );

    public final Setting<SettingColor> animColor = sgAnimation.add(new ColorSetting.Builder()
        .name("anim-color")
        .description("Color of the border trace.")
        .defaultValue(new SettingColor(48, 48, 48, 220))
        .onChanged(c -> FontManager.get().setAnimationColor(packRGB(c)))
        .build()
    );

    // --- Theme group ---
    public final Setting<SettingColor> primary = sgTheme.add(new ColorSetting.Builder()
        .name("primary")
        .description("Line and border color.")
        .defaultValue(new SettingColor(10, 30, 48, 255))    // #0a1e30 deep navy
        .onChanged(c -> FontManager.get().setPrimaryColor(packRGB(c)))
        .build()
    );

    public final Setting<SettingColor> secondary = sgTheme.add(new ColorSetting.Builder()
        .name("secondary")
        .description("Header fill and highlight color.")
        .defaultValue(new SettingColor(29, 158, 117, 255))  // #1D9E75 teal
        .onChanged(c -> FontManager.get().setSecondaryColor(packRGB(c)))
        .build()
    );

    public final Setting<SettingColor> textColor = sgTheme.add(new ColorSetting.Builder()
        .name("text-color")
        .description("Primary text color (active modules, headers).")
        .defaultValue(new SettingColor(240, 240, 250, 255)) // near-white
        .onChanged(c -> FontManager.get().setTextColor(packRGB(c)))
        .build()
    );

    public final Setting<SettingColor> textSecondary = sgTheme.add(new ColorSetting.Builder()
        .name("text-secondary")
        .description("Secondary text color (inactive modules).")
        .defaultValue(new SettingColor(24, 95, 165, 255))   // #185FA5 muted blue
        .onChanged(c -> FontManager.get().setTextSecondary(packRGB(c)))
        .build()
    );

    public Gui() {
        super(Categories.Gui, "gui", "Controls the appearance of the new ClickGui.");

        // Push defaults into FontManager immediately so even before the user
        // opens this module, the runtime reflects our defaults.
        applyToFontManager();
    }

    @Override
    public void onActivate() {
        // Also push when toggled on, in case FontManager was reset
        applyToFontManager();
    }

    /** Push every current setting value to FontManager. Safe to call anytime. */
    public void applyToFontManager() {
        FontManager fm = FontManager.get();
        fm.setGuiScale(scale.get().floatValue());
        fm.setScale(textScale.get().floatValue());
        fm.setFont(font.get().name());
        fm.setBlur(blur.get());
        fm.setTextShadow(textShadow.get());
        fm.setAnimation(animation.get());
        fm.setAnimationLTR(animDirection.get() == AnimDirection.LTR);
        fm.setAnimationColor(packRGB(animColor.get()));
        fm.setPrimaryColor(packRGB(primary.get()));
        fm.setSecondaryColor(packRGB(secondary.get()));
        fm.setTextColor(packRGB(textColor.get()));
        fm.setTextSecondary(packRGB(textSecondary.get()));
    }

    /** Pack SettingColor's a/r/g/b into a 0xAARRGGBB int. */
    private static int packRGB(SettingColor c) {
        return ((c.a & 0xFF) << 24) | ((c.r & 0xFF) << 16) | ((c.g & 0xFF) << 8) | (c.b & 0xFF);
    }
}
