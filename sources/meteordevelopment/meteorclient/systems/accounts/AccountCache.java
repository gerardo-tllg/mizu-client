package meteordevelopment.meteorclient.systems.accounts;

import com.mojang.util.UndashedUuid;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.NbtException;
import meteordevelopment.meteorclient.utils.render.PlayerHeadTexture;
import meteordevelopment.meteorclient.utils.render.PlayerHeadUtils;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/AccountCache.class */
public class AccountCache implements ISerializable<AccountCache> {
    public String username = "";
    public String uuid = "";
    private PlayerHeadTexture headTexture;

    public PlayerHeadTexture getHeadTexture() {
        return this.headTexture != null ? this.headTexture : PlayerHeadUtils.STEVE_HEAD;
    }

    public void loadHead() {
        if (this.uuid == null || this.uuid.isBlank()) {
            return;
        }
        MeteorClient.mc.execute(() -> {
            this.headTexture = PlayerHeadUtils.fetchHead(UndashedUuid.fromStringLenient(this.uuid));
        });
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10582("username", this.username);
        tag.method_10582("uuid", this.uuid);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public AccountCache fromTag(class_2487 tag) {
        if (tag.method_10558("username").isEmpty() || tag.method_10558("uuid").isEmpty()) {
            throw new NbtException();
        }
        this.username = (String) tag.method_10558("username").get();
        this.uuid = (String) tag.method_10558("uuid").get();
        loadHead();
        return this;
    }
}
