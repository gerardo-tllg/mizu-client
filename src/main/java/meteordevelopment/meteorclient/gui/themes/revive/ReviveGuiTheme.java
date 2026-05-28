/*
 * ReviveClient - Revive GUI Theme
 * A sleek, modern dark theme with blue accent colors.
 */

package meteordevelopment.meteorclient.gui.themes.revive;

import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.themes.revive.widgets.*;
import meteordevelopment.meteorclient.gui.themes.revive.widgets.WReviveMultiLabel;
import meteordevelopment.meteorclient.gui.themes.revive.widgets.input.WReviveDropdown;
import meteordevelopment.meteorclient.gui.themes.revive.widgets.input.WReviveSlider;
import meteordevelopment.meteorclient.gui.themes.revive.widgets.input.WReviveTextBox;
import meteordevelopment.meteorclient.gui.themes.revive.widgets.pressable.*;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.widgets.*;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.input.WSlider;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.*;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static net.minecraft.client.MinecraftClient.IS_SYSTEM_MAC;

public class ReviveGuiTheme extends GuiTheme {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");
    private final SettingGroup sgTextColors = settings.createGroup("Text");
    private final SettingGroup sgBackgroundColors = settings.createGroup("Background");
    private final SettingGroup sgOutline = settings.createGroup("Outline");
    private final SettingGroup sgSeparator = settings.createGroup("Separator");
    private final SettingGroup sgScrollbar = settings.createGroup("Scrollbar");
    private final SettingGroup sgSlider = settings.createGroup("Slider");
    private final SettingGroup sgStarscript = settings.createGroup("Starscript");

