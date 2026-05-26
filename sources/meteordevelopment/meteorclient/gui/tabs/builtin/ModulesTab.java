package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.screens.CategorySelectScreen;
import meteordevelopment.meteorclient.gui.screens.SearchOnlyScreen;
import meteordevelopment.meteorclient.gui.screens.SingleCategoryScreen;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.systems.config.Config;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/ModulesTab.class */
public class ModulesTab extends Tab {
    public ModulesTab() {
        super("Modules");
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public TabScreen createScreen(GuiTheme theme) {
        if (Config.get().cleanModulesUi.get().booleanValue()) {
            return new CategorySelectScreen(theme, this);
        }
        return theme.modulesScreen();
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public boolean isScreen(class_437 screen) {
        return GuiThemes.get().isModulesScreen(screen) || (screen instanceof CategorySelectScreen) || (screen instanceof SingleCategoryScreen) || (screen instanceof SearchOnlyScreen);
    }
}
