package meteordevelopment.meteorclient.gui.themes.mizu;

import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.WMizuAccount;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.WMizuHorizontalSeparator;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.WMizuLabel;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.WMizuModule;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.WMizuMultiLabel;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.WMizuQuad;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.WMizuSection;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.WMizuTooltip;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.WMizuTopBar;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.WMizuVerticalSeparator;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.WMizuView;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.WMizuWindow;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.input.WMizuDropdown;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.input.WMizuSlider;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.input.WMizuTextBox;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.pressable.WMizuButton;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.pressable.WMizuCheckbox;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.pressable.WMizuFavorite;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.pressable.WMizuMinus;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.pressable.WMizuPlus;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.pressable.WMizuTriangle;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.widgets.WAccount;
import meteordevelopment.meteorclient.gui.widgets.WHorizontalSeparator;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WQuad;
import meteordevelopment.meteorclient.gui.widgets.WTooltip;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.gui.widgets.WVerticalSeparator;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.input.WSlider;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_310;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/MizuGuiTheme.class */
public class MizuGuiTheme extends GuiTheme {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgColors;
    private final SettingGroup sgTextColors;
    private final SettingGroup sgBackgroundColors;
    private final SettingGroup sgOutline;
    private final SettingGroup sgSeparator;
    private final SettingGroup sgScrollbar;
    private final SettingGroup sgSlider;
    private final SettingGroup sgStarscript;
    public final Setting<Double> scale;
    public final Setting<AlignmentX> moduleAlignment;
    public final Setting<Boolean> categoryIcons;
    public final Setting<Boolean> hideHUD;
    public final Setting<SettingColor> accentColor;
    public final Setting<SettingColor> checkboxColor;
    public final Setting<SettingColor> plusColor;
    public final Setting<SettingColor> minusColor;
    public final Setting<SettingColor> favoriteColor;
    public final Setting<SettingColor> textColor;
    public final Setting<SettingColor> textSecondaryColor;
    public final Setting<SettingColor> textHighlightColor;
    public final Setting<SettingColor> titleTextColor;
    public final Setting<SettingColor> loggedInColor;
    public final Setting<SettingColor> placeholderColor;
    public final ThreeStateColorSetting backgroundColor;
    public final Setting<SettingColor> moduleBackground;
    public final ThreeStateColorSetting outlineColor;
    public final Setting<SettingColor> separatorText;
    public final Setting<SettingColor> separatorCenter;
    public final Setting<SettingColor> separatorEdges;
    public final ThreeStateColorSetting scrollbarColor;
    public final ThreeStateColorSetting sliderHandle;
    public final Setting<SettingColor> sliderLeft;
    public final Setting<SettingColor> sliderRight;
    public final Setting<SettingColor> starscriptText;
    public final Setting<SettingColor> starscriptBraces;
    public final Setting<SettingColor> starscriptParenthesis;
    public final Setting<SettingColor> starscriptDots;
    public final Setting<SettingColor> starscriptCommas;
    public final Setting<SettingColor> starscriptOperators;
    public final Setting<SettingColor> starscriptStrings;
    public final Setting<SettingColor> starscriptNumbers;
    public final Setting<SettingColor> starscriptKeywords;
    public final Setting<SettingColor> starscriptAccessedObjects;

