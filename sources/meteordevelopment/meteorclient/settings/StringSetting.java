package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/StringSetting.class */
public class StringSetting extends Setting<String> {
    public final Class<? extends WTextBox.Renderer> renderer;
    public final CharFilter filter;
    public final boolean wide;

    public StringSetting(String name, String description, String defaultValue, Consumer<String> onChanged, Consumer<Setting<String>> onModuleActivated, IVisible visible, Class<? extends WTextBox.Renderer> renderer, CharFilter filter, boolean wide) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.renderer = renderer;
        this.filter = filter;
        this.wide = wide;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public String parseImpl(String str) {
        return str;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(String value) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2487 save(class_2487 tag) {
        tag.method_10582("value", get());
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public String load(class_2487 tag) {
        set(tag.method_68564("value", ""));
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/StringSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, String, StringSetting> {
        private Class<? extends WTextBox.Renderer> renderer;
        private CharFilter filter;
        private boolean wide;

        public Builder() {
            super("");
        }

        public Builder renderer(Class<? extends WTextBox.Renderer> renderer) {
            this.renderer = renderer;
            return this;
        }

        public Builder filter(CharFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder wide() {
            this.wide = true;
            return this;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public StringSetting build() {
            return new StringSetting(this.name, this.description, (String) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.renderer, this.filter, this.wide);
        }
    }
}
