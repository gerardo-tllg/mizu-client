package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import joptsimple.internal.Strings;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayNetworkHandlerAccessor;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1132;
import net.minecraft.class_124;
import net.minecraft.class_2172;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_2639;
import net.minecraft.class_2641;
import net.minecraft.class_2805;
import net.minecraft.class_5250;
import net.minecraft.class_639;
import net.minecraft.class_642;
import net.minecraft.class_7157;
import org.apache.commons.lang3.StringUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/ServerCommand.class */
public class ServerCommand extends Command {
    private String alias;
    private int ticks;
    private boolean tick;
    private final List<String> plugins;
    private final List<String> commandTreePlugins;
    private static final Set<String> ANTICHEAT_LIST = Set.of((Object[]) new String[]{"nocheatplus", "negativity", "warden", "horizon", "illegalstack", "coreprotect", "exploitsx", "vulcan", "abc", "spartan", "kauri", "anticheatreloaded", "witherac", "godseye", "matrix", "wraith", "antixrayheuristics", "grimac"});
    private static final Set<String> VERSION_ALIASES = Set.of("version", "ver", "about", "bukkit:version", "bukkit:ver", "bukkit:about");
    private static final Random RANDOM = new Random();

    public ServerCommand() {
        super("server", "Prints server information", new String[0]);
        this.ticks = 0;
        this.tick = false;
        this.plugins = new ArrayList();
        this.commandTreePlugins = new ArrayList();
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.executes(context -> {
            basicInfo();
            return 1;
        });
        builder.then(literal("info").executes(ctx -> {
            basicInfo();
            return 1;
        }));
        builder.then(literal("plugins").executes(ctx2 -> {
            this.plugins.addAll(this.commandTreePlugins);
            if (this.alias != null) {
                mc.method_1562().method_52787(new class_2805(RANDOM.nextInt(200), this.alias + " "));
                this.tick = true;
                return 1;
            }
            printPlugins();
            return 1;
        }));
        builder.then(literal("tps").executes(ctx3 -> {
            class_124 color;
            float tps = TickRate.INSTANCE.getTickRate();
            if (tps > 17.0f) {
                color = class_124.field_1060;
            } else {
                color = tps > 12.0f ? class_124.field_1054 : class_124.field_1061;
            }
            info("Current TPS: %s%.2f(default).", color, Float.valueOf(tps));
            return 1;
        }));
    }

    private void basicInfo() {
        class_5250 ipText;
        if (mc.method_1496()) {
            class_1132 server = mc.method_1576();
            info("Singleplayer", new Object[0]);
            if (server != null) {
                info("Version: %s", server.method_3827());
                return;
            }
            return;
        }
        class_642 server2 = mc.method_1558();
        if (server2 == null) {
            info("Couldn't obtain any server information.", new Object[0]);
            return;
        }
        String ipv4 = "";
        try {
            ipv4 = InetAddress.getByName(server2.field_3761).getHostAddress();
        } catch (UnknownHostException e) {
        }
        if (ipv4.isEmpty()) {
            ipText = class_2561.method_43470(String.valueOf(class_124.field_1080) + server2.field_3761);
            ipText.method_10862(ipText.method_10866().method_10958(new class_2558.class_10606(server2.field_3761)).method_10949(new class_2568.class_10613(class_2561.method_43470("Copy to clipboard"))));
        } else {
            ipText = class_2561.method_43470(String.valueOf(class_124.field_1080) + server2.field_3761);
            ipText.method_10862(ipText.method_10866().method_10958(new class_2558.class_10606(server2.field_3761)).method_10949(new class_2568.class_10613(class_2561.method_43470("Copy to clipboard"))));
            class_5250 ipv4Text = class_2561.method_43470(String.format("%s (%s)", class_124.field_1080, ipv4));
            ipv4Text.method_10862(ipText.method_10866().method_10958(new class_2558.class_10606(ipv4)).method_10949(new class_2568.class_10613(class_2561.method_43470("Copy to clipboard"))));
            ipText.method_10852(ipv4Text);
        }
        info(class_2561.method_43470(String.format("%sIP: ", class_124.field_1080)).method_10852(ipText));
        info("Port: %d", Integer.valueOf(class_639.method_2950(server2.field_3761).method_2954()));
        Object[] objArr = new Object[1];
        objArr[0] = mc.method_1562().method_52790() != null ? mc.method_1562().method_52790() : "unknown";
        info("Type: %s", objArr);
        Object[] objArr2 = new Object[1];
        objArr2[0] = server2.field_3757 != null ? server2.field_3757.getString() : "unknown";
        info("Motd: %s", objArr2);
        info("Version: %s", server2.field_3760.getString());
        info("Protocol version: %d", Integer.valueOf(server2.field_3756));
        info("Difficulty: %s (Local: %.2f)", mc.field_1687.method_8407().method_5463().getString(), Float.valueOf(mc.field_1687.method_8404(mc.field_1724.method_24515()).method_5457()));
        info("Day: %d", Long.valueOf(mc.field_1687.method_8532() / 24000));
        info("Permission level: %s", formatPerms());
    }

