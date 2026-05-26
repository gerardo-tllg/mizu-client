package meteordevelopment.meteorclient.settings;

import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_2338;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/BlockPosSetting.class */
public class BlockPosSetting extends Setting<class_2338> {
    public BlockPosSetting(String name, String description, class_2338 defaultValue, Consumer<class_2338> onChanged, Consumer<Setting<class_2338>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2338 parseImpl(String str) {
        List<String> values = List.of((Object[]) str.split(","));
        if (values.size() != 3) {
            return null;
        }
        class_2338 bp = null;
        try {
            bp = new class_2338(Integer.parseInt(values.get(0)), Integer.parseInt(values.get(1)), Integer.parseInt(values.get(2)));
        } catch (NumberFormatException e) {
        }
        return bp;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(class_2338 value) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    protected class_2487 save(class_2487 tag) {
        tag.method_10539("value", new int[]{((class_2338) this.value).method_10263(), ((class_2338) this.value).method_10264(), ((class_2338) this.value).method_10260()});
        return tag;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2338 load(class_2487 tag) {
        if (tag.method_10561("value").isPresent()) {
            int[] value = (int[]) tag.method_10561("value").get();
            set(new class_2338(value[0], value[1], value[2]));
        }
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/BlockPosSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, class_2338, BlockPosSetting> {
        public Builder() {
            super(new class_2338(0, 0, 0));
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public BlockPosSetting build() {
            return new BlockPosSetting(this.name, this.description, (class_2338) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
