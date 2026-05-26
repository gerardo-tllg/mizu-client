package meteordevelopment.meteorclient.systems.modules.combat.autocrystal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javassist.bytecode.Opcode;
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
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/autocrystal/AutoCrystalRenderer.class */
public class AutoCrystalRenderer {
    private final AutoCrystal autoCrystal;
    private final Settings settings = new Settings();
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final Setting<RenderMode> renderMode = this.sgRender.add(new EnumSetting.Builder().name("render-mode").description("Mode for rendering.").defaultValue(RenderMode.DelayDraw).build());
    private final Setting<ShapeMode> simpleShapeMode = this.sgRender.add(new EnumSetting.Builder().name("simple-shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).visible(() -> {
        return this.renderMode.get() == RenderMode.Simple;
    }).build());
    private final Setting<SettingColor> simpleColor = this.sgRender.add(new ColorSetting.Builder().name("simple-color").description("Color to render place delays in").defaultValue(Color.RED.a(40)).visible(() -> {
        return this.renderMode.get() == RenderMode.Simple;
    }).build());
    private final Setting<Double> simpleDrawTime = this.sgRender.add(new DoubleSetting.Builder().name("simple-draw-time").description("How long to draw the box").defaultValue(0.15d).min(0.0d).sliderMax(1.0d).visible(() -> {
        return this.renderMode.get() == RenderMode.Simple;
    }).build());
    private final Setting<ShapeMode> placeDelayShapeMode = this.sgRender.add(new EnumSetting.Builder().name("place-delay-shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).visible(() -> {
        return this.renderMode.get() == RenderMode.DelayDraw;
    }).build());
    private final Setting<SettingColor> placeDelayColor = this.sgRender.add(new ColorSetting.Builder().name("place-delay-color").description("Color to render place delays in").defaultValue(new Color(Opcode.FDIV, 0, 255, 40)).visible(() -> {
        return this.renderMode.get() == RenderMode.DelayDraw;
    }).build());
    private final Setting<Double> placeDelayFadeTime = this.sgRender.add(new DoubleSetting.Builder().name("place-delay-fade-time").description("How long to fade the box").defaultValue(0.7d).min(0.0d).sliderMax(2.0d).visible(() -> {
        return this.renderMode.get() == RenderMode.DelayDraw;
    }).build());
    private final Setting<ShapeMode> breakDelayShapeMode = this.sgRender.add(new EnumSetting.Builder().name("break-delay-shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).visible(() -> {
        return this.renderMode.get() == RenderMode.DelayDraw;
    }).build());
    private final Setting<SettingColor> breakDelayColor = this.sgRender.add(new ColorSetting.Builder().name("break-delay-color").description("Color to render break delays in").defaultValue(Color.BLACK.a(0)).visible(() -> {
        return this.renderMode.get() == RenderMode.DelayDraw;
    }).build());
    private final Setting<Double> breakDelayFadeTime = this.sgRender.add(new DoubleSetting.Builder().name("break-delay-fade-time").description("How long to fade the box").defaultValue(0.4d).min(0.0d).sliderMax(2.0d).visible(() -> {
        return this.renderMode.get() == RenderMode.DelayDraw;
    }).build());
    private final Setting<Double> breakDelayFadeExponent = this.sgRender.add(new DoubleSetting.Builder().name("break-delay-fade-exponent").description("Adds an exponent to the fade").defaultValue(1.6d).min(0.2d).sliderMax(4.0d).visible(() -> {
        return this.renderMode.get() == RenderMode.DelayDraw;
    }).build());
    private final Map<class_2338, Long> crystalRenderPlaceDelays = new HashMap();
    private final Map<CrystalBreakRender, Long> crystalRenderBreakDelays = new HashMap();
    private class_2338 simpleRenderPos = null;
    private Timer simpleRenderTimer = new Timer();

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/autocrystal/AutoCrystalRenderer$RenderMode.class */
    private enum RenderMode {
        None,
        DelayDraw,
        Simple,
        Debug
    }

    public AutoCrystalRenderer(AutoCrystal ac) {
        ac.settings.groups.addAll(this.settings.groups);
        this.autoCrystal = ac;
    }

    public void onActivate() {
        this.crystalRenderPlaceDelays.clear();
        this.crystalRenderBreakDelays.clear();
    }

    public void onRender3D(Render3DEvent event) {
        switch (this.renderMode.get()) {
            case DelayDraw:
                drawDelay(event);
                break;
            case Simple:
                drawSimple(event);
                break;
            case Debug:
                drawDebug(event);
                break;
        }
    }

    public void onBreakCrystal(class_1297 entity) {
        long currentTime = System.currentTimeMillis();
        CrystalBreakRender breakRender = new CrystalBreakRender(this);
        breakRender.pos = new class_243(0.0d, 0.0d, 0.0d);
        breakRender.entity = entity;
        this.crystalRenderBreakDelays.put(breakRender, Long.valueOf(currentTime));
    }

    public void onPlaceCrystal(class_2338 pos) {
        long currentTime = System.currentTimeMillis();
        this.crystalRenderPlaceDelays.put(pos, Long.valueOf(currentTime));
        this.simpleRenderPos = pos;
        this.simpleRenderTimer.reset();
    }

    private void drawSimple(Render3DEvent event) {
        if (this.simpleRenderPos != null && !this.simpleRenderTimer.passedS(this.simpleDrawTime.get().doubleValue())) {
            event.renderer.box(this.simpleRenderPos, this.simpleColor.get(), this.simpleColor.get(), this.simpleShapeMode.get(), 0);
        }
    }

    private void drawDelay(Render3DEvent event) {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<class_2338, Long>> var4 = this.crystalRenderPlaceDelays.entrySet().iterator();
        while (var4.hasNext()) {
            Map.Entry<class_2338, Long> placeDelay = var4.next();
            if (currentTime - placeDelay.getValue().longValue() > this.placeDelayFadeTime.get().doubleValue() * 1000.0d) {
                var4.remove();
            } else {
                double time = (currentTime - placeDelay.getValue().longValue()) / 1000.0d;
                double timeCompletion = time / this.placeDelayFadeTime.get().doubleValue();
                renderBoxSized(event, placeDelay.getKey(), 1.0d, 1.0d - timeCompletion, this.placeDelayColor.get(), this.placeDelayColor.get(), this.placeDelayShapeMode.get());
            }
        }
        Iterator<Map.Entry<CrystalBreakRender, Long>> var5 = this.crystalRenderBreakDelays.entrySet().iterator();
        while (var5.hasNext()) {
            Map.Entry<CrystalBreakRender, Long> breakDelay = var5.next();
            if (currentTime - breakDelay.getValue().longValue() > this.breakDelayFadeTime.get().doubleValue() * 1000.0d) {
                var5.remove();
            } else {
                breakDelay.getKey();
                double time2 = (currentTime - breakDelay.getValue().longValue()) / 1000.0d;
                double timeCompletion2 = time2 / this.breakDelayFadeTime.get().doubleValue();
                this.breakDelayColor.get().copy().a((int) (((double) this.breakDelayColor.get().a) * Math.pow(1.0d - timeCompletion2, this.breakDelayFadeExponent.get().doubleValue())));
            }
        }
    }

    private void drawDebug(Render3DEvent event) {
        int r = (int) Math.floor(this.autoCrystal.placeRange.get().doubleValue());
        class_2338 eyePos = class_2338.method_49638(MeteorClient.mc.field_1724.method_33571());
        int ex = eyePos.method_10263();
        int ey = eyePos.method_10264();
        int ez = eyePos.method_10260();
        class_2338.class_2339 mutablePos = new class_2338.class_2339(0, 0, 0);
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if (this.autoCrystal.isValidSpot(x, y, z)) {
                        class_2338 pos = mutablePos.method_10103(ex + x, ey + y, ez + z);
                        event.renderer.box(pos, this.simpleColor.get(), this.simpleColor.get(), this.simpleShapeMode.get(), 0);
                    }
                }
            }
        }
    }

    private void renderBoxSized(Render3DEvent event, class_2338 blockPos, double size, double alpha, Color sideColor, Color lineColor, ShapeMode shapeMode) {
        class_238 orig = new class_238(0.0d, 0.0d, 0.0d, 1.0d, 1.0d, 1.0d);
        double shrinkFactor = 1.0d - size;
        class_238 box = orig.method_1002(orig.method_17939() * shrinkFactor, orig.method_17940() * shrinkFactor, orig.method_17941() * shrinkFactor);
        double xShrink = (orig.method_17939() * shrinkFactor) / 2.0d;
        double yShrink = (orig.method_17940() * shrinkFactor) / 2.0d;
        double zShrink = (orig.method_17941() * shrinkFactor) / 2.0d;
        double x1 = ((double) blockPos.method_10263()) + box.field_1323 + xShrink;
        double y1 = ((double) blockPos.method_10264()) + box.field_1322 + yShrink;
        double z1 = ((double) blockPos.method_10260()) + box.field_1321 + zShrink;
        double x2 = ((double) blockPos.method_10263()) + box.field_1320 + xShrink;
        double y2 = ((double) blockPos.method_10264()) + box.field_1325 + yShrink;
        double z2 = ((double) blockPos.method_10260()) + box.field_1324 + zShrink;
        event.renderer.box(x1, y1, z1, x2, y2, z2, sideColor.copy().a((int) (((double) sideColor.a) * alpha)), sideColor.copy().a((int) (((double) lineColor.a) * alpha)), shapeMode, 0);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/combat/autocrystal/AutoCrystalRenderer$CrystalBreakRender.class */
    private class CrystalBreakRender {
        public class_243 pos;
        public class_1297 entity;

        private CrystalBreakRender(AutoCrystalRenderer autoCrystalRenderer) {
        }
    }
}
