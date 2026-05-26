package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/Sneak.class */
public class Sneak extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/Sneak$Mode.class */
    public enum Mode {
        Packet,
        Vanilla
    }

    public Sneak() {
        super(Categories.Movement, "sneak", "Sneaks for you");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(new EnumSetting.Builder().name("mode").description("Which method to sneak.").defaultValue(Mode.Vanilla).build());
    }

    public boolean doPacket() {
        return isActive() && !this.mc.field_1724.method_31549().field_7479 && this.mode.get() == Mode.Packet;
    }

    public boolean doVanilla() {
        return isActive() && !this.mc.field_1724.method_31549().field_7479 && this.mode.get() == Mode.Vanilla;
    }
}
