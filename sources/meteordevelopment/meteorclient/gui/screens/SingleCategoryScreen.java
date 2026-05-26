package meteordevelopment.meteorclient.gui.screens;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/SingleCategoryScreen.class */
public class SingleCategoryScreen extends TabScreen {
    private final Tab tab;
    private final Category category;

    public SingleCategoryScreen(GuiTheme theme, Tab tab, Category category) {
        super(theme, tab);
        this.tab = tab;
        this.category = category;
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        WWindow window = (WWindow) add(this.theme.window(this.category.name)).center().widget();
        window.padding = 0.0d;
        window.spacing = 0.0d;
        window.view.scrollOnlyWhenMouseOver = true;
        window.view.hasScrollBar = true;
        WButton back = (WButton) window.add(this.theme.button("← Back")).expandX().widget();
        back.action = () -> {
            MeteorClient.mc.method_1507(new CategorySelectScreen(this.theme, this.tab));
        };
        window.add(this.theme.horizontalSeparator()).expandX();
        List<Module> moduleList = new ArrayList<>();
        for (Module module : Modules.get().getGroup(this.category)) {
            if (!Config.get().hiddenModules.get().contains(module)) {
                moduleList.add(module);
            }
        }
        Iterator<Module> it = moduleList.iterator();
        while (it.hasNext()) {
            window.add(this.theme.module(it.next())).expandX();
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
