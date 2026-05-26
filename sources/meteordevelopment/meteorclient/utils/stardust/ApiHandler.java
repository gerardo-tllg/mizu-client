package meteordevelopment.meteorclient.utils.stardust;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import javax.annotation.Nullable;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_746;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/stardust/ApiHandler.class */
public class ApiHandler {
    public static final String API_2B2T_URL = "https://api.2b2t.vc";

    public static void sendErrorResponse() {
        class_746 player = class_310.method_1551().field_1724;
        if (player != null) {
            player.method_7353(class_2561.method_30163("§8[§7MasterClient§8] §4An error occurred§7, §4please try again later or check §7latest.log §4for more info§7.."), false);
        }
    }

    @Nullable
    public String fetchResponse(String requestString) {
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(new URI(requestString)).header("Accept", "*/*").method("GET", HttpRequest.BodyPublishers.noBody()).timeout(Duration.ofSeconds(30L)).build();
            if (req == null) {
                sendErrorResponse();
                return null;
            }
            HttpResponse<String> res = null;
            try {
                res = (HttpResponse) client.sendAsync(req, HttpResponse.BodyHandlers.ofString()).get();
            } catch (Exception err) {
                LogUtil.error(err.toString(), "ApiHandler");
            }
            if (res == null) {
                sendErrorResponse();
                return null;
            }
            if (res.statusCode() == 200) {
                return (String) res.body();
            }
            if (res.statusCode() == 204) {
                return "204 Undocumented";
            }
            sendErrorResponse();
            LogUtil.warn("Received unexpected response from api.2b2t.vc: \"" + String.valueOf(res) + "\"", "ApiHandler");
            return null;
        } catch (URISyntaxException err2) {
            sendErrorResponse();
            LogUtil.error(err2.toString(), "ApiHandler");
            return null;
        }
    }
}
