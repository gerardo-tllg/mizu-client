/*
 * ReviveClient - Single Category Screen
 */

package meteordevelopment.meteorclient.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Shows all modules for a single category, with a Back button
 * to return to the CategorySelectScreen.
 */
public class SingleCategoryScreen extends TabScreen {
    private final Tab tab;
    private final Category category;

    public SingleCategoryScreen(GuiTheme theme, Tab tab, Category category) {
        super(theme, tab);
        this.tab = tab;
        this.category = category;
    }

    @Override
    public void initWidgets() {
        var window = add(theme.window(category.name)).center().widget();
        window.padding = 0;
        window.spacing = 0;
        window.view.scrollOnlyWhenMouseOver = true;
        window.view.hasScrollBar = true;

        // Back button at the top
        WButton back = window.add(theme.button("← Back")).expandX().widget();
        back.action = () -> mc.setScreen(new CategorySelectScreen(theme, tab));

        window.add(theme.horizontalSeparator()).expandX();

        // Module list
        List<Module> moduleList = new ArrayList<>();
        for (Module module : Modules.get().getGroup(category)) {
            if (!Config.get().hiddenModules.get().contains(module)) {
                moduleList.add(module);
            }
        }

        for (Module module : moduleList) {
            window.add(theme.module(module)).expandX();
        }
    }

    @Override
    public void reload() {}

    @Override
    public boolean toClipboard() { return false; }

    @Override
    public boolean fromClipboard() { return false; }
}
