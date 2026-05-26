package meteordevelopment.meteorclient.gui.screens;

import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/SearchOnlyScreen.class */
public class SearchOnlyScreen extends TabScreen {
    private final Tab tab;

    public SearchOnlyScreen(GuiTheme theme, Tab tab) {
        super(theme, tab);
        this.tab = tab;
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        WWindow window = (WWindow) add(this.theme.window("Search")).center().widget();
        window.view.scrollOnlyWhenMouseOver = true;
        window.view.hasScrollBar = true;
        WButton back = (WButton) window.add(this.theme.button("← Back")).expandX().widget();
        back.action = () -> {
            MeteorClient.mc.method_1507(new CategorySelectScreen(this.theme, this.tab));
        };
        window.add(this.theme.horizontalSeparator()).expandX();
        WVerticalList results = this.theme.verticalList();
        WTextBox textBox = (WTextBox) window.add(this.theme.textBox("")).minWidth(200.0d).expandX().widget();
        textBox.setFocused(true);
        textBox.action = () -> {
            results.clear();
            populateResults(results, textBox.get());
        };
        window.add(results).expandX();
        populateResults(results, "");
    }

    private void populateResults(WVerticalList list, String text) {
        if (text.isEmpty()) {
            return;
        }
        Set<Module> byTitle = Modules.get().searchTitles(text);
        if (!byTitle.isEmpty()) {
            WSection section = (WSection) list.add(this.theme.section("Modules")).expandX().widget();
            section.spacing = 0.0d;
            int count = 0;
            for (Module module : byTitle) {
                if (count >= Config.get().moduleSearchCount.get().intValue()) {
                    break;
                }
                section.add(this.theme.module(module)).expandX();
                count++;
            }
        }
        Set<Module> bySettings = Modules.get().searchSettingTitles(text);
        if (!bySettings.isEmpty()) {
            WSection section2 = (WSection) list.add(this.theme.section("Settings")).expandX().widget();
            section2.spacing = 0.0d;
            int count2 = 0;
            for (Module module2 : bySettings) {
                if (count2 < Config.get().moduleSearchCount.get().intValue()) {
                    section2.add(this.theme.module(module2)).expandX();
                    count2++;
                } else {
                    return;
                }
            }
        }
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void reload() {
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public boolean toClipboard() {
        return false;
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public boolean fromClipboard() {
        return false;
    }
}
