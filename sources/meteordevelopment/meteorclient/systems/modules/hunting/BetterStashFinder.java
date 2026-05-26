package meteordevelopment.meteorclient.systems.modules.hunting;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StorageBlockListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1802;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2591;
import net.minecraft.class_2595;
import net.minecraft.class_2601;
import net.minecraft.class_2609;
import net.minecraft.class_2611;
import net.minecraft.class_2614;
import net.minecraft.class_2627;
import net.minecraft.class_3719;
import net.minecraft.class_418;
import net.minecraft.class_5321;
import net.minecraft.class_8887;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.map.mods.SupportMods;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ChunkDataEvent;
import xaeroplus.module.ModuleManager;
import xaeroplus.module.impl.OldChunks;
import xaeroplus.module.impl.PaletteNewChunks;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/BetterStashFinder.class */
public class BetterStashFinder extends Module {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final SettingGroup sgGeneral;
    public List<Chunk> chunks;
    private final Setting<List<class_2591<?>>> storageBlocks;
    private final Setting<Integer> minimumStorageCount;
    private final Setting<Boolean> shulkerInstantHit;
    private final Setting<Boolean> crafterInstantHit;
    private final Setting<Boolean> disableOnTeleport;
    private final Setting<Boolean> ignoreTrialChambers;
    private final Setting<Integer> minimumDistance;
    private final Setting<Boolean> onlyOldchunks;
    private final Setting<Boolean> saveToWaypoints;
    private final Setting<Boolean> sendNotifications;
    private final Setting<Mode> notificationMode;
    private final Setting<Boolean> sendWebhook;
    public final Setting<String> webhookLink;
    public final Setting<Boolean> advancedLogging;
    public final Setting<Boolean> ping;
    public final Setting<String> discordId;
    private class_243 lastPosition;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/BetterStashFinder$Mode.class */
    public enum Mode {
        Chat,
        Toast,
        Both
    }

