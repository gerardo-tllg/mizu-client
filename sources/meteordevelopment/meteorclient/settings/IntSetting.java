package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/IntSetting.class */
public class IntSetting extends Setting<Integer> {
    public final int min;
    public final int max;
    public final int sliderMin;
    public final int sliderMax;
    public final boolean noSlider;

    private IntSetting(String name, String description, int defaultValue, Consumer<Integer> onChanged, Consumer<Setting<Integer>> onModuleActivated, IVisible visible, int min, int max, int sliderMin, int sliderMax, boolean noSlider) {
        super(name, description, Integer.valueOf(defaultValue), onChanged, onModuleActivated, visible);
        this.min = min;
        this.max = max;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.noSlider = noSlider;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Integer parseImpl(String str) {
        try {
            return Integer.valueOf(Integer.parseInt(str.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(Integer value) {
        return value.intValue() >= this.min && value.intValue() <= this.max;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2487 save(class_2487 tag) {
        tag.method_10569("value", get().intValue());
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Integer load(class_2487 tag) {
        set(Integer.valueOf(tag.method_68083("value", 0)));
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/IntSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, Integer, IntSetting> {
        private int min;
        private int max;
        private int sliderMin;
        private int sliderMax;
        private boolean noSlider;

        public Builder() {
            super(0);
            this.min = Integer.MIN_VALUE;
            this.max = Integer.MAX_VALUE;
            this.sliderMin = 0;
            this.sliderMax = 10;
            this.noSlider = false;
        }

        public Builder min(int min) {
            this.min = min;
            return this;
        }

        public Builder max(int max) {
            this.max = max;
            return this;
        }

        public Builder range(int min, int max) {
            this.min = Math.min(min, max);
            this.max = Math.max(min, max);
            return this;
        }

        public Builder sliderMin(int min) {
            this.sliderMin = min;
            return this;
        }

        public Builder sliderMax(int max) {
            this.sliderMax = max;
            return this;
        }

        public Builder sliderRange(int min, int max) {
            this.sliderMin = min;
            this.sliderMax = max;
            return this;
        }

        public Builder noSlider() {
            this.noSlider = true;
            return this;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public IntSetting build() {
            return new IntSetting(this.name, this.description, ((Integer) this.defaultValue).intValue(), this.onChanged, this.onModuleActivated, this.visible, this.min, this.max, Math.max(this.sliderMin, this.min), Math.min(this.sliderMax, this.max), this.noSlider);
        }
    }
}
