package net.fabricmc.fabric.mixin.resource.loader;

import java.util.Objects;
import net.fabricmc.fabric.impl.resource.loader.FabricRecipeManager;
import net.minecraft.class_1863;
import net.minecraft.class_7225;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/ServerRecipeManagerMixin.class */
@Mixin({class_1863.class})
public class ServerRecipeManagerMixin implements FabricRecipeManager {

    @Unique
    private class_7225.class_7874 registries;

    @Inject(method = {"<init>(Lnet/minecraft/class_7225$class_7874;)V"}, at = {@At("TAIL")})
    private void init(class_7225.class_7874 registries, CallbackInfo ci) {
        this.registries = registries;
    }

    @Override // net.fabricmc.fabric.impl.resource.loader.FabricRecipeManager
    public class_7225.class_7874 fabric_getRegistries() {
        return (class_7225.class_7874) Objects.requireNonNull(this.registries);
    }
}
