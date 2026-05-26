package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.pathing.PathManagers;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/PathManagerTab.class */
public class PathManagerTab extends Tab {
    public PathManagerTab() {
        super(PathManagers.get().getName());
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public TabScreen createScreen(GuiTheme theme) {
        return new PathManagerScreen(theme, this);
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public boolean isScreen(class_437 screen) {
        return screen instanceof PathManagerScreen;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/PathManagerTab$PathManagerScreen.class */
    private static class PathManagerScreen extends WindowTabScreen {
        public PathManagerScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
            PathManagers.get().getSettings().get().onActivated();
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public void initWidgets() {
            WTextBox filter = (WTextBox) add(this.theme.textBox("")).minWidth(400.0d).expandX().widget();
            filter.setFocused(true);
            filter.action = () -> {
                clear();
                add(filter);
                add(this.theme.settings(PathManagers.get().getSettings().get(), filter.get().trim())).expandX();
            };
            add(this.theme.settings(PathManagers.get().getSettings().get(), filter.get().trim())).expandX();
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        protected void onClosed() {
            PathManagers.get().getSettings().save();
        }
    }
}
