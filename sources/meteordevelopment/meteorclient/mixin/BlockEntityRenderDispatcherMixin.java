package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.RenderBlockEntityEvent;
import net.minecraft.class_2586;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_824;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/BlockEntityRenderDispatcherMixin.class */
@Mixin({class_824.class})
public abstract class BlockEntityRenderDispatcherMixin {
    @Inject(method = {"render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"}, at = {@At("HEAD")}, cancellable = true)
    private <E extends class_2586> void onRenderEntity(E blockEntity, float tickDelta, class_4587 matrix, class_4597 vertexConsumerProvider, CallbackInfo info) {
        RenderBlockEntityEvent event = (RenderBlockEntityEvent) MeteorClient.EVENT_BUS.post(RenderBlockEntityEvent.get(blockEntity));
        if (event.isCancelled()) {
            info.cancel();
        }
    }
}
