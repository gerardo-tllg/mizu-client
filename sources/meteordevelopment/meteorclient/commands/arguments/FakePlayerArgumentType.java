package meteordevelopment.meteorclient.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerManager;
import net.minecraft.class_2172;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/arguments/FakePlayerArgumentType.class */
public class FakePlayerArgumentType implements ArgumentType<String> {
    private static final FakePlayerArgumentType INSTANCE = new FakePlayerArgumentType();
    private static final Collection<String> EXAMPLES = List.of("swavez", "MizuPlayer");

    public static FakePlayerArgumentType create() {
        return INSTANCE;
    }

    public static FakePlayerEntity get(CommandContext<?> context) {
        return FakePlayerManager.get((String) context.getArgument("fp", String.class));
    }

    private FakePlayerArgumentType() {
    }

    /* JADX INFO: renamed from: parse, reason: merged with bridge method [inline-methods] */
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return class_2172.method_9264(FakePlayerManager.stream().map(fakePlayerEntity -> {
            return fakePlayerEntity.method_5477().getString();
        }), builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
