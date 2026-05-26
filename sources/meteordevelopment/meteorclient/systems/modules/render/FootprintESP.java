package meteordevelopment.meteorclient.systems.modules.render;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2338;
import net.minecraft.class_238;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/FootprintESP.class */
public class FootprintESP extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Boolean> smoothOutlines;
    private final Setting<Double> smoothenAmount;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<Double> height;
    private final Setting<SettingColor> lineColor;
    private final Setting<SettingColor> sideColor;
    private final Set<class_2338> standingBlocks;

    public FootprintESP() {
        super(Categories.Render, "footprint-esp", "Highlights the block face you are standing on.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.smoothOutlines = this.sgGeneral.add(new BoolSetting.Builder().name("smooth-outlines").description("Whether to remove inner lines when standing on multiple blocks.").defaultValue(true).build());
        this.smoothenAmount = this.sgGeneral.add(new DoubleSetting.Builder().name("smoothen").description("How much to smoothen the edges of the rendered block faces.").defaultValue(0.0d).min(0.0d).max(0.5d).sliderMax(0.5d).visible(() -> {
            return this.smoothOutlines.get().booleanValue();
        }).build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Lines).build());
        this.height = this.sgRender.add(new DoubleSetting.Builder().name("height").description("The height of rendering above the block.").defaultValue(0.02d).min(0.0d).max(1.0d).sliderMax(0.2d).build());
        this.lineColor = this.sgRender.add(new ColorSetting.Builder().name("line-color").description("The color of the lines.").defaultValue(new SettingColor(0, 255, 255, 255)).build());
        this.sideColor = this.sgRender.add(new ColorSetting.Builder().name("side-color").description("The color of the sides.").defaultValue(new SettingColor(0, 255, 255, 25)).build());
        this.standingBlocks = new HashSet();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        this.standingBlocks.clear();
        class_238 playerBox = this.mc.field_1724.method_5829();
        int feetY = this.mc.field_1724.method_24515().method_10264() - 1;
        for (int x = (int) Math.floor(playerBox.field_1323); x < Math.ceil(playerBox.field_1320); x++) {
            for (int z = (int) Math.floor(playerBox.field_1321); z < Math.ceil(playerBox.field_1324); z++) {
                class_2338 blockPos = new class_2338(x, feetY, z);
                if (this.mc.field_1687.method_8320(blockPos).method_26234(this.mc.field_1687, blockPos)) {
                    this.standingBlocks.add(blockPos);
                }
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.standingBlocks.isEmpty()) {
            return;
        }
        if (!this.smoothOutlines.get().booleanValue() || this.standingBlocks.size() == 1) {
            Iterator<class_2338> it = this.standingBlocks.iterator();
            while (it.hasNext()) {
                renderBlockFace(event.renderer, it.next());
            }
            return;
        }
        int minX = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int y = 0;
        for (class_2338 pos : this.standingBlocks) {
            minX = Math.min(minX, pos.method_10263());
            minZ = Math.min(minZ, pos.method_10260());
            maxX = Math.max(maxX, pos.method_10263());
            maxZ = Math.max(maxZ, pos.method_10260());
            y = pos.method_10264();
        }
        renderRoundedOutline(event.renderer, minX, y, minZ, maxX + 1, maxZ + 1);
    }

    private void renderBlockFace(Renderer3D renderer, class_2338 pos) {
        double x = pos.method_10263();
        double y = pos.method_10264() + 1;
        double z = pos.method_10260();
        double h = this.height.get().doubleValue();
        if (this.shapeMode.get().lines()) {
            renderer.line(x, y + h, z, x + 1.0d, y + h, z, this.lineColor.get());
            renderer.line(x + 1.0d, y + h, z, x + 1.0d, y + h, z + 1.0d, this.lineColor.get());
            renderer.line(x + 1.0d, y + h, z + 1.0d, x, y + h, z + 1.0d, this.lineColor.get());
            renderer.line(x, y + h, z + 1.0d, x, y + h, z, this.lineColor.get());
        }
        if (this.shapeMode.get().sides()) {
            renderer.quad(x, y + h, z, x, y + h, z + 1.0d, x + 1.0d, y + h, z + 1.0d, x + 1.0d, y + h, z, this.sideColor.get());
        }
    }

    private void renderRoundedOutline(Renderer3D renderer, double minX, double y, double minZ, double maxX, double maxZ) {
        double h = this.height.get().doubleValue();
        double radius = this.smoothenAmount.get().doubleValue();
        double drawY = y + 1.0d + h;
        double innerMinX = minX + radius;
        double innerMaxX = maxX - radius;
        double innerMinZ = minZ + radius;
        double innerMaxZ = maxZ - radius;
        if (this.shapeMode.get().lines()) {
            renderer.line(innerMinX, drawY, minZ, innerMaxX, drawY, minZ, this.lineColor.get());
            renderer.line(maxX, drawY, innerMinZ, maxX, drawY, innerMaxZ, this.lineColor.get());
            renderer.line(innerMaxX, drawY, maxZ, innerMinX, drawY, maxZ, this.lineColor.get());
            renderer.line(minX, drawY, innerMaxZ, minX, drawY, innerMinZ, this.lineColor.get());
            drawRoundedCorner(renderer, innerMaxX, drawY, innerMinZ, radius, 0, 8, this.lineColor.get());
            drawRoundedCorner(renderer, innerMaxX, drawY, innerMaxZ, radius, 1, 8, this.lineColor.get());
            drawRoundedCorner(renderer, innerMinX, drawY, innerMaxZ, radius, 2, 8, this.lineColor.get());
            drawRoundedCorner(renderer, innerMinX, drawY, innerMinZ, radius, 3, 8, this.lineColor.get());
        }
        if (this.shapeMode.get().sides()) {
            double d = (minX + maxX) / 2.0d;
            double d2 = (minZ + maxZ) / 2.0d;
            renderer.quad(innerMinX, drawY, innerMinZ, innerMinX, drawY, innerMaxZ, innerMaxX, drawY, innerMaxZ, innerMaxX, drawY, innerMinZ, this.sideColor.get());
            int corner = 0;
            while (corner < 4) {
                drawRoundedCornerFill(renderer, (corner == 0 || corner == 3) ? innerMinX : innerMaxX, drawY, corner < 2 ? innerMinZ : innerMaxZ, radius, corner, 8, this.sideColor.get());
                corner++;
            }
            renderer.quad(innerMinX, drawY, minZ, innerMinX, drawY, innerMinZ, innerMaxX, drawY, innerMinZ, innerMaxX, drawY, minZ, this.sideColor.get());
            renderer.quad(innerMaxX, drawY, innerMinZ, innerMaxX, drawY, innerMaxZ, maxX, drawY, innerMaxZ, maxX, drawY, innerMinZ, this.sideColor.get());
            renderer.quad(innerMinX, drawY, innerMaxZ, innerMinX, drawY, maxZ, innerMaxX, drawY, maxZ, innerMaxX, drawY, innerMaxZ, this.sideColor.get());
            renderer.quad(minX, drawY, innerMinZ, minX, drawY, innerMaxZ, innerMinX, drawY, innerMaxZ, innerMinX, drawY, innerMinZ, this.sideColor.get());
        }
    }

    private void drawRoundedCorner(Renderer3D renderer, double x, double y, double z, double radius, int corner, int segments, SettingColor color) {
        double startAngle = 1.5707963267948966d * ((double) ((corner + 3) % 4));
        double endAngle = startAngle + 1.5707963267948966d;
        double lastX = x + (radius * Math.cos(startAngle));
        double lastZ = z + (radius * Math.sin(startAngle));
        for (int i = 1; i <= segments; i++) {
            double angle = startAngle + (((endAngle - startAngle) * ((double) i)) / ((double) segments));
            double newX = x + (radius * Math.cos(angle));
            double newZ = z + (radius * Math.sin(angle));
            renderer.line(lastX, y, lastZ, newX, y, newZ, color);
            lastX = newX;
            lastZ = newZ;
        }
    }

    private void drawRoundedCornerFill(Renderer3D renderer, double x, double y, double z, double radius, int corner, int segments, SettingColor color) {
        double startAngle = 1.5707963267948966d * ((double) ((corner + 3) % 4));
        double endAngle = startAngle + 1.5707963267948966d;
        double lastX = x + (radius * Math.cos(startAngle));
        double lastZ = z + (radius * Math.sin(startAngle));
        for (int i = 1; i <= segments; i++) {
            double angle = startAngle + (((endAngle - startAngle) * ((double) i)) / ((double) segments));
            double newX = x + (radius * Math.cos(angle));
            double newZ = z + (radius * Math.sin(angle));
            renderer.quad(x, y, z, lastX, y, lastZ, newX, y, newZ, x, y, z, color);
            lastX = newX;
            lastZ = newZ;
        }
    }
}
