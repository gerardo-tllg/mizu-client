package net.fabricmc.fabric.mixin.resource.loader;

import com.mojang.datafixers.DataFixer;
import java.net.Proxy;
import java.util.List;
import net.fabricmc.fabric.impl.resource.loader.FabricOriginalKnownPacksGetter;
import net.minecraft.class_32;
import net.minecraft.class_3283;
import net.minecraft.class_3950;
import net.minecraft.class_6904;
import net.minecraft.class_7497;
import net.minecraft.class_9226;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/MinecraftServerMixin.class */
@Mixin({MinecraftServer.class})
public class MinecraftServerMixin implements FabricOriginalKnownPacksGetter {

    @Unique
    private List<class_9226> fabric_originalKnownPacks;

    @Inject(method = {"<init>(Ljava/lang/Thread;Lnet/minecraft/class_32$class_5143;Lnet/minecraft/class_3283;Lnet/minecraft/class_6904;Ljava/net/Proxy;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/class_7497;Lnet/minecraft/class_3950;)V"}, at = {@At("TAIL")})
    private void init(Thread serverThread, class_32.class_5143 session, class_3283 dataPackManager, class_6904 saveLoader, Proxy proxy, DataFixer dataFixer, class_7497 apiServices, class_3950 worldGenerationProgressListenerFactory, CallbackInfo ci) {
        this.fabric_originalKnownPacks = saveLoader.comp_356().method_29213().flatMap(pack -> {
            return pack.method_56926().comp_2332().stream();
        }).toList();
    }

    /* JADX WARN: Removed duplicated region for block: B:14:0x004c  */
    @org.spongepowered.asm.mixin.injection.Redirect(method = {"method_29736(Lnet/minecraft/class_3283;Lnet/minecraft/class_7712;ZZ)Lnet/minecraft/class_7712;"}, at = @org.spongepowered.asm.mixin.injection.At(value = "INVOKE", target = "Ljava/util/List;contains(Ljava/lang/Object;)Z"))
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private static boolean onCheckDisabled(java.util.List<java.lang.String> r3, java.lang.Object r4, net.minecraft.class_3283 r5) {
        /*
            r0 = r4
            java.lang.String r0 = (java.lang.String) r0
            r6 = r0
            r0 = r3
            r1 = r6
            boolean r0 = r0.contains(r1)
            r7 = r0
            r0 = r7
            if (r0 == 0) goto L15
            r0 = 1
            return r0
        L15:
            r0 = r5
            r1 = r6
            net.minecraft.class_3288 r0 = r0.method_14449(r1)
            r8 = r0
            r0 = r8
            net.minecraft.class_5352 r0 = r0.method_29483()
            boolean r0 = r0 instanceof net.fabricmc.fabric.impl.resource.loader.BuiltinModResourcePackSource
            if (r0 == 0) goto L7b
            r0 = r8
            net.minecraft.class_3262 r0 = r0.method_14458()
            r9 = r0
            r0 = r9
            boolean r0 = r0 instanceof net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack     // Catch: java.lang.Throwable -> L5e
            if (r0 == 0) goto L4c
            r0 = r9
            net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack r0 = (net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack) r0     // Catch: java.lang.Throwable -> L5e
            r10 = r0
            r0 = r10
            net.fabricmc.fabric.api.resource.ResourcePackActivationType r0 = r0.getActivationType()     // Catch: java.lang.Throwable -> L5e
            boolean r0 = r0.isEnabledByDefault()     // Catch: java.lang.Throwable -> L5e
            if (r0 != 0) goto L4c
            r0 = 1
            goto L4d
        L4c:
            r0 = 0
        L4d:
            r10 = r0
            r0 = r9
            if (r0 == 0) goto L5b
            r0 = r9
            r0.close()
        L5b:
            r0 = r10
            return r0
        L5e:
            r10 = move-exception
            r0 = r9
            if (r0 == 0) goto L78
            r0 = r9
            r0.close()     // Catch: java.lang.Throwable -> L6f
            goto L78
        L6f:
            r11 = move-exception
            r0 = r10
            r1 = r11
            r0.addSuppressed(r1)
        L78:
            r0 = r10
            throw r0
        L7b:
            r0 = 0
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: net.fabricmc.fabric.mixin.resource.loader.MinecraftServerMixin.onCheckDisabled(java.util.List, java.lang.Object, net.minecraft.class_3283):boolean");
    }

    @Override // net.fabricmc.fabric.impl.resource.loader.FabricOriginalKnownPacksGetter
    public List<class_9226> fabric_getOriginalKnownPacks() {
        return this.fabric_originalKnownPacks;
    }
}
