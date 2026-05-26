package meteordevelopment.meteorclient.settings;

import java.lang.Enum;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/EnumSetting.class */
public class EnumSetting<T extends Enum<?>> extends Setting<T> {
    private final T[] values;
    private final List<String> suggestions;

    public EnumSetting(String str, String str2, T t, Consumer<T> consumer, Consumer<Setting<T>> consumer2, IVisible iVisible) {
        super(str, str2, t, consumer, consumer2, iVisible);
        this.values = (T[]) ((Enum[]) t.getDeclaringClass().getEnumConstants());
        this.suggestions = new ArrayList(this.values.length);
        for (T t2 : this.values) {
            this.suggestions.add(t2.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public T parseImpl(String str) {
        for (T possibleValue : this.values) {
            if (str.equalsIgnoreCase(possibleValue.toString())) {
                return possibleValue;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(T value) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<String> getSuggestions() {
        return this.suggestions;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2487 save(class_2487 tag) {
        tag.method_10582("value", get().toString());
        return tag;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public T load(class_2487 tag) {
        parse(tag.method_68564("value", ""));
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/EnumSetting$Builder.class */
    public static class Builder<T extends Enum<?>> extends Setting.SettingBuilder<Builder<T>, T, EnumSetting<T>> {
        public Builder() {
            super(null);
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public EnumSetting<T> build() {
            return new EnumSetting<>(this.name, this.description, (Enum) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
