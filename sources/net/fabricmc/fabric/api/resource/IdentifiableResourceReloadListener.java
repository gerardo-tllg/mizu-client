package net.fabricmc.fabric.api.resource;

import java.util.Collection;
import java.util.Collections;
import net.minecraft.class_2960;
import net.minecraft.class_3302;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/api/resource/IdentifiableResourceReloadListener.class */
public interface IdentifiableResourceReloadListener extends class_3302 {
    class_2960 getFabricId();

    default Collection<class_2960> getFabricDependencies() {
        return Collections.emptyList();
    }
}
