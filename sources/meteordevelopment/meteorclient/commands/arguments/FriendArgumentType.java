package meteordevelopment.meteorclient.commands.arguments;

import com.google.common.collect.Streams;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import net.minecraft.class_2172;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/arguments/FriendArgumentType.class */
public class FriendArgumentType implements ArgumentType<String> {
    private static final FriendArgumentType INSTANCE = new FriendArgumentType();
    private static final Collection<String> EXAMPLES = List.of("_Synful8169", "Crownizzle");

    public static FriendArgumentType create() {
        return INSTANCE;
    }

    public static Friend get(CommandContext<?> context) {
        return Friends.get().get((String) context.getArgument("friend", String.class));
    }

    private FriendArgumentType() {
    }

    /* JADX INFO: renamed from: parse, reason: merged with bridge method [inline-methods] */
    public String m113parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return class_2172.method_9264(Streams.stream(Friends.get()).map((v0) -> {
            return v0.getName();
        }), builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
