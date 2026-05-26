package net.fabricmc.fabric.impl.resource.loader;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.class_3264;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/ServerLanguageUtil.class */
public final class ServerLanguageUtil {
    private static final String ASSETS_PREFIX = class_3264.field_14188.method_14413() + "/";

    private ServerLanguageUtil() {
    }

    public static Collection<Path> getModLanguageFiles() {
        Set<Path> paths = new LinkedHashSet<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            if (!mod.getMetadata().getType().equals("builtin")) {
                Map<class_3264, Set<String>> map = ModNioResourcePack.readNamespaces(mod.getRootPaths(), mod.getMetadata().getId());
                for (String ns : map.get(class_3264.field_14188)) {
                    Optional optionalFilter = mod.findPath(ASSETS_PREFIX + ns + "/lang/en_us.json").filter(x$0 -> {
                        return Files.isRegularFile(x$0, new LinkOption[0]);
                    });
                    Objects.requireNonNull(paths);
                    optionalFilter.ifPresent((v1) -> {
                        r1.add(v1);
                    });
                }
            }
        }
        return Collections.unmodifiableCollection(paths);
    }
}
