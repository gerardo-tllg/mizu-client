package meteordevelopment.meteorclient.gui.screens;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/CategorySelectScreen.class */
public class CategorySelectScreen extends TabScreen {
    private final Tab tab;

    public CategorySelectScreen(GuiTheme theme, Tab tab) {
        super(theme, tab);
        this.tab = tab;
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        WWindow window = (WWindow) add(this.theme.window("Modules")).center().widget();
        window.view.scrollOnlyWhenMouseOver = true;
        WVerticalList list = (WVerticalList) window.add(this.theme.verticalList()).minWidth(200.0d).widget();
        for (Category category : Modules.loopCategories()) {
            List<Module> moduleList = new ArrayList<>();
            for (Module module : Modules.get().getGroup(category)) {
                if (!Config.get().hiddenModules.get().contains(module)) {
                    moduleList.add(module);
                }
            }
            if (!moduleList.isEmpty()) {
                WButton btn = (WButton) list.add(this.theme.button(category.name)).expandX().widget();
                btn.action = () -> {
                    MeteorClient.mc.method_1507(new SingleCategoryScreen(this.theme, this.tab, category));
                };
            }
        }
        list.add(this.theme.horizontalSeparator()).expandX();
        WButton search = (WButton) list.add(this.theme.button("Search")).expandX().widget();
        search.action = () -> {
            MeteorClient.mc.method_1507(new SearchOnlyScreen(this.theme, this.tab));
        };
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
