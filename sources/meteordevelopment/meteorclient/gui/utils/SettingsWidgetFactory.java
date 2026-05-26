package meteordevelopment.meteorclient.gui.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Settings;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/utils/SettingsWidgetFactory.class */
public abstract class SettingsWidgetFactory {
    private static final Map<Class<?>, Function<GuiTheme, Factory>> customFactories = new HashMap();
    protected final GuiTheme theme;
    protected final Map<Class<?>, Factory> factories = new HashMap();

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/utils/SettingsWidgetFactory$Factory.class */
    @FunctionalInterface
    public interface Factory {
        void create(WTable wTable, Setting<?> setting);
    }

    public abstract WWidget create(GuiTheme guiTheme, Settings settings, String str);

    public SettingsWidgetFactory(GuiTheme theme) {
        this.theme = theme;
    }

    public static void registerCustomFactory(Class<?> settingClass, Function<GuiTheme, Factory> factoryFunction) {
        customFactories.put(settingClass, factoryFunction);
    }

    public static void unregisterCustomFactory(Class<?> settingClass) {
        customFactories.remove(settingClass);
    }

    protected Factory getFactory(Class<?> settingClass) {
        return customFactories.containsKey(settingClass) ? customFactories.get(settingClass).apply(this.theme) : this.factories.get(settingClass);
    }
}
