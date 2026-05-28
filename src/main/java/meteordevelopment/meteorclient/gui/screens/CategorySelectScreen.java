/*
 * ReviveClient - Clean Category Select Screen
 */

package meteordevelopment.meteorclient.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * A clean category-selection screen.
 * Shows one button per module category. Clicking a button opens
 * a single-category ModulesScreen scoped to that category.
 */
public class CategorySelectScreen extends TabScreen {
    private final Tab tab;

    public CategorySelectScreen(GuiTheme theme, Tab tab) {
        super(theme, tab);
        this.tab = tab;
    }

    @Override
    public void initWidgets() {
        // Centered window
        var window = add(theme.window("Modules")).center().widget();
        window.view.scrollOnlyWhenMouseOver = true;

        WVerticalList list = window.add(theme.verticalList()).minWidth(200).widget();

        // One button per category
        for (Category category : Modules.loopCategories()) {
            // Skip empty categories
            List<Module> moduleList = new ArrayList<>();
            for (Module module : Modules.get().getGroup(category)) {
                if (!Config.get().hiddenModules.get().contains(module)) {
                    moduleList.add(module);
                }
            }
            if (moduleList.isEmpty()) continue;

            WButton btn = list.add(theme.button(category.name)).expandX().widget();
            btn.action = () -> mc.setScreen(new SingleCategoryScreen(theme, tab, category));
        }

        // Search button at the bottom
        list.add(theme.horizontalSeparator()).expandX();
        WButton search = list.add(theme.button("Search")).expandX().widget();
        search.action = () -> mc.setScreen(new SearchOnlyScreen(theme, tab));
    }

    @Override
    public void reload() {}

    @Override
    public boolean toClipboard() { return false; }

    @Override
    public boolean fromClipboard() { return false; }
}
