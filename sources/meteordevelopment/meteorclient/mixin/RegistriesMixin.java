package meteordevelopment.meteorclient.mixin;

import java.util.function.Supplier;
import net.minecraft.class_7923;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/RegistriesMixin.class */
@Mixin({class_7923.class})
public abstract class RegistriesMixin {
    @Redirect(method = {"create(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/registry/MutableRegistry;Lnet/minecraft/registry/Registries$Initializer;)Lnet/minecraft/registry/MutableRegistry;"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/Bootstrap;ensureBootstrapped(Ljava/util/function/Supplier;)V"))
    private static void ignoreBootstrap(Supplier<String> callerGetter) {
    }
}
