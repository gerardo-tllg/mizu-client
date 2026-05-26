package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_9975;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/SkyRenderingMixin.class */
@Mixin({class_9975.class})
public abstract class SkyRenderingMixin {
    @Shadow
    public abstract void method_62302(float f, float f2, float f3);

    @Inject(method = {"renderEndSky"}, at = {@At("HEAD")}, cancellable = true)
    private void onRenderEndSky(CallbackInfo ci) {
        Color color;
        Ambience ambience = (Ambience) Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.customSkyColor.get().booleanValue() && (color = ambience.skyColor()) != null) {
            method_62302(color.r / 255.0f, color.g / 255.0f, color.b / 255.0f);
            ci.cancel();
        }
    }
}
