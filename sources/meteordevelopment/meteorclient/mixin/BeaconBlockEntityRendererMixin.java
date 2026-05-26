package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.class_10633;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_822;
import net.minecraft.class_827;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/BeaconBlockEntityRendererMixin.class */
@Mixin({class_822.class})
public abstract class BeaconBlockEntityRendererMixin<T extends class_2586 & class_10633> implements class_827<T> {
    @Inject(method = {"render"}, at = {@At("HEAD")}, cancellable = true)
    private void onRender(T entity, float tickProgress, class_4587 matrices, class_4597 vertexConsumers, int light, int overlay, class_243 cameraPos, CallbackInfo ci) {
        if (((NoRender) Modules.get().get(NoRender.class)).noBeaconBeams()) {
            ci.cancel();
        }
    }
}
