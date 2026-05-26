package meteordevelopment.meteorclient.systems.modules.world;

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
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
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
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1802;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2591;
import net.minecraft.class_2595;
import net.minecraft.class_2601;
import net.minecraft.class_2609;
import net.minecraft.class_2611;
import net.minecraft.class_2614;
import net.minecraft.class_2627;
import net.minecraft.class_3719;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/StashFinder.class */
public class StashFinder extends Module {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final SettingGroup sgGeneral;
    private final Setting<List<class_2591<?>>> storageBlocks;
    private final Setting<Integer> minimumStorageCount;
    private final Setting<Integer> minimumDistance;
    private final Setting<Boolean> sendNotifications;
    private final Setting<Mode> notificationMode;
    public List<Chunk> chunks;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/StashFinder$Mode.class */
    public enum Mode {
        Chat,
        Toast,
        Both
    }

    public StashFinder() {
        super(Categories.World, "stash-finder", "Searches loaded chunks for storage blocks. Saves to <your minecraft folder>/meteor-client");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.storageBlocks = this.sgGeneral.add(new StorageBlockListSetting.Builder().name("storage-blocks").description("Select the storage blocks to search for.").defaultValue(StorageBlockListSetting.STORAGE_BLOCKS).build());
        this.minimumStorageCount = this.sgGeneral.add(new IntSetting.Builder().name("minimum-storage-count").description("The minimum amount of storage blocks in a chunk to record the chunk.").defaultValue(4).min(1).sliderMin(1).build());
        this.minimumDistance = this.sgGeneral.add(new IntSetting.Builder().name("minimum-distance").description("The minimum distance you must be from spawn to record a certain chunk.").defaultValue(0).min(0).sliderMax(10000).build());
        this.sendNotifications = this.sgGeneral.add(new BoolSetting.Builder().name("notifications").description("Sends Minecraft notifications when new stashes are found.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgGeneral;
        EnumSetting.Builder builderDefaultValue = new EnumSetting.Builder().name("notification-mode").description("The mode to use for notifications.").defaultValue(Mode.Both);
        Setting<Boolean> setting = this.sendNotifications;
        Objects.requireNonNull(setting);
        this.notificationMode = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        this.chunks = new ArrayList();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        load();
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        double chunkXAbs = Math.abs(event.chunk().method_12004().field_9181 * 16);
        double chunkZAbs = Math.abs(event.chunk().method_12004().field_9180 * 16);
        if (Math.sqrt((chunkXAbs * chunkXAbs) + (chunkZAbs * chunkZAbs)) < this.minimumDistance.get().intValue()) {
        }
        Chunk chunk = new Chunk(event.chunk().method_12004());
        for (class_2586 blockEntity : event.chunk().method_12214().values()) {
            if (this.storageBlocks.get().contains(blockEntity.method_11017())) {
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
                }
            }
        }
        if (chunk.getTotal() >= this.minimumStorageCount.get().intValue()) {
            Chunk prevChunk = null;
            int i = this.chunks.indexOf(chunk);
            if (i < 0) {
                this.chunks.add(chunk);
            } else {
                prevChunk = this.chunks.set(i, chunk);
            }
            saveJson();
            saveCsv();
            if (this.sendNotifications.get().booleanValue()) {
                if (!chunk.equals(prevChunk) || !chunk.countsEqual(prevChunk)) {
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
                if (this.chunks.remove(chunk)) {
                    table.clear();
                    fillTable(theme, table);
                    saveJson();
                    saveCsv();
                }
            };
            table.row();
        }
    }

    /* JADX WARN: Type inference failed for: r3v0, types: [meteordevelopment.meteorclient.systems.modules.world.StashFinder$1] */
    private void load() {
        boolean loaded = false;
        File file = getJsonFile();
        if (file.exists()) {
            try {
                FileReader reader = new FileReader(file);
                this.chunks = (List) GSON.fromJson(reader, new TypeToken<List<Chunk>>() { // from class: meteordevelopment.meteorclient.systems.modules.world.StashFinder.1
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
        return new File(new File(new File(MeteorClient.FOLDER, "stashes"), Utils.getFileWorldName()), "stashes.json");
    }

    private File getCsvFile() {
        return new File(new File(new File(MeteorClient.FOLDER, "stashes"), Utils.getFileWorldName()), "stashes.csv");
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return String.valueOf(this.chunks.size());
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/StashFinder$Chunk.class */
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

        public Chunk(class_1923 chunkPos) {
            this.chunkPos = chunkPos;
            calculatePos();
        }

        public void calculatePos() {
            this.x = (this.chunkPos.field_9181 * 16) + 8;
            this.z = (this.chunkPos.field_9180 * 16) + 8;
        }

        public int getTotal() {
            return this.chests + this.barrels + this.shulkers + this.enderChests + this.furnaces + this.dispensersDroppers + this.hoppers;
        }

        public void write(Writer writer) throws IOException {
            sb.setLength(0);
            sb.append(this.x).append(',').append(this.z).append(',');
            sb.append(this.chests).append(',').append(this.barrels).append(',').append(this.shulkers).append(',').append(this.enderChests).append(',').append(this.furnaces).append(',').append(this.dispensersDroppers).append(',').append(this.hoppers).append('\n');
            writer.write(sb.toString());
        }

        public boolean countsEqual(Chunk c) {
            if (c == null) {
                return false;
            }
            return (this.chests == c.chests && this.barrels == c.barrels && this.shulkers == c.shulkers && this.enderChests == c.enderChests && this.furnaces == c.furnaces && this.dispensersDroppers == c.dispensersDroppers && this.hoppers == c.hoppers) ? false : true;
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

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/StashFinder$ChunkScreen.class */
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
        }
    }
}
