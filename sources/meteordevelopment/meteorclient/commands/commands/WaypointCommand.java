package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.WaypointArgumentType;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.class_124;
import net.minecraft.class_2172;
import net.minecraft.class_2267;
import net.minecraft.class_2277;
import net.minecraft.class_2338;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/WaypointCommand.class */
public class WaypointCommand extends Command {
    public WaypointCommand() {
        super("waypoint", "Manages waypoints.", "wp");
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(literal("list").executes(context -> {
            if (!Waypoints.get().isEmpty()) {
                info(String.valueOf(class_124.field_1068) + "Created Waypoints:", new Object[0]);
                for (Waypoint waypoint : Waypoints.get()) {
                    info("Name: (highlight)'%s'(default), Dimension: (highlight)%s(default), Pos: (highlight)%s(default)", waypoint.name.get(), waypoint.dimension.get(), waypointPos(waypoint));
                }
                return 1;
            }
            error("No created waypoints.", new Object[0]);
            return 1;
        }));
        builder.then(literal("get").then(argument("waypoint", WaypointArgumentType.create()).executes(context2 -> {
            Waypoint waypoint = WaypointArgumentType.get(context2);
            info("Name: " + String.valueOf(class_124.field_1068) + waypoint.name.get(), new Object[0]);
            info("Actual Dimension: " + String.valueOf(class_124.field_1068) + String.valueOf(waypoint.dimension.get()), new Object[0]);
            info("Position: " + String.valueOf(class_124.field_1068) + waypointFullPos(waypoint), new Object[0]);
            info("Visible: " + (waypoint.visible.get().booleanValue() ? String.valueOf(class_124.field_1060) + "True" : String.valueOf(class_124.field_1061) + "False"), new Object[0]);
            return 1;
        })));
        builder.then(literal("add").then(argument("pos", class_2277.method_9737()).then(argument("waypoint", StringArgumentType.greedyString()).executes(context3 -> {
            return addWaypoint(context3, true);
        }))).then(argument("waypoint", StringArgumentType.greedyString()).executes(context4 -> {
            return addWaypoint(context4, false);
        })));
        builder.then(literal("delete").then(argument("waypoint", WaypointArgumentType.create()).executes(context5 -> {
            Waypoint waypoint = WaypointArgumentType.get(context5);
            info("The waypoint (highlight)'%s'(default) has been deleted.", waypoint.name.get());
            Waypoints.get().remove(waypoint);
            return 1;
        })));
        builder.then(literal("toggle").then(argument("waypoint", WaypointArgumentType.create()).executes(context6 -> {
            Waypoint waypoint = WaypointArgumentType.get(context6);
            waypoint.visible.set(Boolean.valueOf(!waypoint.visible.get().booleanValue()));
            Waypoints.get().save();
            return 1;
        })));
    }

    private String waypointPos(Waypoint waypoint) {
        return "X: " + waypoint.pos.get().method_10263() + " Z: " + waypoint.pos.get().method_10260();
    }

    private String waypointFullPos(Waypoint waypoint) {
        return "X: " + waypoint.pos.get().method_10263() + ", Y: " + waypoint.pos.get().method_10264() + ", Z: " + waypoint.pos.get().method_10260();
    }

    private int addWaypoint(CommandContext<class_2172> context, boolean withCoords) {
        if (mc.field_1724 == null) {
            return -1;
        }
        class_2338 pos = withCoords ? ((class_2267) context.getArgument("pos", class_2267.class)).method_9704(mc.field_1724.method_5671(mc.method_1576().method_30002())) : mc.field_1724.method_24515().method_10086(2);
        Waypoint waypoint = new Waypoint.Builder().name(StringArgumentType.getString(context, "waypoint")).pos(pos).dimension(PlayerUtils.getDimension()).build();
        Waypoints.get().add(waypoint);
        info("Created waypoint with name: (highlight)%s(default)", waypoint.name.get());
        return 1;
    }
}
