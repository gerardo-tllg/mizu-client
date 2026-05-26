package meteordevelopment.meteorclient.systems.profiles;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.macros.Macros;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.class_2487;
import net.minecraft.class_2520;
import org.apache.commons.io.FileUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/profiles/Profile.class */
public class Profile implements ISerializable<Profile> {
    public final Settings settings = new Settings();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgSave = this.settings.createGroup("Save");
    public Setting<String> name = this.sgGeneral.add(new StringSetting.Builder().name("name").description("The name of the profile.").filter(Utils::nameFilter).build());
    public Setting<List<String>> loadOnJoin = this.sgGeneral.add(new StringListSetting.Builder().name("load-on-join").description("Which servers to set this profile as active when joining.").filter(Utils::ipFilter).build());
    public Setting<Boolean> hud = this.sgSave.add(new BoolSetting.Builder().name("hud").description("Whether the profile should save hud.").defaultValue(false).build());
    public Setting<Boolean> macros = this.sgSave.add(new BoolSetting.Builder().name("macros").description("Whether the profile should save macros.").defaultValue(false).build());
    public Setting<Boolean> modules = this.sgSave.add(new BoolSetting.Builder().name("modules").description("Whether the profile should save modules.").defaultValue(false).build());
    public Setting<Boolean> waypoints = this.sgSave.add(new BoolSetting.Builder().name("waypoints").description("Whether the profile should save waypoints.").defaultValue(false).build());

    public Profile() {
    }

    public Profile(class_2520 tag) {
        fromTag2((class_2487) tag);
    }

    public void load() {
        File folder = getFile();
        if (this.hud.get().booleanValue()) {
            Hud.get().load(folder);
        }
        if (this.macros.get().booleanValue()) {
            Macros.get().load(folder);
        }
        if (this.modules.get().booleanValue()) {
            Modules.get().load(folder);
        }
        if (this.waypoints.get().booleanValue()) {
            Waypoints.get().load(folder);
        }
    }

    public void save() {
        File folder = getFile();
        if (this.hud.get().booleanValue()) {
            Hud.get().save(folder);
        }
        if (this.macros.get().booleanValue()) {
            Macros.get().save(folder);
        }
        if (this.modules.get().booleanValue()) {
            Modules.get().save(folder);
        }
        if (this.waypoints.get().booleanValue()) {
            Waypoints.get().save(folder);
        }
    }

    public void delete() {
        try {
            FileUtils.deleteDirectory(getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getFile() {
        return new File(Profiles.FOLDER, this.name.get());
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10566("settings", this.settings.toTag());
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Profile fromTag2(class_2487 tag) {
        if (tag.method_10545("settings")) {
            this.settings.fromTag2(tag.method_68568("settings"));
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
        Profile profile = (Profile) o;
        return Objects.equals(profile.name.get(), this.name.get());
    }
}
