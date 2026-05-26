package meteordevelopment.meteorclient.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/arguments/WaypointArgumentType.class */
public class WaypointArgumentType implements ArgumentType<String> {
    private static final WaypointArgumentType GREEDY = new WaypointArgumentType(true);
    private static final WaypointArgumentType QUOTED = new WaypointArgumentType(false);
    private static final DynamicCommandExceptionType NO_SUCH_WAYPOINT = new DynamicCommandExceptionType(name -> {
        return class_2561.method_43470("Waypoint with name '" + String.valueOf(name) + "' doesn't exist.");
    });
    private final boolean greedyString;

    private WaypointArgumentType(boolean greedyString) {
        this.greedyString = greedyString;
    }

    public static WaypointArgumentType create() {
        return GREEDY;
    }

    public static WaypointArgumentType create(boolean greedy) {
        return greedy ? GREEDY : QUOTED;
    }

    public static Waypoint get(CommandContext<?> context) {
        return Waypoints.get().get((String) context.getArgument("waypoint", String.class));
    }

    public static Waypoint get(CommandContext<?> context, String name) {
        return Waypoints.get().get((String) context.getArgument(name, String.class));
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: com.mojang.brigadier.exceptions.CommandSyntaxException */
    /* JADX INFO: renamed from: parse, reason: merged with bridge method [inline-methods] */
    public String parse(StringReader reader) throws CommandSyntaxException {
        String argument;
        if (this.greedyString) {
            argument = reader.getRemaining();
            reader.setCursor(reader.getTotalLength());
        } else {
            argument = reader.readString();
        }
        if (Waypoints.get().get(argument) == null) {
            throw NO_SUCH_WAYPOINT.create(argument);
        }
        return argument;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return class_2172.method_9265(getExamples(), builder);
    }

    public Collection<String> getExamples() {
        List<String> names = new ArrayList<>();
        for (Waypoint waypoint : Waypoints.get()) {
            names.add(waypoint.name.get());
        }
        return names;
    }
}
