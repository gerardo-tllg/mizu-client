package meteordevelopment.meteorclient.systems.macros;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.starscript.Script;
import net.minecraft.class_2487;
import net.minecraft.class_2520;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/macros/Macro.class */
public class Macro implements ISerializable<Macro> {
    public final Settings settings = new Settings();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public Setting<String> name = this.sgGeneral.add(new StringSetting.Builder().name("name").description("The name of the macro.").build());
    public Setting<List<String>> messages = this.sgGeneral.add(new StringListSetting.Builder().name("messages").description("The messages for the macro to send.").onChanged(v -> {
        this.dirty = true;
    }).renderer(StarscriptTextBoxRenderer.class).build());
    public Setting<Keybind> keybind = this.sgGeneral.add(new KeybindSetting.Builder().name("keybind").description("The bind to run the macro.").build());
    private final List<Script> scripts = new ArrayList(1);
    private boolean dirty;

    public Macro() {
    }

    public Macro(class_2520 tag) {
        fromTag((class_2487) tag);
    }

    public boolean onAction(boolean isKey, int value, int modifiers) {
        if (this.keybind.get().matches(isKey, value, modifiers) && MeteorClient.mc.field_1755 == null) {
            return onAction();
        }
        return false;
    }

    public boolean onAction() {
        if (this.dirty) {
            this.scripts.clear();
            Iterator<String> it = this.messages.get().iterator();
            while (it.hasNext()) {
                Script script = MeteorStarscript.compile(it.next());
                if (script != null) {
                    this.scripts.add(script);
                }
            }
            this.dirty = false;
        }
        Iterator<Script> it2 = this.scripts.iterator();
        while (it2.hasNext()) {
            String message = MeteorStarscript.run(it2.next());
            if (message != null) {
                ChatUtils.sendPlayerMsg(message);
            }
        }
        return true;
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
    public Macro fromTag(class_2487 tag) {
        if (tag.method_10545("settings")) {
            this.settings.fromTag(tag.method_68568("settings"));
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
        Macro macro = (Macro) o;
        return Objects.equals(macro.name.get(), this.name.get());
    }
}
