package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.MacroArgumentType;
import meteordevelopment.meteorclient.systems.macros.Macro;
import net.minecraft.class_2172;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/MacroCommand.class */
public class MacroCommand extends Command {
    public MacroCommand() {
        super("macro", "Allows you to execute macros.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(argument("macro", MacroArgumentType.create()).executes(context -> {
            Macro macro = MacroArgumentType.get(context);
            macro.onAction();
            return 1;
        }));
    }
}
