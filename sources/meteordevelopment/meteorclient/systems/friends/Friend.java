package meteordevelopment.meteorclient.systems.friends;

import com.mojang.util.UndashedUuid;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.render.PlayerHeadTexture;
import meteordevelopment.meteorclient.utils.render.PlayerHeadUtils;
import net.minecraft.class_1657;
import net.minecraft.class_2487;
import org.jetbrains.annotations.NotNull;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/friends/Friend.class */
public class Friend implements ISerializable<Friend>, Comparable<Friend> {
    public volatile String name;

    @Nullable
    private volatile UUID id;

    @Nullable
    private volatile PlayerHeadTexture headTexture;
    private volatile boolean updating;

    public Friend(String name, @Nullable UUID id) {
        this.name = name;
        this.id = id;
        this.headTexture = null;
    }

    public Friend(class_1657 player) {
        this(player.method_5477().getString(), player.method_5667());
    }

    public Friend(String name) {
        this(name, null);
    }

    public String getName() {
        return this.name;
    }

    public PlayerHeadTexture getHead() {
        return this.headTexture != null ? this.headTexture : PlayerHeadUtils.STEVE_HEAD;
    }

    public void updateInfo() {
        this.updating = true;
        APIResponse res = (APIResponse) Http.get("https://api.mojang.com/users/profiles/minecraft/" + this.name).sendJson(APIResponse.class);
        if (res == null || res.name == null || res.id == null) {
            return;
        }
        this.name = res.name;
        this.id = UndashedUuid.fromStringLenient(res.id);
        MeteorClient.mc.execute(() -> {
            this.headTexture = PlayerHeadUtils.fetchHead(this.id);
        });
        this.updating = false;
    }

    public boolean headTextureNeedsUpdate() {
        return !this.updating && this.headTexture == null;
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10582("name", this.name);
        if (this.id != null) {
            tag.method_10582("id", UndashedUuid.toString(this.id));
        }
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Friend fromTag2(class_2487 tag) {
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Friend friend = (Friend) o;
        return Objects.equals(this.name, friend.name);
    }

    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override // java.lang.Comparable
    public int compareTo(@NotNull Friend friend) {
        return this.name.compareTo(friend.name);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/friends/Friend$APIResponse.class */
    private static class APIResponse {
        String name;
        String id;

        private APIResponse() {
        }
    }
}
