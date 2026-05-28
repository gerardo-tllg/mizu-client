/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.gui;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.AntiCheatConfig;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

/**
 * Clickgui entry for AntiCheatConfig. Surfaces every setting from the
 * AntiCheatConfig system (rotations, block-placement, swap, network) as a
 * regular module under the Gui category. Setting objects are shared with
 * AntiCheatConfig, so edits sync both ways and persistence stays in
 * AntiCheatConfig (serialize=false here to avoid double-writing the same
 * values into the module save file).
 */
public class AntiCheat extends Module {
    public AntiCheat() {
        super(Categories.Gui, "anticheat", "Bypass tweaks for anti-cheat plugins (Grim, Polar, etc).");
        serialize = false;

        AntiCheatConfig config = AntiCheatConfig.get();
        for (SettingGroup srcGroup : config.settings) {
            SettingGroup destGroup = settings.createGroup(srcGroup.name);
            for (Setting<?> s : srcGroup) {
                destGroup.add(s);
            }
        }
    }
}
