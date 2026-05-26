package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.class_1799;
import net.minecraft.class_2172;
import net.minecraft.class_2287;
import net.minecraft.class_2561;
import net.minecraft.class_2873;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/GiveCommand.class */
public class GiveCommand extends Command {
    private static final SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(class_2561.method_43470("You must be in creative mode to use this."));
    private static final SimpleCommandExceptionType NO_SPACE = new SimpleCommandExceptionType(class_2561.method_43470("No space in hotbar."));

    public GiveCommand() {
        super("give", "Gives you any item.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(argument("item", class_2287.method_9776(REGISTRY_ACCESS)).executes(context -> {
            if (!mc.field_1724.method_31549().field_7477) {
                throw NOT_IN_CREATIVE.create();
            }
            class_1799 item = class_2287.method_9777(context, "item").method_9781(1, false);
            giveItem(item);
            return 1;
        }).then(argument("number", IntegerArgumentType.integer(1, 99)).executes(context2 -> {
            if (!mc.field_1724.method_31549().field_7477) {
                throw NOT_IN_CREATIVE.create();
            }
            class_1799 item = class_2287.method_9777(context2, "item").method_9781(IntegerArgumentType.getInteger(context2, "number"), true);
            giveItem(item);
            return 1;
        })));
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: com.mojang.brigadier.exceptions.CommandSyntaxException */
    private void giveItem(class_1799 item) throws CommandSyntaxException {
        FindItemResult fir = InvUtils.find((v0) -> {
            return v0.method_7960();
        }, 0, 8);
        if (!fir.found()) {
            throw NO_SPACE.create();
        }
        mc.method_1562().method_52787(new class_2873(36 + fir.slot(), item));
        mc.field_1724.field_7498.method_7611(36 + fir.slot()).method_53512(item);
    }
}