    public BetterStashFinder() {
        super(Categories.Hunting, "better-stash-finder", "Meteors StashFinder but with more features.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.chunks = new ArrayList();
        this.storageBlocks = this.sgGeneral.add(new StorageBlockListSetting.Builder().name("storage-blocks").description("Select the storage blocks to search for.").defaultValue(StorageBlockListSetting.STORAGE_BLOCKS).build());
        this.minimumStorageCount = this.sgGeneral.add(new IntSetting.Builder().name("minimum-storage-count").description("The minimum amount of storage blocks in a chunk to record the chunk.").defaultValue(4).min(1).sliderMin(1).build());
        this.shulkerInstantHit = this.sgGeneral.add(new BoolSetting.Builder().name("shulker-instant-hit").description("If a single shulker counts as a stash.").defaultValue(false).build());
        this.crafterInstantHit = this.sgGeneral.add(new BoolSetting.Builder().name("crafter-instant-hit").description("If a single auto crafter counts as a stash.").defaultValue(false).build());
        this.disableOnTeleport = this.sgGeneral.add(new BoolSetting.Builder().name("disable-on-teleport-or-death").description("If on, will disable this module when respawning or teleporting to try to prevent coord leaks.").defaultValue(false).build());
        this.ignoreTrialChambers = this.sgGeneral.add(new BoolSetting.Builder().name("ignore-trial-chambers").description("Attempts to ignore trial chambers, but may cause false negatives if someone made their base to look like a trial chamber.").defaultValue(true).build());
        this.minimumDistance = this.sgGeneral.add(new IntSetting.Builder().name("minimum-distance").description("The minimum distance you must be from spawn to record a certain chunk.").defaultValue(0).min(0).sliderMax(10000).build());
        this.onlyOldchunks = this.sgGeneral.add(new BoolSetting.Builder().name("only-old-chunks").description("Checks that the chunks it scans have already been loaded.").defaultValue(true).build());
        this.saveToWaypoints = this.sgGeneral.add(new BoolSetting.Builder().name("save-to-waypoints").description("Creates xaeros minimap waypoints for stash finds.").defaultValue(false).onChanged((v1) -> {
            waypointSettingChanged(v1);
        }).build());
        this.sendNotifications = this.sgGeneral.add(new BoolSetting.Builder().name("notifications").description("Sends Minecraft notifications when new stashes are found.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgGeneral;
        EnumSetting.Builder builderDefaultValue = new EnumSetting.Builder().name("notification-mode").description("The mode to use for notifications.").defaultValue(Mode.Both);
        Setting<Boolean> setting = this.sendNotifications;
        Objects.requireNonNull(setting);
        this.notificationMode = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        this.sendWebhook = this.sgGeneral.add(new BoolSetting.Builder().name("send-webhook").description("Sends a webhook when a stash is found.").defaultValue(false).build());
        SettingGroup settingGroup2 = this.sgGeneral;
        StringSetting.Builder builderDefaultValue2 = new StringSetting.Builder().name("webhook-link").description("A discord webhook link. Looks like this: https://discord.com/api/webhooks/webhookUserId/webHookTokenOrSomething").defaultValue("");
        Setting<Boolean> setting2 = this.sendWebhook;
        Objects.requireNonNull(setting2);
        this.webhookLink = settingGroup2.add(builderDefaultValue2.visible(setting2::get).build());
        this.advancedLogging = this.sgGeneral.add(new BoolSetting.Builder().name("advanced-logging").description("Will log more information, including the amount of each container found.").defaultValue(false).build());
        SettingGroup settingGroup3 = this.sgGeneral;
        BoolSetting.Builder builderDefaultValue3 = new BoolSetting.Builder().name("ping-for-stash-finder").description("Pings you for stash finder and base finder messages").defaultValue(false);
        Setting<Boolean> setting3 = this.sendWebhook;
        Objects.requireNonNull(setting3);
        this.ping = settingGroup3.add(builderDefaultValue3.visible(setting3::get).build());
        this.discordId = this.sgGeneral.add(new StringSetting.Builder().name("discord-ID").description("Your discord ID").defaultValue("").visible(() -> {
            return this.sendWebhook.get().booleanValue() && this.ping.get().booleanValue();
        }).build());
        this.lastPosition = null;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.lastPosition = null;
        XaeroPlus.EVENT_BUS.register(this);
        load();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        XaeroPlus.EVENT_BUS.unregister(this);
    }

    @EventHandler(priority = -1)
    public void onChunkData(ChunkDataEvent event) {
        WaypointSet waypointSet;
        if (event.seenChunk()) {
            return;
        }
        double chunkXAbs = Math.abs(event.chunk().method_12004().field_9181 * 16);
        double chunkZAbs = Math.abs(event.chunk().method_12004().field_9180 * 16);
        if (Math.sqrt((chunkXAbs * chunkXAbs) + (chunkZAbs * chunkZAbs)) < this.minimumDistance.get().intValue()) {
            return;
        }
        Chunk chunk = new Chunk(event.chunk().method_12004());
        class_5321<class_1937> currentDimension = this.mc.field_1687.method_27983();
        class_1923 chunkPos = chunk.chunkPos;
        PaletteNewChunks paletteNewChunks = ModuleManager.getModule(PaletteNewChunks.class);
        boolean is119NewChunk = paletteNewChunks.isNewChunk(chunkPos.field_9181, chunkPos.field_9180, currentDimension);
        boolean is112OldChunk = ModuleManager.getModule(OldChunks.class).isOldChunk(chunkPos.field_9181, chunkPos.field_9180, currentDimension);
        if (this.onlyOldchunks.get().booleanValue() && is119NewChunk && !is112OldChunk) {
            return;
        }
        for (class_2586 blockEntity : event.chunk().method_12214().values()) {
            if (this.storageBlocks.get().contains(blockEntity.method_11017())) {
                class_2248 blockUnder = this.mc.field_1687.method_8320(blockEntity.method_11016().method_10074()).method_26204();
                if (!this.ignoreTrialChambers.get().booleanValue() || !blockUnder.equals(class_2246.field_33408)) {
                    if (!blockUnder.equals(class_2246.field_47035) && !blockUnder.equals(class_2246.field_27133) && !blockUnder.equals(class_2246.field_33407)) {
                        if (blockEntity instanceof class_2595) {
                            chunk.chests++;
                        } else if (blockEntity instanceof class_3719) {
                            chunk.barrels++;
                        } else if (blockEntity instanceof class_2627) {
                            chunk.shulkers++;
                        } else if (blockEntity instanceof class_2611) {
                            chunk.enderChests++;
                        } else if (blockEntity instanceof class_2609) {
                            chunk.furnaces++;
                        } else if (blockEntity instanceof class_2601) {
                            chunk.dispensersDroppers++;
                        } else if (blockEntity instanceof class_2614) {
                            chunk.hoppers++;
                        } else if (blockEntity instanceof class_8887) {
                            chunk.crafters++;
                        }
                    }
                }
            }
        }
        if (chunk.getTotal() >= this.minimumStorageCount.get().intValue() || ((this.shulkerInstantHit.get().booleanValue() && chunk.shulkers > 0) || (this.crafterInstantHit.get().booleanValue() && chunk.crafters > 0))) {
            Chunk prevChunk = null;
            int i = this.chunks.indexOf(chunk);
            if (i < 0) {
                this.chunks.add(chunk);
            } else {
                prevChunk = this.chunks.set(i, chunk);
            }
            saveJson();
            saveCsv();
            if (!chunk.equals(prevChunk) || !chunk.countsEqual(prevChunk)) {
                if (this.sendNotifications.get().booleanValue()) {
                    switch (this.notificationMode.get()) {
                        case Chat:
                            info("Found stash at (highlight)%s(default), (highlight)%s(default).", Integer.valueOf(chunk.x), Integer.valueOf(chunk.z));
                            break;
                        case Toast:
                            this.mc.method_1566().method_1999(new MeteorToast(class_1802.field_8106, this.title, "Found Stash!"));
                            break;
                        case Both:
                            info("Found stash at (highlight)%s(default), (highlight)%s(default).", Integer.valueOf(chunk.x), Integer.valueOf(chunk.z));
                            this.mc.method_1566().method_1999(new MeteorToast(class_1802.field_8106, this.title, "Found Stash!"));
                            break;
                    }
                }
                if (this.sendWebhook.get().booleanValue() && !this.webhookLink.get().isEmpty()) {
                    if (this.advancedLogging.get().booleanValue()) {
                        String chunkType = "";
                        if (is119NewChunk && !is112OldChunk) {
                            chunkType = "new";
                        } else if (is119NewChunk && is112OldChunk) {
                            chunkType = "unfollowed 1.12";
                        } else if (!is119NewChunk && !is112OldChunk) {
                            chunkType = "1.19";
                        } else if (!is119NewChunk && is112OldChunk) {
                            chunkType = "followed 1.12";
                        }
                        String json = "{\"embeds\": [{\"title\": \"Stash Found!\",\"color\": 2154012,\"description\": \"Coordinates: || X: " + chunk.x + " Z: " + chunk.z + "|| in " + chunkType + " chunks\",\"fields\": [{\"name\": \"Chests\",\"value\": " + chunk.chests + ",\"inline\": true},{\"name\": \"Barrels\",\"value\": " + chunk.barrels + ",\"inline\": true},{\"name\": \"Shulkers\",\"value\": " + chunk.shulkers + ",\"inline\": true},{\"name\": \"Ender Chests\",\"value\": " + chunk.enderChests + ",\"inline\": true},{\"name\": \"Hoppers\",\"value\": " + chunk.hoppers + ",\"inline\": true},{\"name\": \"Dispensers/Droppers\",\"value\": " + chunk.dispensersDroppers + ",\"inline\": true},{\"name\": \"Furnaces\",\"value\": " + chunk.furnaces + ",\"inline\": true},{\"name\": \"Crafters\",\"value\": " + chunk.crafters + ",\"inline\": true}]}]}";
                        new Thread(() -> {
                            HuntingUtils.sendWebhook(this.webhookLink.get(), json, this.ping.get().booleanValue() ? this.discordId.get() : null);
                        }).start();
                    } else {
                        String message = "Found stash at " + chunk.x + ", " + chunk.z + ".";
                        new Thread(() -> {
                            HuntingUtils.sendWebhook(this.webhookLink.get(), this.title, message, this.ping.get().booleanValue() ? this.discordId.get() : null, this.mc.field_1724.method_7334().getName());
                        }).start();
                    }
                }
                if (!this.saveToWaypoints.get().booleanValue() || (waypointSet = getWaypointSet()) == null) {
                    return;
                }
                addToWaypoints(waypointSet, chunk);
                SupportMods.xaeroMinimap.requestWaypointsRefresh();
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public WWidget getWidget(GuiTheme theme) {
        this.chunks.sort(Comparator.comparingInt(value -> {
            return -value.getTotal();
        }));
        WVerticalList list = theme.verticalList();
        WButton clear = (WButton) list.add(theme.button("Clear")).widget();
        WTable table = new WTable();
        if (!this.chunks.isEmpty()) {
            list.add(table);
        }
        clear.action = () -> {
            removeAllStashWaypoints(this.chunks);
            this.chunks.clear();
            table.clear();
        };
        fillTable(theme, table);
        return list;
    }

    private void fillTable(GuiTheme theme, WTable table) {
        for (Chunk chunk : this.chunks) {
            table.add(theme.label("Pos: " + chunk.x + ", " + chunk.z));
            table.add(theme.label("Total: " + chunk.getTotal()));
            WButton open = (WButton) table.add(theme.button("Open")).widget();
            open.action = () -> {
                this.mc.method_1507(new ChunkScreen(theme, chunk));
            };
            WButton gotoBtn = (WButton) table.add(theme.button("Goto")).widget();
            gotoBtn.action = () -> {
                PathManagers.get().moveTo(new class_2338(chunk.x, 0, chunk.z), true);
            };
            WMinus delete = (WMinus) table.add(theme.minus()).widget();
            delete.action = () -> {
                WaypointSet waypointSet;
                if (this.chunks.remove(chunk)) {
                    table.clear();
                    fillTable(theme, table);
                    saveJson();
                    saveCsv();
                    Waypoint waypoint = getWaypointByCoordinate(chunk.x, chunk.z);
                    if (waypoint != null && (waypointSet = getWaypointSet()) != null) {
                        waypointSet.remove(waypoint);
                        SupportMods.xaeroMinimap.requestWaypointsRefresh();
                    }
                }
            };
            table.row();
        }
    }

    /* JADX WARN: Type inference failed for: r3v0, types: [meteordevelopment.meteorclient.systems.modules.hunting.BetterStashFinder$1] */
    private void load() {
        boolean loaded = false;
        File file = getJsonFile();
        if (file.exists()) {
            try {
                FileReader reader = new FileReader(file);
                this.chunks = (List) GSON.fromJson(reader, new TypeToken<List<Chunk>>() { // from class: meteordevelopment.meteorclient.systems.modules.hunting.BetterStashFinder.1
                }.getType());
                reader.close();
                Iterator<Chunk> it = this.chunks.iterator();
                while (it.hasNext()) {
                    it.next().calculatePos();
                }
                loaded = true;
            } catch (Exception e) {
                if (this.chunks == null) {
                    this.chunks = new ArrayList();
                }
            }
        }
        File file2 = getCsvFile();
        if (!loaded && file2.exists()) {
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(file2));
                reader2.readLine();
                while (true) {
                    String line = reader2.readLine();
                    if (line != null) {
                        String[] values = line.split(" ");
                        Chunk chunk = new Chunk(new class_1923(Integer.parseInt(values[0]), Integer.parseInt(values[1])));
                        chunk.chests = Integer.parseInt(values[2]);
                        chunk.shulkers = Integer.parseInt(values[3]);
                        chunk.enderChests = Integer.parseInt(values[4]);
                        chunk.furnaces = Integer.parseInt(values[5]);
                        chunk.dispensersDroppers = Integer.parseInt(values[6]);
                        chunk.hoppers = Integer.parseInt(values[7]);
                        this.chunks.add(chunk);
                    } else {
                        reader2.close();
                        return;
                    }
                }
            } catch (Exception e2) {
                if (this.chunks == null) {
                    this.chunks = new ArrayList();
                }
            }
        }
    }

    private void saveCsv() {
        try {
            File file = getCsvFile();
            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            writer.write("X,Z,Chests,Barrels,Shulkers,EnderChests,Furnaces,DispensersDroppers,Hoppers\n");
            for (Chunk chunk : this.chunks) {
                chunk.write(writer);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveJson() {
        try {
            File file = getJsonFile();
            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            GSON.toJson(this.chunks, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getJsonFile() {
        return new File(new File(new File(MeteorClient.FOLDER, "better-stash-finder"), Utils.getFileWorldName()), "stashes.json");
    }

    private File getCsvFile() {
        return new File(new File(new File(MeteorClient.FOLDER, "better-stash-finder"), Utils.getFileWorldName()), "stashes.csv");
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return String.valueOf(this.chunks.size());
    }

    private Waypoint getWaypointByCoordinate(int x, int z) {
        WaypointSet waypointSet = getWaypointSet();
        if (waypointSet == null) {
            return null;
        }
        for (Waypoint waypoint : waypointSet.getWaypoints()) {
            if (waypoint.getX() == x && waypoint.getZ() == z) {
                return waypoint;
            }
        }
        return null;
    }

    private void removeAllStashWaypoints(List<Chunk> chunks) {
        WaypointSet waypointSet = getWaypointSet();
        if (waypointSet == null) {
            return;
        }
        for (Chunk chunk : chunks) {
            Waypoint waypoint = getWaypointByCoordinate(chunk.x, chunk.z);
            if (waypoint != null) {
                waypointSet.remove(waypoint);
            }
        }
        SupportMods.xaeroMinimap.requestWaypointsRefresh();
    }

    private WaypointSet getWaypointSet() {
        MinimapWorld currentWorld;
        MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (minimapSession == null || (currentWorld = minimapSession.getWorldManager().getCurrentWorld()) == null) {
            return null;
        }
        return currentWorld.getCurrentWaypointSet();
    }

    private void addToWaypoints(WaypointSet waypointSet, Chunk chunk) {
        int x = chunk.x;
        int z = chunk.z;
        if (getWaypointByCoordinate(x, z) != null) {
            return;
        }
        String waypointName = getWaypointName(chunk);
        int color = 0;
        if (chunk.getTotal() < 15) {
            color = 10;
        } else if (chunk.getTotal() < 50) {
            color = 14;
        } else if (chunk.getTotal() < 100) {
            color = 12;
        } else if (chunk.getTotal() >= 100) {
            color = 4;
        }
        Waypoint waypoint = new Waypoint(x, 70, z, waypointName, "S", color, 0, false);
        waypointSet.add(waypoint);
    }

    private static String getWaypointName(Chunk chunk) {
        String waypointName;
        waypointName = "";
        waypointName = chunk.chests > 0 ? waypointName + "C:" + chunk.chests : "";
        if (chunk.barrels > 0) {
            waypointName = waypointName + "B:" + chunk.barrels;
        }
        if (chunk.shulkers > 0) {
            waypointName = waypointName + "S:" + chunk.shulkers;
        }
        if (chunk.enderChests > 0) {
            waypointName = waypointName + "E:" + chunk.enderChests;
        }
        if (chunk.hoppers > 0) {
            waypointName = waypointName + "H:" + chunk.hoppers;
        }
        if (chunk.dispensersDroppers > 0) {
            waypointName = waypointName + "D:" + chunk.dispensersDroppers;
        }
        if (chunk.furnaces > 0) {
            waypointName = waypointName + "F:" + chunk.furnaces;
        }
        if (chunk.crafters > 0) {
            waypointName = waypointName + "A:" + chunk.crafters;
        }
        return waypointName;
    }

    private void waypointSettingChanged(boolean enabled) {
        if (!enabled) {
            removeAllStashWaypoints(this.chunks);
            return;
        }
        WaypointSet waypointSet = getWaypointSet();
        if (waypointSet == null) {
            return;
        }
        for (Chunk chunk : this.chunks) {
            addToWaypoints(waypointSet, chunk);
        }
        SupportMods.xaeroMinimap.requestWaypointsRefresh();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/BetterStashFinder$Chunk.class */
    public static class Chunk {
        private static final StringBuilder sb = new StringBuilder();
        public class_1923 chunkPos;
        public transient int x;
        public transient int z;
        public int chests;
        public int barrels;
        public int shulkers;
        public int enderChests;
        public int furnaces;
        public int dispensersDroppers;
        public int hoppers;
        public int crafters;

        public Chunk(class_1923 chunkPos) {
            this.chunkPos = chunkPos;
            calculatePos();
        }

        public void calculatePos() {
            this.x = (this.chunkPos.field_9181 * 16) + 8;
            this.z = (this.chunkPos.field_9180 * 16) + 8;
        }

        public int getTotal() {
            return this.chests + this.barrels + this.shulkers + this.enderChests + this.furnaces + this.dispensersDroppers + this.hoppers + this.crafters;
        }

        public void write(Writer writer) throws IOException {
            sb.setLength(0);
            sb.append(this.x).append(',').append(this.z).append(',');
            sb.append(this.chests).append(',').append(this.barrels).append(',').append(this.shulkers).append(',').append(this.enderChests).append(',').append(this.furnaces).append(',').append(this.dispensersDroppers).append(',').append(this.hoppers).append(',').append(this.crafters).append('\n');
            writer.write(sb.toString());
        }

        public boolean countsEqual(Chunk c) {
            if (c == null) {
                return false;
            }
            return (this.chests == c.chests && this.barrels == c.barrels && this.shulkers == c.shulkers && this.enderChests == c.enderChests && this.furnaces == c.furnaces && this.dispensersDroppers == c.dispensersDroppers && this.hoppers == c.hoppers && this.crafters == c.crafters) ? false : true;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Chunk chunk = (Chunk) o;
            return Objects.equals(this.chunkPos, chunk.chunkPos);
        }

        public int hashCode() {
            return Objects.hash(this.chunkPos);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/BetterStashFinder$ChunkScreen.class */
    private static class ChunkScreen extends WindowScreen {
        private final Chunk chunk;

        public ChunkScreen(GuiTheme theme, Chunk chunk) {
            super(theme, "Chunk at " + chunk.x + ", " + chunk.z);
            this.chunk = chunk;
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public void initWidgets() {
            WTable t = (WTable) add(this.theme.table()).expandX().widget();
            t.add(this.theme.label("Total:"));
            t.add(this.theme.label(this.chunk.getTotal()));
            t.row();
            t.add(this.theme.horizontalSeparator()).expandX();
            t.row();
            t.add(this.theme.label("Chests:"));
            t.add(this.theme.label(this.chunk.chests));
            t.row();
            t.add(this.theme.label("Barrels:"));
            t.add(this.theme.label(this.chunk.barrels));
            t.row();
            t.add(this.theme.label("Shulkers:"));
            t.add(this.theme.label(this.chunk.shulkers));
            t.row();
            t.add(this.theme.label("Ender Chests:"));
            t.add(this.theme.label(this.chunk.enderChests));
            t.row();
            t.add(this.theme.label("Furnaces:"));
            t.add(this.theme.label(this.chunk.furnaces));
            t.row();
            t.add(this.theme.label("Dispensers and droppers:"));
            t.add(this.theme.label(this.chunk.dispensersDroppers));
            t.row();
            t.add(this.theme.label("Hoppers:"));
            t.add(this.theme.label(this.chunk.hoppers));
            t.row();
            t.add(this.theme.label("Crafters:"));
            t.add(this.theme.label(this.chunk.crafters));
        }
    }

    @meteordevelopment.orbit.EventHandler(priority = 100)
    private void onOpenScreenEvent(OpenScreenEvent event) {
        if ((event.screen instanceof class_418) && this.disableOnTeleport.get().booleanValue()) {
            toggle();
        }
    }

    @meteordevelopment.orbit.EventHandler(priority = 100)
    private void onPlayerMove(PlayerMoveEvent event) {
        if (this.lastPosition != null && this.disableOnTeleport.get().booleanValue() && this.mc.field_1724.method_5707(this.lastPosition) > 256.0d) {
            toggle();
        }
        this.lastPosition = this.mc.field_1724.method_19538();
    }
}