    public String formatPerms() {
        int p = 5;
        while (!mc.field_1724.method_64475(p) && p > 0) {
            p--;
        }
        switch (p) {
            case 0:
                return "0 (No Perms)";
            case 1:
                return "1 (No Perms)";
            case 2:
                return "2 (Player Command Access)";
            case 3:
                return "3 (Server Command Access)";
            case 4:
                return "4 (Operator)";
            default:
                return p + " (Unknown)";
        }
    }

    private void printPlugins() {
        this.plugins.sort(String.CASE_INSENSITIVE_ORDER);
        this.plugins.replaceAll(this::formatName);
        if (!this.plugins.isEmpty()) {
            info("Plugins (%d): %s ", Integer.valueOf(this.plugins.size()), Strings.join((String[]) this.plugins.toArray(new String[0]), ", "));
        } else {
            error("No plugins found.", new Object[0]);
        }
        this.tick = false;
        this.ticks = 0;
        this.plugins.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.tick) {
            this.ticks++;
            if (this.ticks >= 100) {
                printPlugins();
            }
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!this.tick || !(event.packet instanceof class_2805)) {
            return;
        }
        event.cancel();
    }

    @EventHandler
    private void onReadPacket(PacketEvent.Receive event) {
        class_2641 class_2641Var = event.packet;
        if (class_2641Var instanceof class_2641) {
            class_2641 packet = class_2641Var;
            ClientPlayNetworkHandlerAccessor handler = event.connection.method_10744();
            this.commandTreePlugins.clear();
            this.alias = null;
            packet.method_11403(class_7157.method_46722(handler.getCombinedDynamicRegistries(), handler.getEnabledFeatures())).getChildren().forEach(node -> {
                String[] split = node.getName().split(":");
                if (split.length > 1 && !this.commandTreePlugins.contains(split[0])) {
                    this.commandTreePlugins.add(split[0]);
                }
                if (this.alias == null && VERSION_ALIASES.contains(node.getName())) {
                    this.alias = node.getName();
                }
            });
        }
        if (this.tick) {
            try {
                class_2639 class_2639Var = event.packet;
                if (class_2639Var instanceof class_2639) {
                    class_2639 packet2 = class_2639Var;
                    Suggestions matches = packet2.method_11397();
                    if (matches.isEmpty()) {
                        error("An error occurred while trying to find plugins.", new Object[0]);
                        return;
                    }
                    for (Suggestion suggestion : matches.getList()) {
                        String pluginName = suggestion.getText();
                        if (!this.plugins.contains(pluginName.toLowerCase())) {
                            this.plugins.add(pluginName);
                        }
                    }
                    printPlugins();
                }
            } catch (Exception e) {
                error("An error occurred while trying to find plugins.", new Object[0]);
            }
        }
    }

    private String formatName(String name) {
        if (ANTICHEAT_LIST.contains(name.toLowerCase())) {
            return String.format("%s%s(default)", class_124.field_1061, name);
        }
        if (StringUtils.containsIgnoreCase(name, "exploit") || StringUtils.containsIgnoreCase(name, "cheat") || StringUtils.containsIgnoreCase(name, "illegal")) {
            return String.format("%s%s(default)", class_124.field_1061, name);
        }
        return String.format("(highlight)%s(default)", name);
    }
}
