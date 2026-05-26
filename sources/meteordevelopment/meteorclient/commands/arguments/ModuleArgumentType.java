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
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/arguments/ModuleArgumentType.class */
public class ModuleArgumentType implements ArgumentType<Module> {
    private static final ModuleArgumentType INSTANCE = new ModuleArgumentType();
    private static final DynamicCommandExceptionType NO_SUCH_MODULE = new DynamicCommandExceptionType(name -> {
        return class_2561.method_43470("Module with name " + String.valueOf(name) + " doesn't exist.");
    });
    private static final Collection<String> EXAMPLES = (Collection) Modules.get().getAll().stream().limit(3).map(module -> {
        return module.name;
    }).collect(Collectors.toList());

    public static ModuleArgumentType create() {
        return INSTANCE;
    }

    public static Module get(CommandContext<?> context) {
        return (Module) context.getArgument("module", Module.class);
    }

    private ModuleArgumentType() {
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: com.mojang.brigadier.exceptions.CommandSyntaxException */
    /* JADX INFO: renamed from: parse, reason: merged with bridge method [inline-methods] */
    public Module m117parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        Module module = Modules.get().get(argument);
        if (module == null) {
            throw NO_SUCH_MODULE.create(argument);
        }
        return module;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return class_2172.method_9264(Modules.get().getAll().stream().map(module -> {
            return module.name;
        }), builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
