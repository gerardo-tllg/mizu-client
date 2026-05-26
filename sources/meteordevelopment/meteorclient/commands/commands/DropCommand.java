package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2172;
import net.minecraft.class_2287;
import net.minecraft.class_2561;
import net.minecraft.class_746;
import net.minecraft.class_9274;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/DropCommand.class */
public class DropCommand extends Command {
    private static final SimpleCommandExceptionType NOT_SPECTATOR = new SimpleCommandExceptionType(class_2561.method_43470("Can't drop items while in spectator."));
    private static final SimpleCommandExceptionType NO_SUCH_ITEM = new SimpleCommandExceptionType(class_2561.method_43470("Could not find an item with that name!"));

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/DropCommand$PlayerConsumer.class */
    @FunctionalInterface
    private interface PlayerConsumer {
        void accept(class_746 class_746Var) throws CommandSyntaxException;
    }

    public DropCommand() {
        super("drop", "Automatically drops specified items.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(literal("hand").executes(context -> {
            return drop(player -> {
                player.method_7290(true);
            });
        }));
        builder.then(literal("offhand").executes(context2 -> {
            return drop(player -> {
                InvUtils.drop().slotOffhand();
            });
        }));
        builder.then(literal("hotbar").executes(context3 -> {
            return drop(player -> {
                for (int i = 0; i < 9; i++) {
                    InvUtils.drop().slotHotbar(i);
                }
            });
        }));
        builder.then(literal("inventory").executes(context4 -> {
            return drop(player -> {
                for (int i = 9; i < player.method_31548().method_67533().size(); i++) {
                    InvUtils.drop().slotMain(i - 9);
                }
            });
        }));
        builder.then(literal("all").executes(context5 -> {
            return drop(player -> {
                for (int i = 0; i < player.method_31548().method_5439(); i++) {
                    InvUtils.drop().slot(i);
                }
                if (!mc.field_1724.method_6079().method_7960()) {
                    InvUtils.drop().slotOffhand();
                }
            });
        }));
        builder.then(literal("armor").executes(context6 -> {
            return drop(player -> {
                for (class_1304 equipmentSlot : class_9274.field_49224) {
                    if (equipmentSlot.method_5925() == class_1304.class_1305.field_6178) {
                        InvUtils.drop().slotArmor(equipmentSlot.method_5927());
                    }
                }
            });
        }));
        builder.then(argument("item", class_2287.method_9776(REGISTRY_ACCESS)).executes(context7 -> {
            return drop(player -> {
                class_1799 stack = class_2287.method_9777(context7, "item").method_9781(1, false);
                if (stack == null || stack.method_7909() == class_1802.field_8162) {
                    throw NO_SUCH_ITEM.create();
                }
                for (int i = 0; i < player.method_31548().method_5439(); i++) {
                    if (stack.method_7909() == player.method_31548().method_5438(i).method_7909()) {
                        InvUtils.drop().slot(i);
                    }
                }
            });
        }));
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: com.mojang.brigadier.exceptions.CommandSyntaxException */
    private int drop(PlayerConsumer consumer) throws CommandSyntaxException {
        if (mc.field_1724.method_7325()) {
            throw NOT_SPECTATOR.create();
        }
        consumer.accept(mc.field_1724);
        return 1;
    }
}
