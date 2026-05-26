package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.render.prompts.YesNoPrompt;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/ConfigTab.class */
public class ConfigTab extends Tab {
    public ConfigTab() {
        super("Config");
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public TabScreen createScreen(GuiTheme theme) {
        return new ConfigScreen(theme, this);
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public boolean isScreen(class_437 screen) {
        return screen instanceof ConfigScreen;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/ConfigTab$ConfigScreen.class */
    public static class ConfigScreen extends WindowTabScreen {
        private final Settings settings;

        public ConfigScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
            this.settings = Config.get().settings;
            this.settings.onActivated();
            onClosed(() -> {
                String prefix = Config.get().prefix.get();
                if (prefix.isBlank()) {
                    YesNoPrompt.create(theme, this.parent).title("Empty command prefix").message("You have set your command prefix to nothing.").message("This WILL prevent you from sending chat messages.").message("Do you want to reset your prefix back to '.'?").onYes(() -> {
                        Config.get().prefix.set(".");
                    }).id("empty-command-prefix").show();
                } else if (prefix.equals("/")) {
                    YesNoPrompt.create(theme, this.parent).title("Potential prefix conflict").message("You have set your command prefix to '/', which is used by minecraft.").message("This can cause conflict issues between meteor and minecraft commands.").message("Do you want to reset your prefix to '.'?").onYes(() -> {
                        Config.get().prefix.set(".");
                    }).id("minecraft-prefix-conflict").show();
                } else if (prefix.length() > 7) {
                    YesNoPrompt.create(theme, this.parent).title("Long command prefix").message("You have set your command prefix to a very long string.").message("This means that in order to execute any command, you will need to type %s followed by the command you want to run.", prefix).message("Do you want to reset your prefix back to '.'?").onYes(() -> {
                        Config.get().prefix.set(".");
                    }).id("long-command-prefix").show();
                }
            });
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
            return NbtUtils.toClipboard(Config.get());
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Config.get());
        }
    }
}
