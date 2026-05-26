package meteordevelopment.meteorclient.settings;

import java.util.List;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/ActionSetting.class */
public class ActionSetting extends Setting<Boolean> {
    public final Runnable action;
    public final String buttonLabel;

    private ActionSetting(String name, String description, Runnable action, String buttonLabel, IVisible visible) {
        super(name, description, false, null, null, visible);
        this.action = action;
        this.buttonLabel = buttonLabel;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Boolean parseImpl(String str) {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(Boolean value) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<String> getSuggestions() {
        return List.of();
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2487 save(class_2487 tag) {
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Boolean load(class_2487 tag) {
        return false;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/ActionSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, Boolean, ActionSetting> {
        private Runnable action;
        private String buttonLabel;

        public Builder() {
            super(false);
            this.action = () -> {
            };
            this.buttonLabel = "open";
        }

        public Builder action(Runnable action) {
            this.action = action;
            return this;
        }

        public Builder buttonLabel(String label) {
            this.buttonLabel = label;
            return this;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public ActionSetting build() {
            return new ActionSetting(this.name, this.description, this.action, this.buttonLabel, this.visible);
        }
    }
}
