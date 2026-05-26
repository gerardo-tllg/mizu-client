package meteordevelopment.meteorclient.systems.modules.gui;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.AntiCheatConfig;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/gui/AntiCheat.class */
public class AntiCheat extends Module {
    public AntiCheat() {
        super(Categories.Gui, "anticheat", "Bypass tweaks for anti-cheat plugins (Grim, Polar, etc).");
        this.serialize = false;
        AntiCheatConfig config = AntiCheatConfig.get();
        for (SettingGroup srcGroup : config.settings) {
            SettingGroup destGroup = this.settings.createGroup(srcGroup.name);
            for (Setting<?> s : srcGroup) {
                destGroup.add(s);
            }
        }
    }
}
