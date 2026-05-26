package meteordevelopment.meteorclient.commands.arguments;

import com.google.common.collect.Streams;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/arguments/SettingArgumentType.class */
public class SettingArgumentType implements ArgumentType<String> {
    private static final SettingArgumentType INSTANCE = new SettingArgumentType();
    private static final DynamicCommandExceptionType NO_SUCH_SETTING = new DynamicCommandExceptionType(name -> {
        return class_2561.method_43470("No such setting '" + String.valueOf(name) + "'.");
    });

    public static SettingArgumentType create() {
        return INSTANCE;
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: com.mojang.brigadier.exceptions.CommandSyntaxException */
    public static Setting<?> get(CommandContext<?> context) throws CommandSyntaxException {
        Module module = (Module) context.getArgument("module", Module.class);
        String settingName = (String) context.getArgument("setting", String.class);
        Setting<?> setting = module.settings.get(settingName);
        if (setting == null) {
            throw NO_SUCH_SETTING.create(settingName);
        }
        return setting;
    }

    private SettingArgumentType() {
    }

    /* JADX INFO: renamed from: parse, reason: merged with bridge method [inline-methods] */
    public String m129parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Stream<String> stream = Streams.stream(((Module) context.getArgument("module", Module.class)).settings.iterator()).flatMap(settings -> {
            return Streams.stream(settings.iterator());
        }).map(setting -> {
            return setting.name;
        });
        return class_2172.method_9264(stream, builder);
    }
}
