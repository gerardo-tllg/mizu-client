package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.ToIntFunction;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.RegistryEntryReferenceArgumentType;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_1799;
import net.minecraft.class_1887;
import net.minecraft.class_2172;
import net.minecraft.class_2561;
import net.minecraft.class_437;
import net.minecraft.class_490;
import net.minecraft.class_6880;
import net.minecraft.class_7924;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/EnchantCommand.class */
public class EnchantCommand extends Command {
    private static final SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(class_2561.method_43470("You must be in creative mode to use this."));
    private static final SimpleCommandExceptionType NOT_HOLDING_ITEM = new SimpleCommandExceptionType(class_2561.method_43470("You need to hold some item to enchant."));

    public EnchantCommand() {
        super("enchant", "Enchants the item in your hand. REQUIRES Creative mode.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(literal("one").then(argument("enchantment", RegistryEntryReferenceArgumentType.enchantment()).then(literal("level").then(argument("level", IntegerArgumentType.integer()).executes(context -> {
            one(context, enchantment -> {
                return ((Integer) context.getArgument("level", Integer.class)).intValue();
            });
            return 1;
        }))).then(literal("max").executes(context2 -> {
            one(context2, (v0) -> {
                return v0.method_8183();
            });
            return 1;
        }))));
        builder.then(literal("all_possible").then(literal("level").then(argument("level", IntegerArgumentType.integer()).executes(context3 -> {
            all(true, enchantment -> {
                return ((Integer) context3.getArgument("level", Integer.class)).intValue();
            });
            return 1;
        }))).then(literal("max").executes(context4 -> {
            all(true, (v0) -> {
                return v0.method_8183();
            });
            return 1;
        })));
        builder.then(literal("all").then(literal("level").then(argument("level", IntegerArgumentType.integer()).executes(context5 -> {
            all(false, enchantment -> {
                return ((Integer) context5.getArgument("level", Integer.class)).intValue();
            });
            return 1;
        }))).then(literal("max").executes(context6 -> {
            all(false, (v0) -> {
                return v0.method_8183();
            });
            return 1;
        })));
        builder.then(literal("clear").executes(context7 -> {
            class_1799 itemStack = tryGetItemStack();
            Utils.clearEnchantments(itemStack);
            syncItem();
            return 1;
        }));
        builder.then(literal("remove").then(argument("enchantment", RegistryEntryReferenceArgumentType.enchantment()).executes(context8 -> {
            class_1799 itemStack = tryGetItemStack();
            class_6880.class_6883<class_1887> enchantment = RegistryEntryReferenceArgumentType.getEnchantment(context8, "enchantment");
            Utils.removeEnchantment(itemStack, (class_1887) enchantment.comp_349());
            syncItem();
            return 1;
        })));
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: com.mojang.brigadier.exceptions.CommandSyntaxException */
    private void one(CommandContext<class_2172> context, ToIntFunction<class_1887> level) throws CommandSyntaxException {
        class_1799 itemStack = tryGetItemStack();
        class_6880.class_6883<class_1887> enchantment = RegistryEntryReferenceArgumentType.getEnchantment(context, "enchantment");
        Utils.addEnchantment(itemStack, enchantment, level.applyAsInt((class_1887) enchantment.comp_349()));
        syncItem();
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: com.mojang.brigadier.exceptions.CommandSyntaxException */
    private void all(boolean onlyPossible, ToIntFunction<class_1887> level) throws CommandSyntaxException {
        class_1799 itemStack = tryGetItemStack();
        mc.method_1562().method_29091().method_46759(class_7924.field_41265).ifPresent(registry -> {
            registry.method_42017().forEach(enchantment -> {
                if (!onlyPossible || ((class_1887) enchantment.comp_349()).method_8192(itemStack)) {
                    Utils.addEnchantment(itemStack, enchantment, level.applyAsInt((class_1887) enchantment.comp_349()));
                }
            });
        });
        syncItem();
    }

    private void syncItem() {
        mc.method_1507(new class_490(mc.field_1724));
        mc.method_1507((class_437) null);
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: com.mojang.brigadier.exceptions.CommandSyntaxException */
    private class_1799 tryGetItemStack() throws CommandSyntaxException {
        if (!mc.field_1724.method_68878()) {
            throw NOT_IN_CREATIVE.create();
        }
        class_1799 itemStack = getItemStack();
        if (itemStack == null) {
            throw NOT_HOLDING_ITEM.create();
        }
        return itemStack;
    }

    private class_1799 getItemStack() {
        class_1799 itemStack = mc.field_1724.method_6047();
        if (itemStack == null) {
            itemStack = mc.field_1724.method_6079();
        }
        if (itemStack.method_7960()) {
            return null;
        }
        return itemStack;
    }
}
