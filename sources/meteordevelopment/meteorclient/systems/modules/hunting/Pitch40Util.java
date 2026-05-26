package meteordevelopment.meteorclient.systems.modules.hunting;

import java.util.Objects;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/Pitch40Util.class */
public class Pitch40Util extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Boolean> autoBoundAdjust;
    public final Setting<Double> boundGap;
    public final Setting<Boolean> autoFirework;
    public final Setting<Double> velocityThreshold;
    public final Setting<Integer> fireworkCooldown;
    private boolean fallenLastTick;
    private boolean increasedLowerThisTick;
    private int ticksSinceLastFirework;
    private double minY;
    private double maxY;
    private double lastY;

    public Pitch40Util() {
        super(Categories.Hunting, "pitch-40-util", "Auto-manages pitch 40 elytra flying for optimal long-distance travel.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.autoBoundAdjust = this.sgGeneral.add(new BoolSetting.Builder().name("auto-adjust-bounds").description("Adjusts your bounds to make you continue to gain height. Good for fixing falling on reconnect or lag, etc.").defaultValue(true).build());
        this.boundGap = this.sgGeneral.add(new DoubleSetting.Builder().name("bound-gap").description("The gap between the upper and lower bounds. Used when reconnecting, or when at max height if Auto Adjust Bounds is enabled.").defaultValue(60.0d).sliderRange(50.0d, 100.0d).build());
        this.autoFirework = this.sgGeneral.add(new BoolSetting.Builder().name("auto-firework").description("Uses a firework automatically if your velocity is too low.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgGeneral;
        DoubleSetting.Builder builderSliderRange = new DoubleSetting.Builder().name("velocity-threshold").description("Velocity must be below this value when going up for firework to activate.").defaultValue(-0.05d).sliderRange(-0.5d, 1.0d);
        Setting<Boolean> setting = this.autoFirework;
        Objects.requireNonNull(setting);
        this.velocityThreshold = settingGroup.add(builderSliderRange.visible(setting::get).build());
        SettingGroup settingGroup2 = this.sgGeneral;
        IntSetting.Builder builderSliderRange2 = new IntSetting.Builder().name("firework-cooldown").description("Minimum amount of ticks to wait between using fireworks").defaultValue(20).min(1).sliderRange(5, 100);
        Setting<Boolean> setting2 = this.autoFirework;
        Objects.requireNonNull(setting2);
        this.fireworkCooldown = settingGroup2.add(builderSliderRange2.visible(setting2::get).build());
        this.fallenLastTick = false;
        this.increasedLowerThisTick = false;
        this.ticksSinceLastFirework = 0;
        this.minY = Double.NEGATIVE_INFINITY;
        this.maxY = Double.POSITIVE_INFINITY;
        this.lastY = Double.NEGATIVE_INFINITY;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.fallenLastTick = false;
        this.increasedLowerThisTick = false;
        this.ticksSinceLastFirework = 0;
        this.minY = Double.NEGATIVE_INFINITY;
        this.maxY = Double.POSITIVE_INFINITY;
        this.lastY = this.mc.field_1724.method_23318();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (this.autoBoundAdjust.get().booleanValue()) {
            if (!this.fallenLastTick && this.lastY > this.mc.field_1724.method_23318()) {
                this.fallenLastTick = true;
                this.maxY = this.lastY;
                if (this.maxY - this.minY > this.boundGap.get().doubleValue() * 2.0d && !this.increasedLowerThisTick) {
                    this.minY = this.maxY - this.boundGap.get().doubleValue();
                }
                this.increasedLowerThisTick = false;
            } else if (this.fallenLastTick && this.lastY < this.mc.field_1724.method_23318()) {
                this.fallenLastTick = false;
                this.minY = this.lastY;
                this.increasedLowerThisTick = true;
            }
        }
        this.lastY = this.mc.field_1724.method_23318();
        if (this.autoFirework.get().booleanValue() && this.ticksSinceLastFirework >= this.fireworkCooldown.get().intValue()) {
            double velocity = this.mc.field_1724.method_18798().field_1351;
            if (!this.fallenLastTick && velocity < this.velocityThreshold.get().doubleValue()) {
                HuntingUtils.firework();
                this.ticksSinceLastFirework = 0;
            }
        }
        this.ticksSinceLastFirework++;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        if (this.mc.field_1724 == null) {
            return null;
        }
        return String.format("%.0f/%.0f", Double.valueOf(this.minY), Double.valueOf(this.maxY));
    }
}
