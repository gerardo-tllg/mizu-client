package net.fabricmc.fabric.mixin.resource.loader;

import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.minecraft.class_9220;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/SelectKnownPacksC2SPacketMixin.class */
@Mixin({class_9220.class})
public class SelectKnownPacksC2SPacketMixin {
    @ModifyArg(method = {"<clinit>()V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_9135;method_58000(I)Lnet/minecraft/class_9139$class_9140;"))
    private static int setMaxKnownPacks(int constant) {
        return ModResourcePackCreator.MAX_KNOWN_PACKS;
    }
}
