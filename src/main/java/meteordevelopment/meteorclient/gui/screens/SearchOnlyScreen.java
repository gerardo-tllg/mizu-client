/*
 * ReviveClient - Search Only Screen
 */

package meteordevelopment.meteorclient.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

import java.util.Set;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Search screen used by the clean category-select UI.
 */
public class SearchOnlyScreen extends TabScreen {
    private final Tab tab;

    public SearchOnlyScreen(GuiTheme theme, Tab tab) {
        super(theme, tab);
        this.tab = tab;
    }

    @Override
    public void initWidgets() {
        var window = add(theme.window("Search")).center().widget();
        window.view.scrollOnlyWhenMouseOver = true;
        window.view.hasScrollBar = true;

        // Back button
        WButton back = window.add(theme.button("← Back")).expandX().widget();
        back.action = () -> mc.setScreen(new CategorySelectScreen(theme, tab));

        window.add(theme.horizontalSeparator()).expandX();

        WVerticalList results = theme.verticalList();

        WTextBox textBox = window.add(theme.textBox("")).minWidth(200).expandX().widget();
        textBox.setFocused(true);
        textBox.action = () -> {
            results.clear();
            populateResults(results, textBox.get());
        };

        window.add(results).expandX();
        populateResults(results, "");
    }

    private void populateResults(WVerticalList list, String text) {
        if (text.isEmpty()) return;

        // By title
        Set<Module> byTitle = Modules.get().searchTitles(text);
        if (!byTitle.isEmpty()) {
            WSection section = list.add(theme.section("Modules")).expandX().widget();
            section.spacing = 0;
            int count = 0;
            for (Module module : byTitle) {
                if (count >= Config.get().moduleSearchCount.get()) break;
                section.add(theme.module(module)).expandX();
                count++;
            }
        }

        // By setting name
        Set<Module> bySettings = Modules.get().searchSettingTitles(text);
        if (!bySettings.isEmpty()) {
            WSection section = list.add(theme.section("Settings")).expandX().widget();
            section.spacing = 0;
            int count = 0;
            for (Module module : bySettings) {
                if (count >= Config.get().moduleSearchCount.get()) break;
                section.add(theme.module(module)).expandX();
                count++;
            }
        }
    }

    @Override
    public void reload() {}

    @Override
    public boolean toClipboard() { return false; }

    @Override
    public boolean fromClipboard() { return false; }
}
