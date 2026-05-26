package net.fabricmc.fabric.mixin.resource.loader.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.minecraft.class_3283;
import net.minecraft.class_9226;
import net.minecraft.class_9247;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/client/ClientDataPackManagerMixin.class */
@Mixin({class_9247.class})
@Environment(EnvType.CLIENT)
public class ClientDataPackManagerMixin {

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("ClientDataPackManagerMixin");

    @Redirect(method = {"<init>()V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3286;method_52443()Lnet/minecraft/class_3283;"))
    public class_3283 createClientManager() {
        return ModResourcePackUtil.createClientManager();
    }

    @ModifyReturnValue(method = {"method_57049(Ljava/util/List;)Ljava/util/List;"}, at = {@At("RETURN")})
    List<class_9226> getCommonKnownPacksReturn(List<class_9226> original) {
        if (original.size() > ModResourcePackCreator.MAX_KNOWN_PACKS) {
            LOGGER.warn("Too many knownPacks: Found {}; max {}", Integer.valueOf(original.size()), Integer.valueOf(ModResourcePackCreator.MAX_KNOWN_PACKS));
            return original.subList(0, ModResourcePackCreator.MAX_KNOWN_PACKS);
        }
        return original;
    }
}
