package meteordevelopment.meteorclient.settings;

import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/ColorSetting.class */
public class ColorSetting extends Setting<SettingColor> {
    private static final List<String> SUGGESTIONS = List.of("0 0 0 255", "225 25 25 255", "25 225 25 255", "25 25 225 255", "255 255 255 255");

    public ColorSetting(String name, String description, SettingColor defaultValue, Consumer<SettingColor> onChanged, Consumer<Setting<SettingColor>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public SettingColor parseImpl(String str) {
        try {
            String[] strs = str.split(" ");
            return new SettingColor(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]), Integer.parseInt(strs[2]), Integer.parseInt(strs[3]));
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            return null;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v3, types: [T, meteordevelopment.meteorclient.utils.render.color.SettingColor] */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public void resetImpl() {
        if (this.value != 0) {
            ((SettingColor) this.value).set((Color) this.defaultValue);
        } else {
            this.value = new SettingColor((SettingColor) this.defaultValue);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(SettingColor value) {
        value.validate();
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<String> getSuggestions() {
        return SUGGESTIONS;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    protected class_2487 save(class_2487 tag) {
        tag.method_10566("value", get().toTag());
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public SettingColor load(class_2487 tag) {
        get().fromTag(tag.method_68568("value"));
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/ColorSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, SettingColor, ColorSetting> {
        public Builder() {
            super(new SettingColor());
        }

        /* JADX WARN: Can't rename method to resolve collision */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public ColorSetting build() {
            return new ColorSetting(this.name, this.description, (SettingColor) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public Builder defaultValue(SettingColor defaultValue) {
            ((SettingColor) this.defaultValue).set((Color) defaultValue);
            return this;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public Builder defaultValue(Color defaultValue) {
            ((SettingColor) this.defaultValue).set(defaultValue);
            return this;
        }
    }
}
