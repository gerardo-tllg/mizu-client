package meteordevelopment.meteorclient.systems.modules;

import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.AddonManager;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_124;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import org.jetbrains.annotations.NotNull;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/Module.class */
public abstract class Module implements ISerializable<Module>, Comparable<Module> {
    protected final class_310 mc;
    public final Category category;
    public final String name;
    public final String title;
    public final String description;
    public final String[] aliases;
    public final Color color;
    public final MeteorAddon addon;
    public final Settings settings;
    private boolean active;
    public boolean serialize;
    public boolean runInMainMenu;
    public boolean autoSubscribe;
    public final Keybind keybind;
    public boolean toggleOnBindRelease;
    public boolean chatFeedback;
    public boolean favorite;

    public Module(Category category, String name, String description, String... aliases) {
        this.settings = new Settings();
        this.serialize = true;
        this.runInMainMenu = false;
        this.autoSubscribe = true;
        this.keybind = Keybind.none();
        this.toggleOnBindRelease = false;
        this.chatFeedback = true;
        this.favorite = false;
        if (name.contains(" ")) {
            MeteorClient.LOG.warn("Module '{}' contains invalid characters in its name making it incompatible with Meteor Client commands.", name);
        }
        this.mc = class_310.method_1551();
        this.category = category;
        this.name = name;
        this.title = Utils.nameToTitle(name);
        this.description = description;
        this.aliases = aliases;
        this.color = Color.fromHsv(Utils.random(0.0d, 360.0d), 0.35d, 1.0d);
        String classname = getClass().getName();
        for (MeteorAddon addon : AddonManager.ADDONS) {
            if (classname.startsWith(addon.getPackage())) {
                this.addon = addon;
                return;
            }
        }
        this.addon = null;
    }

    public Module(Category category, String name, String desc) {
        this(category, name, desc, new String[0]);
    }

    public WWidget getWidget(GuiTheme theme) {
        return null;
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    public void toggle() {
        if (!this.active) {
            this.active = true;
            Modules.get().addActive(this);
            this.settings.onActivated();
            if (this.runInMainMenu || Utils.canUpdate()) {
                if (this.autoSubscribe) {
                    MeteorClient.EVENT_BUS.subscribe(this);
                }
                onActivate();
                return;
            }
            return;
        }
        if (this.runInMainMenu || Utils.canUpdate()) {
            if (this.autoSubscribe) {
                MeteorClient.EVENT_BUS.unsubscribe(this);
            }
            onDeactivate();
        }
        this.active = false;
        Modules.get().removeActive(this);
    }

    public void sendToggledMsg() {
        if (Config.get().chatFeedback.get().booleanValue() && this.chatFeedback) {
            ChatUtils.forceNextPrefixClass(getClass());
            int iHashCode = hashCode();
            class_124 class_124Var = class_124.field_1080;
            Object[] objArr = new Object[2];
            objArr[0] = this.title;
            objArr[1] = isActive() ? String.valueOf(class_124.field_1060) + "on" : String.valueOf(class_124.field_1061) + "off";
            ChatUtils.sendMsg(iHashCode, class_124Var, "Toggled (highlight)%s(default) %s(default).", objArr);
        }
    }

    public void info(class_2561 message) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.sendMsg(this.title, message);
    }

    public void info(String message, Object... args) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.infoPrefix(this.title, message, args);
    }

    public void warning(String message, Object... args) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.warningPrefix(this.title, message, args);
    }

    public void error(String message, Object... args) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.errorPrefix(this.title, message, args);
    }

    public boolean isActive() {
        return this.active;
    }

    public String getInfoString() {
        return null;
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        if (!this.serialize) {
            return null;
        }
        class_2487 tag = new class_2487();
        tag.method_10582("name", this.name);
        tag.method_10566("keybind", this.keybind.toTag());
        tag.method_10556("toggleOnKeyRelease", this.toggleOnBindRelease);
        tag.method_10556("chatFeedback", this.chatFeedback);
        tag.method_10556("favorite", this.favorite);
        tag.method_10566("settings", this.settings.toTag());
        tag.method_10556("active", this.active);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Module fromTag(class_2487 tag) {
        this.keybind.fromTag(tag.method_68568("keybind"));
        this.toggleOnBindRelease = tag.method_68566("toggleOnKeyRelease", false);
        this.chatFeedback = !tag.method_10545("chatFeedback") || tag.method_68566("chatFeedback", false);
        this.favorite = tag.method_68566("favorite", false);
        class_2487 class_2487VarMethod_10580 = tag.method_10580("settings");
        if (class_2487VarMethod_10580 instanceof class_2487) {
            this.settings.fromTag(class_2487VarMethod_10580);
        }
        boolean active = tag.method_68566("active", false);
        if (active != isActive()) {
            toggle();
        }
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Module module = (Module) o;
        return Objects.equals(this.name, module.name);
    }

    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override // java.lang.Comparable
    public int compareTo(@NotNull Module o) {
        return this.name.compareTo(o.name);
    }
}
