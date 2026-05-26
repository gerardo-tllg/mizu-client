package meteordevelopment.meteorclient.settings;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_1291;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/StatusEffectAmplifierMapSetting.class */
public class StatusEffectAmplifierMapSetting extends Setting<Reference2IntMap<class_1291>> {
    public static final Reference2IntMap<class_1291> EMPTY_STATUS_EFFECT_MAP = createStatusEffectMap();

    public StatusEffectAmplifierMapSetting(String name, String description, Reference2IntMap<class_1291> defaultValue, Consumer<Reference2IntMap<class_1291>> onChanged, Consumer<Setting<Reference2IntMap<class_1291>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    /* JADX WARN: Type inference failed for: r1v0, types: [T, it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap] */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public void resetImpl() {
        this.value = new Reference2IntOpenHashMap((Reference2IntMap) this.defaultValue);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Reference2IntMap<class_1291> parseImpl(String str) {
        String[] values = str.split(",");
        Reference2IntOpenHashMap reference2IntOpenHashMap = new Reference2IntOpenHashMap(EMPTY_STATUS_EFFECT_MAP);
        try {
            for (String value : values) {
                String[] split = value.split(" ");
                class_1291 effect = (class_1291) parseId(class_7923.field_41174, split[0]);
                int level = Integer.parseInt(split[1]);
                reference2IntOpenHashMap.put(effect, level);
            }
        } catch (Exception e) {
        }
        return reference2IntOpenHashMap;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(Reference2IntMap<class_1291> value) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2487 save(class_2487 tag) {
        class_2487 valueTag = new class_2487();
        ObjectIterator it = get().keySet().iterator();
        while (it.hasNext()) {
            class_1291 statusEffect = (class_1291) it.next();
            class_2960 id = class_7923.field_41174.method_10221(statusEffect);
            if (id != null) {
                valueTag.method_10569(id.toString(), get().getInt(statusEffect));
            }
        }
        tag.method_10566("value", valueTag);
        return tag;
    }

    private static Reference2IntMap<class_1291> createStatusEffectMap() {
        Reference2IntArrayMap reference2IntArrayMap = new Reference2IntArrayMap(class_7923.field_41174.method_10235().size());
        class_7923.field_41174.forEach(potion -> {
            reference2IntArrayMap.put(potion, 0);
        });
        return reference2IntArrayMap;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Reference2IntMap<class_1291> load(class_2487 tag) {
        get().clear();
        class_2487 valueTag = tag.method_68568("value");
        for (String key : valueTag.method_10541()) {
            class_1291 statusEffect = (class_1291) class_7923.field_41174.method_63535(class_2960.method_60654(key));
            if (statusEffect != null) {
                get().put(statusEffect, valueTag.method_68083(key, 0));
            }
        }
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/StatusEffectAmplifierMapSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, Reference2IntMap<class_1291>, StatusEffectAmplifierMapSetting> {
        public Builder() {
            super(new Reference2IntOpenHashMap(0));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public StatusEffectAmplifierMapSetting build() {
            return new StatusEffectAmplifierMapSetting(this.name, this.description, (Reference2IntMap) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
