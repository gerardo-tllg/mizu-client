package meteordevelopment.meteorclient.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.stardust.ApiHandler;
import meteordevelopment.meteorclient.utils.stardust.LogUtil;
import net.minecraft.class_2172;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_746;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/LastSeen2b2t.class */
public class LastSeen2b2t extends Command {
    private final String API_ENDPOINT = "/seen?playerName=";

    public LastSeen2b2t() {
        super("lastseen2b2t", "Check the last-seen status of a 2b2t player.", "ls", "seen");
        this.API_ENDPOINT = "/seen?playerName=";
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(argument("player", StringArgumentType.word()).executes(ctx -> {
            MeteorExecutor.execute(() -> {
                class_746 player = class_310.method_1551().field_1724;
                String playerString = (String) ctx.getArgument("player", String.class);
                String requestString = "https://api.2b2t.vc/seen?playerName=" + playerString.trim();
                String response = new ApiHandler().fetchResponse(requestString);
                if (response == null) {
                    return;
                }
                if (response.equals("204 Undocumented") || response.contains("\"lastSeen\":null")) {
                    if (player == null) {
                        return;
                    }
                    player.method_7353(class_2561.method_30163("§8[§7MasterClient§8] §4That player has not been seen§7..."), false);
                    return;
                }
                JsonElement seenJson = JsonParser.parseString(response);
                if (seenJson.getAsJsonObject().has("lastSeen")) {
                    String lastSeen = seenJson.getAsJsonObject().get("lastSeen").getAsString();
                    Instant instant = Instant.parse(lastSeen);
                    ZonedDateTime zonedTime = instant.atZone(ZoneId.systemDefault());
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM dd yyyy, HH:mm", Locale.US);
                    String formattedTimestamp = String.join(" §r§7at " + "MasterClient" + "§o", zonedTime.format(fmt).split(", "));
                    if (player != null) {
                        player.method_7353(class_2561.method_30163("§8[§7MasterClient§8] §7" + "MasterClient" + playerString + "§7 was last seen on " + "MasterClient" + formattedTimestamp + "§7."), false);
                        return;
                    }
                    return;
                }
                ApiHandler.sendErrorResponse();
                LogUtil.warn("Received unexpected output from api.2b2t.vc: \"" + String.valueOf(seenJson) + "\"", getName());
            });
            return 1;
        }));
    }
}
