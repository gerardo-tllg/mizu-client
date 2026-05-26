package meteordevelopment.meteorclient.systems.friends;

import com.mojang.util.UndashedUuid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.class_1657;
import net.minecraft.class_2487;
import net.minecraft.class_640;
import org.jetbrains.annotations.NotNull;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/friends/Friends.class */
public class Friends extends System<Friends> implements Iterable<Friend> {
    private final List<Friend> friends;

    public Friends() {
        super("friends");
        this.friends = new ArrayList();
    }

    public static Friends get() {
        return (Friends) Systems.get(Friends.class);
    }

    public boolean add(Friend friend) {
        if (!friend.name.isEmpty() && !friend.name.contains(" ") && !this.friends.contains(friend)) {
            this.friends.add(friend);
            save();
            return true;
        }
        return false;
    }

    public boolean remove(Friend friend) {
        if (this.friends.remove(friend)) {
            save();
            return true;
        }
        return false;
    }

    public Friend get(String name) {
        for (Friend friend : this.friends) {
            if (friend.name.equalsIgnoreCase(name)) {
                return friend;
            }
        }
        return null;
    }

    public Friend get(class_1657 player) {
        return get(player.method_5477().getString());
    }

    public Friend get(class_640 player) {
        return get(player.method_2966().getName());
    }

    public boolean isFriend(class_1657 player) {
        return (player == null || get(player) == null) ? false : true;
    }

    public boolean isFriend(class_640 player) {
        return get(player) != null;
    }

    public boolean shouldAttack(class_1657 player) {
        return !isFriend(player);
    }

    public int count() {
        return this.friends.size();
    }

    public boolean isEmpty() {
        return this.friends.isEmpty();
    }

    @Override // java.lang.Iterable
    @NotNull
    public Iterator<Friend> iterator() {
        return this.friends.iterator();
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10566("friends", NbtUtils.listToTag(this.friends));
        return tag;
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Friends fromTag(class_2487 tag) {
        Friend friend;
        this.friends.clear();
        for (class_2487 friendTag : tag.method_68569("friends")) {
            if (friendTag.method_10545("name")) {
                String name = friendTag.method_68564("name", "");
                if (get(name) == null) {
                    String uuid = friendTag.method_68564("id", "");
                    if (!uuid.isBlank()) {
                        friend = new Friend(name, UndashedUuid.fromStringLenient(uuid));
                    } else {
                        friend = new Friend(name);
                    }
                    Friend friend2 = friend;
                    this.friends.add(friend2);
                }
            }
        }
        Collections.sort(this.friends);
        MeteorExecutor.execute(() -> {
            this.friends.forEach((v0) -> {
                v0.updateInfo();
            });
        });
        return this;
    }
}
