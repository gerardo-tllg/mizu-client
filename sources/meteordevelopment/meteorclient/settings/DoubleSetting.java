package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/DoubleSetting.class */
public class DoubleSetting extends Setting<Double> {
    public final double min;
    public final double max;
    public final double sliderMin;
    public final double sliderMax;
    public final boolean onSliderRelease;
    public final int decimalPlaces;
    public final boolean noSlider;

    private DoubleSetting(String name, String description, double defaultValue, Consumer<Double> onChanged, Consumer<Setting<Double>> onModuleActivated, IVisible visible, double min, double max, double sliderMin, double sliderMax, boolean onSliderRelease, int decimalPlaces, boolean noSlider) {
        super(name, description, Double.valueOf(defaultValue), onChanged, onModuleActivated, visible);
        this.min = min;
        this.max = max;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.decimalPlaces = decimalPlaces;
        this.onSliderRelease = onSliderRelease;
        this.noSlider = noSlider;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Double parseImpl(String str) {
        try {
            return Double.valueOf(Double.parseDouble(str.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(Double value) {
        return value.doubleValue() >= this.min && value.doubleValue() <= this.max;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    protected class_2487 save(class_2487 tag) {
        tag.method_10549("value", get().doubleValue());
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Double load(class_2487 tag) {
        set(Double.valueOf(tag.method_68563("value", 0.0d)));
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/DoubleSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, Double, DoubleSetting> {
        public double min;
        public double max;
        public double sliderMin;
        public double sliderMax;
        public boolean onSliderRelease;
        public int decimalPlaces;
        public boolean noSlider;

        public Builder() {
            super(Double.valueOf(0.0d));
            this.min = Double.NEGATIVE_INFINITY;
            this.max = Double.POSITIVE_INFINITY;
            this.sliderMin = 0.0d;
            this.sliderMax = 10.0d;
            this.onSliderRelease = false;
            this.decimalPlaces = 3;
            this.noSlider = false;
        }

        /* JADX WARN: Type inference failed for: r1v1, types: [V, java.lang.Double] */
        public Builder defaultValue(double defaultValue) {
            this.defaultValue = Double.valueOf(defaultValue);
            return this;
        }

        public Builder min(double min) {
            this.min = min;
            return this;
        }

        public Builder max(double max) {
            this.max = max;
            return this;
        }

        public Builder range(double min, double max) {
            this.min = Math.min(min, max);
            this.max = Math.max(min, max);
            return this;
        }

        public Builder sliderMin(double min) {
            this.sliderMin = min;
            return this;
        }

        public Builder sliderMax(double max) {
            this.sliderMax = max;
            return this;
        }

        public Builder sliderRange(double min, double max) {
            this.sliderMin = min;
            this.sliderMax = max;
            return this;
        }

        public Builder onSliderRelease() {
            this.onSliderRelease = true;
            return this;
        }

        public Builder decimalPlaces(int decimalPlaces) {
            this.decimalPlaces = decimalPlaces;
            return this;
        }

        public Builder noSlider() {
            this.noSlider = true;
            return this;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public DoubleSetting build() {
            return new DoubleSetting(this.name, this.description, ((Double) this.defaultValue).doubleValue(), this.onChanged, this.onModuleActivated, this.visible, this.min, this.max, Math.max(this.sliderMin, this.min), Math.min(this.sliderMax, this.max), this.onSliderRelease, this.decimalPlaces, this.noSlider);
        }
    }
}
