package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.HashSet;
import java.util.Set;

public class FootprintESP extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> smoothOutlines = sgGeneral.add(new BoolSetting.Builder()
        .name("smooth-outlines")
        .description("Whether to remove inner lines when standing on multiple blocks.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> smoothenAmount = sgGeneral.add(new DoubleSetting.Builder()
        .name("smoothen")
        .description("How much to smoothen the edges of the rendered block faces.")
        .defaultValue(0.0)
        .min(0.0)
        .max(0.5)
        .sliderMax(0.5)
        .visible(() -> smoothOutlines.get())
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Lines)
        .build()
    );

    private final Setting<Double> height = sgRender.add(new DoubleSetting.Builder()
        .name("height")
        .description("The height of rendering above the block.")
        .defaultValue(0.02)
        .min(0.0)
        .max(1.0)
        .sliderMax(0.2)
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The color of the lines.")
        .defaultValue(new SettingColor(0, 255, 255, 255))
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The color of the sides.")
        .defaultValue(new SettingColor(0, 255, 255, 25))
        .build()
    );

    private final Set<BlockPos> standingBlocks = new HashSet<>();

    public FootprintESP() {
        super(Categories.Render, "footprint-esp", "Highlights the block face you are standing on.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        standingBlocks.clear();

        Box playerBox = mc.player.getBoundingBox();

        int feetY = mc.player.getBlockPos().getY() - 1;

        for (int x = (int) Math.floor(playerBox.minX); x < Math.ceil(playerBox.maxX); x++) {
            for (int z = (int) Math.floor(playerBox.minZ); z < Math.ceil(playerBox.maxZ); z++) {
                BlockPos blockPos = new BlockPos(x, feetY, z);

                if (mc.world.getBlockState(blockPos).isFullCube(mc.world, blockPos)) {
                    standingBlocks.add(blockPos);
                }
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (standingBlocks.isEmpty()) return;

        if (!smoothOutlines.get() || standingBlocks.size() == 1) {
            for (BlockPos pos : standingBlocks) {
                renderBlockFace(event.renderer, pos);
            }
            return;
        }

        int minX = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int y = 0;

        for (BlockPos pos : standingBlocks) {
            minX = Math.min(minX, pos.getX());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxZ = Math.max(maxZ, pos.getZ());
            y = pos.getY();
        }

        renderRoundedOutline(event.renderer, minX, y, minZ, maxX + 1, maxZ + 1);
    }

    private void renderBlockFace(Renderer3D renderer, BlockPos pos) {
        double x = pos.getX();
        double y = pos.getY() + 1;
        double z = pos.getZ();
        double h = height.get();

        if (shapeMode.get().lines()) {
            renderer.line(x, y + h, z, x + 1, y + h, z, lineColor.get());
            renderer.line(x + 1, y + h, z, x + 1, y + h, z + 1, lineColor.get());
            renderer.line(x + 1, y + h, z + 1, x, y + h, z + 1, lineColor.get());
            renderer.line(x, y + h, z + 1, x, y + h, z, lineColor.get());
        }

        if (shapeMode.get().sides()) {
            renderer.quad(x, y + h, z, x, y + h, z + 1, x + 1, y + h, z + 1, x + 1, y + h, z, sideColor.get());
        }
    }

    private void renderRoundedOutline(Renderer3D renderer, double minX, double y, double minZ, double maxX, double maxZ) {
        double h = height.get();
        double radius = smoothenAmount.get();

        double drawY = y + 1 + h;

        double innerMinX = minX + radius;
        double innerMaxX = maxX - radius;
        double innerMinZ = minZ + radius;
        double innerMaxZ = maxZ - radius;

        int segments = 8;

        if (shapeMode.get().lines()) {

            renderer.line(innerMinX, drawY, minZ, innerMaxX, drawY, minZ, lineColor.get());

            renderer.line(maxX, drawY, innerMinZ, maxX, drawY, innerMaxZ, lineColor.get());

            renderer.line(innerMaxX, drawY, maxZ, innerMinX, drawY, maxZ, lineColor.get());

            renderer.line(minX, drawY, innerMaxZ, minX, drawY, innerMinZ, lineColor.get());

            drawRoundedCorner(renderer, innerMaxX, drawY, innerMinZ, radius, 0, segments, lineColor.get());
            drawRoundedCorner(renderer, innerMaxX, drawY, innerMaxZ, radius, 1, segments, lineColor.get());
            drawRoundedCorner(renderer, innerMinX, drawY, innerMaxZ, radius, 2, segments, lineColor.get());
            drawRoundedCorner(renderer, innerMinX, drawY, innerMinZ, radius, 3, segments, lineColor.get());
        }

        if (shapeMode.get().sides()) {

            double centerX = (minX + maxX) / 2;
            double centerZ = (minZ + maxZ) / 2;

            renderer.quad(innerMinX, drawY, innerMinZ, innerMinX, drawY, innerMaxZ,
                innerMaxX, drawY, innerMaxZ, innerMaxX, drawY, innerMinZ, sideColor.get());

            for (int corner = 0; corner < 4; corner++) {
                drawRoundedCornerFill(renderer, corner == 0 || corner == 3 ? innerMinX : innerMaxX,
                    drawY,
                    corner < 2 ? innerMinZ : innerMaxZ,
                    radius, corner, segments, sideColor.get());
            }

            renderer.quad(innerMinX, drawY, minZ, innerMinX, drawY, innerMinZ,
                innerMaxX, drawY, innerMinZ, innerMaxX, drawY, minZ, sideColor.get());

            renderer.quad(innerMaxX, drawY, innerMinZ, innerMaxX, drawY, innerMaxZ,
                maxX, drawY, innerMaxZ, maxX, drawY, innerMinZ, sideColor.get());

            renderer.quad(innerMinX, drawY, innerMaxZ, innerMinX, drawY, maxZ,
                innerMaxX, drawY, maxZ, innerMaxX, drawY, innerMaxZ, sideColor.get());

            renderer.quad(minX, drawY, innerMinZ, minX, drawY, innerMaxZ,
                innerMinX, drawY, innerMaxZ, innerMinX, drawY, innerMinZ, sideColor.get());
        }
    }

    private void drawRoundedCorner(Renderer3D renderer, double x, double y, double z, double radius, int corner, int segments, SettingColor color) {

        double startAngle = Math.PI / 2 * ((corner + 3) % 4);
        double endAngle = startAngle + Math.PI / 2;

        double lastX = x + radius * Math.cos(startAngle);
        double lastZ = z + radius * Math.sin(startAngle);

        for (int i = 1; i <= segments; i++) {
            double angle = startAngle + (endAngle - startAngle) * i / segments;
            double newX = x + radius * Math.cos(angle);
            double newZ = z + radius * Math.sin(angle);

            renderer.line(lastX, y, lastZ, newX, y, newZ, color);

            lastX = newX;
            lastZ = newZ;
        }
    }

    private void drawRoundedCornerFill(Renderer3D renderer, double x, double y, double z, double radius, int corner, int segments, SettingColor color) {

        double startAngle = Math.PI / 2 * ((corner + 3) % 4);
        double endAngle = startAngle + Math.PI / 2;

        double lastX = x + radius * Math.cos(startAngle);
        double lastZ = z + radius * Math.sin(startAngle);

        for (int i = 1; i <= segments; i++) {
            double angle = startAngle + (endAngle - startAngle) * i / segments;
            double newX = x + radius * Math.cos(angle);
            double newZ = z + radius * Math.sin(angle);

            renderer.quad(x, y, z, lastX, y, lastZ, newX, y, newZ, x, y, z, color);

            lastX = newX;
            lastZ = newZ;
        }
    }
}
