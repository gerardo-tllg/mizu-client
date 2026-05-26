package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import java.util.function.Supplier;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.settings.Setting;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/ProvidedStringSetting.class */
public class ProvidedStringSetting extends StringSetting {
    public final Supplier<String[]> supplier;

    public ProvidedStringSetting(String name, String description, String defaultValue, Consumer<String> onChanged, Consumer<Setting<String>> onModuleActivated, IVisible visible, Class<? extends WTextBox.Renderer> renderer, boolean wide, Supplier<String[]> supplier) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, renderer, null, wide);
        this.supplier = supplier;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/ProvidedStringSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, String, ProvidedStringSetting> {
        private Class<? extends WTextBox.Renderer> renderer;
        private Supplier<String[]> supplier;
        private boolean wide;

        public Builder() {
            super(null);
        }

        public Builder renderer(Class<? extends WTextBox.Renderer> renderer) {
            this.renderer = renderer;
            return this;
        }

        public Builder supplier(Supplier<String[]> supplier) {
            this.supplier = supplier;
            return this;
        }

        public Builder wide() {
            this.wide = true;
            return this;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public ProvidedStringSetting build() {
            return new ProvidedStringSetting(this.name, this.description, (String) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.renderer, this.wide, this.supplier);
        }
    }
}
