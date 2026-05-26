package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.AutoWasp;
import net.minecraft.class_2172;
import net.minecraft.class_2561;
import net.minecraft.class_746;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/WaspCommand.class */
public class WaspCommand extends Command {
    private static final SimpleCommandExceptionType CANT_WASP_SELF = new SimpleCommandExceptionType(class_2561.method_43470("You cannot target yourself!"));

    public WaspCommand() {
        super("wasp", "Sets the auto wasp target.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        AutoWasp wasp = (AutoWasp) Modules.get().get(AutoWasp.class);
        builder.then(literal("reset").executes(context -> {
            if (wasp.isActive()) {
                wasp.toggle();
                return 1;
            }
            return 1;
        }));
        builder.then(argument("player", PlayerArgumentType.create()).executes(context2 -> {
            class_746 class_746Var = PlayerArgumentType.get(context2);
            if (class_746Var == mc.field_1724) {
                throw CANT_WASP_SELF.create();
            }
            wasp.target = class_746Var;
            if (!wasp.isActive()) {
                wasp.toggle();
            }
            info(class_746Var.method_5477().getString() + " set as target.", new Object[0]);
            return 1;
        }));
    }
}
