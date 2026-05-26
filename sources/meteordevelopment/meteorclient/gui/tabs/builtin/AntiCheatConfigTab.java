package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.config.AntiCheatConfig;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/AntiCheatConfigTab.class */
public class AntiCheatConfigTab extends Tab {
    public AntiCheatConfigTab() {
        super("AntiCheat");
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public TabScreen createScreen(GuiTheme theme) {
        return new AntiCheatConfigScreen(theme, this);
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public boolean isScreen(class_437 screen) {
        return screen instanceof AntiCheatConfigScreen;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/AntiCheatConfigTab$AntiCheatConfigScreen.class */
    public static class AntiCheatConfigScreen extends WindowTabScreen {
        private final Settings settings;

        public AntiCheatConfigScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
            this.settings = AntiCheatConfig.get().settings;
            this.settings.onActivated();
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public void initWidgets() {
            add(this.theme.settings(this.settings)).expandX();
        }

        public void method_25393() {
            super.method_25393();
            this.settings.tick(this.window, this.theme);
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public boolean toClipboard() {
            return NbtUtils.toClipboard(AntiCheatConfig.get());
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(AntiCheatConfig.get());
        }
    }
}
