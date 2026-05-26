package net.fabricmc.fabric.impl.resource.loader;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.class_155;
import net.minecraft.class_2561;
import net.minecraft.class_3262;
import net.minecraft.class_3264;
import net.minecraft.class_3272;
import net.minecraft.class_3283;
import net.minecraft.class_3285;
import net.minecraft.class_3286;
import net.minecraft.class_3288;
import net.minecraft.class_5359;
import net.minecraft.class_7701;
import net.minecraft.class_7712;
import net.minecraft.class_8580;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/ModResourcePackUtil.class */
public final class ModResourcePackUtil {
    public static final Gson GSON = new Gson();
    private static final Logger LOGGER = LoggerFactory.getLogger(ModResourcePackUtil.class);
    private static final String LOAD_ORDER_KEY = "fabric:resource_load_order";

    private ModResourcePackUtil() {
    }

    public static List<ModResourcePack> getModResourcePacks(FabricLoader fabricLoader, class_3264 type, @Nullable String subPath) {
        ModResourcePack pack;
        ModResourcePackSorter sorter = new ModResourcePackSorter();
        Collection<ModContainer> containers = fabricLoader.getAllMods();
        List<String> allIds = containers.stream().map((v0) -> {
            return v0.getMetadata();
        }).map((v0) -> {
            return v0.getId();
        }).toList();
        for (ModContainer container : containers) {
            ModMetadata metadata = container.getMetadata();
            String id = metadata.getId();
            if (!metadata.getType().equals("builtin") && (pack = ModNioResourcePack.create(id, container, subPath, type, ResourcePackActivationType.ALWAYS_ENABLED, true)) != null) {
                sorter.addPack(pack);
                CustomValue loadOrder = metadata.getCustomValue(LOAD_ORDER_KEY);
                if (loadOrder != null) {
                    if (loadOrder.getType() == CustomValue.CvType.OBJECT) {
                        CustomValue.CvObject object = loadOrder.getAsObject();
                        addLoadOrdering(object, allIds, sorter, Order.BEFORE, id);
                        addLoadOrdering(object, allIds, sorter, Order.AFTER, id);
                    } else {
                        LOGGER.error("[Fabric] Resource load order should be an object");
                    }
                }
            }
        }
        return sorter.getPacks();
    }

    public static void addLoadOrdering(CustomValue.CvObject object, List<String> allIds, ModResourcePackSorter sorter, Order order, String currentId) {
        List<String> modIds = new ArrayList<>();
        CustomValue array = object.get(order.jsonKey);
        if (array == null) {
            return;
        }
        switch (AnonymousClass1.$SwitchMap$net$fabricmc$loader$api$metadata$CustomValue$CvType[array.getType().ordinal()]) {
            case 1:
                modIds.add(array.getAsString());
                break;
            case 2:
                for (CustomValue id : array.getAsArray()) {
                    if (id.getType() == CustomValue.CvType.STRING) {
                        modIds.add(id.getAsString());
                    }
                }
                break;
            default:
                LOGGER.error("[Fabric] {} should be a string or an array", order.jsonKey);
                return;
        }
        Stream<String> stream = modIds.stream();
        Objects.requireNonNull(allIds);
        stream.filter((v1) -> {
            return r1.contains(v1);
        }).forEach(modId -> {
            sorter.addLoadOrdering(modId, currentId, order);
        });
    }

