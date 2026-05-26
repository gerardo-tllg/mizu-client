package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_1291;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2520;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/StatusEffectListSetting.class */
public class StatusEffectListSetting extends Setting<List<class_1291>> {
    public StatusEffectListSetting(String name, String description, List<class_1291> defaultValue, Consumer<List<class_1291>> onChanged, Consumer<Setting<List<class_1291>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    /* JADX WARN: Type inference failed for: r1v0, types: [T, java.util.ArrayList] */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public void resetImpl() {
        this.value = new ArrayList((Collection) this.defaultValue);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<class_1291> parseImpl(String str) {
        String[] values = str.split(",");
        List<class_1291> effects = new ArrayList<>(values.length);
        try {
            for (String value : values) {
                class_1291 effect = (class_1291) parseId(class_7923.field_41174, value);
                if (effect != null) {
                    effects.add(effect);
                }
            }
        } catch (Exception e) {
        }
        return effects;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(List<class_1291> value) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public Iterable<class_2960> getIdentifierSuggestions() {
        return class_7923.field_41174.method_10235();
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2487 save(class_2487 tag) {
        class_2499 valueTag = new class_2499();
        for (class_1291 effect : get()) {
            class_2960 id = class_7923.field_41174.method_10221(effect);
            if (id != null) {
                valueTag.add(class_2519.method_23256(id.toString()));
            }
        }
        tag.method_10566("value", valueTag);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<class_1291> load(class_2487 tag) {
        get().clear();
        for (class_2520 tagI : tag.method_68569("value")) {
            class_1291 effect = (class_1291) class_7923.field_41174.method_63535(class_2960.method_60654((String) tagI.method_68658().orElse("")));
            if (effect != null) {
                get().add(effect);
            }
        }
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/StatusEffectListSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, List<class_1291>, StatusEffectListSetting> {
        public Builder() {
            super(new ArrayList(0));
        }

        public Builder defaultValue(class_1291... defaults) {
            return defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList());
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public StatusEffectListSetting build() {
            return new StatusEffectListSetting(this.name, this.description, (List) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
