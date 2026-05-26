package meteordevelopment.meteorclient.settings;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.lang.reflect.AccessFlag;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_1887;
import net.minecraft.class_1893;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2520;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import net.minecraft.class_7924;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/EnchantmentListSetting.class */
public class EnchantmentListSetting extends Setting<Set<class_5321<class_1887>>> {
    public EnchantmentListSetting(String name, String description, Set<class_5321<class_1887>> defaultValue, Consumer<Set<class_5321<class_1887>>> onChanged, Consumer<Setting<Set<class_5321<class_1887>>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    /* JADX WARN: Type inference failed for: r1v0, types: [T, it.unimi.dsi.fastutil.objects.ObjectOpenHashSet] */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public void resetImpl() {
        this.value = new ObjectOpenHashSet((Collection) this.defaultValue);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Set<class_5321<class_1887>> parseImpl(String str) {
        String[] values = str.split(",");
        ObjectOpenHashSet objectOpenHashSet = new ObjectOpenHashSet(values.length);
        for (String value : values) {
            String name = value.trim();
            class_2960 id = name.contains(":") ? class_2960.method_60654(name) : class_2960.method_60656(name);
            objectOpenHashSet.add(class_5321.method_29179(class_7924.field_41265, id));
        }
        return objectOpenHashSet;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(Set<class_5321<class_1887>> value) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public Iterable<class_2960> getIdentifierSuggestions() {
        return (Iterable) Optional.ofNullable(class_310.method_1551().method_1562()).flatMap(networkHandler -> {
            return networkHandler.method_29091().method_46759(class_7924.field_41265);
        }).map((v0) -> {
            return v0.method_10235();
        }).orElse(Set.of());
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2487 save(class_2487 tag) {
        class_2499 valueTag = new class_2499();
        for (class_5321<class_1887> ench : get()) {
            valueTag.add(class_2519.method_23256(ench.method_29177().toString()));
        }
        tag.method_10566("value", valueTag);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Set<class_5321<class_1887>> load(class_2487 tag) {
        get().clear();
        for (class_2520 tagI : tag.method_68569("value")) {
            get().add(class_5321.method_29179(class_7924.field_41265, class_2960.method_60654((String) tagI.method_68658().orElse(""))));
        }
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/EnchantmentListSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, Set<class_5321<class_1887>>, EnchantmentListSetting> {
        private static final Set<class_5321<class_1887>> VANILLA_DEFAULTS;

        public Builder() {
            super(new ObjectOpenHashSet());
        }

        public Builder vanillaDefaults() {
            return defaultValue(VANILLA_DEFAULTS);
        }

        @SafeVarargs
        public final Builder defaultValue(class_5321<class_1887>... defaults) {
            return defaultValue(defaults != null ? new ObjectOpenHashSet(defaults) : new ObjectOpenHashSet());
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public EnchantmentListSetting build() {
            return new EnchantmentListSetting(this.name, this.description, (Set) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }

        static {
            Stream streamFilter = Arrays.stream(class_1893.class.getDeclaredFields()).filter(field -> {
                return field.accessFlags().containsAll(List.of(AccessFlag.PUBLIC, AccessFlag.STATIC, AccessFlag.FINAL));
            }).filter(field2 -> {
                return field2.getType() == class_5321.class;
            }).map(field3 -> {
                try {
                    return field3.get(null);
                } catch (IllegalAccessException e) {
                    return null;
                }
            }).filter(Objects::nonNull);
            Class<class_5321> cls = class_5321.class;
            Objects.requireNonNull(class_5321.class);
            VANILLA_DEFAULTS = (Set) streamFilter.map(cls::cast).filter(registryKey -> {
                return registryKey.method_58273() == class_7924.field_41265;
            }).collect(Collectors.toSet());
        }
    }
}
