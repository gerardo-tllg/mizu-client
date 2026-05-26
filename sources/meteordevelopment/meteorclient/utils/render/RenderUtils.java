package meteordevelopment.meteorclient.utils.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import org.joml.Vector3f;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/RenderUtils.class */
public class RenderUtils {
    public static class_243 center;
    private static final Pool<RenderBlock> renderBlockPool = new Pool<>(RenderBlock::new);
    private static final List<RenderBlock> renderBlocks = new ArrayList();
    private static final long initTime = System.nanoTime();

    private RenderUtils() {
    }

    @PostInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(RenderUtils.class);
    }

    public static void drawItem(class_332 drawContext, class_1799 itemStack, int x, int y, float scale, boolean overlay, String countOverride) {
        class_4587 matrices = drawContext.method_51448();
        matrices.method_22903();
        matrices.method_22905(scale, scale, 1.0f);
        matrices.method_46416(0.0f, 0.0f, 401.0f);
        int scaledX = (int) (x / scale);
        int scaledY = (int) (y / scale);
        drawContext.method_51427(itemStack, scaledX, scaledY);
        if (overlay) {
            drawContext.method_51432(MeteorClient.mc.field_1772, itemStack, scaledX, scaledY, countOverride);
        }
        matrices.method_22909();
    }

    public static void drawItem(class_332 drawContext, class_1799 itemStack, int x, int y, float scale, boolean overlay) {
        drawItem(drawContext, itemStack, x, y, scale, overlay, null);
    }

    public static void updateScreenCenter() {
        class_310 mc = class_310.method_1551();
        Vector3f pos = new Vector3f(0.0f, 0.0f, 1.0f);
        if (((Boolean) mc.field_1690.method_42448().method_41753()).booleanValue()) {
            class_4587 bobViewMatrices = new class_4587();
            bobView(bobViewMatrices);
            pos.mulPosition(bobViewMatrices.method_23760().method_23761().invert());
        }
        center = new class_243(pos.x, -pos.y, pos.z).method_1037(-((float) Math.toRadians(mc.field_1773.method_19418().method_19329()))).method_1024(-((float) Math.toRadians(mc.field_1773.method_19418().method_19330()))).method_1019(mc.field_1773.method_19418().method_19326());
    }

    private static void bobView(class_4587 matrices) {
        class_1657 class_1657VarMethod_1560 = class_310.method_1551().method_1560();
        if (class_1657VarMethod_1560 instanceof class_1657) {
            class_1657 playerEntity = class_1657VarMethod_1560;
            float h = playerEntity.field_42108.method_48569();
            float i = playerEntity.field_42108.method_48566();
            matrices.method_22904(-(((double) (class_3532.method_15374(h * 3.1415927f) * i)) * 0.5d), Math.abs(class_3532.method_15362(h * 3.1415927f) * i), 0.0d);
            matrices.method_22907(class_7833.field_40718.rotationDegrees(class_3532.method_15374(h * 3.1415927f) * i * 3.0f));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(Math.abs(class_3532.method_15362((h * 3.1415927f) - 0.2f) * i) * 5.0f));
        }
    }

    public static void renderTickingBlock(class_2338 blockPos, Color sideColor, Color lineColor, ShapeMode shapeMode, int excludeDir, int duration, boolean fade, boolean shrink) {
        Iterator<RenderBlock> iterator = renderBlocks.iterator();
        while (iterator.hasNext()) {
            RenderBlock next = iterator.next();
            if (next.pos.equals(blockPos)) {
                iterator.remove();
                renderBlockPool.free(next);
            }
        }
        renderBlocks.add(renderBlockPool.get().set(blockPos, sideColor, lineColor, shapeMode, excludeDir, duration, fade, shrink));
    }

    @EventHandler
    private static void onTick(TickEvent.Pre event) {
        if (renderBlocks.isEmpty()) {
            return;
        }
        renderBlocks.forEach((v0) -> {
            v0.tick();
        });
        Iterator<RenderBlock> iterator = renderBlocks.iterator();
        while (iterator.hasNext()) {
            RenderBlock next = iterator.next();
            if (next.ticks <= 0) {
                iterator.remove();
                renderBlockPool.free(next);
            }
        }
    }

    @EventHandler
    private static void onRender(Render3DEvent event) {
        renderBlocks.forEach(block -> {
            block.render(event);
        });
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/RenderUtils$RenderBlock.class */
    public static class RenderBlock {
        public class_2338.class_2339 pos = new class_2338.class_2339();
        public Color sideColor;
        public Color lineColor;
        public ShapeMode shapeMode;
        public int excludeDir;
        public int ticks;
        public int duration;
        public boolean fade;
        public boolean shrink;

        public RenderBlock set(class_2338 blockPos, Color sideColor, Color lineColor, ShapeMode shapeMode, int excludeDir, int duration, boolean fade, boolean shrink) {
            this.pos.method_10101(blockPos);
            this.sideColor = sideColor;
            this.lineColor = lineColor;
            this.shapeMode = shapeMode;
            this.excludeDir = excludeDir;
            this.fade = fade;
            this.shrink = shrink;
            this.ticks = duration;
            this.duration = duration;
            return this;
        }

        public void tick() {
            this.ticks--;
        }

        public void render(Render3DEvent event) {
            int preSideA = this.sideColor.a;
            int preLineA = this.lineColor.a;
            double x1 = this.pos.method_10263();
            double y1 = this.pos.method_10264();
            double z1 = this.pos.method_10260();
            double x2 = this.pos.method_10263() + 1;
            double y2 = this.pos.method_10264() + 1;
            double z2 = this.pos.method_10260() + 1;
            double d = ((double) (this.ticks - event.tickDelta)) / ((double) this.duration);
            if (this.fade) {
                this.sideColor.a = (int) (((double) this.sideColor.a) * d);
                this.lineColor.a = (int) (((double) this.lineColor.a) * d);
            }
            if (this.shrink) {
                x1 += d;
                y1 += d;
                z1 += d;
                x2 -= d;
                y2 -= d;
                z2 -= d;
            }
            event.renderer.box(x1, y1, z1, x2, y2, z2, this.sideColor, this.lineColor, this.shapeMode, this.excludeDir);
            this.sideColor.a = preSideA;
            this.lineColor.a = preLineA;
        }
    }

    public static double getCurrentGameTickCalculated() {
        return getCurrentGameTickCalculatedNano(System.nanoTime());
    }

    public static double getCurrentGameTickCalculatedNano(long nanoTime) {
        return (nanoTime - initTime) / TimeUnit.MILLISECONDS.toNanos(50L);
    }
}
