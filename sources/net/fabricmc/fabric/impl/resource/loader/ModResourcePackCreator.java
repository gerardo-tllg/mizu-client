package net.fabricmc.fabric.impl.resource.loader;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.resource.loader.PlaceholderResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2561;
import net.minecraft.class_3264;
import net.minecraft.class_3285;
import net.minecraft.class_3288;
import net.minecraft.class_5352;
import net.minecraft.class_9224;
import net.minecraft.class_9225;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/ModResourcePackCreator.class */
public class ModResourcePackCreator implements class_3285 {
    public static final String FABRIC = "fabric";
    private static final String PROGRAMMER_ART = "programmer_art";
    private static final String HIGH_CONTRAST = "high_contrast";
    public static final Set<String> POST_CHANGE_HANDLE_REQUIRED = Set.of(FABRIC, PROGRAMMER_ART, HIGH_CONTRAST);

    @VisibleForTesting
    public static final Predicate<Set<String>> BASE_PARENT = enabled -> {
        return enabled.contains(FABRIC);
    };

    @VisibleForTesting
    public static final Predicate<Set<String>> PROGRAMMER_ART_PARENT = enabled -> {
        return enabled.contains(FABRIC) && enabled.contains(PROGRAMMER_ART);
    };

    @VisibleForTesting
    public static final Predicate<Set<String>> HIGH_CONTRAST_PARENT = enabled -> {
        return enabled.contains(FABRIC) && enabled.contains(HIGH_CONTRAST);
    };
    public static final class_5352 RESOURCE_PACK_SOURCE = new class_5352() { // from class: net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator.1
        public class_2561 method_45282(class_2561 packName) {
            return class_2561.method_43469("pack.nameAndSource", new Object[]{packName, class_2561.method_43471("pack.source.fabricmod")});
        }

        public boolean method_45279() {
            return true;
        }
    };
    public static final ModResourcePackCreator CLIENT_RESOURCE_PACK_PROVIDER = new ModResourcePackCreator(class_3264.field_14188);
    public static final int MAX_KNOWN_PACKS = Integer.getInteger("fabric-resource-loader-v0:maxKnownPacks", 1024).intValue();
    private final class_3264 type;
    private final class_9225 activationInfo;
    private final boolean forClientDataPackManager;

    public ModResourcePackCreator(class_3264 type) {
        this(type, false);
    }

    protected ModResourcePackCreator(class_3264 type, boolean forClientDataPackManager) {
        this.type = type;
        this.activationInfo = new class_9225(!forClientDataPackManager, class_3288.class_3289.field_14280, false);
        this.forClientDataPackManager = forClientDataPackManager;
    }

    public void method_14453(Consumer<class_3288> consumer) {
        class_9224 metadata = new class_9224(FABRIC, class_2561.method_43471("pack.name.fabricMods"), RESOURCE_PACK_SOURCE, Optional.empty());
        consumer.accept(class_3288.method_45275(metadata, new PlaceholderResourcePack.Factory(this.type, metadata), this.type, this.activationInfo));
        registerModPack(consumer, null, BASE_PARENT);
        if (this.type == class_3264.field_14188) {
            registerModPack(consumer, PROGRAMMER_ART, PROGRAMMER_ART_PARENT);
            registerModPack(consumer, HIGH_CONTRAST, HIGH_CONTRAST_PARENT);
        }
        ResourceManagerHelperImpl.registerBuiltinResourcePacks(this.type, consumer);
    }

    private void registerModPack(Consumer<class_3288> consumer, @Nullable String subPath, Predicate<Set<String>> parents) {
        List<ModResourcePack> packs = ModResourcePackUtil.getModResourcePacks(FabricLoader.getInstance(), this.type, subPath);
        for (ModResourcePack pack : packs) {
            FabricResourcePackProfile fabricResourcePackProfileMethod_45275 = class_3288.method_45275(pack.method_56926(), new ModResourcePackFactory(pack), this.type, this.activationInfo);
            if (fabricResourcePackProfileMethod_45275 != null) {
                if (!this.forClientDataPackManager) {
                    fabricResourcePackProfileMethod_45275.fabric_setParentsPredicate(parents);
                }
                consumer.accept(fabricResourcePackProfileMethod_45275);
            }
        }
    }
}
