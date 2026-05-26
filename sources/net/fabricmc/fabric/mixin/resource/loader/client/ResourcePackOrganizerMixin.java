package net.fabricmc.fabric.mixin.resource.loader.client;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.resource.loader.FabricResourcePackProfile;
import net.minecraft.class_3283;
import net.minecraft.class_3288;
import net.minecraft.class_5369;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/client/ResourcePackOrganizerMixin.class */
@Mixin({class_5369.class})
@Environment(EnvType.CLIENT)
public class ResourcePackOrganizerMixin {

    @Shadow
    @Final
    List<class_3288> field_25455;

    @Shadow
    @Final
    List<class_3288> field_25456;

    @Inject(method = {"<init>(Ljava/lang/Runnable;Ljava/util/function/Function;Lnet/minecraft/class_3283;Ljava/util/function/Consumer;)V"}, at = {@At("TAIL")})
    private void removeHiddenPacksInit(Runnable updateCallback, Function iconIdSupplier, class_3283 resourcePackManager, Consumer applier, CallbackInfo ci) {
        this.field_25455.removeIf(profile -> {
            return ((FabricResourcePackProfile) profile).fabric_isHidden();
        });
        this.field_25456.removeIf(profile2 -> {
            return ((FabricResourcePackProfile) profile2).fabric_isHidden();
        });
    }

    @Inject(method = {"method_29981()V"}, at = {@At("TAIL")})
    private void removeHiddenPacksRefresh(CallbackInfo ci) {
        this.field_25455.removeIf(profile -> {
            return ((FabricResourcePackProfile) profile).fabric_isHidden();
        });
        this.field_25456.removeIf(profile2 -> {
            return ((FabricResourcePackProfile) profile2).fabric_isHidden();
        });
    }
}
