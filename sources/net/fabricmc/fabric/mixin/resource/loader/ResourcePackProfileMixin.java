package net.fabricmc.fabric.mixin.resource.loader;

import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.fabric.impl.resource.loader.FabricResourcePackProfile;
import net.fabricmc.fabric.impl.resource.loader.ResourcePackSourceTracker;
import net.minecraft.class_3262;
import net.minecraft.class_3288;
import net.minecraft.class_9224;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/ResourcePackProfileMixin.class */
@Mixin({class_3288.class})
abstract class ResourcePackProfileMixin implements FabricResourcePackProfile {

    @Unique
    private static final Predicate<Set<String>> DEFAULT_PARENT_PREDICATE = parents -> {
        return true;
    };

    @Unique
    private Predicate<Set<String>> parentsPredicate = DEFAULT_PARENT_PREDICATE;

    @Shadow
    public abstract class_9224 method_56933();

    ResourcePackProfileMixin() {
    }

    @Inject(method = {"method_14458()Lnet/minecraft/class_3262;"}, at = {@At("RETURN")})
    private void onCreateResourcePack(CallbackInfoReturnable<class_3262> info) {
        ResourcePackSourceTracker.setSource((class_3262) info.getReturnValue(), method_56933().comp_2331());
    }

    @Override // net.fabricmc.fabric.impl.resource.loader.FabricResourcePackProfile
    public boolean fabric_isHidden() {
        return this.parentsPredicate != DEFAULT_PARENT_PREDICATE;
    }

    @Override // net.fabricmc.fabric.impl.resource.loader.FabricResourcePackProfile
    public boolean fabric_parentsEnabled(Set<String> enabled) {
        return this.parentsPredicate.test(enabled);
    }

    @Override // net.fabricmc.fabric.impl.resource.loader.FabricResourcePackProfile
    public void fabric_setParentsPredicate(Predicate<Set<String>> predicate) {
        this.parentsPredicate = predicate;
    }
}
