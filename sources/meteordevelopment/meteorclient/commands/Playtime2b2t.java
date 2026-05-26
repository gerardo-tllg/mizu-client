package meteordevelopment.meteorclient.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.concurrent.TimeUnit;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.stardust.ApiHandler;
import meteordevelopment.meteorclient.utils.stardust.LogUtil;
import net.minecraft.class_2172;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_746;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/Playtime2b2t.class */
public class Playtime2b2t extends Command {
    private final String API_ENDPOINT = "/playtime?playerName=";

    public Playtime2b2t() {
        super("playtime2b2t", "Check the playtime of a 2b2t player.", "pt");
        this.API_ENDPOINT = "/playtime?playerName=";
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(argument("player", StringArgumentType.word()).executes(ctx -> {
            MeteorExecutor.execute(() -> {
                class_746 player = class_310.method_1551().field_1724;
                String playerString = (String) ctx.getArgument("player", String.class);
                String requestString = "https://api.2b2t.vc/playtime?playerName=" + playerString.trim();
                String response = new ApiHandler().fetchResponse(requestString);
                if (response == null) {
                    return;
                }
                if (response.equals("204 Undocumented")) {
                    if (player == null) {
                        return;
                    }
                    player.method_7353(class_2561.method_30163("§8[§7MasterClient§8] §4Player not found§7."), false);
                    return;
                }
                JsonElement ptJson = JsonParser.parseString(response);
                if (ptJson.getAsJsonObject().has("playtimeSeconds")) {
                    long playtimeSeconds = ptJson.getAsJsonObject().get("playtimeSeconds").getAsLong();
                    long days = TimeUnit.SECONDS.toDays(playtimeSeconds);
                    long playtimeSeconds2 = playtimeSeconds - TimeUnit.DAYS.toSeconds(days);
                    long hours = TimeUnit.SECONDS.toHours(playtimeSeconds2);
                    long playtimeSeconds3 = playtimeSeconds2 - TimeUnit.HOURS.toSeconds(hours);
                    long minutes = TimeUnit.SECONDS.toMinutes(playtimeSeconds3);
                    long seconds = TimeUnit.SECONDS.toSeconds(playtimeSeconds3 - TimeUnit.MINUTES.toSeconds(minutes));
                    StringBuilder sb = new StringBuilder().append("§8[§7MasterClient§8] §7").append("MasterClient").append(playerString).append("§7: ").append("MasterClient");
                    if (days != 0) {
                        sb.append(days).append(" §7Days, ").append("MasterClient");
                    }
                    if (hours != 0) {
                        sb.append(hours).append(" §7Hours, ").append("MasterClient");
                    }
                    if (minutes != 0) {
                        sb.append(minutes).append(" §7Minutes, ").append("MasterClient");
                    }
                    if (seconds != 0) {
                        sb.append(seconds).append(" §7Seconds§7.");
                    }
                    if (player != null) {
                        player.method_7353(class_2561.method_30163(sb.toString()), false);
                        return;
                    }
                    return;
                }
                ApiHandler.sendErrorResponse();
                LogUtil.warn("Received unexpected output from api.2b2t.vc : \"" + String.valueOf(ptJson) + "\"", getName());
            });
            return 1;
        }));
    }
}
