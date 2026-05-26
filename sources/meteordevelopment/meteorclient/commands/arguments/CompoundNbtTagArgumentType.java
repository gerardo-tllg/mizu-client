package meteordevelopment.meteorclient.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;
import net.minecraft.class_2487;
import net.minecraft.class_2522;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/arguments/CompoundNbtTagArgumentType.class */
public class CompoundNbtTagArgumentType implements ArgumentType<class_2487> {
    private static final CompoundNbtTagArgumentType INSTANCE = new CompoundNbtTagArgumentType();
    private static final Collection<String> EXAMPLES = List.of("{foo:bar}", "{foo:[aa, bb],bar:15}");

    public static CompoundNbtTagArgumentType create() {
        return INSTANCE;
    }

    public static class_2487 get(CommandContext<?> context) {
        return (class_2487) context.getArgument("nbt", class_2487.class);
    }

    private CompoundNbtTagArgumentType() {
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: com.mojang.brigadier.exceptions.CommandSyntaxException */
    /* JADX INFO: renamed from: parse, reason: merged with bridge method [inline-methods] */
    public class_2487 parse(StringReader reader) throws CommandSyntaxException {
        reader.skipWhitespace();
        if (!reader.canRead()) {
            throw class_2522.field_56410.createWithContext(reader);
        }
        StringBuilder b = new StringBuilder();
        int open = 0;
        while (reader.canRead()) {
            if (reader.peek() == '{') {
                open++;
            } else if (reader.peek() == '}') {
                open--;
            }
            if (open == 0) {
                break;
            }
            b.append(reader.read());
        }
        reader.expect('}');
        b.append('}');
        return class_2522.method_67315(b.toString().replace("$", "§").replace("§§", "$"));
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
