package net.fabricmc.fabric.mixin.resource.loader;

import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.minecraft.class_3806;
import net.minecraft.class_7712;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/ServerPropertiesHandlerMixin.class */
@Mixin({class_3806.class})
public class ServerPropertiesHandlerMixin {
    @Redirect(method = {"<init>(Ljava/util/Properties;)V"}, at = @At(value = "FIELD", target = "Lnet/minecraft/class_7712;field_40260:Lnet/minecraft/class_7712;"))
    private class_7712 replaceDefaultDataConfiguration() {
        return ModResourcePackUtil.createDefaultDataConfiguration();
    }
}
