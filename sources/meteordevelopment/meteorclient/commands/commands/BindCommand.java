package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ModuleArgumentType;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.class_2172;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/BindCommand.class */
public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "Binds a specified module to the next pressed key.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(argument("module", ModuleArgumentType.create()).executes(context -> {
            Module module = (Module) context.getArgument("module", Module.class);
            Modules.get().setModuleToBind(module);
            Modules.get().awaitKeyRelease();
            module.info("Press a key to bind the module to.", new Object[0]);
            return 1;
        }));
    }
}
