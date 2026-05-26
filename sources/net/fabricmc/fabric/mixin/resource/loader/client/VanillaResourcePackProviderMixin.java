package net.fabricmc.fabric.mixin.resource.loader.client;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.minecraft.class_1065;
import net.minecraft.class_3288;
import net.minecraft.class_7678;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/client/VanillaResourcePackProviderMixin.class */
@Mixin({class_7678.class})
@Environment(EnvType.CLIENT)
public class VanillaResourcePackProviderMixin {
    @Inject(method = {"method_14453(Ljava/util/function/Consumer;)V"}, at = {@At("RETURN")})
    private void addBuiltinResourcePacks(Consumer<class_3288> consumer, CallbackInfo ci) {
        if (this instanceof class_1065) {
            ModResourcePackCreator.CLIENT_RESOURCE_PACK_PROVIDER.method_14453(consumer);
        }
    }
}
