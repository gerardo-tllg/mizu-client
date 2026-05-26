package meteordevelopment.meteorclient.commands.commands;

import baritone.api.BaritoneAPI;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.pathing.IPathManager;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1299;
import net.minecraft.class_1672;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2172;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2604;
import net.minecraft.class_2767;
import net.minecraft.class_3417;
import net.minecraft.class_4208;
import net.minecraft.class_5250;
import net.minecraft.class_9291;
import net.minecraft.class_9292;
import net.minecraft.class_9323;
import net.minecraft.class_9334;
import net.minecraft.class_9428;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/LocateCommand.class */
public class LocateCommand extends Command {
    private class_243 firstStart;
    private class_243 firstEnd;
    private class_243 secondStart;
    private class_243 secondEnd;
    private final List<class_2248> netherFortressBlocks;
    private final List<class_2248> monumentBlocks;
    private final List<class_2248> strongholdBlocks;
    private final List<class_2248> endCityBlocks;

    public LocateCommand() {
        super("locate", "Locates structures", "loc");
        this.netherFortressBlocks = List.of(class_2246.field_10266, class_2246.field_10364, class_2246.field_9974);
        this.monumentBlocks = List.of(class_2246.field_10006, class_2246.field_10174, class_2246.field_10297);
        this.strongholdBlocks = List.of(class_2246.field_10398);
        this.endCityBlocks = List.of(class_2246.field_10286, class_2246.field_10505, class_2246.field_10175, class_2246.field_9992, class_2246.field_10462, class_2246.field_10455);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(literal("buried_treasure").executes(s -> {
            class_1799 stack = mc.field_1724.method_31548().method_7391();
            if (stack.method_7909() != class_1802.field_8204 || stack.method_58694(class_9334.field_50239) == null || !((class_2561) stack.method_58694(class_9334.field_50239)).getString().equals(class_2561.method_43471("filled_map.buried_treasure").getString())) {
                error("You need to hold a (highlight)buried treasure map(default)!", new Object[0]);
                return 1;
            }
            class_9292 mapDecorationsComponent = (class_9292) stack.method_58694(class_9334.field_49647);
            if (mapDecorationsComponent == null) {
                error("Couldn't locate the map icons!", new Object[0]);
                return 1;
            }
            for (class_9292.class_9293 decoration : mapDecorationsComponent.comp_2404().values()) {
                if (((class_9428) decoration.comp_2405().comp_349()).comp_2514().toString().equals("minecraft:red_x")) {
                    class_243 coords = new class_243(decoration.comp_2406(), 62.0d, decoration.comp_2407());
                    class_5250 text = class_2561.method_43470("Buried Treasure located at ");
                    text.method_10852(ChatUtils.formatCoords(coords));
                    text.method_27693(".");
                    info(text);
                    return 1;
                }
            }
            error("Couldn't locate the buried treasure!", new Object[0]);
            return 1;
        }));
        builder.then(literal("mansion").executes(s2 -> {
            class_1799 stack = mc.field_1724.method_31548().method_7391();
            if (stack.method_7909() != class_1802.field_8204 || stack.method_58694(class_9334.field_50239) == null || !((class_2561) stack.method_58694(class_9334.field_50239)).getString().equals(class_2561.method_43471("filled_map.mansion").getString())) {
                error("You need to hold a (highlight)woodland explorer map(default)!", new Object[0]);
                return 1;
            }
            class_9292 mapDecorationsComponent = (class_9292) stack.method_58694(class_9334.field_49647);
            if (mapDecorationsComponent == null) {
                error("Couldn't locate the map icons!", new Object[0]);
                return 1;
            }
            for (class_9292.class_9293 decoration : mapDecorationsComponent.comp_2404().values()) {
                if (((class_9428) decoration.comp_2405().comp_349()).comp_2514().toString().equals("minecraft:woodland_mansion")) {
                    class_243 coords = new class_243(decoration.comp_2406(), 62.0d, decoration.comp_2407());
                    class_5250 text = class_2561.method_43470("Mansion located at ");
                    text.method_10852(ChatUtils.formatCoords(coords));
                    text.method_27693(".");
                    info(text);
                    return 1;
                }
            }
            error("Couldn't locate the mansion!", new Object[0]);
            return 1;
        }));
        builder.then(literal("monument").executes(s3 -> {
            class_1799 stack = mc.field_1724.method_31548().method_7391();
            if (stack.method_7909() == class_1802.field_8204 && stack.method_58694(class_9334.field_50239) != null && ((class_2561) stack.method_58694(class_9334.field_50239)).getString().equals(class_2561.method_43471("filled_map.monument").getString())) {
                class_9292 mapDecorationsComponent = (class_9292) stack.method_58694(class_9334.field_49647);
                if (mapDecorationsComponent == null) {
                    error("Couldn't locate the map icons!", new Object[0]);
                    return 1;
                }
                for (class_9292.class_9293 decoration : mapDecorationsComponent.comp_2404().values()) {
                    if (((class_9428) decoration.comp_2405().comp_349()).comp_2514().toString().equals("minecraft:ocean_monument")) {
                        class_243 coords = new class_243(decoration.comp_2406(), 62.0d, decoration.comp_2407());
                        class_5250 text = class_2561.method_43470("Monument located at ");
                        text.method_10852(ChatUtils.formatCoords(coords));
                        text.method_27693(".");
                        info(text);
                        return 1;
                    }
                }
                error("Couldn't locate the monument!", new Object[0]);
                return 1;
            }
            if (BaritoneUtils.IS_AVAILABLE) {
                class_243 coords2 = findByBlockList(this.monumentBlocks);
                if (coords2 == null) {
                    error("No monument found. Try using an (highlight)ocean explorer map(default) for more success.", new Object[0]);
                    return 1;
                }
                class_5250 text2 = class_2561.method_43470("Monument located at ");
                text2.method_10852(ChatUtils.formatCoords(coords2));
                text2.method_27693(".");
                info(text2);
                return 1;
            }
            error("Locating this structure without an (highlight)ocean explorer map(default) requires Baritone.", new Object[0]);
            return 1;
        }));
        builder.then(literal("stronghold").executes(s4 -> {
            boolean foundEye = InvUtils.testInHotbar(class_1802.field_8449);
            if (foundEye) {
                if (BaritoneUtils.IS_AVAILABLE) {
                    IPathManager iPathManager = PathManagers.get();
                    Class<class_1672> cls = class_1672.class;
                    Objects.requireNonNull(class_1672.class);
                    iPathManager.follow((v1) -> {
                        return r1.isInstance(v1);
                    });
                }
                this.firstStart = null;
                this.firstEnd = null;
                this.secondStart = null;
                this.secondEnd = null;
                MeteorClient.EVENT_BUS.subscribe(this);
                info("Please throw the first Eye of Ender", new Object[0]);
                return 1;
            }
            if (BaritoneUtils.IS_AVAILABLE) {
                class_243 coords = findByBlockList(this.strongholdBlocks);
                if (coords == null) {
                    error("No stronghold found nearby. You can use (highlight)Ender Eyes(default) for more success.", new Object[0]);
                    return 1;
                }
                class_5250 text = class_2561.method_43470("Stronghold located at ");
                text.method_10852(ChatUtils.formatCoords(coords));
                text.method_27693(".");
                info(text);
                return 1;
            }
            error("No Eyes of Ender found in hotbar.", new Object[0]);
            return 1;
        }));
        builder.then(literal("nether_fortress").executes(s5 -> {
            if (mc.field_1687.method_27983() != class_1937.field_25180) {
                error("You need to be in the nether to locate a nether fortress.", new Object[0]);
                return 1;
            }
            if (!BaritoneUtils.IS_AVAILABLE) {
                error("Locating this structure requires Baritone.", new Object[0]);
                return 1;
            }
            class_243 coords = findByBlockList(this.netherFortressBlocks);
            if (coords == null) {
                error("No nether fortress found.", new Object[0]);
                return 1;
            }
            class_5250 text = class_2561.method_43470("Fortress located at ");
            text.method_10852(ChatUtils.formatCoords(coords));
            text.method_27693(".");
            info(text);
            return 1;
        }));
        builder.then(literal("end_city").executes(s6 -> {
            if (mc.field_1687.method_27983() != class_1937.field_25181) {
                error("You need to be in the end to locate an end city.", new Object[0]);
                return 1;
            }
            if (!BaritoneUtils.IS_AVAILABLE) {
                error("Locating this structure requires Baritone.", new Object[0]);
                return 1;
            }
            class_243 coords = findByBlockList(this.endCityBlocks);
            if (coords == null) {
                error("No end city found.", new Object[0]);
                return 1;
            }
            class_5250 text = class_2561.method_43470("End city located at ");
            text.method_10852(ChatUtils.formatCoords(coords));
            text.method_27693(".");
            info(text);
            return 1;
        }));
        builder.then(literal("lodestone").executes(s7 -> {
            class_1799 stack = mc.field_1724.method_31548().method_7391();
            if (stack.method_7909() != class_1802.field_8251) {
                error("You need to hold a (highlight)lodestone(default) compass!", new Object[0]);
                return 1;
            }
            class_9323 components = stack.method_57353();
            if (components == null) {
                error("Couldn't get the components data. Are you holding a (highlight)lodestone(default) compass?", new Object[0]);
                return 1;
            }
            class_9291 lodestoneTrackerComponent = (class_9291) components.method_58694(class_9334.field_49614);
            if (lodestoneTrackerComponent == null) {
                error("Couldn't get the components data. Are you holding a (highlight)lodestone(default) compass?", new Object[0]);
                return 1;
            }
            if (lodestoneTrackerComponent.comp_2402().isEmpty()) {
                error("Couldn't get the lodestone's target!", new Object[0]);
                return 1;
            }
            class_243 coords = class_243.method_24954(((class_4208) lodestoneTrackerComponent.comp_2402().get()).comp_2208());
            class_5250 text = class_2561.method_43470("Lodestone located at ");
            text.method_10852(ChatUtils.formatCoords(coords));
            text.method_27693(".");
            info(text);
            return 1;
        }));
        builder.then(literal("cancel").executes(s8 -> {
            cancel();
            return 1;
        }));
    }

