package net.fabricmc.fabric.impl.resource.loader;

import java.util.WeakHashMap;
import net.minecraft.class_3262;
import net.minecraft.class_5352;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/ResourcePackSourceTracker.class */
public final class ResourcePackSourceTracker {
    private static final WeakHashMap<class_3262, class_5352> SOURCES = new WeakHashMap<>();

    public static class_5352 getSource(class_3262 pack) {
        return SOURCES.getOrDefault(pack, class_5352.field_25347);
    }

    public static void setSource(class_3262 pack, class_5352 source) {
        SOURCES.put(pack, source);
    }
}
