package meteordevelopment.meteorclient.settings;

import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/BoolSetting.class */
public class BoolSetting extends Setting<Boolean> {
    private static final List<String> SUGGESTIONS = List.of("true", "false", "toggle");

    private BoolSetting(String name, String description, Boolean defaultValue, Consumer<Boolean> onChanged, Consumer<Setting<Boolean>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Boolean parseImpl(String str) {
        if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("1")) {
            return true;
        }
        if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("0")) {
            return false;
        }
        if (str.equalsIgnoreCase("toggle")) {
            return Boolean.valueOf(!get().booleanValue());
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(Boolean value) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<String> getSuggestions() {
        return SUGGESTIONS;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2487 save(class_2487 tag) {
        tag.method_10556("value", get().booleanValue());
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Boolean load(class_2487 tag) {
        set(Boolean.valueOf(tag.method_68566("value", false)));
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/BoolSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, Boolean, BoolSetting> {
        public Builder() {
            super(false);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public BoolSetting build() {
            return new BoolSetting(this.name, this.description, (Boolean) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
