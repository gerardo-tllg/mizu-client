package meteordevelopment.meteorclient.settings;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import net.minecraft.class_1299;
import net.minecraft.class_1311;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2520;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/EntityTypeListSetting.class */
public class EntityTypeListSetting extends Setting<Set<class_1299<?>>> {
    public final Predicate<class_1299<?>> filter;
    private List<String> suggestions;
    private static final List<String> groups = List.of("animal", "wateranimal", "monster", "ambient", "misc");

    public EntityTypeListSetting(String name, String description, Set<class_1299<?>> defaultValue, Consumer<Set<class_1299<?>>> onChanged, Consumer<Setting<Set<class_1299<?>>>> onModuleActivated, IVisible visible, Predicate<class_1299<?>> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.filter = filter;
    }

    /* JADX WARN: Type inference failed for: r1v0, types: [T, it.unimi.dsi.fastutil.objects.ObjectOpenHashSet] */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public void resetImpl() {
        this.value = new ObjectOpenHashSet((Collection) this.defaultValue);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Set<class_1299<?>> parseImpl(String str) {
        String[] values = str.split(",");
        ObjectOpenHashSet objectOpenHashSet = new ObjectOpenHashSet(values.length);
        try {
            for (String value : values) {
                class_1299<?> entity = (class_1299) parseId(class_7923.field_41177, value);
                if (entity != null) {
                    objectOpenHashSet.add(entity);
                } else {
                    String lowerValue = value.trim().toLowerCase();
                    if (groups.contains(lowerValue)) {
                        for (class_1299<?> entityType : class_7923.field_41177) {
                            if (this.filter == null || this.filter.test(entityType)) {
                                switch (lowerValue) {
                                    case "animal":
                                        if (entityType.method_5891() == class_1311.field_6294) {
                                            objectOpenHashSet.add(entityType);
                                            break;
                                        }
                                        break;
                                    case "wateranimal":
                                        if (entityType.method_5891() == class_1311.field_24460 || entityType.method_5891() == class_1311.field_6300 || entityType.method_5891() == class_1311.field_30092 || entityType.method_5891() == class_1311.field_34447) {
                                            objectOpenHashSet.add(entityType);
                                            break;
                                        }
                                        break;
                                    case "monster":
                                        if (entityType.method_5891() == class_1311.field_6302) {
                                            objectOpenHashSet.add(entityType);
                                            break;
                                        }
                                        break;
                                    case "ambient":
                                        if (entityType.method_5891() == class_1311.field_6303) {
                                            objectOpenHashSet.add(entityType);
                                            break;
                                        }
                                        break;
                                    case "misc":
                                        if (entityType.method_5891() == class_1311.field_17715) {
                                            objectOpenHashSet.add(entityType);
                                            break;
                                        }
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return objectOpenHashSet;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(Set<class_1299<?>> value) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<String> getSuggestions() {
        if (this.suggestions == null) {
            this.suggestions = new ArrayList(groups);
            for (class_1299<?> entityType : class_7923.field_41177) {
                if (this.filter == null || this.filter.test(entityType)) {
                    this.suggestions.add(class_7923.field_41177.method_10221(entityType).toString());
                }
            }
        }
        return this.suggestions;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2487 save(class_2487 tag) {
        class_2499 valueTag = new class_2499();
        for (class_1299<?> entityType : get()) {
            valueTag.add(class_2519.method_23256(class_7923.field_41177.method_10221(entityType).toString()));
        }
        tag.method_10566("value", valueTag);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Set<class_1299<?>> load(class_2487 tag) {
        get().clear();
        class_2499<class_2520> valueTag = tag.method_68569("value");
        for (class_2520 tagI : valueTag) {
            class_1299<?> type = (class_1299) class_7923.field_41177.method_63535(class_2960.method_60654((String) tagI.method_68658().orElse("")));
            if (this.filter == null || this.filter.test(type)) {
                get().add(type);
            }
        }
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/EntityTypeListSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, Set<class_1299<?>>, EntityTypeListSetting> {
        private Predicate<class_1299<?>> filter;

        public Builder() {
            super(new ObjectOpenHashSet(0));
        }

        public Builder defaultValue(class_1299<?>... defaults) {
            return defaultValue(defaults != null ? new ObjectOpenHashSet(defaults) : new ObjectOpenHashSet(0));
        }

        public Builder onlyAttackable() {
            this.filter = EntityUtils::isAttackable;
            return this;
        }

        public Builder filter(Predicate<class_1299<?>> filter) {
            this.filter = filter;
            return this;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public EntityTypeListSetting build() {
            return new EntityTypeListSetting(this.name, this.description, (Set) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.filter);
        }
    }
}
