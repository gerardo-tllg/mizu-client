package net.fabricmc.fabric.impl.resource.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3255;
import net.minecraft.class_3262;
import net.minecraft.class_3264;
import net.minecraft.class_4239;
import net.minecraft.class_5250;
import net.minecraft.class_7367;
import net.minecraft.class_7677;
import net.minecraft.class_9224;
import net.minecraft.class_9226;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/ModNioResourcePack.class */
public class ModNioResourcePack implements class_3262, ModResourcePack {
    private final String id;
    private final ModContainer mod;
    private final List<Path> basePaths;
    private final class_3264 type;
    private final ResourcePackActivationType activationType;
    private final Map<class_3264, Set<String>> namespaces;
    private final class_9224 metadata;
    private final boolean modBundled;
    private static final Logger LOGGER = LoggerFactory.getLogger(ModNioResourcePack.class);
    private static final Pattern RESOURCE_PACK_PATH = Pattern.compile("[a-z0-9-_.]+");
    private static final FileSystem DEFAULT_FS = FileSystems.getDefault();
    private static final String resPrefix = class_3264.field_14188.method_14413() + "/";
    private static final String dataPrefix = class_3264.field_14190.method_14413() + "/";

    @Nullable
    public static ModNioResourcePack create(String id, ModContainer mod, String subPath, class_3264 type, ResourcePackActivationType activationType, boolean modBundled) {
        List<Path> paths;
        class_5250 class_5250VarMethod_43469;
        List<Path> rootPaths = mod.getRootPaths();
        if (subPath == null) {
            paths = rootPaths;
        } else {
            paths = new ArrayList<>(rootPaths.size());
            Iterator<Path> it = rootPaths.iterator();
            while (it.hasNext()) {
                Path path = it.next().toAbsolutePath().normalize();
                Path childPath = path.resolve(subPath.replace("/", path.getFileSystem().getSeparator())).normalize();
                if (childPath.startsWith(path) && exists(childPath)) {
                    paths.add(childPath);
                }
            }
        }
        if (paths.isEmpty()) {
            return null;
        }
        String packId = (subPath == null || !modBundled) ? id : id + "_" + subPath;
        if (subPath == null) {
            class_5250VarMethod_43469 = class_2561.method_43469("pack.name.fabricMod", new Object[]{mod.getMetadata().getName()});
        } else {
            class_5250VarMethod_43469 = class_2561.method_43469("pack.name.fabricMod.subPack", new Object[]{mod.getMetadata().getName(), class_2561.method_43471("resourcePack." + subPath + ".name")});
        }
        class_9224 metadata = new class_9224(packId, class_5250VarMethod_43469, ModResourcePackCreator.RESOURCE_PACK_SOURCE, Optional.of(new class_9226(ModResourcePackCreator.FABRIC, packId, mod.getMetadata().getVersion().getFriendlyString())));
        ModNioResourcePack ret = new ModNioResourcePack(packId, mod, paths, type, activationType, modBundled, metadata);
        if (ret.method_14406(type).isEmpty()) {
            return null;
        }
        return ret;
    }

    private ModNioResourcePack(String id, ModContainer mod, List<Path> paths, class_3264 type, ResourcePackActivationType activationType, boolean modBundled, class_9224 metadata) {
        this.id = id;
        this.mod = mod;
        this.basePaths = paths;
        this.type = type;
        this.activationType = activationType;
        this.modBundled = modBundled;
        this.namespaces = readNamespaces(paths, mod.getMetadata().getId());
        this.metadata = metadata;
    }

    @Override // net.fabricmc.fabric.api.resource.ModResourcePack
    public ModNioResourcePack createOverlay(String overlay) {
        return new ModNioResourcePack(this.id, this.mod, this.basePaths.stream().map(path -> {
            return path.resolve(overlay);
        }).toList(), this.type, this.activationType, this.modBundled, this.metadata);
    }

