package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/player/Multitask.class */
public class Multitask extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> attackingEntities;

    public Multitask() {
        super(Categories.Player, "multitask", "Lets you use items and attack at the same time.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.attackingEntities = this.sgGeneral.add(new BoolSetting.Builder().name("attacking-entities").description("Lets you attack entities while using an item.").defaultValue(true).build());
    }

    public boolean attackingEntities() {
        return isActive() && this.attackingEntities.get().booleanValue();
    }
}
