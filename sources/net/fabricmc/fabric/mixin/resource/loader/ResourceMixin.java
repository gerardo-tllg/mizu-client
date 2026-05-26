package net.fabricmc.fabric.mixin.resource.loader;

import net.fabricmc.fabric.impl.resource.loader.FabricResource;
import net.fabricmc.fabric.impl.resource.loader.ResourcePackSourceTracker;
import net.minecraft.class_3298;
import net.minecraft.class_5352;
import org.spongepowered.asm.mixin.Mixin;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/ResourceMixin.class */
@Mixin({class_3298.class})
class ResourceMixin implements FabricResource {
    ResourceMixin() {
    }

    @Override // net.fabricmc.fabric.impl.resource.loader.FabricResource
    public class_5352 getFabricPackSource() {
        class_3298 self = (class_3298) this;
        return ResourcePackSourceTracker.getSource(self.method_45304());
    }
}
