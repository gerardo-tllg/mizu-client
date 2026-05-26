package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.render.color.RainbowColors;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2520;
import org.jetbrains.annotations.NotNull;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/Settings.class */
public class Settings implements ISerializable<Settings>, Iterable<SettingGroup> {
    private SettingGroup defaultGroup;
    public final List<SettingGroup> groups = new ArrayList(1);

    public void onActivated() {
        for (SettingGroup group : this.groups) {
            for (Setting<?> setting : group) {
                setting.onActivated();
            }
        }
    }

    public Setting<?> get(String name) {
        for (SettingGroup sg : this) {
            for (Setting<?> setting : sg) {
                if (name.equalsIgnoreCase(setting.name)) {
                    return setting;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> Setting<T> get(String name, Class<T> tClass) {
        for (SettingGroup sg : this) {
            for (Setting<T> setting : sg) {
                Class<?> sClass = setting.getDefaultValue().getClass();
                if (name.equalsIgnoreCase(setting.name) && tClass.equals(sClass)) {
                    return setting;
                }
            }
        }
        return null;
    }

    public void reset() {
        for (SettingGroup group : this.groups) {
            for (Setting<?> setting : group) {
                setting.reset();
            }
        }
    }

    public SettingGroup getGroup(String name) {
        for (SettingGroup sg : this) {
            if (sg.name.equals(name)) {
                return sg;
            }
        }
        return null;
    }

    public int sizeGroups() {
        return this.groups.size();
    }

    public SettingGroup getDefaultGroup() {
        if (this.defaultGroup == null) {
            this.defaultGroup = createGroup("General");
        }
        return this.defaultGroup;
    }

    public SettingGroup createGroup(String name, boolean expanded) {
        SettingGroup group = new SettingGroup(name, expanded);
        this.groups.add(group);
        return group;
    }

    public SettingGroup createGroup(String name) {
        return createGroup(name, true);
    }

    public void registerColorSettings(Module module) {
        for (SettingGroup group : this) {
            for (Setting<?> setting : group) {
                setting.module = module;
                if (setting instanceof ColorSetting) {
                    RainbowColors.addSetting((Setting<meteordevelopment.meteorclient.utils.render.color.SettingColor>) setting);
                } else if (setting instanceof ColorListSetting) {
                    RainbowColors.addSettingList((Setting<java.util.List<meteordevelopment.meteorclient.utils.render.color.SettingColor>>) setting);
                }
            }
        }
    }

    public void unregisterColorSettings() {
        for (SettingGroup group : this) {
            for (Setting<?> setting : group) {
                if (setting instanceof ColorSetting) {
                    RainbowColors.removeSetting((Setting<meteordevelopment.meteorclient.utils.render.color.SettingColor>) setting);
                } else if (setting instanceof ColorListSetting) {
                    RainbowColors.removeSettingList((Setting<java.util.List<meteordevelopment.meteorclient.utils.render.color.SettingColor>>) setting);
                }
            }
        }
    }

    public void tick(WContainer settings, GuiTheme theme) {
        for (SettingGroup group : this.groups) {
            for (Setting<?> setting : group) {
                boolean visible = setting.isVisible();
                if (visible != setting.lastWasVisible) {
                    settings.clear();
                    settings.add(theme.settings(this)).expandX();
                }
                setting.lastWasVisible = visible;
            }
        }
    }

    @Override // java.lang.Iterable
    @NotNull
    public Iterator<SettingGroup> iterator() {
        return this.groups.iterator();
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        class_2499 groupsTag = new class_2499();
        for (SettingGroup group : this.groups) {
            if (group.wasChanged()) {
                groupsTag.add(group.toTag());
            }
        }
        if (!groupsTag.isEmpty()) {
            tag.method_10566("groups", groupsTag);
        }
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Settings fromTag(class_2487 tag) {
        reset();
        class_2499 groupsTag = tag.method_68569("groups");
        for (Object t : groupsTag) {
            class_2487 groupTag = (class_2487) t;
            SettingGroup sg = getGroup(groupTag.method_68564("name", ""));
            if (sg != null) {
                sg.fromTag(groupTag);
            }
        }
        return this;
    }
}
