package net.fabricmc.fabric.impl.resource.loader;

import java.util.Set;
import java.util.function.Predicate;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/FabricResourcePackProfile.class */
public interface FabricResourcePackProfile {
    default boolean fabric_isHidden() {
        return false;
    }

    default boolean fabric_parentsEnabled(Set<String> enabled) {
        return true;
    }

    default void fabric_setParentsPredicate(Predicate<Set<String>> predicate) {
    }
}
