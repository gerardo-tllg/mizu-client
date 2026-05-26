package meteordevelopment.meteorclient.systems.waypoints;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.waypoints.events.WaypointAddedEvent;
import meteordevelopment.meteorclient.systems.waypoints.events.WaypointRemovedEvent;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.files.StreamUtils;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_2487;
import net.minecraft.class_2520;
import org.jetbrains.annotations.NotNull;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/waypoints/Waypoints.class */
public class Waypoints extends System<Waypoints> implements Iterable<Waypoint> {
    public static final String[] BUILTIN_ICONS = {"square", "circle", "triangle", "star", "diamond", "skull"};
    public final Map<String, class_1044> icons;
    private final List<Waypoint> waypoints;

    public Waypoints() {
        super(null);
        this.icons = new ConcurrentHashMap();
        this.waypoints = Collections.synchronizedList(new ArrayList());
    }

    public static Waypoints get() {
        return (Waypoints) Systems.get(Waypoints.class);
    }

    @Override // meteordevelopment.meteorclient.systems.System
    public void init() {
        File iconsFolder = new File(new File(MeteorClient.FOLDER, "waypoints"), "icons");
        iconsFolder.mkdirs();
        for (String builtinIcon : BUILTIN_ICONS) {
            File iconFile = new File(iconsFolder, builtinIcon + ".png");
            if (!iconFile.exists()) {
                copyIcon(iconFile);
            }
        }
        File[] files = iconsFolder.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.getName().endsWith(".png")) {
                try {
                    String name = file.getName().replace(".png", "");
                    class_1044 texture = new class_1043((Supplier) null, class_1011.method_4309(new FileInputStream(file)));
                    this.icons.put(name, texture);
                } catch (IOException e) {
                    MeteorClient.LOG.error("Failed to read a waypoint icon", e);
                }
            }
        }
    }

    public boolean add(Waypoint waypoint) {
        if (this.waypoints.contains(waypoint)) {
            save();
            return true;
        }
        this.waypoints.add(waypoint);
        save();
        MeteorClient.EVENT_BUS.post(new WaypointAddedEvent(waypoint));
        return false;
    }

    public boolean remove(Waypoint waypoint) {
        boolean removed = this.waypoints.remove(waypoint);
        if (removed) {
            save();
            MeteorClient.EVENT_BUS.post(new WaypointRemovedEvent(waypoint));
        }
        return removed;
    }

    public Waypoint get(String name) {
        for (Waypoint waypoint : this.waypoints) {
            if (waypoint.name.get().equalsIgnoreCase(name)) {
                return waypoint;
            }
        }
        return null;
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        load();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onGameDisconnected(GameLeftEvent event) {
        this.waypoints.clear();
    }

    public static boolean checkDimension(Waypoint waypoint) {
        Dimension playerDim = PlayerUtils.getDimension();
        Dimension waypointDim = waypoint.dimension.get();
        if (playerDim == waypointDim) {
            return true;
        }
        if (!waypoint.opposite.get().booleanValue()) {
            return false;
        }
        boolean playerOpp = playerDim == Dimension.Overworld || playerDim == Dimension.Nether;
        boolean waypointOpp = waypointDim == Dimension.Overworld || waypointDim == Dimension.Nether;
        return playerOpp && waypointOpp;
    }

    @Override // meteordevelopment.meteorclient.systems.System
    public File getFile() {
        if (Utils.canUpdate()) {
            return new File(new File(MeteorClient.FOLDER, "waypoints"), Utils.getFileWorldName() + ".nbt");
        }
        return null;
    }

    public boolean isEmpty() {
        return this.waypoints.isEmpty();
    }

    @Override // java.lang.Iterable
    @NotNull
    public Iterator<Waypoint> iterator() {
        return new WaypointIterator();
    }

    private void copyIcon(File file) {
        String path = "/assets/meteor-client/textures/icons/waypoints/" + file.getName();
        InputStream in = Waypoints.class.getResourceAsStream(path);
        if (in == null) {
            MeteorClient.LOG.error("Failed to read a resource: {}", path);
        } else {
            StreamUtils.copy(in, file);
        }
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10566("waypoints", NbtUtils.listToTag(this.waypoints));
        return tag;
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Waypoints fromTag2(class_2487 tag) {
        this.waypoints.clear();
        for (class_2520 waypointTag : tag.method_68569("waypoints")) {
            this.waypoints.add(new Waypoint(waypointTag));
        }
        return this;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/waypoints/Waypoints$WaypointIterator.class */
    private final class WaypointIterator implements Iterator<Waypoint> {
        private final Iterator<Waypoint> it;

        private WaypointIterator() {
            this.it = Waypoints.this.waypoints.iterator();
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.it.hasNext();
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // java.util.Iterator
        public Waypoint next() {
            return this.it.next();
        }

        @Override // java.util.Iterator
        public void remove() {
            this.it.remove();
            Waypoints.this.save();
        }
    }
}
