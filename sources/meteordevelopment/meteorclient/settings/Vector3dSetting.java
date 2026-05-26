package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_2487;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/Vector3dSetting.class */
public class Vector3dSetting extends Setting<Vector3d> {
    public final double min;
    public final double max;
    public final double sliderMin;
    public final double sliderMax;
    public final boolean onSliderRelease;
    public final int decimalPlaces;
    public final boolean noSlider;

    public Vector3dSetting(String name, String description, Vector3d defaultValue, Consumer<Vector3d> onChanged, Consumer<Setting<Vector3d>> onModuleActivated, IVisible visible, double min, double max, double sliderMin, double sliderMax, boolean onSliderRelease, int decimalPlaces, boolean noSlider) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.min = min;
        this.max = max;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.decimalPlaces = decimalPlaces;
        this.onSliderRelease = onSliderRelease;
        this.noSlider = noSlider;
    }

    public boolean set(double x, double y, double z) {
        ((Vector3d) this.value).set(x, y, z);
        return super.set((Vector3d) this.value);
    }

    /* JADX WARN: Type inference failed for: r1v3, types: [T, org.joml.Vector3d] */
    @Override // meteordevelopment.meteorclient.settings.Setting
    protected void resetImpl() {
        if (this.value == 0) {
            this.value = new Vector3d();
        }
        ((Vector3d) this.value).set((Vector3dc) this.defaultValue);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Vector3d parseImpl(String str) {
        try {
            String[] strs = str.split(" ");
            return new Vector3d(Double.parseDouble(strs[0]), Double.parseDouble(strs[1]), Double.parseDouble(strs[2]));
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(Vector3d value) {
        return value.x >= this.min && value.x <= this.max && value.y >= this.min && value.y <= this.max && value.z >= this.min && value.z <= this.max;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    protected class_2487 save(class_2487 tag) {
        class_2487 valueTag = new class_2487();
        valueTag.method_10549("x", get().x);
        valueTag.method_10549("y", get().y);
        valueTag.method_10549("z", get().z);
        tag.method_10566("value", valueTag);
        return tag;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Vector3d load(class_2487 tag) {
        if (tag.method_10562("value").isEmpty()) {
            return get();
        }
        class_2487 valueTag = (class_2487) tag.method_10562("value").get();
        set(valueTag.method_68563("x", 0.0d), valueTag.method_68563("y", 0.0d), valueTag.method_68563("z", 0.0d));
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/Vector3dSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, Vector3d, Vector3dSetting> {
        public double min;
        public double max;
        public double sliderMin;
        public double sliderMax;
        public boolean onSliderRelease;
        public int decimalPlaces;
        public boolean noSlider;

        public Builder() {
            super(new Vector3d());
            this.min = Double.NEGATIVE_INFINITY;
            this.max = Double.POSITIVE_INFINITY;
            this.sliderMin = 0.0d;
            this.sliderMax = 10.0d;
            this.onSliderRelease = false;
            this.decimalPlaces = 3;
            this.noSlider = false;
        }

        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public Builder defaultValue(Vector3d defaultValue) {
            ((Vector3d) this.defaultValue).set(defaultValue);
            return this;
        }

        public Builder defaultValue(double x, double y, double z) {
            ((Vector3d) this.defaultValue).set(x, y, z);
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
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public Vector3dSetting build() {
            return new Vector3dSetting(this.name, this.description, (Vector3d) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.min, this.max, this.sliderMin, this.sliderMax, this.onSliderRelease, this.decimalPlaces, this.noSlider);
        }
    }
}
