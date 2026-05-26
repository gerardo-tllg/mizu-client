package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.class_2172;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/FovCommand.class */
public class FovCommand extends Command {
    public FovCommand() {
        super("fov", "Changes your fov.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(argument("fov", IntegerArgumentType.integer(0, Opcode.GETFIELD)).executes(context -> {
            mc.field_1690.method_41808().setValue(context.getArgument("fov", Integer.class));
            return 1;
        }));
    }
}
