package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import net.minecraft.class_1921;
import net.minecraft.class_2680;
import net.minecraft.class_3610;
import net.minecraft.class_4696;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/RenderLayersMixin.class */
@Mixin({class_4696.class})
public abstract class RenderLayersMixin {
    @Inject(method = {"getFluidLayer"}, at = {@At("HEAD")}, cancellable = true)
    private static void onGetFluidLayer(class_3610 state, CallbackInfoReturnable<class_1921> cir) {
        if (Modules.get() == null) {
            return;
        }
        Ambience ambience = (Ambience) Modules.get().get(Ambience.class);
        int a = ambience.lavaColor.get().a;
        if (ambience.isActive() && ambience.customLavaColor.get().booleanValue() && a > 0 && a < 255) {
            cir.setReturnValue(class_1921.method_23583());
        }
    }
}
