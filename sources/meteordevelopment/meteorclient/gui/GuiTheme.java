package meteordevelopment.meteorclient.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.screens.ModuleScreen;
import meteordevelopment.meteorclient.gui.screens.ModulesScreen;
import meteordevelopment.meteorclient.gui.screens.NotebotSongsScreen;
import meteordevelopment.meteorclient.gui.screens.ProxiesScreen;
import meteordevelopment.meteorclient.gui.screens.accounts.AccountsScreen;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.utils.WindowConfig;
import meteordevelopment.meteorclient.gui.widgets.WAccount;
import meteordevelopment.meteorclient.gui.widgets.WHorizontalSeparator;
import meteordevelopment.meteorclient.gui.widgets.WItem;
import meteordevelopment.meteorclient.gui.widgets.WItemWithLabel;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WQuad;
import meteordevelopment.meteorclient.gui.widgets.WTexture;
import meteordevelopment.meteorclient.gui.widgets.WTooltip;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.gui.widgets.WVerticalSeparator;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WBlockPosEdit;
import meteordevelopment.meteorclient.gui.widgets.input.WDoubleEdit;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.input.WIntEdit;
import meteordevelopment.meteorclient.gui.widgets.input.WSlider;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/GuiTheme.class */
public abstract class GuiTheme implements ISerializable<GuiTheme> {
    public static final double TITLE_TEXT_SCALE = 1.25d;
    public final String name;
    public boolean disableHoverColor;
    protected SettingsWidgetFactory settingsFactory;
    public final Settings settings = new Settings();
    protected final Map<String, WindowConfig> windowConfigs = new HashMap();

    public abstract WWindow window(WWidget wWidget, String str);

    public abstract WLabel label(String str, boolean z, double d);

    public abstract WHorizontalSeparator horizontalSeparator(String str);

    public abstract WVerticalSeparator verticalSeparator();

    protected abstract WButton button(String str, GuiTexture guiTexture);

    public abstract WMinus minus();

    public abstract WPlus plus();

    public abstract WCheckbox checkbox(boolean z);

    public abstract WSlider slider(double d, double d2, double d3);

    public abstract WTextBox textBox(String str, String str2, CharFilter charFilter, Class<? extends WTextBox.Renderer> cls);

    public abstract <T> WDropdown<T> dropdown(T[] tArr, T t);

    public abstract WTriangle triangle();

    public abstract WTooltip tooltip(String str);

    public abstract WView view();

    public abstract WSection section(String str, boolean z, WWidget wWidget);

    public abstract WAccount account(WidgetScreen widgetScreen, Account<?> account);

    public abstract WWidget module(Module module);

    public abstract WQuad quad(Color color);

    public abstract WTopBar topBar();

    public abstract WFavorite favorite(boolean z);

    public abstract Color textColor();

    public abstract Color textSecondaryColor();

    public abstract Color starscriptTextColor();

    public abstract Color starscriptBraceColor();

    public abstract Color starscriptParenthesisColor();

    public abstract Color starscriptDotColor();

    public abstract Color starscriptCommaColor();

    public abstract Color starscriptOperatorColor();

    public abstract Color starscriptStringColor();

    public abstract Color starscriptNumberColor();

    public abstract Color starscriptKeywordColor();

    public abstract Color starscriptAccessedObjectColor();

    public abstract TextRenderer textRenderer();

    public abstract double scale(double d);

    public abstract boolean categoryIcons();

    public abstract boolean hideHUD();

    public GuiTheme(String name) {
        this.name = name;
    }

    public void beforeRender() {
        this.disableHoverColor = false;
    }

    public WWindow window(String title) {
        return window(null, title);
    }

    public WLabel label(String text, boolean title) {
        return label(text, title, 0.0d);
    }

    public WLabel label(String text, double maxWidth) {
        return label(text, false, maxWidth);
    }

    public WLabel label(String text) {
        return label(text, false);
    }

    public WHorizontalSeparator horizontalSeparator() {
        return horizontalSeparator(null);
    }

    public WButton button(String text) {
        return button(text, null);
    }

    public WButton button(GuiTexture texture) {
        return button(null, texture);
    }

    public WTextBox textBox(String text, CharFilter filter, Class<? extends WTextBox.Renderer> renderer) {
        return textBox(text, null, filter, renderer);
    }

    public WTextBox textBox(String text, String placeholder, CharFilter filter) {
        return textBox(text, placeholder, filter, null);
    }

    public WTextBox textBox(String text, CharFilter filter) {
        return textBox(text, filter, (Class<? extends WTextBox.Renderer>) null);
    }

    public WTextBox textBox(String text, String placeholder) {
        return textBox(text, placeholder, (text1, c) -> {
            return true;
        }, null);
    }

    public WTextBox textBox(String text) {
        return textBox(text, (text1, c) -> {
            return true;
        }, (Class<? extends WTextBox.Renderer>) null);
    }

    public <T extends Enum<?>> WDropdown<T> dropdown(T value) {
        Class<?> klass = value.getDeclaringClass();
        return dropdown((Enum[]) klass.getEnumConstants(), value);
    }

    public WVerticalList verticalList() {
        return (WVerticalList) w(new WVerticalList());
    }

