package meteordevelopment.meteorclient.mixin;

import java.util.UUID;
import net.minecraft.class_1068;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/DefaultSkinHelperMixin.class */
@Mixin({class_1068.class})
public abstract class DefaultSkinHelperMixin {
    @Inject(method = {"getSkinTextures(Ljava/util/UUID;)Lnet/minecraft/client/util/SkinTextures;"}, at = {@At("HEAD")}, cancellable = true)
    private static void onShouldUseSlimModel(UUID uuid, CallbackInfoReturnable<Boolean> info) {
        if (uuid == null) {
            info.setReturnValue(false);
        }
    }
}
