/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.screens.CategorySelectScreen;
import meteordevelopment.meteorclient.gui.screens.SearchOnlyScreen;
import meteordevelopment.meteorclient.gui.screens.SingleCategoryScreen;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.systems.config.Config;
import net.minecraft.client.gui.screen.Screen;

public class ModulesTab extends Tab {
    public ModulesTab() {
        super("Modules");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        if (Config.get().cleanModulesUi.get()) {
            return new CategorySelectScreen(theme, this);
        }
        return theme.modulesScreen();
    }

    @Override
    public boolean isScreen(Screen screen) {
        return GuiThemes.get().isModulesScreen(screen)
            || screen instanceof CategorySelectScreen
            || screen instanceof SingleCategoryScreen
            || screen instanceof SearchOnlyScreen;
    }
}
