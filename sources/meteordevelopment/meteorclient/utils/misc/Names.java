package meteordevelopment.meteorclient.utils.misc;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ResourcePacksReloadedEvent;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1074;
import net.minecraft.class_1146;
import net.minecraft.class_1291;
import net.minecraft.class_1299;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1887;
import net.minecraft.class_2248;
import net.minecraft.class_2394;
import net.minecraft.class_2396;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3544;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import org.apache.commons.lang3.StringUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/Names.class */
public class Names {
    private static final Map<class_1291, String> statusEffectNames = new Reference2ObjectOpenHashMap(16);
    private static final Map<class_1792, String> itemNames = new Reference2ObjectOpenHashMap(128);
    private static final Map<class_2248, String> blockNames = new Reference2ObjectOpenHashMap(128);
    private static final Map<class_5321<class_1887>, String> enchantmentKeyNames = new WeakHashMap(16);
    private static final Map<class_6880<class_1887>, String> enchantmentEntryNames = new Reference2ObjectOpenHashMap(16);
    private static final Map<class_1299<?>, String> entityTypeNames = new Reference2ObjectOpenHashMap(64);
    private static final Map<class_2396<?>, String> particleTypesNames = new Reference2ObjectOpenHashMap(64);
    private static final Map<class_2960, String> soundNames = new HashMap(64);

    private Names() {
    }

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(Names.class);
    }

    @EventHandler
    private static void onResourcePacksReloaded(ResourcePacksReloadedEvent event) {
        statusEffectNames.clear();
        itemNames.clear();
        blockNames.clear();
        enchantmentEntryNames.clear();
        entityTypeNames.clear();
        particleTypesNames.clear();
        soundNames.clear();
    }

    public static String get(class_1291 effect) {
        return statusEffectNames.computeIfAbsent(effect, effect1 -> {
            return class_3544.method_15440(class_1074.method_4662(effect1.method_5567(), new Object[0]));
        });
    }

    public static String get(class_1792 item) {
        return itemNames.computeIfAbsent(item, item1 -> {
            return class_3544.method_15440(class_1074.method_4662(item1.method_7876(), new Object[0]));
        });
    }

    public static String get(class_2248 block) {
        return blockNames.computeIfAbsent(block, block1 -> {
            return class_3544.method_15440(class_1074.method_4662(block1.method_63499(), new Object[0]));
        });
    }

    public static String get(class_5321<class_1887> enchantment) {
        return enchantmentKeyNames.computeIfAbsent(enchantment, enchantment1 -> {
            return (String) Optional.ofNullable(class_310.method_1551().method_1562()).map((v0) -> {
                return v0.method_29091();
            }).flatMap(registryManager -> {
                return registryManager.method_46759(class_7924.field_41265);
            }).flatMap(registry -> {
                return registry.method_10223(enchantment.method_29177());
            }).map((v0) -> {
                return get(v0);
            }).orElseGet(() -> {
                String key = "enchantment." + enchantment1.method_29177().method_42094();
                String translated = class_1074.method_4662(key, new Object[0]);
                return translated == key ? enchantment1.method_29177().toString() : translated;
            });
        });
    }

    public static String get(class_6880<class_1887> enchantment) {
        return enchantmentEntryNames.computeIfAbsent(enchantment, enchantment1 -> {
            return class_3544.method_15440(((class_1887) enchantment.comp_349()).comp_2686().getString());
        });
    }

    public static String get(class_1299<?> entityType) {
        return entityTypeNames.computeIfAbsent(entityType, entityType1 -> {
            return class_3544.method_15440(class_1074.method_4662(entityType1.method_5882(), new Object[0]));
        });
    }

    public static String get(class_2396<?> type) {
        return !(type instanceof class_2394) ? "" : particleTypesNames.computeIfAbsent(type, effect1 -> {
            return StringUtils.capitalize(class_7923.field_41180.method_10221(type).method_12832().replace("_", " "));
        });
    }

    public static String getSoundName(class_2960 id) {
        return soundNames.computeIfAbsent(id, identifier -> {
            class_2561 text;
            class_1146 soundSet = MeteorClient.mc.method_1483().method_4869(identifier);
            if (soundSet != null && (text = soundSet.method_4886()) != null) {
                return class_3544.method_15440(text.getString());
            }
            return identifier.method_12832();
        });
    }

    public static String get(class_1799 stack) {
        return stack.method_7964().getString();
    }
}
