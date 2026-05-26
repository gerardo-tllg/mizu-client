package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.AmbientOcclusionEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_4970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/AbstractBlockMixin.class */
@Mixin({class_4970.class})
public abstract class AbstractBlockMixin {
    @Inject(method = {"getAmbientOcclusionLightLevel"}, at = {@At("HEAD")}, cancellable = true)
    private void onGetAmbientOcclusionLightLevel(class_2680 state, class_1922 world, class_2338 pos, CallbackInfoReturnable<Float> info) {
        AmbientOcclusionEvent event = (AmbientOcclusionEvent) MeteorClient.EVENT_BUS.post(AmbientOcclusionEvent.get());
        if (event.lightLevel != -1.0f) {
            info.setReturnValue(Float.valueOf(event.lightLevel));
        }
    }

    @Inject(method = {"getRenderingSeed"}, at = {@At("HEAD")}, cancellable = true)
    private void onRenderingSeed(class_2680 state, class_2338 pos, CallbackInfoReturnable<Long> cir) {
        if (((NoRender) Modules.get().get(NoRender.class)).noTextureRotations()) {
            cir.setReturnValue(0L);
        }
    }
}
