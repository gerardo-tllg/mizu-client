package meteordevelopment.meteorclient.utils.render.color;

import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.misc.UnorderedArrayList;
import meteordevelopment.orbit.EventHandler;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/color/RainbowColors.class */
public class RainbowColors {
    private static final List<Setting<SettingColor>> colorSettings = new UnorderedArrayList();
    private static final List<Setting<List<SettingColor>>> colorListSettings = new UnorderedArrayList();
    private static final List<SettingColor> colors = new UnorderedArrayList();
    private static final List<Runnable> listeners = new UnorderedArrayList();
    public static final RainbowColor GLOBAL = new RainbowColor();

    private RainbowColors() {
    }

    @PostInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(RainbowColors.class);
    }

    public static void addSetting(Setting<SettingColor> setting) {
        colorSettings.add(setting);
    }

    public static void addSettingList(Setting<List<SettingColor>> setting) {
        colorListSettings.add(setting);
    }

    public static void removeSetting(Setting<SettingColor> setting) {
        colorSettings.remove(setting);
    }

    public static void removeSettingList(Setting<List<SettingColor>> setting) {
        colorListSettings.remove(setting);
    }

    public static void add(SettingColor color) {
        colors.add(color);
    }

    public static void register(Runnable runnable) {
        listeners.add(runnable);
    }

    @EventHandler
    private static void onTick(TickEvent.Post event) {
        GLOBAL.setSpeed(Config.get().rainbowSpeed.get().doubleValue() / 100.0d);
        GLOBAL.getNext();
        for (Setting<SettingColor> setting : colorSettings) {
            if (setting.module == null || setting.module.isActive()) {
                setting.get().update();
            }
        }
        for (Setting<List<SettingColor>> setting2 : colorListSettings) {
            if (setting2.module == null || setting2.module.isActive()) {
                for (SettingColor color : setting2.get()) {
                    color.update();
                }
            }
        }
        for (SettingColor color2 : colors) {
            color2.update();
        }
        for (Waypoint waypoint : Waypoints.get()) {
            waypoint.color.get().update();
        }
        if (MeteorClient.mc.field_1755 instanceof WidgetScreen) {
            for (SettingGroup group : GuiThemes.get().settings) {
                for (Setting<?> setting3 : group) {
                    if (setting3 instanceof ColorSetting) {
                        ((SettingColor) setting3.get()).update();
                    }
                }
            }
        }
        for (Runnable listener : listeners) {
            listener.run();
        }
    }
}
