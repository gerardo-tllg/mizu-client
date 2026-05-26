package meteordevelopment.meteorclient.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IBlockData;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.IGetter;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.class_2248;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/BlockDataSetting.class */
public class BlockDataSetting<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBlockData<T>> extends Setting<Map<class_2248, T>> {
    public final IGetter<T> defaultData;

    public BlockDataSetting(String name, String description, Map<class_2248, T> defaultValue, Consumer<Map<class_2248, T>> onChanged, Consumer<Setting<Map<class_2248, T>>> onModuleActivated, IGetter<T> defaultData, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.defaultData = defaultData;
    }

    /* JADX WARN: Type inference failed for: r1v0, types: [T, java.util.HashMap] */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public void resetImpl() {
        this.value = new HashMap((Map) this.defaultValue);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Map<class_2248, T> parseImpl(String str) {
        return new HashMap(0);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(Map<class_2248, T> value) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    protected class_2487 save(class_2487 tag) {
        class_2487 valueTag = new class_2487();
        for (class_2248 block : ((Map) get()).keySet()) {
            valueTag.method_10566(class_7923.field_41175.method_10221(block).toString(), ((ISerializable) ((ICopyable) ((Map) get()).get(block))).toTag());
        }
        tag.method_10566("value", valueTag);
        return tag;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Map<class_2248, T> load(class_2487 tag) {
        ((Map) get()).clear();
        class_2487 valueTag = tag.method_68568("value");
        for (String key : valueTag.method_10541()) {
            ((Map) get()).put((class_2248) class_7923.field_41175.method_63535(class_2960.method_60654(key)), (ICopyable) ((ISerializable) ((ICopyable) this.defaultData.get()).copy()).fromTag2(valueTag.method_68568(key)));
        }
        return (Map) get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/BlockDataSetting$Builder.class */
    public static class Builder<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBlockData<T>> extends Setting.SettingBuilder<Builder<T>, Map<class_2248, T>, BlockDataSetting<T>> {
        private IGetter<T> defaultData;

        public Builder() {
            super(new HashMap(0));
        }

        public Builder<T> defaultData(IGetter<T> defaultData) {
            this.defaultData = defaultData;
            return this;
        }

        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public BlockDataSetting<T> build() {
            return new BlockDataSetting<>(this.name, this.description, (Map) this.defaultValue, this.onChanged, this.onModuleActivated, this.defaultData, this.visible);
        }
    }
}
