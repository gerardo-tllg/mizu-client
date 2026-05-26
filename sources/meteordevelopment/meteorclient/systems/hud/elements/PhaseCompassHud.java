package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.Collections;
import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BreakIndicators;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_2338;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/PhaseCompassHud.class */
public class PhaseCompassHud extends HudElement {
    public static final HudElementInfo<PhaseCompassHud> INFO = new HudElementInfo<>(Hud.GROUP, "phase-compass", "Displays a compass that points to the best phase location.", PhaseCompassHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgColors;
    private final Setting<Double> scale;
    private final Setting<Integer> radius;
    private final Setting<SettingColor> colorBestPhase;
    private final Setting<SettingColor> colorOther;
    private final Setting<Boolean> shadow;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/PhaseCompassHud$Direction.class */
    private enum Direction {
        N,
        W,
        S,
        E
    }

    public PhaseCompassHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgColors = this.settings.createGroup("Colors");
        this.scale = this.sgGeneral.add(new DoubleSetting.Builder().name("scale").description("The scale of the compass.").defaultValue(1.0d).min(1.0d).sliderRange(1.0d, 5.0d).onChanged(aDouble -> {
            calculateSize();
        }).build());
        this.radius = this.sgGeneral.add(new IntSetting.Builder().name("radius").description("The radius in blocks to scan for phase locations.").defaultValue(3).min(1).sliderRange(1, 10).build());
        this.colorBestPhase = this.sgColors.add(new ColorSetting.Builder().name("best-phase-color").description("Color of the best phase location.").defaultValue(new SettingColor(0, 255, 0, 255)).build());
        this.colorOther = this.sgColors.add(new ColorSetting.Builder().name("other-color").description("Color of other directions.").defaultValue(new SettingColor(255, 255, 255, 255)).build());
        this.shadow = this.sgGeneral.add(new BoolSetting.Builder().name("shadow").description("Text shadow.").defaultValue(false).build());
        calculateSize();
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void setSize(double width, double height) {
        super.setSize(width, height);
    }

    private void calculateSize() {
        setSize(100.0d * this.scale.get().doubleValue(), 100.0d * this.scale.get().doubleValue());
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        double centerX = ((double) this.x) + (((double) getWidth()) / 2.0d);
        double centerY = ((double) this.y) + (((double) getHeight()) / 2.0d);
        double yaw = Math.toRadians(isInEditor() ? 180.0d : class_3532.method_15393(MeteorClient.mc.field_1724.method_36454()));
        class_2338 bestPhasePos = findBestPhaseLocation();
        if (bestPhasePos != null) {
            double angle = Math.atan2(((double) bestPhasePos.method_10260()) - MeteorClient.mc.field_1724.method_23321(), ((double) bestPhasePos.method_10263()) - MeteorClient.mc.field_1724.method_23317()) - yaw;
            double endX = centerX + (Math.sin(angle) * 40.0d * this.scale.get().doubleValue());
            double endY = centerY - ((Math.cos(angle) * 40.0d) * this.scale.get().doubleValue());
            renderer.line(centerX, centerY, endX, endY, this.colorBestPhase.get());
            renderer.text("⬆", endX - (renderer.textWidth("⬆", this.shadow.get().booleanValue(), 1.0d) / 2.0d), endY - (renderer.textHeight(this.shadow.get().booleanValue(), 1.0d) / 2.0d), this.colorBestPhase.get(), this.shadow.get().booleanValue(), 1.0d);
        }
        for (Direction direction : Direction.values()) {
            renderer.text(direction.name(), (centerX + getX(direction, yaw)) - (renderer.textWidth(direction.name(), this.shadow.get().booleanValue(), 1.0d) / 2.0d), (centerY + getY(direction, yaw)) - (renderer.textHeight(this.shadow.get().booleanValue(), 1.0d) / 2.0d), this.colorOther.get(), this.shadow.get().booleanValue(), 1.0d);
        }
    }

    private class_2338 findBestPhaseLocation() {
        class_2338 playerPos = MeteorClient.mc.field_1724.method_24515();
        BreakIndicators breakIndicators = (BreakIndicators) Modules.get().get(BreakIndicators.class);
        Set<class_2338> breakingBlocks = breakIndicators != null ? breakIndicators.breakStartTimes.keySet() : Collections.emptySet();
        if (!breakingBlocks.isEmpty()) {
            class_2338 breakingBlock = breakingBlocks.iterator().next();
            if (!breakingBlock.equals(playerPos)) {
                double angle = Math.atan2(breakingBlock.method_10260() - playerPos.method_10260(), breakingBlock.method_10263() - playerPos.method_10263()) + 3.141592653589793d;
                return new class_2338(playerPos.method_10263() + ((int) (Math.cos(angle) * ((double) 5))), playerPos.method_10264(), playerPos.method_10260() + ((int) (Math.sin(angle) * ((double) 5))));
            }
            return null;
        }
        return null;
    }

    private double getX(Direction direction, double yaw) {
        return Math.sin(getPos(direction, yaw)) * this.scale.get().doubleValue() * 40.0d;
    }

    private double getY(Direction direction, double yaw) {
        return Math.cos(getPos(direction, yaw)) * this.scale.get().doubleValue() * 40.0d;
    }

    private double getPos(Direction direction, double yaw) {
        return yaw + ((((double) direction.ordinal()) * 3.141592653589793d) / 2.0d);
    }
}
