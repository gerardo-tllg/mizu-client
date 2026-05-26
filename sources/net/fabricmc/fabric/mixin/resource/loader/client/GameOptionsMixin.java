package net.fabricmc.fabric.mixin.resource.loader.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2505;
import net.minecraft.class_2507;
import net.minecraft.class_2519;
import net.minecraft.class_315;
import net.minecraft.class_3262;
import net.minecraft.class_3288;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/client/GameOptionsMixin.class */
@Mixin({class_315.class})
@Environment(EnvType.CLIENT)
public class GameOptionsMixin {

    @Shadow
    public List<String> field_1887;

    @Shadow
    @Final
    static Logger field_1834;

    @Inject(method = {"method_1636()V"}, at = {@At("RETURN")})
    private void onLoad(CallbackInfo ci) {
        Path dataDir = FabricLoader.getInstance().getGameDir().resolve("data");
        if (Files.notExists(dataDir, new LinkOption[0])) {
            try {
                Files.createDirectories(dataDir, new FileAttribute[0]);
            } catch (IOException e) {
                field_1834.warn("[Fabric Resource Loader] Could not create data directory: " + String.valueOf(dataDir.toAbsolutePath()));
            }
        }
        Path trackerFile = dataDir.resolve("fabricDefaultResourcePacks.dat");
        Set<String> trackedPacks = new HashSet<>();
        if (Files.exists(trackerFile, new LinkOption[0])) {
            try {
                class_2487 data = class_2507.method_30613(trackerFile, class_2505.method_53898());
                class_2499 values = (class_2499) data.method_10554("values").orElseThrow();
                for (int i = 0; i < values.size(); i++) {
                    trackedPacks.add((String) values.method_10608(i).orElseThrow());
                }
            } catch (IOException e2) {
                field_1834.warn("[Fabric Resource Loader] Could not read " + String.valueOf(trackerFile.toAbsolutePath()), e2);
            }
        }
        Set<String> removedPacks = new HashSet<>(trackedPacks);
        Set<String> resourcePacks = new LinkedHashSet<>(this.field_1887);
        List<class_3288> profiles = new ArrayList<>();
        ModResourcePackCreator modResourcePackCreator = ModResourcePackCreator.CLIENT_RESOURCE_PACK_PROVIDER;
        Objects.requireNonNull(profiles);
        modResourcePackCreator.method_14453((v1) -> {
            r1.add(v1);
        });
        for (class_3288 profile : profiles) {
            if (profile.method_14463().equals(ModResourcePackCreator.FABRIC)) {
                resourcePacks.add(profile.method_14463());
            } else {
                class_3262 pack = profile.method_14458();
                try {
                    if (pack instanceof ModNioResourcePack) {
                        ModNioResourcePack builtinPack = (ModNioResourcePack) pack;
                        if (builtinPack.getActivationType().isEnabledByDefault()) {
                            if (trackedPacks.add(builtinPack.method_14409())) {
                                resourcePacks.add(profile.method_14463());
                            } else {
                                removedPacks.remove(builtinPack.method_14409());
                            }
                        }
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
        try {
            class_2499 values2 = new class_2499();
            for (String id : trackedPacks) {
                if (!removedPacks.contains(id)) {
                    values2.add(class_2519.method_23256(id));
                }
            }
            class_2487 nbt = new class_2487();
            nbt.method_10566("values", values2);
            class_2507.method_30614(nbt, trackerFile);
        } catch (IOException e3) {
            field_1834.warn("[Fabric Resource Loader] Could not write to " + String.valueOf(trackerFile.toAbsolutePath()), e3);
        }
        this.field_1887 = new ArrayList(resourcePacks);
    }
}
