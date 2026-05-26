package meteordevelopment.meteorclient.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_2172;
import net.minecraft.class_2960;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/arguments/SettingValueArgumentType.class */
public class SettingValueArgumentType implements ArgumentType<String> {
    private static final SettingValueArgumentType INSTANCE = new SettingValueArgumentType();

    public static SettingValueArgumentType create() {
        return INSTANCE;
    }

    public static String get(CommandContext<?> context) {
        return (String) context.getArgument("value", String.class);
    }

    private SettingValueArgumentType() {
    }

    /* JADX INFO: renamed from: parse, reason: merged with bridge method [inline-methods] */
    public String m131parse(StringReader reader) throws CommandSyntaxException {
        String text = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());
        return text;
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: com.mojang.brigadier.exceptions.CommandSyntaxException */
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        try {
            Setting<?> setting = SettingArgumentType.get(context);
            Iterable<class_2960> identifiers = setting.getIdentifierSuggestions();
            if (identifiers != null) {
                return class_2172.method_9270(identifiers, builder);
            }
            return class_2172.method_9265(setting.getSuggestions(), builder);
        } catch (CommandSyntaxException e) {
            return Suggestions.empty();
        }
    }
}