    // General
    public final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Scale of the GUI.")
        .defaultValue(1)
        .min(0.75)
        .sliderRange(0.75, 4)
        .onSliderRelease()
        .onChanged(v -> { if (mc.currentScreen instanceof WidgetScreen) ((WidgetScreen) mc.currentScreen).invalidate(); })
        .build()
    );

    public final Setting<AlignmentX> moduleAlignment = sgGeneral.add(new EnumSetting.Builder<AlignmentX>()
        .name("module-alignment")
        .description("How module titles are aligned.")
        .defaultValue(AlignmentX.Center)
        .build()
    );

    public final Setting<Boolean> categoryIcons = sgGeneral.add(new BoolSetting.Builder()
        .name("category-icons")
        .description("Adds item icons to module categories.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> hideHUD = sgGeneral.add(new BoolSetting.Builder()
        .name("hide-HUD")
        .description("Hide HUD when in GUI.")
        .defaultValue(false)
        .onChanged(v -> { if (mc.currentScreen instanceof WidgetScreen) mc.options.hudHidden = v; })
        .build()
    );

    // Colors — sleek blue accent palette
    public final Setting<SettingColor> accentColor     = color("accent",   "Main accent color.",          new SettingColor(0, 150, 255));
    public final Setting<SettingColor> checkboxColor   = color("checkbox", "Color of checkbox.",          new SettingColor(0, 150, 255));
    public final Setting<SettingColor> plusColor       = color("plus",     "Color of plus button.",       new SettingColor(40, 220, 120));
    public final Setting<SettingColor> minusColor      = color("minus",    "Color of minus button.",      new SettingColor(220, 60, 60));
    public final Setting<SettingColor> favoriteColor   = color("favorite", "Color of favorite button.",   new SettingColor(255, 200, 0));

    // Text
    public final Setting<SettingColor> textColor          = color(sgTextColors, "text",            "Color of text.",           new SettingColor(220, 220, 220));
    public final Setting<SettingColor> textSecondaryColor = color(sgTextColors, "text-secondary",  "Color of secondary text.", new SettingColor(120, 120, 130));
    public final Setting<SettingColor> textHighlightColor = color(sgTextColors, "text-highlight",  "Color of highlight.",      new SettingColor(0, 130, 255, 80));
    public final Setting<SettingColor> titleTextColor     = color(sgTextColors, "title-text",      "Color of title text.",     new SettingColor(255, 255, 255));
    public final Setting<SettingColor> loggedInColor      = color(sgTextColors, "logged-in-text",  "Logged-in account name.",  new SettingColor(40, 220, 120));
    public final Setting<SettingColor> placeholderColor   = color(sgTextColors, "placeholder",     "Placeholder text color.",  new SettingColor(255, 255, 255, 25));

    // Background — very dark near-black with slight blue tint
    public final ThreeStateColorSetting backgroundColor = new ThreeStateColorSetting(
        sgBackgroundColors, "background",
        new SettingColor(12, 12, 18, 230),
        new SettingColor(20, 20, 30, 230),
        new SettingColor(28, 28, 42, 230)
    );

    public final Setting<SettingColor> moduleBackground = color(sgBackgroundColors, "module-background", "Module bg when active.", new SettingColor(0, 100, 200, 60));

    // Outline — subtle blue-tinted outline
    public final ThreeStateColorSetting outlineColor = new ThreeStateColorSetting(
        sgOutline, "outline",
        new SettingColor(35, 35, 55, 200),
        new SettingColor(0, 130, 255, 160),
        new SettingColor(0, 160, 255, 220)
    );

    // Separator
    public final Setting<SettingColor> separatorText  = color(sgSeparator, "separator-text",  "Separator text.",  new SettingColor(180, 180, 200));
    public final Setting<SettingColor> separatorCenter= color(sgSeparator, "separator-center","Separator line.",  new SettingColor(0, 130, 255, 200));
    public final Setting<SettingColor> separatorEdges = color(sgSeparator, "separator-edges", "Separator edges.", new SettingColor(0, 80, 160, 80));

    // Scrollbar
    public final ThreeStateColorSetting scrollbarColor = new ThreeStateColorSetting(
        sgScrollbar, "scrollbar",
        new SettingColor(0, 100, 200, 160),
        new SettingColor(0, 130, 255, 200),
        new SettingColor(0, 160, 255, 240)
    );

    // Slider
    public final ThreeStateColorSetting sliderHandle = new ThreeStateColorSetting(
        sgSlider, "slider-handle",
        new SettingColor(0, 150, 255),
        new SettingColor(60, 180, 255),
        new SettingColor(100, 210, 255)
    );
    public final Setting<SettingColor> sliderLeft  = color(sgSlider, "slider-left",  "Filled part of slider.",  new SettingColor(0, 130, 255, 200));
    public final Setting<SettingColor> sliderRight = color(sgSlider, "slider-right", "Unfilled part of slider.", new SettingColor(35, 35, 55, 180));

    // Starscript
    public final Setting<SettingColor> starscriptText            = color(sgStarscript, "starscript-text",             "Text.",             new SettingColor(220, 220, 220));
    public final Setting<SettingColor> starscriptBraces          = color(sgStarscript, "starscript-braces",           "Braces.",           new SettingColor(0, 150, 255));
    public final Setting<SettingColor> starscriptParenthesis     = color(sgStarscript, "starscript-parenthesis",      "Parenthesis.",      new SettingColor(0, 150, 255));
    public final Setting<SettingColor> starscriptDots            = color(sgStarscript, "starscript-dots",             "Dots.",             new SettingColor(180, 180, 200));
    public final Setting<SettingColor> starscriptCommas          = color(sgStarscript, "starscript-commas",           "Commas.",           new SettingColor(180, 180, 200));
    public final Setting<SettingColor> starscriptOperators       = color(sgStarscript, "starscript-operators",        "Operators.",        new SettingColor(0, 200, 255));
    public final Setting<SettingColor> starscriptStrings         = color(sgStarscript, "starscript-strings",          "Strings.",          new SettingColor(100, 220, 100));
    public final Setting<SettingColor> starscriptNumbers         = color(sgStarscript, "starscript-numbers",          "Numbers.",          new SettingColor(255, 160, 60));
    public final Setting<SettingColor> starscriptKeywords        = color(sgStarscript, "starscript-keywords",         "Keywords.",         new SettingColor(0, 150, 255));
    public final Setting<SettingColor> starscriptAccessedObjects = color(sgStarscript, "starscript-accessed-objects", "Accessed objects.", new SettingColor(0, 200, 255));

    public ReviveGuiTheme() {
        super("Revive");

        settingsFactory = new DefaultSettingsWidgetFactory(this);
    }

    // ── Color helper methods ──────────────────────────────────────────────────

    private Setting<SettingColor> color(SettingGroup group, String name, String description, SettingColor color) {
        return group.add(new ColorSetting.Builder()
            .name(name + "-color")
            .description(description)
            .defaultValue(color)
            .build());
    }

    private Setting<SettingColor> color(String name, String description, SettingColor color) {
        return color(sgColors, name, description, color);
    }

    // ── Widget factories ──────────────────────────────────────────────────────

    @Override
    public WWindow window(WWidget icon, String title) {
        return w(new WReviveWindow(icon, title));
    }

    @Override
    public WLabel label(String text, boolean title, double maxWidth) {
        if (maxWidth == 0) return w(new WReviveLabel(text, title));
        return w(new WReviveMultiLabel(text, title, maxWidth));
    }

    @Override
    public WHorizontalSeparator horizontalSeparator(String text) {
        return w(new WReviveHorizontalSeparator(text));
    }

    @Override
    public WVerticalSeparator verticalSeparator() {
        return w(new WReviveVerticalSeparator());
    }

    @Override
    protected WButton button(String text, GuiTexture texture) {
        return w(new WReviveButton(text, texture));
    }

    @Override
    public WMinus minus() { return w(new WReviveMinus()); }

    @Override
    public WPlus plus() { return w(new WRevivePlus()); }

    @Override
    public WCheckbox checkbox(boolean checked) { return w(new WReviveCheckbox(checked)); }

    @Override
    public WSlider slider(double value, double min, double max) { return w(new WReviveSlider(value, min, max)); }

    @Override
    public WTextBox textBox(String text, String placeholder, CharFilter filter, Class<? extends WTextBox.Renderer> renderer) {
        return w(new WReviveTextBox(text, placeholder, filter, renderer));
    }

    @Override
    public <T> WDropdown<T> dropdown(T[] values, T value) { return w(new WReviveDropdown<>(values, value)); }

    @Override
    public WTriangle triangle() { return w(new WReviveTriangle()); }

    @Override
    public WTooltip tooltip(String text) { return w(new WReviveTooltip(text)); }

    @Override
    public WView view() { return w(new WReviveView()); }

    @Override
    public WSection section(String title, boolean expanded, WWidget headerWidget) {
        return w(new WReviveSection(title, expanded, headerWidget));
    }

    @Override
    public WAccount account(WidgetScreen screen, Account<?> account) {
        return w(new WReviveAccount(screen, account));
    }

    @Override
    public WWidget module(Module module) { return w(new WReviveModule(module)); }

    @Override
    public WQuad quad(Color color) { return w(new WReviveQuad(color)); }

    @Override
    public WTopBar topBar() { return w(new WReviveTopBar()); }

    @Override
    public WFavorite favorite(boolean checked) { return w(new WReviveFavorite(checked)); }

    // ── Color accessors ───────────────────────────────────────────────────────

    @Override public Color textColor()               { return textColor.get(); }
    @Override public Color textSecondaryColor()      { return textSecondaryColor.get(); }
    @Override public Color starscriptTextColor()     { return starscriptText.get(); }
    @Override public Color starscriptBraceColor()    { return starscriptBraces.get(); }
    @Override public Color starscriptParenthesisColor() { return starscriptParenthesis.get(); }
    @Override public Color starscriptDotColor()      { return starscriptDots.get(); }
    @Override public Color starscriptCommaColor()    { return starscriptCommas.get(); }
    @Override public Color starscriptOperatorColor() { return starscriptOperators.get(); }
    @Override public Color starscriptStringColor()   { return starscriptStrings.get(); }
    @Override public Color starscriptNumberColor()   { return starscriptNumbers.get(); }
    @Override public Color starscriptKeywordColor()  { return starscriptKeywords.get(); }
    @Override public Color starscriptAccessedObjectColor() { return starscriptAccessedObjects.get(); }

    @Override
    public TextRenderer textRenderer() { return TextRenderer.get(); }

    @Override
    public double scale(double value) {
        double scaled = value * scale.get();
        if (IS_SYSTEM_MAC) {
            scaled /= (double) mc.getWindow().getWidth() / mc.getWindow().getFramebufferWidth();
        }
        return scaled;
    }

    @Override public boolean categoryIcons() { return categoryIcons.get(); }
    @Override public boolean hideHUD()       { return hideHUD.get(); }

    // ── ThreeStateColorSetting helper ─────────────────────────────────────────

    public class ThreeStateColorSetting {
        private final Setting<SettingColor> normal, hovered, pressed;

        public ThreeStateColorSetting(SettingGroup group, String name, SettingColor c1, SettingColor c2, SettingColor c3) {
            normal  = color(group, name,             "Color of " + name + ".",               c1);
            hovered = color(group, "hovered-" + name,"Color of " + name + " when hovered.",  c2);
            pressed = color(group, "pressed-" + name,"Color of " + name + " when pressed.",  c3);
        }

        public SettingColor get() { return normal.get(); }

        public SettingColor get(boolean pressed, boolean hovered, boolean bypassDisableHoverColor) {
            if (pressed) return this.pressed.get();
            return (hovered && (bypassDisableHoverColor || !disableHoverColor)) ? this.hovered.get() : this.normal.get();
        }

        public SettingColor get(boolean pressed, boolean hovered) {
            return get(pressed, hovered, false);
        }
    }
}
