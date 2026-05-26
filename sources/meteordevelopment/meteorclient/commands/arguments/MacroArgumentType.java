package meteordevelopment.meteorclient.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.systems.macros.Macro;
import meteordevelopment.meteorclient.systems.macros.Macros;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/arguments/MacroArgumentType.class */
public class MacroArgumentType implements ArgumentType<Macro> {
    private static final MacroArgumentType INSTANCE = new MacroArgumentType();
    private static final DynamicCommandExceptionType NO_SUCH_MACRO = new DynamicCommandExceptionType(name -> {
        return class_2561.method_43470("Macro with name " + String.valueOf(name) + " doesn't exist.");
    });

    public static MacroArgumentType create() {
        return INSTANCE;
    }

    public static Macro get(CommandContext<?> context) {
        return (Macro) context.getArgument("macro", Macro.class);
    }

    private MacroArgumentType() {
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: com.mojang.brigadier.exceptions.CommandSyntaxException */
    /* JADX INFO: renamed from: parse, reason: merged with bridge method [inline-methods] */
    public Macro m115parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        Macro macro = Macros.get().get(argument);
        if (macro == null) {
            throw NO_SUCH_MACRO.create(argument);
        }
        return macro;
    }

    public CompletableFuture<Suggestions> listSuggestions(CommandContext context, SuggestionsBuilder builder) {
        return class_2172.method_9264(Macros.get().getAll().stream().map(macro -> {
            return macro.name.get();
        }), builder);
    }

    public Collection<String> getExamples() {
        return (Collection) Macros.get().getAll().stream().limit(3L).map(macro -> {
            return macro.name.get();
        }).collect(Collectors.toList());
    }
}
