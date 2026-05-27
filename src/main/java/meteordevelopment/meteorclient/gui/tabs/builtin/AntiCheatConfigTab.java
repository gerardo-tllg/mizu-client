/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.config.AntiCheatConfig;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.screen.Screen;

public class AntiCheatConfigTab extends Tab {
    public AntiCheatConfigTab() {
        super("AntiCheat");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new AntiCheatConfigScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof AntiCheatConfigScreen;
    }

    public static class AntiCheatConfigScreen extends WindowTabScreen {
        private final Settings settings;

        public AntiCheatConfigScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);

            settings = AntiCheatConfig.get().settings;
            settings.onActivated();
        }

        @Override
        public void initWidgets() {
            add(theme.settings(settings)).expandX();
        }

        @Override
        public void tick() {
            super.tick();

            settings.tick(window, theme);
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(AntiCheatConfig.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(AntiCheatConfig.get());
        }
    }
}
