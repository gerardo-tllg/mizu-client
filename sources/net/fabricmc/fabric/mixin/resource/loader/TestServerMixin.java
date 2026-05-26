package net.fabricmc.fabric.mixin.resource.loader;

import java.util.List;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.minecraft.class_5359;
import net.minecraft.class_6306;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/mixin/resource/loader/TestServerMixin.class */
@Mixin({class_6306.class})
public class TestServerMixin {
    @Redirect(method = {"method_40378(Ljava/lang/Thread;Lnet/minecraft/class_32$class_5143;Lnet/minecraft/class_3283;Ljava/util/Optional;Z)Lnet/minecraft/class_6306;"}, at = @At(value = "NEW", target = "(Ljava/util/List;Ljava/util/List;)Lnet/minecraft/class_5359;"))
    private static class_5359 replaceDefaultDataPackSettings(List<String> enabled, List<String> disabled) {
        return ModResourcePackUtil.createTestServerSettings(enabled, disabled);
    }
}