    /* JADX INFO: renamed from: net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/ModResourcePackUtil$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$net$fabricmc$loader$api$metadata$CustomValue$CvType = new int[CustomValue.CvType.values().length];

        static {
            try {
                $SwitchMap$net$fabricmc$loader$api$metadata$CustomValue$CvType[CustomValue.CvType.STRING.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$net$fabricmc$loader$api$metadata$CustomValue$CvType[CustomValue.CvType.ARRAY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public static void refreshAutoEnabledPacks(List<class_3288> enabledProfiles, Map<String, class_3288> allProfiles) {
        LOGGER.debug("[Fabric] Starting internal pack sorting with: {}", enabledProfiles.stream().map((v0) -> {
            return v0.method_14463();
        }).toList());
        enabledProfiles.removeIf(profile -> {
            return ((FabricResourcePackProfile) profile).fabric_isHidden();
        });
        LOGGER.debug("[Fabric] Removed all internal packs, result: {}", enabledProfiles.stream().map((v0) -> {
            return v0.method_14463();
        }).toList());
        ListIterator<class_3288> it = enabledProfiles.listIterator();
        Set<String> seen = new LinkedHashSet<>();
        while (it.hasNext()) {
            class_3288 profile2 = it.next();
            seen.add(profile2.method_14463());
            Iterator<class_3288> it2 = allProfiles.values().iterator();
            while (it2.hasNext()) {
                FabricResourcePackProfile fabricResourcePackProfile = (class_3288) it2.next();
                FabricResourcePackProfile fp = fabricResourcePackProfile;
                if (fp.fabric_isHidden() && fp.fabric_parentsEnabled(seen) && seen.add(fabricResourcePackProfile.method_14463())) {
                    it.add(fabricResourcePackProfile);
                    LOGGER.debug("[Fabric] cur @ {}, auto-enabled {}, currently enabled: {}", new Object[]{profile2.method_14463(), fabricResourcePackProfile.method_14463(), seen});
                }
            }
        }
        LOGGER.debug("[Fabric] Final sorting result: {}", enabledProfiles.stream().map((v0) -> {
            return v0.method_14463();
        }).toList());
    }

    public static boolean containsDefault(String filename, boolean modBundled) {
        return "pack.mcmeta".equals(filename) || (modBundled && "pack.png".equals(filename));
    }

    public static InputStream getDefaultIcon() throws IOException {
        Optional<Path> loaderIconPath = FabricLoader.getInstance().getModContainer("fabric-resource-loader-v0").flatMap(resourceLoaderContainer -> {
            Optional iconPath = resourceLoaderContainer.getMetadata().getIconPath(512);
            Objects.requireNonNull(resourceLoaderContainer);
            return iconPath.flatMap(resourceLoaderContainer::findPath);
        });
        if (loaderIconPath.isPresent()) {
            return Files.newInputStream(loaderIconPath.get(), new OpenOption[0]);
        }
        return null;
    }

    public static InputStream openDefault(ModContainer container, class_3264 type, String filename) throws IOException {
        switch (filename) {
            case "pack.mcmeta":
                String description = (String) Objects.requireNonNullElse(container.getMetadata().getId(), "");
                String metadata = serializeMetadata(class_155.method_16673().method_48017(type), description);
                return IOUtils.toInputStream(metadata, Charsets.UTF_8);
            case "pack.png":
                Optional iconPath = container.getMetadata().getIconPath(512);
                Objects.requireNonNull(container);
                Optional<Path> path = iconPath.flatMap(container::findPath);
                if (path.isPresent()) {
                    return Files.newInputStream(path.get(), new OpenOption[0]);
                }
                return getDefaultIcon();
            default:
                return null;
        }
    }

    public static class_3272 getMetadataPack(int packVersion, class_2561 description) {
        return new class_3272(description, packVersion, Optional.empty());
    }

    public static JsonObject getMetadataPackJson(int packVersion, class_2561 description) {
        return ((JsonElement) class_3272.field_14202.comp_3437().encodeStart(JsonOps.INSTANCE, getMetadataPack(packVersion, description)).getOrThrow()).getAsJsonObject();
    }

    public static String serializeMetadata(int packVersion, String description) {
        JsonObject pack = getMetadataPackJson(packVersion, class_2561.method_43470(description));
        JsonObject metadata = new JsonObject();
        metadata.add("pack", pack);
        return GSON.toJson(metadata);
    }

    public static class_2561 getName(ModMetadata info) {
        if (info.getId() != null) {
            return class_2561.method_43470(info.getId());
        }
        return class_2561.method_43469("pack.name.fabricMod", new Object[]{info.getId()});
    }

    public static class_7712 createDefaultDataConfiguration() {
        ModResourcePackCreator modResourcePackCreator = new ModResourcePackCreator(class_3264.field_14190);
        List<class_3288> moddedResourcePacks = new ArrayList<>();
        Objects.requireNonNull(moddedResourcePacks);
        modResourcePackCreator.method_14453((v1) -> {
            r1.add(v1);
        });
        List<String> enabled = new ArrayList<>(class_5359.field_25393.method_29547());
        List<String> disabled = new ArrayList<>(class_5359.field_25393.method_29550());
        for (class_3288 profile : moddedResourcePacks) {
            if (profile.method_29483() == ModResourcePackCreator.RESOURCE_PACK_SOURCE) {
                enabled.add(profile.method_14463());
            } else {
                class_3262 pack = profile.method_14458();
                try {
                    if ((pack instanceof ModNioResourcePack) && ((ModNioResourcePack) pack).getActivationType().isEnabledByDefault()) {
                        enabled.add(profile.method_14463());
                    } else {
                        disabled.add(profile.method_14463());
                    }
                    if (pack != null) {
                        pack.close();
                    }
                } catch (Throwable th) {
                    if (pack != null) {
                        try {
                            pack.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            }
        }
        return new class_7712(new class_5359(enabled, disabled), class_7701.field_40183);
    }

    public static class_5359 createTestServerSettings(List<String> enabled, List<String> disabled) {
        Set<String> moddedProfiles = new HashSet<>();
        ModResourcePackCreator modResourcePackCreator = new ModResourcePackCreator(class_3264.field_14190);
        modResourcePackCreator.method_14453(profile -> {
            moddedProfiles.add(profile.method_14463());
        });
        List<String> moveToTheEnd = new ArrayList<>();
        Iterator<String> it = enabled.iterator();
        while (it.hasNext()) {
            String profile2 = it.next();
            if (moddedProfiles.contains(profile2)) {
                moveToTheEnd.add(profile2);
                it.remove();
            }
        }
        enabled.addAll(moveToTheEnd);
        return new class_5359(enabled, disabled);
    }

    public static class_3283 createClientManager() {
        return new class_3283(new class_3285[]{new class_3286(new class_8580(path -> {
            return true;
        })), new ModResourcePackCreator(class_3264.field_14190, true)});
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/ModResourcePackUtil$Order.class */
    public enum Order {
        BEFORE("before"),
        AFTER("after");

        private final String jsonKey;

        Order(String jsonKey) {
            this.jsonKey = jsonKey;
        }
    }
}
