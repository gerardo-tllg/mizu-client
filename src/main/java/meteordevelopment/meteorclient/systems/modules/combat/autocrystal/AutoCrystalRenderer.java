package meteordevelopment.meteorclient.systems.modules.combat.autocrystal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.utils.player.Timer;
// import meteordevelopment.meteorclient.utils.render.WireframeEntityRenderer; // Disabled
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AutoCrystalRenderer {
    private final Settings settings = new Settings();
    private final SettingGroup sgRender;
    private final Setting<RenderMode> renderMode;
    private final Setting<ShapeMode> simpleShapeMode;
    private final Setting<SettingColor> simpleColor;
    private final Setting<Double> simpleDrawTime;
    private final Setting<ShapeMode> placeDelayShapeMode;
    private final Setting<SettingColor> placeDelayColor;
    private final Setting<Double> placeDelayFadeTime;
    private final Setting<ShapeMode> breakDelayShapeMode;
    private final Setting<SettingColor> breakDelayColor;
    private final Setting<Double> breakDelayFadeTime;
    private final Setting<Double> breakDelayFadeExponent;
    private final Map<BlockPos, Long> crystalRenderPlaceDelays;
    private final Map<CrystalBreakRender, Long> crystalRenderBreakDelays;
    private final AutoCrystal autoCrystal;
    private BlockPos simpleRenderPos;
    private Timer simpleRenderTimer;

    public AutoCrystalRenderer(AutoCrystal ac) {
        this.sgRender = this.settings.createGroup("Render");
        this.renderMode = this.sgRender.add(new EnumSetting.Builder<RenderMode>()
            .name("render-mode")
            .description("Mode for rendering.")
            .defaultValue(RenderMode.DelayDraw)
            .build());
        this.simpleShapeMode = this.sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("simple-shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .visible(() -> this.renderMode.get() == RenderMode.Simple)
            .build());
        this.simpleColor = this.sgRender.add(new ColorSetting.Builder()
            .name("simple-color")
            .description("Color to render place delays in")
            .defaultValue(Color.RED.a(40))
            .visible(() -> this.renderMode.get() == RenderMode.Simple)
            .build());
        this.simpleDrawTime = this.sgRender.add(new DoubleSetting.Builder()
            .name("simple-draw-time")
            .description("How long to draw the box")
            .defaultValue(0.15)
            .min(0.0)
            .sliderMax(1.0)
            .visible(() -> this.renderMode.get() == RenderMode.Simple)
            .build());
        this.placeDelayShapeMode = this.sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("place-delay-shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .visible(() -> this.renderMode.get() == RenderMode.DelayDraw)
            .build());
        this.placeDelayColor = this.sgRender.add(new ColorSetting.Builder()
            .name("place-delay-color")
            .description("Color to render place delays in")
            .defaultValue(new Color(110, 0, 255, 40))
            .visible(() -> this.renderMode.get() == RenderMode.DelayDraw)
            .build());
        this.placeDelayFadeTime = this.sgRender.add(new DoubleSetting.Builder()
            .name("place-delay-fade-time")
            .description("How long to fade the box")
            .defaultValue(0.7)
            .min(0.0)
            .sliderMax(2.0)
            .visible(() -> this.renderMode.get() == RenderMode.DelayDraw)
            .build());
        this.breakDelayShapeMode = this.sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("break-delay-shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .visible(() -> this.renderMode.get() == RenderMode.DelayDraw)
            .build());
        this.breakDelayColor = this.sgRender.add(new ColorSetting.Builder()
            .name("break-delay-color")
            .description("Color to render break delays in")
            .defaultValue(Color.BLACK.a(0))
            .visible(() -> this.renderMode.get() == RenderMode.DelayDraw)
            .build());
        this.breakDelayFadeTime = this.sgRender.add(new DoubleSetting.Builder()
            .name("break-delay-fade-time")
            .description("How long to fade the box")
            .defaultValue(0.4)
            .min(0.0)
            .sliderMax(2.0)
            .visible(() -> this.renderMode.get() == RenderMode.DelayDraw)
            .build());
        this.breakDelayFadeExponent = this.sgRender.add(new DoubleSetting.Builder()
            .name("break-delay-fade-exponent")
            .description("Adds an exponent to the fade")
            .defaultValue(1.6)
            .min(0.2)
            .sliderMax(4.0)
            .visible(() -> this.renderMode.get() == RenderMode.DelayDraw)
            .build());
        this.crystalRenderPlaceDelays = new HashMap<>();
        this.crystalRenderBreakDelays = new HashMap<>();
        this.simpleRenderPos = null;
        this.simpleRenderTimer = new Timer();
        ac.settings.groups.addAll(this.settings.groups);
        this.autoCrystal = ac;
    }

    public void onActivate() {
        this.crystalRenderPlaceDelays.clear();
        this.crystalRenderBreakDelays.clear();
    }

    public void onRender3D(Render3DEvent event) {
        switch (this.renderMode.get().ordinal()) {
            case 0:
            default:
                break;
            case 1:
                this.drawDelay(event);
                break;
            case 2:
                this.drawSimple(event);
                break;
            case 3:
                this.drawDebug(event);
        }
    }

    public void onBreakCrystal(Entity entity) {
        long currentTime = System.currentTimeMillis();
        CrystalBreakRender breakRender = new CrystalBreakRender();
        breakRender.pos = new Vec3d(0.0, 0.0, 0.0);
        breakRender.entity = entity;
        this.crystalRenderBreakDelays.put(breakRender, currentTime);
    }

    public void onPlaceCrystal(BlockPos pos) {
        long currentTime = System.currentTimeMillis();
        this.crystalRenderPlaceDelays.put(pos, currentTime);
        this.simpleRenderPos = pos;
        this.simpleRenderTimer.reset();
    }

    private void drawSimple(Render3DEvent event) {
        if (this.simpleRenderPos != null && !this.simpleRenderTimer.passedS(this.simpleDrawTime.get())) {
            event.renderer.box(this.simpleRenderPos, this.simpleColor.get(), this.simpleColor.get(), this.simpleShapeMode.get(), 0);
        }
    }

    private void drawDelay(Render3DEvent event) {
        long currentTime = System.currentTimeMillis();
        Iterator<Entry<BlockPos, Long>> var4 = this.crystalRenderPlaceDelays.entrySet().iterator();

        Entry<CrystalBreakRender, Long> breakDelay;
        while (var4.hasNext()) {
            Entry<BlockPos, Long> placeDelay = var4.next();
            if (!((double)(currentTime - placeDelay.getValue()) > this.placeDelayFadeTime.get() * 1000.0)) {
                double time = (double)(currentTime - placeDelay.getValue()) / 1000.0;
                double timeCompletion = time / this.placeDelayFadeTime.get();
                this.renderBoxSized(event, placeDelay.getKey(), 1.0, 1.0 - timeCompletion, this.placeDelayColor.get(), this.placeDelayColor.get(), this.placeDelayShapeMode.get());
            }
        }

        Iterator<Entry<CrystalBreakRender, Long>> var5 = this.crystalRenderBreakDelays.entrySet().iterator();

        while (var5.hasNext()) {
            breakDelay = var5.next();
            if (!((double)(currentTime - breakDelay.getValue()) > this.breakDelayFadeTime.get() * 1000.0)) {
                CrystalBreakRender render = breakDelay.getKey();

                // WireframeEntityRenderer disabled - class not available
                /*if (render.parts == null && render.entity != null) {
                    render.parts = WireframeEntityRenderer.cloneEntityForRendering(event, render.entity, render.pos);
                    render.entity = null;
                }*/

                double time = (double)(currentTime - breakDelay.getValue()) / 1000.0;
                double timeCompletion = time / this.breakDelayFadeTime.get();
                Color color = this.breakDelayColor.get().copy().a((int)((double)this.breakDelayColor.get().a * Math.pow(1.0 - timeCompletion, this.breakDelayFadeExponent.get())));

                // WireframeEntityRenderer.render(event, render.pos, render.parts, 1.0, color, color, this.breakDelayShapeMode.get()); // Disabled
            }
        }
    }

    private void drawDebug(Render3DEvent event) {
        int r = (int) Math.floor(this.autoCrystal.placeRange.get());
        BlockPos eyePos = BlockPos.ofFloored(mc.player.getEyePos());
        int ex = eyePos.getX();
        int ey = eyePos.getY();
        int ez = eyePos.getZ();
        BlockPos.Mutable mutablePos = new BlockPos.Mutable(0, 0, 0);

        for (int x = -r; x <= r; ++x) {
            for (int y = -r; y <= r; ++y) {
                for (int z = -r; z <= r; ++z) {
                    if (this.autoCrystal.isValidSpot(x, y, z)) {
                        BlockPos pos = mutablePos.set(ex + x, ey + y, ez + z);
                        event.renderer.box(pos, this.simpleColor.get(), this.simpleColor.get(), this.simpleShapeMode.get(), 0);
                    }
                }
            }
        }
    }

    private void renderBoxSized(Render3DEvent event, BlockPos blockPos, double size, double alpha, Color sideColor, Color lineColor, ShapeMode shapeMode) {
        Box orig = new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        double shrinkFactor = 1.0 - size;
        Box box = orig.shrink(orig.getLengthX() * shrinkFactor, orig.getLengthY() * shrinkFactor, orig.getLengthZ() * shrinkFactor);
        double xShrink = orig.getLengthX() * shrinkFactor / 2.0;
        double yShrink = orig.getLengthY() * shrinkFactor / 2.0;
        double zShrink = orig.getLengthZ() * shrinkFactor / 2.0;
        double x1 = (double) blockPos.getX() + box.minX + xShrink;
        double y1 = (double) blockPos.getY() + box.minY + yShrink;
        double z1 = (double) blockPos.getZ() + box.minZ + zShrink;
        double x2 = (double) blockPos.getX() + box.maxX + xShrink;
        double y2 = (double) blockPos.getY() + box.maxY + yShrink;
        double z2 = (double) blockPos.getZ() + box.maxZ + zShrink;
        event.renderer.box(x1, y1, z1, x2, y2, z2, sideColor.copy().a((int)((double) sideColor.a * alpha)), sideColor.copy().a((int)((double) lineColor.a * alpha)), shapeMode, 0);
    }

    private enum RenderMode {
        None,
        DelayDraw,
        Simple,
        Debug
    }

    private class CrystalBreakRender {
        public Vec3d pos;
        // public List<WireframeEntityRenderer.RenderablePart> parts; // Disabled - WireframeEntityRenderer not available
        public Entity entity;
    }
}
