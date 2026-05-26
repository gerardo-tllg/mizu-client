package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_1792;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2520;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/ItemListSetting.class */
public class ItemListSetting extends Setting<List<class_1792>> {
    public final Predicate<class_1792> filter;
    private final boolean bypassFilterWhenSavingAndLoading;

    public ItemListSetting(String name, String description, List<class_1792> defaultValue, Consumer<List<class_1792>> onChanged, Consumer<Setting<List<class_1792>>> onModuleActivated, IVisible visible, Predicate<class_1792> filter, boolean bypassFilterWhenSavingAndLoading) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.filter = filter;
        this.bypassFilterWhenSavingAndLoading = bypassFilterWhenSavingAndLoading;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<class_1792> parseImpl(String str) {
        String[] values = str.split(",");
        List<class_1792> items = new ArrayList<>(values.length);
        try {
            for (String value : values) {
                class_1792 item = (class_1792) parseId(class_7923.field_41178, value);
                if (item != null && (this.filter == null || this.filter.test(item))) {
                    items.add(item);
                }
            }
        } catch (Exception e) {
        }
        return items;
    }

    /* JADX WARN: Type inference failed for: r1v0, types: [T, java.util.ArrayList] */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public void resetImpl() {
        this.value = new ArrayList((Collection) this.defaultValue);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(List<class_1792> value) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public Iterable<class_2960> getIdentifierSuggestions() {
        return class_7923.field_41178.method_10235();
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2487 save(class_2487 tag) {
        class_2499 valueTag = new class_2499();
        for (class_1792 item : get()) {
            if (this.bypassFilterWhenSavingAndLoading || this.filter == null || this.filter.test(item)) {
                valueTag.add(class_2519.method_23256(class_7923.field_41178.method_10221(item).toString()));
            }
        }
        tag.method_10566("value", valueTag);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<class_1792> load(class_2487 tag) {
        get().clear();
        class_2499<class_2520> valueTag = tag.method_68569("value");
        for (class_2520 tagI : valueTag) {
            class_1792 item = (class_1792) class_7923.field_41178.method_63535(class_2960.method_60654((String) tagI.method_68658().orElse("")));
            if (this.bypassFilterWhenSavingAndLoading || this.filter == null || this.filter.test(item)) {
                get().add(item);
            }
        }
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/ItemListSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, List<class_1792>, ItemListSetting> {
        private Predicate<class_1792> filter;
        private boolean bypassFilterWhenSavingAndLoading;

        public Builder() {
            super(new ArrayList(0));
        }

        public Builder defaultValue(class_1792... defaults) {
            return defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList());
        }

        public Builder filter(Predicate<class_1792> filter) {
            this.filter = filter;
            return this;
        }

        public Builder bypassFilterWhenSavingAndLoading() {
            this.bypassFilterWhenSavingAndLoading = true;
            return this;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public ItemListSetting build() {
            return new ItemListSetting(this.name, this.description, (List) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.filter, this.bypassFilterWhenSavingAndLoading);
        }
    }
}
