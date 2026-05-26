package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.utils.misc.MyPotion;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/PotionSetting.class */
public class PotionSetting extends EnumSetting<MyPotion> {
    public PotionSetting(String name, String description, MyPotion defaultValue, Consumer<MyPotion> onChanged, Consumer<Setting<MyPotion>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/PotionSetting$Builder.class */
    public static class Builder extends EnumSetting.Builder<MyPotion> {
        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.settings.EnumSetting.Builder, meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public EnumSetting<MyPotion> build() {
            return new PotionSetting(this.name, this.description, (MyPotion) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