    public WHorizontalList horizontalList() {
        return (WHorizontalList) w(new WHorizontalList());
    }

    public WTable table() {
        return (WTable) w(new WTable());
    }

    public WSection section(String title, boolean expanded) {
        return section(title, expanded, null);
    }

    public WSection section(String title) {
        return section(title, true);
    }

    public WItem item(class_1799 itemStack) {
        return (WItem) w(new WItem(itemStack));
    }

    public WItemWithLabel itemWithLabel(class_1799 stack, String name) {
        return (WItemWithLabel) w(new WItemWithLabel(stack, name));
    }

    public WItemWithLabel itemWithLabel(class_1799 stack) {
        return itemWithLabel(stack, Names.get(stack.method_7909()));
    }

    public WTexture texture(double width, double height, double rotation, Texture texture) {
        return (WTexture) w(new WTexture(width, height, rotation, texture));
    }

    public WIntEdit intEdit(int value, int min, int max, int sliderMin, int sliderMax, boolean noSlider) {
        return (WIntEdit) w(new WIntEdit(value, min, max, sliderMin, sliderMax, noSlider));
    }

    public WIntEdit intEdit(int value, int min, int max, int sliderMin, int sliderMax) {
        return (WIntEdit) w(new WIntEdit(value, min, max, sliderMin, sliderMax, false));
    }

    public WIntEdit intEdit(int value, int min, int max, boolean noSlider) {
        return (WIntEdit) w(new WIntEdit(value, min, max, 0, 0, noSlider));
    }

    public WDoubleEdit doubleEdit(double value, double min, double max, double sliderMin, double sliderMax, int decimalPlaces, boolean noSlider) {
        return (WDoubleEdit) w(new WDoubleEdit(value, min, max, sliderMin, sliderMax, decimalPlaces, noSlider));
    }

    public WDoubleEdit doubleEdit(double value, double min, double max, double sliderMin, double sliderMax) {
        return (WDoubleEdit) w(new WDoubleEdit(value, min, max, sliderMin, sliderMax, 3, false));
    }

    public WDoubleEdit doubleEdit(double value, double min, double max) {
        return (WDoubleEdit) w(new WDoubleEdit(value, min, max, 0.0d, 10.0d, 3, false));
    }

    public WBlockPosEdit blockPosEdit(class_2338 value) {
        return (WBlockPosEdit) w(new WBlockPosEdit(value));
    }

    public WKeybind keybind(Keybind keybind) {
        return keybind(keybind, Keybind.none());
    }

    public WKeybind keybind(Keybind keybind, Keybind defaultValue) {
        return (WKeybind) w(new WKeybind(keybind, defaultValue));
    }

    public WWidget settings(Settings settings, String filter) {
        return this.settingsFactory.create(this, settings, filter);
    }

    public WWidget settings(Settings settings) {
        return settings(settings, "");
    }

    public TabScreen modulesScreen() {
        return new ModulesScreen(this);
    }

    public boolean isModulesScreen(class_437 screen) {
        return screen instanceof ModulesScreen;
    }

    public WidgetScreen moduleScreen(Module module) {
        return new ModuleScreen(this, module);
    }

    public WidgetScreen accountsScreen() {
        return new AccountsScreen(this);
    }

    public NotebotSongsScreen notebotSongs() {
        return new NotebotSongsScreen(this);
    }

    public WidgetScreen proxiesScreen() {
        return new ProxiesScreen(this);
    }

    public double textWidth(String text, int length, boolean title) {
        return scale(textRenderer().getWidth(text, length, false) * (title ? 1.25d : 1.0d));
    }

    public double textWidth(String text) {
        return textWidth(text, text.length(), false);
    }

    public double textHeight(boolean title) {
        return scale(textRenderer().getHeight() * (title ? 1.25d : 1.0d));
    }

    public double textHeight() {
        return textHeight(false);
    }

    public double pad() {
        return scale(6.0d);
    }

    public WindowConfig getWindowConfig(String id) {
        WindowConfig config = this.windowConfigs.get(id);
        if (config != null) {
            return config;
        }
        WindowConfig config2 = new WindowConfig();
        this.windowConfigs.put(id, config2);
        return config2;
    }

    public void clearWindowConfigs() {
        this.windowConfigs.clear();
    }

    protected <T extends WWidget> T w(T widget) {
        widget.theme = this;
        return widget;
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10582("name", this.name);
        tag.method_10566("settings", this.settings.toTag());
        class_2487 configs = new class_2487();
        for (String id : this.windowConfigs.keySet()) {
            configs.method_10566(id, this.windowConfigs.get(id).toTag());
        }
        tag.method_10566("windowConfigs", configs);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public GuiTheme fromTag2(class_2487 tag) {
        Optional optionalMethod_10562 = tag.method_10562("settings");
        Settings settings = this.settings;
        Objects.requireNonNull(settings);
        optionalMethod_10562.ifPresent(settings::fromTag2);
        tag.method_10562("windowConfigs").ifPresent(configs -> {
            for (String id : configs.method_10541()) {
                this.windowConfigs.put(id, new WindowConfig().fromTag2((class_2487) configs.method_10562(id).get()));
            }
        });
        return this;
    }
}
