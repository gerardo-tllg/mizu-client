package meteordevelopment.meteorclient.systems.accounts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.class_156;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/MicrosoftLogin.class */
public class MicrosoftLogin {
    private static final String CLIENT_ID = "4673b348-3efa-4f6a-bbb6-34e141cdc638";
    private static final int PORT = 9675;
    private static HttpServer server;
    private static Consumer<String> callback;

    private MicrosoftLogin() {
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/MicrosoftLogin$LoginData.class */
    public static class LoginData {
        public String mcToken;
        public String newRefreshToken;
        public String uuid;
        public String username;

        public LoginData() {
        }

        public LoginData(String mcToken, String newRefreshToken, String uuid, String username) {
            this.mcToken = mcToken;
            this.newRefreshToken = newRefreshToken;
            this.uuid = uuid;
            this.username = username;
        }

        public boolean isGood() {
            return this.mcToken != null;
        }
    }

    public static String getRefreshToken(Consumer<String> callback2) {
        callback = callback2;
        startServer();
        class_156.method_668().method_670("https://login.live.com/oauth20_authorize.srf?client_id=4673b348-3efa-4f6a-bbb6-34e141cdc638&response_type=code&redirect_uri=http://127.0.0.1:9675&scope=XboxLive.signin%20offline_access&prompt=select_account");
        return "https://login.live.com/oauth20_authorize.srf?client_id=4673b348-3efa-4f6a-bbb6-34e141cdc638&response_type=code&redirect_uri=http://127.0.0.1:9675&scope=XboxLive.signin%20offline_access&prompt=select_account";
    }

    public static LoginData login(String refreshToken) {
        XblXstsResponse xstsRes;
        McResponse mcRes;
        AuthTokenResponse res = (AuthTokenResponse) Http.post("https://login.live.com/oauth20_token.srf").bodyForm("client_id=4673b348-3efa-4f6a-bbb6-34e141cdc638&refresh_token=" + refreshToken + "&grant_type=refresh_token&redirect_uri=http://127.0.0.1:9675").sendJson(AuthTokenResponse.class);
        if (res == null) {
            return new LoginData();
        }
        String accessToken = res.access_token;
        String refreshToken2 = res.refresh_token;
        XblXstsResponse xblRes = (XblXstsResponse) Http.post("https://user.auth.xboxlive.com/user/authenticate").bodyJson("{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"d=" + accessToken + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}").sendJson(XblXstsResponse.class);
        if (xblRes != null && (xstsRes = (XblXstsResponse) Http.post("https://xsts.auth.xboxlive.com/xsts/authorize").bodyJson("{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + xblRes.Token + "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}").sendJson(XblXstsResponse.class)) != null && (mcRes = (McResponse) Http.post("https://api.minecraftservices.com/authentication/login_with_xbox").bodyJson("{\"identityToken\":\"XBL3.0 x=" + xblRes.DisplayClaims.xui[0].uhs + ";" + xstsRes.Token + "\"}").sendJson(McResponse.class)) != null) {
            GameOwnershipResponse gameOwnershipRes = (GameOwnershipResponse) Http.get("https://api.minecraftservices.com/entitlements/mcstore").bearer(mcRes.access_token).sendJson(GameOwnershipResponse.class);
            if (gameOwnershipRes == null || !gameOwnershipRes.hasGameOwnership()) {
                return new LoginData();
            }
            ProfileResponse profileRes = (ProfileResponse) Http.get("https://api.minecraftservices.com/minecraft/profile").bearer(mcRes.access_token).sendJson(ProfileResponse.class);
            return profileRes == null ? new LoginData() : new LoginData(mcRes.access_token, refreshToken2, profileRes.id, profileRes.name);
        }
        return new LoginData();
    }

    private static void startServer() {
        if (server != null) {
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
            server.createContext("/", new Handler());
            server.setExecutor(MeteorExecutor.executor);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopServer() {
        if (server == null) {
            return;
        }
        server.stop(0);
        server = null;
        callback = null;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/MicrosoftLogin$Handler.class */
    private static class Handler implements HttpHandler {
        private Handler() {
        }

        public void handle(HttpExchange req) throws IOException {
            if (req.getRequestMethod().equals("GET")) {
                List<NameValuePair> query = URLEncodedUtils.parse(req.getRequestURI(), StandardCharsets.UTF_8);
                boolean ok = false;
                Iterator<NameValuePair> it = query.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    NameValuePair pair = it.next();
                    if (pair.getName().equals("code")) {
                        handleCode(pair.getValue());
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    writeText(req, "Cannot authenticate.");
                    MicrosoftLogin.callback.accept(null);
                } else {
                    writeText(req, "You may now close this page.");
                }
            }
            MicrosoftLogin.stopServer();
        }

        private void handleCode(String code) {
            AuthTokenResponse res = (AuthTokenResponse) Http.post("https://login.live.com/oauth20_token.srf").bodyForm("client_id=4673b348-3efa-4f6a-bbb6-34e141cdc638&code=" + code + "&grant_type=authorization_code&redirect_uri=http://127.0.0.1:9675").sendJson(AuthTokenResponse.class);
            if (res != null) {
                MicrosoftLogin.callback.accept(res.refresh_token);
            } else {
                MicrosoftLogin.callback.accept(null);
            }
        }

        private void writeText(HttpExchange req, String text) throws IOException {
            OutputStream out = req.getResponseBody();
            req.sendResponseHeaders(200, text.length());
            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/MicrosoftLogin$AuthTokenResponse.class */
    private static class AuthTokenResponse {
        public String access_token;
        public String refresh_token;

        private AuthTokenResponse() {
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/MicrosoftLogin$XblXstsResponse.class */
    private static class XblXstsResponse {
        public String Token;
        public DisplayClaims DisplayClaims;

        private XblXstsResponse() {
        }

        /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/MicrosoftLogin$XblXstsResponse$DisplayClaims.class */
        private static class DisplayClaims {
            private Claim[] xui;

            private DisplayClaims() {
            }

            /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/MicrosoftLogin$XblXstsResponse$DisplayClaims$Claim.class */
            private static class Claim {
                private String uhs;

                private Claim() {
                }
            }
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/MicrosoftLogin$McResponse.class */
    private static class McResponse {
        public String access_token;

        private McResponse() {
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/MicrosoftLogin$GameOwnershipResponse.class */
    private static class GameOwnershipResponse {
        private Item[] items;

        private GameOwnershipResponse() {
        }

        /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/MicrosoftLogin$GameOwnershipResponse$Item.class */
        private static class Item {
            private String name;

            private Item() {
            }
        }

        private boolean hasGameOwnership() {
            boolean hasProduct = false;
            boolean hasGame = false;
            for (Item item : this.items) {
                if (item.name.equals("product_minecraft")) {
                    hasProduct = true;
                } else if (item.name.equals("game_minecraft")) {
                    hasGame = true;
                }
            }
            return hasProduct && hasGame;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/MicrosoftLogin$ProfileResponse.class */
    private static class ProfileResponse {
        public String id;
        public String name;

        private ProfileResponse() {
        }
    }
}
