package meteordevelopment.meteorclient.systems.profiles;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2487;
import org.jetbrains.annotations.NotNull;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/profiles/Profiles.class */
public class Profiles extends System<Profiles> implements Iterable<Profile> {
    public static final File FOLDER = new File(MeteorClient.FOLDER, "profiles");
    private List<Profile> profiles;

    public Profiles() {
        super("profiles");
        this.profiles = new ArrayList();
    }

    public static Profiles get() {
        return (Profiles) Systems.get(Profiles.class);
    }

    public void add(Profile profile) {
        if (!this.profiles.contains(profile)) {
            this.profiles.add(profile);
        }
        profile.save();
        save();
    }

    public void remove(Profile profile) {
        if (this.profiles.remove(profile)) {
            profile.delete();
        }
        save();
    }

    public Profile get(String name) {
        for (Profile profile : this) {
            if (profile.name.get().equalsIgnoreCase(name)) {
                return profile;
            }
        }
        return null;
    }

    public List<Profile> getAll() {
        return this.profiles;
    }

    @Override // meteordevelopment.meteorclient.systems.System
    public File getFile() {
        return new File(FOLDER, "profiles.nbt");
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        for (Profile profile : this) {
            if (profile.loadOnJoin.get().contains(Utils.getWorldName())) {
                profile.load();
            }
        }
    }

    public boolean isEmpty() {
        return this.profiles.isEmpty();
    }

    @Override // java.lang.Iterable
    @NotNull
    public Iterator<Profile> iterator() {
        return this.profiles.iterator();
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10566("profiles", NbtUtils.listToTag(this.profiles));
        return tag;
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Profiles fromTag2(class_2487 tag) {
        this.profiles = NbtUtils.listFromTag(tag.method_68569("profiles"), Profile::new);
        return this;
    }
}
