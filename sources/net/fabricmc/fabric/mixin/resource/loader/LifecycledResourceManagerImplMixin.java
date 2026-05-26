package net.fabricmc.fabric.mixin.resource.loader;

import java.util.List;
import net.fabricmc.fabric.impl.resource.loader.FabricLifecycledResourceManager;
import net.minecraft.class_3262;
import net.minecraft.class_3264;
import net.minecraft.class_6861;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/LifecycledResourceManagerImplMixin.class */
@Mixin({class_6861.class})
public class LifecycledResourceManagerImplMixin implements FabricLifecycledResourceManager {

    @Unique
    private class_3264 fabric_ResourceType;

    @Inject(method = {"<init>(Lnet/minecraft/class_3264;Ljava/util/List;)V"}, at = {@At("TAIL")})
    private void init(class_3264 resourceType, List<class_3262> list, CallbackInfo ci) {
        this.fabric_ResourceType = resourceType;
    }

    @Override // net.fabricmc.fabric.impl.resource.loader.FabricLifecycledResourceManager
    public class_3264 fabric_getResourceType() {
        return this.fabric_ResourceType;
    }
}
