package meteordevelopment.meteorclient.systems.modules.render.blockesp;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockDataSetting;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.GenericSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.RainbowColors;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2791;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/blockesp/BlockESP.class */
public class BlockESP extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<List<class_2248>> blocks;
    private final Setting<ESPBlockData> defaultBlockConfig;
    private final Setting<Map<class_2248, ESPBlockData>> blockConfigs;
    private final Setting<Boolean> tracers;
    private final Setting<Boolean> activatedSpawners;
    private final class_2338.class_2339 blockPos;
    private final Long2ObjectMap<ESPChunk> chunks;
    private final Set<ESPGroup> groups;
    private final ExecutorService workerThread;
    private Dimension lastDimension;

    public BlockESP() {
        super(Categories.Render, "block-esp", "Renders specified blocks through walls.", "search");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.blocks = this.sgGeneral.add(new BlockListSetting.Builder().name("blocks").description("Blocks to search for.").onChanged(blocks1 -> {
            if (!isActive() || !Utils.canUpdate()) {
                return;
            }
            onActivate();
        }).build());
        this.defaultBlockConfig = this.sgGeneral.add(new GenericSetting.Builder().name("default-block-config").description("Default block config.").defaultValue(new ESPBlockData(ShapeMode.Lines, new SettingColor(0, 255, 200), new SettingColor(0, 255, 200, 25), true, new SettingColor(0, 255, 200, Opcode.LUSHR))).build());
        this.blockConfigs = this.sgGeneral.add(new BlockDataSetting.Builder().name("block-configs").description("Config for each block.").defaultData(this.defaultBlockConfig).build());
        this.tracers = this.sgGeneral.add(new BoolSetting.Builder().name("tracers").description("Render tracer lines.").defaultValue(false).build());
        this.activatedSpawners = this.sgGeneral.add(new BoolSetting.Builder().name("activated-spawners").description("Only highlights activated spawners").defaultValue(true).visible(() -> {
            return this.blocks.get().contains(class_2246.field_10260);
        }).build());
        this.blockPos = new class_2338.class_2339();
        this.chunks = new Long2ObjectOpenHashMap();
        this.groups = new ReferenceOpenHashSet();
        this.workerThread = Executors.newSingleThreadExecutor();
        RainbowColors.register(this::onTickRainbow);
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        synchronized (this.chunks) {
            this.chunks.clear();
            this.groups.clear();
        }
        for (class_2791 chunk : Utils.chunks()) {
            searchChunk(chunk);
        }
        this.lastDimension = PlayerUtils.getDimension();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        synchronized (this.chunks) {
            this.chunks.clear();
            this.groups.clear();
        }
    }

    private void onTickRainbow() {
        if (isActive()) {
            this.defaultBlockConfig.get().tickRainbow();
            for (ESPBlockData blockData : this.blockConfigs.get().values()) {
                blockData.tickRainbow();
            }
        }
    }

    ESPBlockData getBlockData(class_2248 block) {
        ESPBlockData blockData = this.blockConfigs.get().get(block);
        return blockData == null ? this.defaultBlockConfig.get() : blockData;
    }

    private void updateChunk(int x, int z) {
        ESPChunk chunk = (ESPChunk) this.chunks.get(class_1923.method_8331(x, z));
        if (chunk != null) {
            chunk.update();
        }
    }

    private void updateBlock(int x, int y, int z) {
        ESPChunk chunk = (ESPChunk) this.chunks.get(class_1923.method_8331(x >> 4, z >> 4));
        if (chunk != null) {
            chunk.update(x, y, z);
        }
    }

    public ESPBlock getBlock(int x, int y, int z) {
        ESPChunk chunk = (ESPChunk) this.chunks.get(class_1923.method_8331(x >> 4, z >> 4));
        if (chunk == null) {
            return null;
        }
        return chunk.get(x, y, z);
    }

    public ESPGroup newGroup(class_2248 block) {
        ESPGroup group;
        synchronized (this.chunks) {
            group = new ESPGroup(block);
            this.groups.add(group);
        }
        return group;
    }

    public void removeGroup(ESPGroup group) {
        synchronized (this.chunks) {
            this.groups.remove(group);
        }
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        searchChunk(event.chunk());
    }

    private void searchChunk(class_2791 chunk) {
        this.workerThread.submit(() -> {
            if (isActive()) {
                ESPChunk schunk = ESPChunk.searchChunk(chunk, this.blocks.get(), this.activatedSpawners.get().booleanValue());
                if (schunk.size() > 0) {
                    synchronized (this.chunks) {
                        this.chunks.put(chunk.method_12004().method_8324(), schunk);
                        schunk.update();
                        updateChunk(chunk.method_12004().field_9181 - 1, chunk.method_12004().field_9180);
                        updateChunk(chunk.method_12004().field_9181 + 1, chunk.method_12004().field_9180);
                        updateChunk(chunk.method_12004().field_9181, chunk.method_12004().field_9180 - 1);
                        updateChunk(chunk.method_12004().field_9181, chunk.method_12004().field_9180 + 1);
                    }
                }
            }
        });
    }

    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event) {
        int bx = event.pos.method_10263();
        int by = event.pos.method_10264();
        int bz = event.pos.method_10260();
        int chunkX = bx >> 4;
        int chunkZ = bz >> 4;
        long key = class_1923.method_8331(chunkX, chunkZ);
        boolean added = this.blocks.get().contains(event.newState.method_26204()) && !this.blocks.get().contains(event.oldState.method_26204());
        boolean removed = (added || this.blocks.get().contains(event.newState.method_26204()) || !this.blocks.get().contains(event.oldState.method_26204())) ? false : true;
        if (added || removed) {
            this.workerThread.submit(() -> {
                synchronized (this.chunks) {
                    ESPChunk chunk = (ESPChunk) this.chunks.get(key);
                    if (chunk == null) {
                        chunk = new ESPChunk(chunkX, chunkZ);
                        if (chunk.shouldBeDeleted()) {
                            return;
                        } else {
                            this.chunks.put(key, chunk);
                        }
                    }
                    this.blockPos.method_10103(bx, by, bz);
                    if (added) {
                        chunk.add(this.blockPos);
                    } else {
                        chunk.remove(this.blockPos);
                    }
                    for (int x = -1; x < 2; x++) {
                        for (int z = -1; z < 2; z++) {
                            for (int y = -1; y < 2; y++) {
                                if (x != 0 || y != 0 || z != 0) {
                                    updateBlock(bx + x, by + y, bz + z);
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        Dimension dimension = PlayerUtils.getDimension();
        if (this.lastDimension != dimension) {
            onActivate();
        }
        this.lastDimension = dimension;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        synchronized (this.chunks) {
            ObjectIterator it = this.chunks.values().iterator();
            while (it.hasNext()) {
                ESPChunk chunk = (ESPChunk) it.next();
                if (chunk.shouldBeDeleted()) {
                    this.workerThread.submit(() -> {
                        ObjectIterator it2 = chunk.blocks.values().iterator();
                        while (it2.hasNext()) {
                            ESPBlock block = (ESPBlock) it2.next();
                            block.group.remove(block, false);
                            block.loaded = false;
                        }
                    });
                    it.remove();
                } else {
                    chunk.render(event);
                }
            }
            if (this.tracers.get().booleanValue()) {
                for (ESPGroup group : this.groups) {
                    group.render(event);
                }
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return "%s groups".formatted(Integer.valueOf(this.groups.size()));
    }
}
