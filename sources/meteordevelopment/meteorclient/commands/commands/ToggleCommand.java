package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ModuleArgumentType;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.class_2172;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/ToggleCommand.class */
public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", "Toggles a module.", "t");
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(literal("all").then(literal("on").executes(context -> {
            new ArrayList(Modules.get().getAll()).forEach(module -> {
                if (!module.isActive()) {
                    module.toggle();
                }
            });
            Hud.get().active = true;
            return 1;
        })).then(literal("off").executes(context2 -> {
            new ArrayList(Modules.get().getActive()).forEach((v0) -> {
                v0.toggle();
            });
            Hud.get().active = false;
            return 1;
        }))).then(argument("module", ModuleArgumentType.create()).executes(context3 -> {
            Module m = ModuleArgumentType.get(context3);
            m.toggle();
            m.sendToggledMsg();
            return 1;
        }).then(literal("on").executes(context4 -> {
            Module m = ModuleArgumentType.get(context4);
            if (!m.isActive()) {
                m.toggle();
                return 1;
            }
            return 1;
        })).then(literal("off").executes(context5 -> {
            Module m = ModuleArgumentType.get(context5);
            if (m.isActive()) {
                m.toggle();
                return 1;
            }
            return 1;
        }))).then(literal("hud").executes(context6 -> {
            Hud.get().active = !Hud.get().active;
            return 1;
        }).then(literal("on").executes(context7 -> {
            Hud.get().active = true;
            return 1;
        })).then(literal("off").executes(context8 -> {
            Hud.get().active = false;
            return 1;
        })));
    }
}
