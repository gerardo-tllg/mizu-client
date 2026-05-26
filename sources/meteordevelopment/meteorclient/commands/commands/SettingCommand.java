package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ModuleArgumentType;
import meteordevelopment.meteorclient.commands.arguments.SettingArgumentType;
import meteordevelopment.meteorclient.commands.arguments.SettingValueArgumentType;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_2172;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/SettingCommand.class */
public class SettingCommand extends Command {
    public SettingCommand() {
        super("settings", "Allows you to view and change module settings.", "s");
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(literal("hud").executes(context -> {
            TabScreen screen = Tabs.get().get(3).createScreen(GuiThemes.get());
            screen.parent = null;
            Utils.screenToOpen = screen;
            return 1;
        }));
        builder.then(argument("module", ModuleArgumentType.create()).executes(context2 -> {
            Module module = (Module) context2.getArgument("module", Module.class);
            WidgetScreen screen = GuiThemes.get().moduleScreen(module);
            screen.parent = null;
            Utils.screenToOpen = screen;
            return 1;
        }));
        builder.then(argument("module", ModuleArgumentType.create()).then(argument("setting", SettingArgumentType.create()).executes(context3 -> {
            Setting<?> setting = SettingArgumentType.get(context3);
            ModuleArgumentType.get(context3).info("Setting (highlight)%s(default) is (highlight)%s(default).", setting.title, setting.get());
            return 1;
        }).then(argument("value", SettingValueArgumentType.create()).executes(context4 -> {
            Setting<?> setting = SettingArgumentType.get(context4);
            String value = SettingValueArgumentType.get(context4);
            if (setting.parse(value)) {
                ModuleArgumentType.get(context4).info("Setting (highlight)%s(default) changed to (highlight)%s(default).", setting.title, value);
                return 1;
            }
            return 1;
        }))));
    }
}
