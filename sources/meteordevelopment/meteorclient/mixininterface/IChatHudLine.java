package meteordevelopment.meteorclient.mixininterface;

import com.mojang.authlib.GameProfile;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixininterface/IChatHudLine.class */
public interface IChatHudLine {
    String meteor$getText();

    int meteor$getId();

    void meteor$setId(int i);

    GameProfile meteor$getSender();

    void meteor$setSender(GameProfile gameProfile);
}
