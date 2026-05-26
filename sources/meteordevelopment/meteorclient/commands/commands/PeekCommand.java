package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_1533;
import net.minecraft.class_1799;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/PeekCommand.class */
public class PeekCommand extends Command {
    private static final class_1799[] ITEMS = new class_1799[27];
    private static final SimpleCommandExceptionType CANT_PEEK = new SimpleCommandExceptionType(class_2561.method_43470("You must be holding a storage block or looking at an item frame."));

    public PeekCommand() {
        super("peek", "Lets you see what's inside storage block items.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.executes(context -> {
            if (Utils.openContainer(mc.field_1724.method_6047(), ITEMS, true) || Utils.openContainer(mc.field_1724.method_6079(), ITEMS, true)) {
                return 1;
            }
            if ((mc.field_1692 instanceof class_1533) && Utils.openContainer(mc.field_1692.method_6940(), ITEMS, true)) {
                return 1;
            }
            throw CANT_PEEK.create();
        });
    }
}
