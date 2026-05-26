package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.class_1058;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/InGameOverlayRendererMixin.class */
@Mixin({class_4603.class})
public abstract class InGameOverlayRendererMixin {
    @Inject(method = {"renderFireOverlay"}, at = {@At("HEAD")}, cancellable = true)
    private static void onRenderFireOverlay(class_4587 matrices, class_4597 vertexConsumers, CallbackInfo ci) {
        if (((NoRender) Modules.get().get(NoRender.class)).noFireOverlay()) {
            ci.cancel();
        }
    }

    @Inject(method = {"renderUnderwaterOverlay"}, at = {@At("HEAD")}, cancellable = true)
    private static void onRenderUnderwaterOverlay(class_310 client, class_4587 matrices, class_4597 vertexConsumers, CallbackInfo ci) {
        if (((NoRender) Modules.get().get(NoRender.class)).noLiquidOverlay()) {
            ci.cancel();
        }
    }

    @Inject(method = {"renderInWallOverlay"}, at = {@At("HEAD")}, cancellable = true)
    private static void render(class_1058 sprite, class_4587 matrices, class_4597 vertexConsumers, CallbackInfo ci) {
        if (((NoRender) Modules.get().get(NoRender.class)).noInWallOverlay()) {
            ci.cancel();
        }
    }
}
