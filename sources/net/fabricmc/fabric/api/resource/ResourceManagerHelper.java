package net.fabricmc.fabric.api.resource;

import java.util.function.Function;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3264;
import net.minecraft.class_7225;
import org.jetbrains.annotations.ApiStatus;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/api/resource/ResourceManagerHelper.class */
@ApiStatus.NonExtendable
public interface ResourceManagerHelper {
    void registerReloadListener(IdentifiableResourceReloadListener identifiableResourceReloadListener);

    void registerReloadListener(class_2960 class_2960Var, Function<class_7225.class_7874, IdentifiableResourceReloadListener> function);

    @Deprecated
    default void addReloadListener(IdentifiableResourceReloadListener listener) {
        registerReloadListener(listener);
    }

    static ResourceManagerHelper get(class_3264 type) {
        return ResourceManagerHelperImpl.get(type);
    }

    static boolean registerBuiltinResourcePack(class_2960 id, ModContainer container, ResourcePackActivationType activationType) {
        return ResourceManagerHelperImpl.registerBuiltinResourcePack(id, "resourcepacks/" + id.method_12832(), container, activationType);
    }

    static boolean registerBuiltinResourcePack(class_2960 id, ModContainer container, class_2561 displayName, ResourcePackActivationType activationType) {
        return ResourceManagerHelperImpl.registerBuiltinResourcePack(id, "resourcepacks/" + id.method_12832(), container, displayName, activationType);
    }

    @Deprecated
    static boolean registerBuiltinResourcePack(class_2960 id, ModContainer container, String displayName, ResourcePackActivationType activationType) {
        return ResourceManagerHelperImpl.registerBuiltinResourcePack(id, "resourcepacks/" + id.method_12832(), container, class_2561.method_43470(displayName), activationType);
    }

    @Deprecated
    static boolean registerBuiltinResourcePack(class_2960 id, String subPath, ModContainer container, boolean enabledByDefault) {
        return ResourceManagerHelperImpl.registerBuiltinResourcePack(id, subPath, container, enabledByDefault ? ResourcePackActivationType.DEFAULT_ENABLED : ResourcePackActivationType.NORMAL);
    }
}