    public MizuGuiTheme() {
        super("Mizu");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgColors = this.settings.createGroup("Colors");
        this.sgTextColors = this.settings.createGroup("Text");
        this.sgBackgroundColors = this.settings.createGroup("Background");
        this.sgOutline = this.settings.createGroup("Outline");
        this.sgSeparator = this.settings.createGroup("Separator");
        this.sgScrollbar = this.settings.createGroup("Scrollbar");
        this.sgSlider = this.settings.createGroup("Slider");
        this.sgStarscript = this.settings.createGroup("Starscript");
        this.scale = this.sgGeneral.add(new DoubleSetting.Builder().name("scale").description("Scale of the GUI.").defaultValue(1.0d).min(0.75d).sliderRange(0.75d, 4.0d).onSliderRelease().onChanged(v -> {
            if (MeteorClient.mc.field_1755 instanceof WidgetScreen) {
                ((WidgetScreen) MeteorClient.mc.field_1755).invalidate();
            }
        }).build());
        this.moduleAlignment = this.sgGeneral.add(new EnumSetting.Builder().name("module-alignment").description("How module titles are aligned.").defaultValue(AlignmentX.Center).build());
        this.categoryIcons = this.sgGeneral.add(new BoolSetting.Builder().name("category-icons").description("Adds item icons to module categories.").defaultValue(false).build());
        this.hideHUD = this.sgGeneral.add(new BoolSetting.Builder().name("hide-HUD").description("Hide HUD when in GUI.").defaultValue(false).onChanged(v2 -> {
            if (MeteorClient.mc.field_1755 instanceof WidgetScreen) {
                MeteorClient.mc.field_1690.field_1842 = v2.booleanValue();
            }
        }).build());
        this.accentColor = color("accent", "Main accent color.", new SettingColor(0, Opcode.FCMPG, 255));
        this.checkboxColor = color("checkbox", "Color of checkbox.", new SettingColor(0, Opcode.FCMPG, 255));
        this.plusColor = color("plus", "Color of plus button.", new SettingColor(40, 220, Opcode.ISHL));
        this.minusColor = color("minus", "Color of minus button.", new SettingColor(220, 60, 60));
        this.favoriteColor = color("favorite", "Color of favorite button.", new SettingColor(255, 200, 0));
        this.textColor = color(this.sgTextColors, "text", "Color of text.", new SettingColor(220, 220, 220));
        this.textSecondaryColor = color(this.sgTextColors, "text-secondary", "Color of secondary text.", new SettingColor(Opcode.ISHL, Opcode.ISHL, Opcode.IXOR));
        this.textHighlightColor = color(this.sgTextColors, "text-highlight", "Color of highlight.", new SettingColor(0, Opcode.IXOR, 255, 80));
        this.titleTextColor = color(this.sgTextColors, "title-text", "Color of title text.", new SettingColor(255, 255, 255));
        this.loggedInColor = color(this.sgTextColors, "logged-in-text", "Logged-in account name.", new SettingColor(40, 220, Opcode.ISHL));
        this.placeholderColor = color(this.sgTextColors, "placeholder", "Placeholder text color.", new SettingColor(255, 255, 255, 25));
        this.backgroundColor = new ThreeStateColorSetting(this.sgBackgroundColors, "background", new SettingColor(12, 12, 18, 230), new SettingColor(20, 20, 30, 230), new SettingColor(28, 28, 42, 230));
        this.moduleBackground = color(this.sgBackgroundColors, "module-background", "Module bg when active.", new SettingColor(0, 100, 200, 60));
        this.outlineColor = new ThreeStateColorSetting(this.sgOutline, "outline", new SettingColor(35, 35, 55, 200), new SettingColor(0, Opcode.IXOR, 255, Opcode.IF_ICMPNE), new SettingColor(0, Opcode.IF_ICMPNE, 255, 220));
        this.separatorText = color(this.sgSeparator, "separator-text", "Separator text.", new SettingColor(Opcode.GETFIELD, Opcode.GETFIELD, 200));
        this.separatorCenter = color(this.sgSeparator, "separator-center", "Separator line.", new SettingColor(0, Opcode.IXOR, 255, 200));
        this.separatorEdges = color(this.sgSeparator, "separator-edges", "Separator edges.", new SettingColor(0, 80, Opcode.IF_ICMPNE, 80));
        this.scrollbarColor = new ThreeStateColorSetting(this.sgScrollbar, "scrollbar", new SettingColor(0, 100, 200, Opcode.IF_ICMPNE), new SettingColor(0, Opcode.IXOR, 255, 200), new SettingColor(0, Opcode.IF_ICMPNE, 255, 240));
        this.sliderHandle = new ThreeStateColorSetting(this.sgSlider, "slider-handle", new SettingColor(0, Opcode.FCMPG, 255), new SettingColor(60, Opcode.GETFIELD, 255), new SettingColor(100, 210, 255));
        this.sliderLeft = color(this.sgSlider, "slider-left", "Filled part of slider.", new SettingColor(0, Opcode.IXOR, 255, 200));
        this.sliderRight = color(this.sgSlider, "slider-right", "Unfilled part of slider.", new SettingColor(35, 35, 55, Opcode.GETFIELD));
        this.starscriptText = color(this.sgStarscript, "starscript-text", "Text.", new SettingColor(220, 220, 220));
        this.starscriptBraces = color(this.sgStarscript, "starscript-braces", "Braces.", new SettingColor(0, Opcode.FCMPG, 255));
        this.starscriptParenthesis = color(this.sgStarscript, "starscript-parenthesis", "Parenthesis.", new SettingColor(0, Opcode.FCMPG, 255));
        this.starscriptDots = color(this.sgStarscript, "starscript-dots", "Dots.", new SettingColor(Opcode.GETFIELD, Opcode.GETFIELD, 200));
        this.starscriptCommas = color(this.sgStarscript, "starscript-commas", "Commas.", new SettingColor(Opcode.GETFIELD, Opcode.GETFIELD, 200));
        this.starscriptOperators = color(this.sgStarscript, "starscript-operators", "Operators.", new SettingColor(0, 200, 255));
        this.starscriptStrings = color(this.sgStarscript, "starscript-strings", "Strings.", new SettingColor(100, 220, 100));
        this.starscriptNumbers = color(this.sgStarscript, "starscript-numbers", "Numbers.", new SettingColor(255, Opcode.IF_ICMPNE, 60));
        this.starscriptKeywords = color(this.sgStarscript, "starscript-keywords", "Keywords.", new SettingColor(0, Opcode.FCMPG, 255));
        this.starscriptAccessedObjects = color(this.sgStarscript, "starscript-accessed-objects", "Accessed objects.", new SettingColor(0, 200, 255));
        this.settingsFactory = new DefaultSettingsWidgetFactory(this);
    }

