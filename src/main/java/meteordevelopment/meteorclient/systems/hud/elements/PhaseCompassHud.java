package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BreakIndicators;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;
import java.util.Set;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PhaseCompassHud extends HudElement {
    public static final HudElementInfo<PhaseCompassHud> INFO = new HudElementInfo<>(Hud.GROUP, "phase-compass", "Displays a compass that points to the best phase location.", PhaseCompassHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale of the compass.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 5)
        .onChanged(aDouble -> calculateSize())
        .build()
    );

    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
        .name("radius")
        .description("The radius in blocks to scan for phase locations.")
        .defaultValue(3)
        .min(1)
        .sliderRange(1, 10)
        .build()
    );

    private final Setting<SettingColor> colorBestPhase = sgColors.add(new ColorSetting.Builder()
        .name("best-phase-color")
        .description("Color of the best phase location.")
        .defaultValue(new SettingColor(0, 255, 0, 255))
        .build()
    );

    private final Setting<SettingColor> colorOther = sgColors.add(new ColorSetting.Builder()
        .name("other-color")
        .description("Color of other directions.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Text shadow.")
        .defaultValue(false)
        .build()
    );

    public PhaseCompassHud() {
        super(INFO);
        calculateSize();
    }

    @Override
    public void setSize(double width, double height) {
        super.setSize(width, height);
    }

    private void calculateSize() {
        setSize(100 * scale.get(), 100 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        double centerX = this.x + (getWidth() / 2.0);
        double centerY = this.y + (getHeight() / 2.0);

        double yaw = isInEditor() ? 180 : MathHelper.wrapDegrees(mc.player.getYaw());
        yaw = Math.toRadians(yaw);

        BlockPos bestPhasePos = findBestPhaseLocation();
        if (bestPhasePos != null) {
            double angle = Math.atan2(bestPhasePos.getZ() - mc.player.getZ(), bestPhasePos.getX() - mc.player.getX()) - yaw;

            double endX = centerX + Math.sin(angle) * 40 * scale.get();
            double endY = centerY - Math.cos(angle) * 40 * scale.get();

            renderer.line(centerX, centerY, endX, endY, colorBestPhase.get());

            renderer.text(
                "⬆",
                endX - (renderer.textWidth("⬆", shadow.get(), 1)) / 2,
                endY - (renderer.textHeight(shadow.get(), 1) / 2),
                colorBestPhase.get(),
                shadow.get(),
                1
            );
        }

        for (Direction direction : Direction.values()) {
            renderer.text(
                direction.name(),
                (centerX + getX(direction, yaw)) - (renderer.textWidth(direction.name(), shadow.get(), 1)) / 2,
                (centerY + getY(direction, yaw)) - (renderer.textHeight(shadow.get(), 1) / 2),
                colorOther.get(),
                shadow.get(),
                1
            );
        }
    }

    private BlockPos findBestPhaseLocation() {
        BlockPos playerPos = mc.player.getBlockPos();
        BreakIndicators breakIndicators = Modules.get().get(BreakIndicators.class);
        Set<BlockPos> breakingBlocks = breakIndicators != null ? breakIndicators.breakStartTimes.keySet() : Collections.emptySet();

        if (!breakingBlocks.isEmpty()) {
            BlockPos breakingBlock = breakingBlocks.iterator().next();
            if (!breakingBlock.equals(playerPos)) {
                double angle = Math.atan2(breakingBlock.getZ() - playerPos.getZ(), breakingBlock.getX() - playerPos.getX()) + Math.PI;
                int distance = 5;
                return new BlockPos(
                    playerPos.getX() + (int) (Math.cos(angle) * distance),
                    playerPos.getY(),
                    playerPos.getZ() + (int) (Math.sin(angle) * distance)
                );
            }
        }

        return null;
    }

    private double getX(Direction direction, double yaw) {
        return Math.sin(getPos(direction, yaw)) * scale.get() * 40;
    }

    private double getY(Direction direction, double yaw) {
        return Math.cos(getPos(direction, yaw)) * scale.get() * 40;
    }

    private double getPos(Direction direction, double yaw) {
        return yaw + direction.ordinal() * Math.PI / 2;
    }

    private enum Direction {
        N, W, S, E
    }
}
