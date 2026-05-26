package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.RenderItemEntityEvent;
import net.minecraft.class_10039;
import net.minecraft.class_10442;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_916;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ItemEntityRendererMixin.class */
@Mixin({class_916.class})
public abstract class ItemEntityRendererMixin {

    @Shadow
    @Final
    private class_10442 field_55293;

    @Inject(method = {"render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at = {@At("HEAD")}, cancellable = true)
    private void renderStack(class_10039 itemEntityRenderState, class_4587 matrixStack, class_4597 vertexConsumerProvider, int i, CallbackInfo ci) {
        RenderItemEntityEvent event = (RenderItemEntityEvent) MeteorClient.EVENT_BUS.post(RenderItemEntityEvent.get(itemEntityRenderState, MeteorClient.mc.method_61966().method_60637(true), matrixStack, vertexConsumerProvider, i, this.field_55293));
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
