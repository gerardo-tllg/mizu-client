package meteordevelopment.meteorclient.systems.modules.render;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2791;
import net.minecraft.class_2806;
import net.minecraft.class_2826;
import net.minecraft.class_2902;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/TunnelESP.class */
public class TunnelESP extends Module {
    private static final class_2338.class_2339 BP = new class_2338.class_2339();
    private static final class_2350[] DIRECTIONS = {class_2350.field_11034, class_2350.field_11043, class_2350.field_11035, class_2350.field_11039};
    private final SettingGroup sgGeneral;
    private final Setting<Double> height;
    private final Setting<Boolean> connected;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Long2ObjectMap<TChunk> chunks;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/TunnelESP$TunnelSide.class */
    private enum TunnelSide {
        Walkable,
        PartiallyBlocked,
        FullyBlocked
    }

    public TunnelESP() {
        super(Categories.Render, "tunnel-esp", "Highlights tunnels.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.height = this.sgGeneral.add(new DoubleSetting.Builder().name("height").description("Height of the rendered box.").defaultValue(0.1d).sliderMax(2.0d).build());
        this.connected = this.sgGeneral.add(new BoolSetting.Builder().name("connected").description("If neighbouring holes should be connected.").defaultValue(true).build());
        this.shapeMode = this.sgGeneral.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.sideColor = this.sgGeneral.add(new ColorSetting.Builder().name("side-color").description("The side color.").defaultValue(new SettingColor(255, Opcode.DRETURN, 25, 50)).build());
        this.lineColor = this.sgGeneral.add(new ColorSetting.Builder().name("line-color").description("The line color.").defaultValue(new SettingColor(255, Opcode.DRETURN, 25, 255)).build());
        this.chunks = new Long2ObjectOpenHashMap();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        this.chunks.clear();
    }

    private static int pack(int x, int y, int z) {
        return ((x & 255) << 24) | ((y & 65535) << 8) | (z & 255);
    }

    private static byte getPackedX(int p) {
        return (byte) ((p >> 24) & 255);
    }

    private static short getPackedY(int p) {
        return (short) ((p >> 8) & 65535);
    }

    private static byte getPackedZ(int p) {
        return (byte) (p & 255);
    }

    private void searchChunk(class_2791 chunk, TChunk tChunk) {
        Context ctx = new Context();
        IntOpenHashSet intOpenHashSet = new IntOpenHashSet();
        int startX = chunk.method_12004().method_8326();
        int startZ = chunk.method_12004().method_8328();
        int endX = chunk.method_12004().method_8327();
        int endZ = chunk.method_12004().method_8329();
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                int height = chunk.method_12032(class_2902.class_2903.field_13202).method_12603(x - startX, z - startZ);
                int iMethod_31607 = this.mc.field_1687.method_31607();
                while (true) {
                    short y = (short) iMethod_31607;
                    if (y < height) {
                        if (isTunnel(ctx, x, y, z)) {
                            intOpenHashSet.add(pack(x - startX, y, z - startZ));
                        }
                        iMethod_31607 = y + 1;
                    }
                }
            }
        }
        IntOpenHashSet intOpenHashSet2 = new IntOpenHashSet();
        IntIterator it = intOpenHashSet.iterator();
        while (it.hasNext()) {
            int packed = it.nextInt();
            byte x2 = getPackedX(packed);
            short y2 = getPackedY(packed);
            byte z2 = getPackedZ(packed);
            if (x2 == 0 || x2 == 15 || z2 == 0 || z2 == 15) {
                intOpenHashSet2.add(packed);
            } else {
                boolean has = false;
                class_2350[] class_2350VarArr = DIRECTIONS;
                int length = class_2350VarArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    class_2350 dir = class_2350VarArr[i];
                    if (!intOpenHashSet.contains(pack(x2 + dir.method_10148(), y2, z2 + dir.method_10165()))) {
                        i++;
                    } else {
                        has = true;
                        break;
                    }
                }
                if (has) {
                    intOpenHashSet2.add(packed);
                }
            }
        }
        tChunk.positions = intOpenHashSet2;
    }

    private boolean isTunnel(Context ctx, int x, int y, int z) {
        TunnelSide s1;
        TunnelSide s2;
        TunnelSide s3;
        TunnelSide s4;
        if (!canWalkIn(ctx, x, y, z) || (s1 = getTunnelSide(ctx, x + 1, y, z)) == TunnelSide.PartiallyBlocked || (s2 = getTunnelSide(ctx, x - 1, y, z)) == TunnelSide.PartiallyBlocked || (s3 = getTunnelSide(ctx, x, y, z + 1)) == TunnelSide.PartiallyBlocked || (s4 = getTunnelSide(ctx, x, y, z - 1)) == TunnelSide.PartiallyBlocked) {
            return false;
        }
        return (s1 == TunnelSide.Walkable && s2 == TunnelSide.Walkable && s3 == TunnelSide.FullyBlocked && s4 == TunnelSide.FullyBlocked) || (s1 == TunnelSide.FullyBlocked && s2 == TunnelSide.FullyBlocked && s3 == TunnelSide.Walkable && s4 == TunnelSide.Walkable);
    }

    private TunnelSide getTunnelSide(Context ctx, int x, int y, int z) {
        return canWalkIn(ctx, x, y, z) ? TunnelSide.Walkable : (canWalkThrough(ctx, x, y, z) || canWalkThrough(ctx, x, y + 1, z)) ? TunnelSide.PartiallyBlocked : TunnelSide.FullyBlocked;
    }

    private boolean canWalkOn(Context ctx, int x, int y, int z) {
        class_2680 state = ctx.get(x, y, z);
        return (state.method_26215() || !state.method_26227().method_15769() || state.method_26220(this.mc.field_1687, BP.method_10103(x, y, z)).method_1110()) ? false : true;
    }

    private boolean canWalkThrough(Context ctx, int x, int y, int z) {
        class_2680 state = ctx.get(x, y, z);
        if (state.method_26215()) {
            return true;
        }
        if (state.method_26227().method_15769()) {
            return state.method_26220(this.mc.field_1687, BP.method_10103(x, y, z)).method_1110();
        }
        return false;
    }

    private boolean canWalkIn(Context ctx, int x, int y, int z) {
        if (canWalkOn(ctx, x, y - 1, z) && canWalkThrough(ctx, x, y, z) && !canWalkThrough(ctx, x, y + 2, z)) {
            return canWalkThrough(ctx, x, y + 1, z);
        }
        return false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        synchronized (this.chunks) {
            ObjectIterator it = this.chunks.values().iterator();
            while (it.hasNext()) {
                ((TChunk) it.next()).marked = false;
            }
            int added = 0;
            for (class_2791 chunk : Utils.chunks(true)) {
                long key = class_1923.method_8331(chunk.method_12004().field_9181, chunk.method_12004().field_9180);
                if (this.chunks.containsKey(key)) {
                    ((TChunk) this.chunks.get(key)).marked = true;
                } else if (added < 48) {
                    TChunk tChunk = new TChunk(chunk.method_12004().field_9181, chunk.method_12004().field_9180);
                    this.chunks.put(tChunk.getKey(), tChunk);
                    MeteorExecutor.execute(() -> {
                        searchChunk(chunk, tChunk);
                    });
                    added++;
                }
            }
            this.chunks.values().removeIf(tChunk2 -> {
                return !tChunk2.marked;
            });
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        synchronized (this.chunks) {
            ObjectIterator it = this.chunks.values().iterator();
            while (it.hasNext()) {
                TChunk chunk = (TChunk) it.next();
                chunk.render(event.renderer);
            }
        }
    }

    private boolean chunkContains(TChunk chunk, int x, int y, int z) {
        int key;
        if (x == -1) {
            chunk = (TChunk) this.chunks.get(class_1923.method_8331(chunk.x - 1, chunk.z));
            key = pack(15, y, z);
        } else if (x == 16) {
            chunk = (TChunk) this.chunks.get(class_1923.method_8331(chunk.x + 1, chunk.z));
            key = pack(0, y, z);
        } else if (z == -1) {
            chunk = (TChunk) this.chunks.get(class_1923.method_8331(chunk.x, chunk.z - 1));
            key = pack(x, y, 15);
        } else if (z == 16) {
            chunk = (TChunk) this.chunks.get(class_1923.method_8331(chunk.x, chunk.z + 1));
            key = pack(x, y, 0);
        } else {
            key = pack(x, y, z);
        }
        return (chunk == null || chunk.positions == null || !chunk.positions.contains(key)) ? false : true;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/TunnelESP$TChunk.class */
    private class TChunk {
        private final int x;
        private final int z;
        public IntSet positions;
        public boolean marked = true;

        public TChunk(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public void render(Renderer3D renderer) {
            if (this.positions == null) {
                return;
            }
            IntIterator it = this.positions.iterator();
            while (it.hasNext()) {
                int pos = it.nextInt();
                int x = TunnelESP.getPackedX(pos);
                int y = TunnelESP.getPackedY(pos);
                int z = TunnelESP.getPackedZ(pos);
                int excludeDir = 0;
                if (TunnelESP.this.connected.get().booleanValue()) {
                    for (class_2350 dir : TunnelESP.DIRECTIONS) {
                        if (TunnelESP.this.chunkContains(this, x + dir.method_10148(), y, z + dir.method_10165())) {
                            excludeDir |= Dir.get(dir);
                        }
                    }
                }
                renderer.box(x + (this.x * 16), y, z + (this.z * 16), r0 + 1, ((double) y) + TunnelESP.this.height.get().doubleValue(), r0 + 1, TunnelESP.this.sideColor.get(), TunnelESP.this.lineColor.get(), TunnelESP.this.shapeMode.get(), excludeDir);
            }
        }

        public long getKey() {
            return class_1923.method_8331(this.x, this.z);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/TunnelESP$Context.class */
    private static class Context {
        private final class_1937 world = MeteorClient.mc.field_1687;
        private class_2791 lastChunk;

        public class_2680 get(int x, int y, int z) {
            class_2826 section;
            if (this.world.method_31601(y)) {
                return class_2246.field_10243.method_9564();
            }
            int cx = x >> 4;
            int cz = z >> 4;
            class_2791 chunk = (this.lastChunk != null && this.lastChunk.method_12004().field_9181 == cx && this.lastChunk.method_12004().field_9180 == cz) ? this.lastChunk : this.world.method_8402(cx, cz, class_2806.field_12803, false);
            if (chunk != null && (section = chunk.method_12006()[chunk.method_31602(y)]) != null) {
                this.lastChunk = chunk;
                return section.method_12254(x & 15, y & 15, z & 15);
            }
            return class_2246.field_10243.method_9564();
        }
    }
}
