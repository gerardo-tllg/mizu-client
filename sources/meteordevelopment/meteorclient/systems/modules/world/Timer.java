package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/world/Timer.class */
public class Timer extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> multiplier;
    public static final double OFF = 1.0d;
    private double override;

    public Timer() {
        super(Categories.World, "timer", "Changes the speed of everything in your game.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.multiplier = this.sgGeneral.add(new DoubleSetting.Builder().name("multiplier").description("The timer multiplier amount.").defaultValue(1.0d).min(0.1d).sliderMin(0.1d).build());
        this.override = 1.0d;
    }

    public double getMultiplier() {
        if (this.override != 1.0d) {
            return this.override;
        }
        if (isActive()) {
            return this.multiplier.get().doubleValue();
        }
        return 1.0d;
    }

    public void setOverride(double override) {
        this.override = override;
    }
}
