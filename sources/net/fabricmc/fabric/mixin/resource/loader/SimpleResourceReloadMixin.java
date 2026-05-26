package net.fabricmc.fabric.mixin.resource.loader;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.fabric.impl.resource.loader.FabricLifecycledResourceManager;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.minecraft.class_3264;
import net.minecraft.class_3300;
import net.minecraft.class_3302;
import net.minecraft.class_3902;
import net.minecraft.class_4011;
import net.minecraft.class_4014;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/SimpleResourceReloadMixin.class */
@Mixin({class_4014.class})
public class SimpleResourceReloadMixin {

    @Unique
    private static final ThreadLocal<class_3264> fabric_resourceType = new ThreadLocal<>();

    @Inject(method = {"method_40087(Lnet/minecraft/class_3300;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/class_4011;"}, at = {@At("HEAD")})
    private static void method_40087(class_3300 resourceManager, List<class_3302> list, Executor executor, Executor executor2, CompletableFuture<class_3902> completableFuture, boolean bl, CallbackInfoReturnable<class_4011> cir) {
        if (resourceManager instanceof FabricLifecycledResourceManager) {
            FabricLifecycledResourceManager flrm = (FabricLifecycledResourceManager) resourceManager;
            fabric_resourceType.set(flrm.fabric_getResourceType());
        }
    }

    @ModifyArg(method = {"method_40087(Lnet/minecraft/class_3300;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/class_4011;"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4014;method_18369(Lnet/minecraft/class_3300;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;)Lnet/minecraft/class_4011;"))
    private static List<class_3302> sortSimple(List<class_3302> reloaders) {
        List<class_3302> sorted = ResourceManagerHelperImpl.sort(fabric_resourceType.get(), reloaders);
        fabric_resourceType.remove();
        return sorted;
    }

    @ModifyArg(method = {"method_40087(Lnet/minecraft/class_3300;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/class_4011;"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4014;method_18369(Lnet/minecraft/class_3300;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;)Lnet/minecraft/class_4011;"))
    private static List<class_3302> sortProfiled(List<class_3302> reloaders) {
        List<class_3302> sorted = ResourceManagerHelperImpl.sort(fabric_resourceType.get(), reloaders);
        fabric_resourceType.remove();
        return sorted;
    }
}
