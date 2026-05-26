package meteordevelopment.meteorclient.utils.render;

import com.google.gson.Gson;
import java.util.Base64;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.accounts.TexturesJson;
import meteordevelopment.meteorclient.systems.accounts.UuidToProfileResponse;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.network.Http;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/PlayerHeadUtils.class */
public class PlayerHeadUtils {
    public static PlayerHeadTexture STEVE_HEAD;

    private PlayerHeadUtils() {
    }

    @PostInit
    public static void init() {
        STEVE_HEAD = new PlayerHeadTexture();
    }

    public static PlayerHeadTexture fetchHead(UUID id) {
        String url;
        if (id == null || (url = getSkinUrl(id)) == null) {
            return null;
        }
        return new PlayerHeadTexture(url);
    }

    public static String getSkinUrl(UUID id) {
        String base64Textures;
        UuidToProfileResponse res2 = (UuidToProfileResponse) Http.get("https://sessionserver.mojang.com/session/minecraft/profile/" + String.valueOf(id)).exceptionHandler(e -> {
            MeteorClient.LOG.error("Could not contact mojang session servers.", e);
        }).sendJson(UuidToProfileResponse.class);
        if (res2 == null || (base64Textures = res2.getPropertyValue("textures")) == null) {
            return null;
        }
        TexturesJson textures = (TexturesJson) new Gson().fromJson(new String(Base64.getDecoder().decode(base64Textures)), TexturesJson.class);
        if (textures.textures.SKIN == null) {
            return null;
        }
        return textures.textures.SKIN.url;
    }
}
