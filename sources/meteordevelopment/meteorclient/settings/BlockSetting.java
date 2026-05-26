package meteordevelopment.meteorclient.settings;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_2248;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/BlockSetting.class */
public class BlockSetting extends Setting<class_2248> {
    public final Predicate<class_2248> filter;

    public BlockSetting(String name, String description, class_2248 defaultValue, Consumer<class_2248> onChanged, Consumer<Setting<class_2248>> onModuleActivated, IVisible visible, Predicate<class_2248> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.filter = filter;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2248 parseImpl(String str) {
        return (class_2248) parseId(class_7923.field_41175, str);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(class_2248 value) {
        return this.filter == null || this.filter.test(value);
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public Iterable<class_2960> getIdentifierSuggestions() {
        return class_7923.field_41175.method_10235();
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    protected class_2487 save(class_2487 tag) {
        tag.method_10582("value", class_7923.field_41175.method_10221(get()).toString());
        return tag;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    /* JADX WARN: Type inference failed for: r0v15, types: [T, java.lang.Object, net.minecraft.class_2248] */
    /* JADX WARN: Type inference failed for: r1v1, types: [T, java.lang.Object] */
    /* JADX WARN: Type inference incomplete: some casts might be missing */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2248 load(class_2487 class_2487Var) {
        this.value = class_7923.field_41175.method_63535(class_2960.method_60654(class_2487Var.method_68564("value", "")));
        if (this.filter != null && !this.filter.test((class_2248) this.value)) {
            Iterator it = class_7923.field_41175.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ?? r0 = (class_2248) it.next();
                if (this.filter.test((class_2248) r0)) {
                    this.value = r0;
                    break;
                }
            }
        }
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/BlockSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, class_2248, BlockSetting> {
        private Predicate<class_2248> filter;

        public Builder() {
            super(null);
        }

        public Builder filter(Predicate<class_2248> filter) {
            this.filter = filter;
            return this;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public BlockSetting build() {
            return new BlockSetting(this.name, this.description, (class_2248) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.filter);
        }
    }
}
