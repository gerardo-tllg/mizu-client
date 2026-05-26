package meteordevelopment.meteorclient.settings;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2520;
import net.minecraft.class_2596;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/PacketListSetting.class */
public class PacketListSetting extends Setting<Set<Class<? extends class_2596<?>>>> {
    public final Predicate<Class<? extends class_2596<?>>> filter;
    private static List<String> suggestions;

    public PacketListSetting(String name, String description, Set<Class<? extends class_2596<?>>> defaultValue, Consumer<Set<Class<? extends class_2596<?>>>> onChanged, Consumer<Setting<Set<Class<? extends class_2596<?>>>>> onModuleActivated, Predicate<Class<? extends class_2596<?>>> filter, IVisible visible) {
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
    public Set<Class<? extends class_2596<?>>> parseImpl(String str) {
        String[] values = str.split(",");
        ObjectOpenHashSet objectOpenHashSet = new ObjectOpenHashSet(values.length);
        try {
            for (String value : values) {
                Class<? extends class_2596<?>> packet = PacketUtils.getPacket(value.trim());
                if (packet != null && (this.filter == null || this.filter.test(packet))) {
                    objectOpenHashSet.add(packet);
                }
            }
        } catch (Exception e) {
        }
        return objectOpenHashSet;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(Set<Class<? extends class_2596<?>>> value) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<String> getSuggestions() {
        if (suggestions == null) {
            suggestions = new ArrayList(PacketUtils.getC2SPackets().size() + PacketUtils.getS2CPackets().size());
            for (Class<? extends class_2596<?>> packet : PacketUtils.getC2SPackets()) {
                suggestions.add(PacketUtils.getName(packet));
            }
            for (Class<? extends class_2596<?>> packet2 : PacketUtils.getS2CPackets()) {
                suggestions.add(PacketUtils.getName(packet2));
            }
        }
        return suggestions;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2487 save(class_2487 tag) {
        class_2499 valueTag = new class_2499();
        for (Class<? extends class_2596<?>> packet : get()) {
            valueTag.add(class_2519.method_23256(PacketUtils.getName(packet)));
        }
        tag.method_10566("value", valueTag);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Set<Class<? extends class_2596<?>>> load(class_2487 tag) {
        get().clear();
        class_2499<class_2520> class_2499VarMethod_10580 = tag.method_10580("value");
        if (class_2499VarMethod_10580 instanceof class_2499) {
            for (class_2520 t : class_2499VarMethod_10580) {
                Class<? extends class_2596<?>> packet = PacketUtils.getPacket((String) t.method_68658().orElse(""));
                if (packet != null && (this.filter == null || this.filter.test(packet))) {
                    get().add(packet);
                }
            }
        }
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/PacketListSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, Set<Class<? extends class_2596<?>>>, PacketListSetting> {
        private Predicate<Class<? extends class_2596<?>>> filter;

        public Builder() {
            super(new ObjectOpenHashSet(0));
        }

        public Builder filter(Predicate<Class<? extends class_2596<?>>> filter) {
            this.filter = filter;
            return this;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public PacketListSetting build() {
            return new PacketListSetting(this.name, this.description, (Set) this.defaultValue, this.onChanged, this.onModuleActivated, this.filter, this.visible);
        }
    }
}