    static Map<class_3264, Set<String>> readNamespaces(List<Path> paths, String modId) {
        Map<class_3264, Set<String>> ret = new EnumMap<>(class_3264.class);
        for (class_3264 type : class_3264.values()) {
            Set<String> namespaces = null;
            for (Path path : paths) {
                Path dir = path.resolve(type.method_14413());
                if (Files.isDirectory(dir, new LinkOption[0])) {
                    String separator = path.getFileSystem().getSeparator();
                    try {
                        DirectoryStream<Path> ds = Files.newDirectoryStream(dir);
                        try {
                            for (Path p : ds) {
                                if (Files.isDirectory(p, new LinkOption[0])) {
                                    String s = p.getFileName().toString().replace(separator, "");
                                    if (!RESOURCE_PACK_PATH.matcher(s).matches()) {
                                        LOGGER.warn("Fabric NioResourcePack: ignored invalid namespace: {} in mod ID {}", s, modId);
                                    } else {
                                        if (namespaces == null) {
                                            namespaces = new HashSet<>();
                                        }
                                        namespaces.add(s);
                                    }
                                }
                            }
                            if (ds != null) {
                                ds.close();
                            }
                        } catch (Throwable th) {
                            if (ds != null) {
                                try {
                                    ds.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    } catch (IOException e) {
                        LOGGER.warn("getNamespaces in mod " + modId + " failed!", e);
                    }
                }
            }
            ret.put(type, namespaces != null ? namespaces : Collections.emptySet());
        }
        return ret;
    }

    private Path getPath(String filename) {
        if (hasAbsentNs(filename)) {
            return null;
        }
        for (Path basePath : this.basePaths) {
            Path childPath = basePath.resolve(filename.replace("/", basePath.getFileSystem().getSeparator())).toAbsolutePath().normalize();
            if (childPath.startsWith(basePath) && exists(childPath)) {
                return childPath;
            }
        }
        return null;
    }

    private boolean hasAbsentNs(String filename) {
        int prefixLen;
        class_3264 type;
        if (filename.startsWith(resPrefix)) {
            prefixLen = resPrefix.length();
            type = class_3264.field_14188;
        } else if (filename.startsWith(dataPrefix)) {
            prefixLen = dataPrefix.length();
            type = class_3264.field_14190;
        } else {
            return false;
        }
        int nsEnd = filename.indexOf(47, prefixLen);
        return nsEnd >= 0 && !this.namespaces.get(type).contains(filename.substring(prefixLen, nsEnd));
    }

    private class_7367<InputStream> openFile(String filename) {
        Path path = getPath(filename);
        if (path != null && Files.isRegularFile(path, new LinkOption[0])) {
            return () -> {
                return Files.newInputStream(path, new OpenOption[0]);
            };
        }
        if (ModResourcePackUtil.containsDefault(filename, this.modBundled)) {
            return () -> {
                return ModResourcePackUtil.openDefault(this.mod, this.type, filename);
            };
        }
        return null;
    }

    @Nullable
    public class_7367<InputStream> method_14410(String... pathSegments) {
        class_4239.method_46345(pathSegments);
        return openFile(String.join("/", pathSegments));
    }

    @Nullable
    public class_7367<InputStream> method_14405(class_3264 type, class_2960 id) {
        Path path = getPath(getFilename(type, id));
        if (path == null) {
            return null;
        }
        return class_7367.create(path);
    }

    public void method_14408(class_3264 type, final String namespace, String path, final class_3262.class_7664 visitor) {
        if (!this.namespaces.getOrDefault(type, Collections.emptySet()).contains(namespace)) {
            return;
        }
        for (Path basePath : this.basePaths) {
            final String separator = basePath.getFileSystem().getSeparator();
            final Path nsPath = basePath.resolve(type.method_14413()).resolve(namespace);
            Path searchPath = nsPath.resolve(path.replace("/", separator)).normalize();
            if (exists(searchPath)) {
                try {
                    Files.walkFileTree(searchPath, new SimpleFileVisitor<Path>() { // from class: net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack.1
                        @Override // java.nio.file.SimpleFileVisitor, java.nio.file.FileVisitor
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            String filename = nsPath.relativize(file).toString().replace(separator, "/");
                            class_2960 identifier = class_2960.method_43902(namespace, filename);
                            if (identifier == null) {
                                ModNioResourcePack.LOGGER.error("Invalid path in mod resource-pack {}: {}:{}, ignoring", new Object[]{ModNioResourcePack.this.id, namespace, filename});
                            } else {
                                visitor.accept(identifier, class_7367.create(file));
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    LOGGER.warn("findResources at " + path + " in namespace " + namespace + ", mod " + this.mod.getMetadata().getId() + " failed!", e);
                }
            }
        }
    }

    public Set<String> method_14406(class_3264 type) {
        return this.namespaces.getOrDefault(type, Collections.emptySet());
    }

    public <T> T method_14407(class_7677<T> class_7677Var) throws IOException {
        InputStream inputStream = (InputStream) ((class_7367) Objects.requireNonNull(openFile("pack.mcmeta"))).get();
        try {
            T t = (T) class_3255.method_14392(class_7677Var, inputStream);
            if (inputStream != null) {
                inputStream.close();
            }
            return t;
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    public class_9224 method_56926() {
        return this.metadata;
    }

    public void close() {
    }

    @Override // net.fabricmc.fabric.api.resource.ModResourcePack
    public ModMetadata getFabricModMetadata() {
        return this.mod.getMetadata();
    }

    public ResourcePackActivationType getActivationType() {
        return this.activationType;
    }

    public String method_14409() {
        return this.id;
    }

    private static boolean exists(Path path) {
        return path.getFileSystem() == DEFAULT_FS ? path.toFile().exists() : Files.exists(path, new LinkOption[0]);
    }

    private static String getFilename(class_3264 type, class_2960 id) {
        return String.format(Locale.ROOT, "%s/%s/%s", type.method_14413(), id.method_12836(), id.method_12832());
    }
}