    private Setting<SettingColor> color(SettingGroup group, String name, String description, SettingColor color) {
        return group.add(new ColorSetting.Builder().name(name + "-color").description(description).defaultValue(color).build());
    }

    private Setting<SettingColor> color(String name, String description, SettingColor color) {
        return color(this.sgColors, name, description, color);
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WWindow window(WWidget icon, String title) {
        return (WWindow) w(new WMizuWindow(icon, title));
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WLabel label(String text, boolean title, double maxWidth) {
        return maxWidth == 0.0d ? (WLabel) w(new WMizuLabel(text, title)) : (WLabel) w(new WMizuMultiLabel(text, title, maxWidth));
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WHorizontalSeparator horizontalSeparator(String text) {
        return (WHorizontalSeparator) w(new WMizuHorizontalSeparator(text));
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WVerticalSeparator verticalSeparator() {
        return (WVerticalSeparator) w(new WMizuVerticalSeparator());
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    protected WButton button(String text, GuiTexture texture) {
        return (WButton) w(new WMizuButton(text, texture));
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WMinus minus() {
        return (WMinus) w(new WMizuMinus());
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WPlus plus() {
        return (WPlus) w(new WMizuPlus());
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WCheckbox checkbox(boolean checked) {
        return (WCheckbox) w(new WMizuCheckbox(checked));
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WSlider slider(double value, double min, double max) {
        return (WSlider) w(new WMizuSlider(value, min, max));
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WTextBox textBox(String text, String placeholder, CharFilter filter, Class<? extends WTextBox.Renderer> renderer) {
        return (WTextBox) w(new WMizuTextBox(text, placeholder, filter, renderer));
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public <T> WDropdown<T> dropdown(T[] values, T value) {
        return (WDropdown) w(new WMizuDropdown(values, value));
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WTriangle triangle() {
        return (WTriangle) w(new WMizuTriangle());
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WTooltip tooltip(String text) {
        return (WTooltip) w(new WMizuTooltip(text));
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WView view() {
        return (WView) w(new WMizuView());
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WSection section(String title, boolean expanded, WWidget headerWidget) {
        return (WSection) w(new WMizuSection(title, expanded, headerWidget));
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WAccount account(WidgetScreen screen, Account<?> account) {
        return (WAccount) w(new WMizuAccount(screen, account));
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WWidget module(Module module) {
        return w(new WMizuModule(module));
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WQuad quad(Color color) {
        return (WQuad) w(new WMizuQuad(color));
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WTopBar topBar() {
        return (WTopBar) w(new WMizuTopBar());
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public WFavorite favorite(boolean checked) {
        return (WFavorite) w(new WMizuFavorite(checked));
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public Color textColor() {
        return this.textColor.get();
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public Color textSecondaryColor() {
        return this.textSecondaryColor.get();
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public Color starscriptTextColor() {
        return this.starscriptText.get();
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public Color starscriptBraceColor() {
        return this.starscriptBraces.get();
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public Color starscriptParenthesisColor() {
        return this.starscriptParenthesis.get();
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public Color starscriptDotColor() {
        return this.starscriptDots.get();
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public Color starscriptCommaColor() {
        return this.starscriptCommas.get();
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public Color starscriptOperatorColor() {
        return this.starscriptOperators.get();
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public Color starscriptStringColor() {
        return this.starscriptStrings.get();
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public Color starscriptNumberColor() {
        return this.starscriptNumbers.get();
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public Color starscriptKeywordColor() {
        return this.starscriptKeywords.get();
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public Color starscriptAccessedObjectColor() {
        return this.starscriptAccessedObjects.get();
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public TextRenderer textRenderer() {
        return TextRenderer.get();
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public double scale(double value) {
        double scaled = value * this.scale.get().doubleValue();
        if (class_310.field_1703) {
            scaled /= ((double) MeteorClient.mc.method_22683().method_4480()) / ((double) MeteorClient.mc.method_22683().method_4489());
        }
        return scaled;
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public boolean categoryIcons() {
        return this.categoryIcons.get().booleanValue();
    }

    @Override // meteordevelopment.meteorclient.gui.GuiTheme
    public boolean hideHUD() {
        return this.hideHUD.get().booleanValue();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/MizuGuiTheme$ThreeStateColorSetting.class */
    public class ThreeStateColorSetting {
        private final Setting<SettingColor> normal;
        private final Setting<SettingColor> hovered;
        private final Setting<SettingColor> pressed;

        public ThreeStateColorSetting(SettingGroup group, String name, SettingColor c1, SettingColor c2, SettingColor c3) {
            this.normal = MizuGuiTheme.this.color(group, name, "Color of " + name + ".", c1);
            this.hovered = MizuGuiTheme.this.color(group, "hovered-" + name, "Color of " + name + " when hovered.", c2);
            this.pressed = MizuGuiTheme.this.color(group, "pressed-" + name, "Color of " + name + " when pressed.", c3);
        }

        public SettingColor get() {
            return this.normal.get();
        }

        public SettingColor get(boolean pressed, boolean hovered, boolean bypassDisableHoverColor) {
            return pressed ? this.pressed.get() : (!hovered || (!bypassDisableHoverColor && MizuGuiTheme.this.disableHoverColor)) ? this.normal.get() : this.hovered.get();
        }

        public SettingColor get(boolean pressed, boolean hovered) {
            return get(pressed, hovered, false);
        }
    }
}


