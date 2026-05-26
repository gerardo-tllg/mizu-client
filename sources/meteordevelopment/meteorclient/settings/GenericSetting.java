package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import meteordevelopment.meteorclient.gui.utils.IScreenFactory;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/GenericSetting.class */
public class GenericSetting<T extends ICopyable<T> & ISerializable<T> & IScreenFactory> extends Setting<T> {
    /* JADX WARN: Incorrect types in method signature: (Ljava/lang/String;Ljava/lang/String;TT;Ljava/util/function/Consumer<TT;>;Ljava/util/function/Consumer<Lmeteordevelopment/meteorclient/settings/Setting<TT;>;>;Lmeteordevelopment/meteorclient/settings/IVisible;)V */
    public GenericSetting(String name, String description, ICopyable defaultValue, Consumer onChanged, Consumer onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    /* JADX WARN: Type inference failed for: r1v6, types: [T, meteordevelopment.meteorclient.utils.misc.ICopyable] */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public void resetImpl() {
        if (this.value == null) {
            this.value = ((ICopyable) this.defaultValue).copy();
        }
        ((ICopyable) this.value).set((ICopyable) this.defaultValue);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Incorrect return type in method signature: (Ljava/lang/String;)TT; */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public ICopyable parseImpl(String str) {
        return ((ICopyable) this.defaultValue).copy();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Incorrect types in method signature: (TT;)Z */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(ICopyable value) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2487 save(class_2487 tag) {
        tag.method_10566("value", ((ISerializable) ((ICopyable) get())).toTag());
        return tag;
    }

    /* JADX WARN: Incorrect return type in method signature: (Lnet/minecraft/class_2487;)TT; */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public ICopyable load(class_2487 tag) {
        ((ISerializable) ((ICopyable) get())).fromTag(tag.method_68568("value"));
        return (ICopyable) get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/GenericSetting$Builder.class */
    public static class Builder<T extends ICopyable<T> & ISerializable<T> & IScreenFactory> extends Setting.SettingBuilder<Builder<T>, T, GenericSetting<T>> {
        public Builder() {
            super(null);
        }

        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public GenericSetting<T> build() {
            return new GenericSetting<>(this.name, this.description, (ICopyable) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
