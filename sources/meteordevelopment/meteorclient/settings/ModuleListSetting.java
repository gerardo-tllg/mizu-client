package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2520;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/ModuleListSetting.class */
public class ModuleListSetting extends Setting<List<Module>> {
    private static List<String> suggestions;

    public ModuleListSetting(String name, String description, List<Module> defaultValue, Consumer<List<Module>> onChanged, Consumer<Setting<List<Module>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    /* JADX WARN: Type inference failed for: r1v0, types: [T, java.util.ArrayList] */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public void resetImpl() {
        this.value = new ArrayList((Collection) this.defaultValue);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<Module> parseImpl(String str) {
        String[] values = str.split(",");
        List<Module> modules = new ArrayList<>(values.length);
        try {
            for (String value : values) {
                Module module = Modules.get().get(value.trim());
                if (module != null) {
                    modules.add(module);
                }
            }
        } catch (Exception e) {
        }
        return modules;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(List<Module> value) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<String> getSuggestions() {
        if (suggestions == null) {
            suggestions = new ArrayList(Modules.get().getAll().size());
            for (Module module : Modules.get().getAll()) {
                suggestions.add(module.name);
            }
        }
        return suggestions;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2487 save(class_2487 tag) {
        class_2499 modulesTag = new class_2499();
        for (Module module : get()) {
            modulesTag.add(class_2519.method_23256(module.name));
        }
        tag.method_10566("modules", modulesTag);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<Module> load(class_2487 tag) {
        get().clear();
        class_2499<class_2520> valueTag = tag.method_68569("modules");
        for (class_2520 tagI : valueTag) {
            Module module = Modules.get().get((String) tagI.method_68658().orElse(""));
            if (module != null) {
                get().add(module);
            }
        }
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/ModuleListSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, List<Module>, ModuleListSetting> {
        public Builder() {
            super(new ArrayList(0));
        }

        @SafeVarargs
        public final Builder defaultValue(Class<? extends Module>... defaults) {
            ArrayList arrayList = new ArrayList();
            for (Class<? extends Module> klass : defaults) {
                if (Modules.get().get(klass) != null) {
                    arrayList.add(Modules.get().get(klass));
                }
            }
            return defaultValue(arrayList);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public ModuleListSetting build() {
            return new ModuleListSetting(this.name, this.description, (List) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