    private void cancel() {
        warning("Locate canceled", new Object[0]);
        MeteorClient.EVENT_BUS.unsubscribe(this);
    }

    @Nullable
    private class_243 findByBlockList(List<class_2248> blockList) {
        List<class_2338> posList = BaritoneAPI.getProvider().getWorldScanner().scanChunkRadius(BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext(), blockList, 64, 10, 32);
        if (posList.isEmpty()) {
            return null;
        }
        if (posList.size() < 3) {
            warning("Only %d block(s) found. This search might be a false positive.", Integer.valueOf(posList.size()));
        }
        return new class_243(((class_2338) posList.getFirst()).method_10263(), ((class_2338) posList.getFirst()).method_10264(), ((class_2338) posList.getFirst()).method_10260());
    }

    @EventHandler
    private void onReadPacket(PacketEvent.Receive event) {
        class_2604 class_2604Var = event.packet;
        if (class_2604Var instanceof class_2604) {
            class_2604 packet = class_2604Var;
            if (packet.method_11169() == class_1299.field_6061) {
                firstPosition(packet.method_11175(), packet.method_11174(), packet.method_11176());
            }
        }
        class_2767 class_2767Var = event.packet;
        if (class_2767Var instanceof class_2767) {
            class_2767 packet2 = class_2767Var;
            if (packet2.method_11894().comp_349() == class_3417.field_15210) {
                lastPosition(packet2.method_11890(), packet2.method_11889(), packet2.method_11893());
            }
        }
    }

