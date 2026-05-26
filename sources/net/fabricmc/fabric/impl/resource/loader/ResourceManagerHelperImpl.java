package net.fabricmc.fabric.impl.resource.loader;

import com.google.common.collect.Lists;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3262;
import net.minecraft.class_3264;
import net.minecraft.class_3288;
import net.minecraft.class_3302;
import net.minecraft.class_3545;
import net.minecraft.class_7225;
import net.minecraft.class_8614;
import net.minecraft.class_9224;
import net.minecraft.class_9225;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/ResourceManagerHelperImpl.class */
public class ResourceManagerHelperImpl implements ResourceManagerHelper {
    private static final Map<class_3264, ResourceManagerHelperImpl> registryMap = new HashMap();
    private static final Set<class_3545<class_2561, ModNioResourcePack>> builtinResourcePacks = new HashSet();
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceManagerHelperImpl.class);
    private final Set<class_2960> addedListenerIds = new HashSet();
    private final Set<ListenerFactory> listenerFactories = new LinkedHashSet();
    private final Set<IdentifiableResourceReloadListener> addedListeners = new LinkedHashSet();
    private final class_3264 type;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/ResourceManagerHelperImpl$ListenerFactory.class */
    private interface ListenerFactory {
        class_2960 id();

        IdentifiableResourceReloadListener get(class_7225.class_7874 class_7874Var);
    }

    private ResourceManagerHelperImpl(class_3264 type) {
        this.type = type;
    }

    public static ResourceManagerHelperImpl get(class_3264 type) {
        return registryMap.computeIfAbsent(type, ResourceManagerHelperImpl::new);
    }

    public static boolean registerBuiltinResourcePack(class_2960 id, String subPath, ModContainer container, class_2561 displayName, ResourcePackActivationType activationType) {
        List<Path> paths = container.getRootPaths();
        String separator = ((Path) paths.getFirst()).getFileSystem().getSeparator();
        String subPath2 = subPath.replace("/", separator);
        ModNioResourcePack resourcePack = ModNioResourcePack.create(id.toString(), container, subPath2, class_3264.field_14188, activationType, false);
        ModNioResourcePack dataPack = ModNioResourcePack.create(id.toString(), container, subPath2, class_3264.field_14190, activationType, false);
        if (resourcePack == null && dataPack == null) {
            return false;
        }
        if (resourcePack != null) {
            builtinResourcePacks.add(new class_3545<>(displayName, resourcePack));
        }
        if (dataPack != null) {
            builtinResourcePacks.add(new class_3545<>(displayName, dataPack));
            return true;
        }
        return true;
    }

    public static boolean registerBuiltinResourcePack(class_2960 id, String subPath, ModContainer container, ResourcePackActivationType activationType) {
        return registerBuiltinResourcePack(id, subPath, container, class_2561.method_43470(id.method_12836() + "/" + id.method_12832()), activationType);
    }

    public static void registerBuiltinResourcePacks(class_3264 resourceType, Consumer<class_3288> consumer) {
        for (final class_3545<class_2561, ModNioResourcePack> entry : builtinResourcePacks) {
            ModNioResourcePack pack = (ModNioResourcePack) entry.method_15441();
            if (!pack.method_14406(resourceType).isEmpty()) {
                class_9224 info = new class_9224(((ModNioResourcePack) entry.method_15441()).method_14409(), (class_2561) entry.method_15442(), new BuiltinModResourcePackSource(pack.getFabricModMetadata().getName()), ((ModNioResourcePack) entry.method_15441()).method_56929());
                class_9225 info2 = new class_9225(pack.getActivationType() == ResourcePackActivationType.ALWAYS_ENABLED, class_3288.class_3289.field_14280, false);
                class_3288 profile = class_3288.method_45275(info, new class_3288.class_7680() { // from class: net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl.1
                    public class_3262 method_52424(class_9224 var1) {
                        return (class_3262) entry.method_15441();
                    }

                    public class_3262 method_52425(class_9224 var1, class_3288.class_7679 metadata) {
                        ModNioResourcePack pack2 = (ModNioResourcePack) entry.method_15441();
                        if (metadata.comp_1584().isEmpty()) {
                            return pack2;
                        }
                        List<class_3262> overlays = new ArrayList<>(metadata.comp_1584().size());
                        for (String overlay : metadata.comp_1584()) {
                            overlays.add(pack2.createOverlay(overlay));
                        }
                        return new class_8614(pack2, overlays);
                    }
                }, resourceType, info2);
                consumer.accept(profile);
            }
        }
    }

    public static List<class_3302> sort(class_3264 type, List<class_3302> listeners) {
        if (type == null) {
            return listeners;
        }
        ResourceManagerHelperImpl instance = get(type);
        if (instance != null) {
            List<class_3302> mutable = new ArrayList<>(listeners);
            instance.sort(mutable);
            return Collections.unmodifiableList(mutable);
        }
        return listeners;
    }

    protected void sort(List<class_3302> listeners) {
        listeners.removeAll(this.addedListeners);
        class_7225.class_7874 wrapperLookup = getWrapperLookup(listeners);
        List<IdentifiableResourceReloadListener> listenersToAdd = Lists.newArrayList();
        for (ListenerFactory addedListener : this.listenerFactories) {
            listenersToAdd.add(addedListener.get(wrapperLookup));
        }
        this.addedListeners.clear();
        this.addedListeners.addAll(listenersToAdd);
        Set<class_2960> resolvedIds = new HashSet<>();
        for (class_3302 listener : listeners) {
            if (listener instanceof IdentifiableResourceReloadListener) {
                resolvedIds.add(((IdentifiableResourceReloadListener) listener).getFabricId());
            }
        }
        int lastSize = -1;
        while (listeners.size() != lastSize) {
            lastSize = listeners.size();
            Iterator<IdentifiableResourceReloadListener> it = listenersToAdd.iterator();
            while (it.hasNext()) {
                IdentifiableResourceReloadListener listener2 = it.next();
                if (resolvedIds.containsAll(listener2.getFabricDependencies())) {
                    resolvedIds.add(listener2.getFabricId());
                    listeners.add(listener2);
                    it.remove();
                }
            }
        }
        Iterator<IdentifiableResourceReloadListener> it2 = listenersToAdd.iterator();
        while (it2.hasNext()) {
            LOGGER.warn("Could not resolve dependencies for listener: " + String.valueOf(it2.next().getFabricId()) + "!");
        }
    }

    @Nullable
    private class_7225.class_7874 getWrapperLookup(List<class_3302> listeners) {
        if (this.type == class_3264.field_14188) {
            return null;
        }
        Iterator<class_3302> it = listeners.iterator();
        while (it.hasNext()) {
            FabricRecipeManager fabricRecipeManager = (class_3302) it.next();
            if (fabricRecipeManager instanceof FabricRecipeManager) {
                FabricRecipeManager recipeManager = fabricRecipeManager;
                return recipeManager.fabric_getRegistries();
            }
        }
        throw new IllegalStateException("No ServerRecipeManager found in listeners!");
    }

    @Override // net.fabricmc.fabric.api.resource.ResourceManagerHelper
    public void registerReloadListener(IdentifiableResourceReloadListener listener) {
        registerReloadListener(new SimpleResourceReloaderFactory(listener));
    }

    @Override // net.fabricmc.fabric.api.resource.ResourceManagerHelper
    public void registerReloadListener(class_2960 identifier, Function<class_7225.class_7874, IdentifiableResourceReloadListener> listenerFactory) {
        if (this.type == class_3264.field_14188) {
            throw new IllegalArgumentException("Cannot register a registry listener for the client resource type!");
        }
        registerReloadListener(new RegistryResourceReloaderFactory(identifier, listenerFactory));
    }

    private void registerReloadListener(ListenerFactory factory) {
        if (!this.addedListenerIds.add(factory.id())) {
            LOGGER.warn("Tried to register resource reload listener " + String.valueOf(factory.id()) + " twice!");
        } else if (!this.listenerFactories.add(factory)) {
            throw new RuntimeException("Listener with previously unknown ID " + String.valueOf(factory.id()) + " already in listener set!");
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/ResourceManagerHelperImpl$SimpleResourceReloaderFactory.class */
    private static final class SimpleResourceReloaderFactory extends Record implements ListenerFactory {
        private final IdentifiableResourceReloadListener listener;

        private SimpleResourceReloaderFactory(IdentifiableResourceReloadListener listener) {
            this.listener = listener;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, SimpleResourceReloaderFactory.class), SimpleResourceReloaderFactory.class, "listener", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/ResourceManagerHelperImpl$SimpleResourceReloaderFactory;->listener:Lnet/fabricmc/fabric/api/resource/IdentifiableResourceReloadListener;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, SimpleResourceReloaderFactory.class), SimpleResourceReloaderFactory.class, "listener", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/ResourceManagerHelperImpl$SimpleResourceReloaderFactory;->listener:Lnet/fabricmc/fabric/api/resource/IdentifiableResourceReloadListener;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, SimpleResourceReloaderFactory.class, Object.class), SimpleResourceReloaderFactory.class, "listener", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/ResourceManagerHelperImpl$SimpleResourceReloaderFactory;->listener:Lnet/fabricmc/fabric/api/resource/IdentifiableResourceReloadListener;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public IdentifiableResourceReloadListener listener() {
            return this.listener;
        }

        @Override // net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl.ListenerFactory
        public class_2960 id() {
            return this.listener.getFabricId();
        }

        @Override // net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl.ListenerFactory
        public IdentifiableResourceReloadListener get(class_7225.class_7874 registry) {
            return this.listener;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/ResourceManagerHelperImpl$RegistryResourceReloaderFactory.class */
    private static final class RegistryResourceReloaderFactory extends Record implements ListenerFactory {
        private final class_2960 id;
        private final Function<class_7225.class_7874, IdentifiableResourceReloadListener> listenerFactory;

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, RegistryResourceReloaderFactory.class), RegistryResourceReloaderFactory.class, "id;listenerFactory", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/ResourceManagerHelperImpl$RegistryResourceReloaderFactory;->id:Lnet/minecraft/class_2960;", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/ResourceManagerHelperImpl$RegistryResourceReloaderFactory;->listenerFactory:Ljava/util/function/Function;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, RegistryResourceReloaderFactory.class), RegistryResourceReloaderFactory.class, "id;listenerFactory", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/ResourceManagerHelperImpl$RegistryResourceReloaderFactory;->id:Lnet/minecraft/class_2960;", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/ResourceManagerHelperImpl$RegistryResourceReloaderFactory;->listenerFactory:Ljava/util/function/Function;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, RegistryResourceReloaderFactory.class, Object.class), RegistryResourceReloaderFactory.class, "id;listenerFactory", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/ResourceManagerHelperImpl$RegistryResourceReloaderFactory;->id:Lnet/minecraft/class_2960;", "FIELD:Lnet/fabricmc/fabric/impl/resource/loader/ResourceManagerHelperImpl$RegistryResourceReloaderFactory;->listenerFactory:Ljava/util/function/Function;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        @Override // net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl.ListenerFactory
        public class_2960 id() {
            return this.id;
        }

        public Function<class_7225.class_7874, IdentifiableResourceReloadListener> listenerFactory() {
            return this.listenerFactory;
        }

        private RegistryResourceReloaderFactory(class_2960 id, Function<class_7225.class_7874, IdentifiableResourceReloadListener> listenerFactory) {
            Objects.requireNonNull(listenerFactory);
            this.id = id;
            this.listenerFactory = listenerFactory;
        }

        @Override // net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl.ListenerFactory
        public IdentifiableResourceReloadListener get(class_7225.class_7874 registry) {
            IdentifiableResourceReloadListener listener = this.listenerFactory.apply(registry);
            if (!this.id.equals(listener.getFabricId())) {
                throw new IllegalStateException("Listener factory for " + String.valueOf(this.id) + " created a listener with ID " + String.valueOf(listener.getFabricId()));
            }
            return listener;
        }
    }
}
