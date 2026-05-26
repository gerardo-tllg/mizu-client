package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ModuleArgumentType;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.class_2172;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/ResetCommand.class */
public class ResetCommand extends Command {
    public ResetCommand() {
        super("reset", "Resets specified settings.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(literal("settings").then(argument("module", ModuleArgumentType.create()).executes(context -> {
            Module module = (Module) context.getArgument("module", Module.class);
            module.settings.forEach(group -> {
                group.forEach((v0) -> {
                    v0.reset();
                });
            });
            module.info("Reset all settings.", new Object[0]);
            return 1;
        })).then(literal("all").executes(context2 -> {
            Modules.get().getAll().forEach(module -> {
                module.settings.forEach(group -> {
                    group.forEach((v0) -> {
                        v0.reset();
                    });
                });
            });
            ChatUtils.infoPrefix("Modules", "Reset all module settings", new Object[0]);
            return 1;
        }))).then(literal("gui").executes(context3 -> {
            GuiThemes.get().clearWindowConfigs();
            ChatUtils.info("Reset GUI positioning.", new Object[0]);
            return 1;
        })).then(literal("bind").then(argument("module", ModuleArgumentType.create()).executes(context4 -> {
            Module module = (Module) context4.getArgument("module", Module.class);
            module.keybind.reset();
            module.info("Reset bind.", new Object[0]);
            return 1;
        })).then(literal("all").executes(context5 -> {
            Modules.get().getAll().forEach(module -> {
                module.keybind.reset();
            });
            ChatUtils.infoPrefix("Modules", "Reset all binds.", new Object[0]);
            return 1;
        }))).then(literal("hud").executes(context6 -> {
            ((Hud) Systems.get(Hud.class)).resetToDefaultElements();
            ChatUtils.infoPrefix("HUD", "Reset all elements.", new Object[0]);
            return 1;
        }));
    }
}
