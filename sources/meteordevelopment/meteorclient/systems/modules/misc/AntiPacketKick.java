package meteordevelopment.meteorclient.systems.modules.misc;

import java.util.Objects;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/AntiPacketKick.class */
public class AntiPacketKick extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Boolean> catchExceptions;
    public final Setting<Boolean> logExceptions;

    public AntiPacketKick() {
        super(Categories.Misc, "anti-packet-kick", "Attempts to prevent you from being disconnected by large packets.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.catchExceptions = this.sgGeneral.add(new BoolSetting.Builder().name("catch-exceptions").description("Drops corrupted packets.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgGeneral;
        BoolSetting.Builder builderDefaultValue = new BoolSetting.Builder().name("log-exceptions").description("Logs caught exceptions.").defaultValue(false);
        Setting<Boolean> setting = this.catchExceptions;
        Objects.requireNonNull(setting);
        this.logExceptions = settingGroup.add(builderDefaultValue.visible(setting::get).build());
    }

    public boolean catchExceptions() {
        return isActive() && this.catchExceptions.get().booleanValue();
    }
}
