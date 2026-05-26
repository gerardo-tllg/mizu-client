package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.class_677;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/FireworksSparkParticleMixin.class */
@Mixin({class_677.class_681.class})
public abstract class FireworksSparkParticleMixin {
    @Inject(method = {"addExplosionParticle"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/particle/FireworksSparkParticle$Explosion;setTrail(Z)V")}, cancellable = true)
    private void onAddExplosion(double x, double y, double z, double velocityX, double velocityY, double velocityZ, IntList colors, IntList targetColors, boolean trail, boolean flicker, CallbackInfo info, @Local class_677.class_680 explosion) {
        if (explosion == null) {
            info.cancel();
        }
    }
}
