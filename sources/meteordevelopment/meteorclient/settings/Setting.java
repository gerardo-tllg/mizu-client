package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.IGetter;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.class_2378;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/Setting.class */
public abstract class Setting<T> implements IGetter<T>, ISerializable<T> {
    private static final List<String> NO_SUGGESTIONS = new ArrayList(0);
    public final String name;
    public final String title;
    public final String description;
    private final IVisible visible;
    protected final T defaultValue;
    protected T value;
    public final Consumer<Setting<T>> onModuleActivated;
    private final Consumer<T> onChanged;
    public Module module;
    public boolean lastWasVisible;

    protected abstract T parseImpl(String str);

    protected abstract boolean isValueValid(T t);

    protected abstract class_2487 save(class_2487 class_2487Var);

    protected abstract T load(class_2487 class_2487Var);

    public Setting(String name, String description, T defaultValue, Consumer<T> onChanged, Consumer<Setting<T>> onModuleActivated, IVisible visible) {
        this.name = name;
        this.title = Utils.nameToTitle(name);
        this.description = description;
        this.defaultValue = defaultValue;
        this.onChanged = onChanged;
        this.onModuleActivated = onModuleActivated;
        this.visible = visible;
        resetImpl();
    }

    @Override // meteordevelopment.meteorclient.utils.misc.IGetter
    public T get() {
        return this.value;
    }

    public boolean set(T value) {
        if (!isValueValid(value)) {
            return false;
        }
        this.value = value;
        onChanged();
        return true;
    }

    protected void resetImpl() {
        this.value = this.defaultValue;
    }

    public void reset() {
        resetImpl();
        onChanged();
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }

    public boolean parse(String str) {
        T newValue = parseImpl(str);
        if (newValue != null && isValueValid(newValue)) {
            this.value = newValue;
            onChanged();
        }
        return newValue != null;
    }

    public boolean wasChanged() {
        return !Objects.equals(this.value, this.defaultValue);
    }

    public void onChanged() {
        if (this.onChanged != null) {
            this.onChanged.accept(this.value);
        }
    }

    public void onActivated() {
        if (this.onModuleActivated != null) {
            this.onModuleActivated.accept(this);
        }
    }

    public boolean isVisible() {
        return this.visible == null || this.visible.isVisible();
    }

    public Iterable<class_2960> getIdentifierSuggestions() {
        return null;
    }

    public List<String> getSuggestions() {
        return NO_SUGGESTIONS;
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10582("name", this.name);
        save(tag);
        return tag;
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public T fromTag(class_2487 tag) {
        T value = load(tag);
        onChanged();
        return value;
    }

    public String toString() {
        return this.value.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Setting<?> setting = (Setting) o;
        return Objects.equals(this.name, setting.name);
    }

    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Nullable
    public static <T> T parseId(class_2378<T> class_2378Var, String str) {
        String strTrim = str.trim();
        class_2960 class_2960VarMethod_60654 = strTrim.contains(":") ? class_2960.method_60654(strTrim) : class_2960.method_60655("minecraft", strTrim);
        if (class_2378Var.method_10250(class_2960VarMethod_60654)) {
            return (T) class_2378Var.method_63535(class_2960VarMethod_60654);
        }
        return null;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/Setting$SettingBuilder.class */
    public static abstract class SettingBuilder<B, V, S> {
        protected String name = "undefined";
        protected String description = "";
        protected V defaultValue;
        protected IVisible visible;
        protected Consumer<V> onChanged;
        protected Consumer<Setting<V>> onModuleActivated;

        public abstract S build();

        protected SettingBuilder(V defaultValue) {
            this.defaultValue = defaultValue;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public B name(String name) {
            this.name = name;
            return this;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public B description(String description) {
            this.description = description;
            return this;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public B defaultValue(V defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public B visible(IVisible visible) {
            this.visible = visible;
            return this;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public B onChanged(Consumer<V> onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public B onModuleActivated(Consumer<Setting<V>> onModuleActivated) {
            this.onModuleActivated = onModuleActivated;
            return this;
        }
    }
}