    private void firstPosition(double x, double y, double z) {
        class_243 pos = new class_243(x, y, z);
        if (this.firstStart == null) {
            this.firstStart = pos;
        } else {
            this.secondStart = pos;
        }
    }

    private void lastPosition(double x, double y, double z) {
        Object[] objArr = new Object[1];
        objArr[0] = this.firstEnd == null ? "First" : "Second";
        info("%s Eye of Ender's trajectory saved.", objArr);
        class_243 pos = new class_243(x, y, z);
        if (this.firstEnd == null) {
            this.firstEnd = pos;
            info("Please throw the second Eye Of Ender from a different location.", new Object[0]);
        } else {
            this.secondEnd = pos;
            findStronghold();
        }
    }

    private void findStronghold() {
        PathManagers.get().stop();
        if (this.firstStart == null || this.firstEnd == null || this.secondStart == null || this.secondEnd == null) {
            error("Missing position data", new Object[0]);
            cancel();
            return;
        }
        double[] start = {this.secondStart.field_1352, this.secondStart.field_1350, this.secondEnd.field_1352, this.secondEnd.field_1350};
        double[] end = {this.firstStart.field_1352, this.firstStart.field_1350, this.firstEnd.field_1352, this.firstEnd.field_1350};
        double[] intersection = calcIntersection(start, end);
        if (Double.isNaN(intersection[0]) || Double.isNaN(intersection[1]) || Double.isInfinite(intersection[0]) || Double.isInfinite(intersection[1])) {
            error("Unable to calculate intersection.", new Object[0]);
            cancel();
            return;
        }
        MeteorClient.EVENT_BUS.unsubscribe(this);
        class_243 coords = new class_243(intersection[0], 0.0d, intersection[1]);
        class_5250 text = class_2561.method_43470("Stronghold roughly located at ");
        text.method_10852(ChatUtils.formatCoords(coords));
        text.method_27693(".");
        info(text);
    }

    private double[] calcIntersection(double[] line, double[] line2) {
        double a1 = line[3] - line[1];
        double b1 = line[0] - line[2];
        double c1 = (a1 * line[0]) + (b1 * line[1]);
        double a2 = line2[3] - line2[1];
        double b2 = line2[0] - line2[2];
        double c2 = (a2 * line2[0]) + (b2 * line2[1]);
        double delta = (a1 * b2) - (a2 * b1);
        return new double[]{((b2 * c1) - (b1 * c2)) / delta, ((a1 * c2) - (a2 * c1)) / delta};
    }
}
