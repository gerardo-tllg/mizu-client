package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.class_10042;
import net.minecraft.class_10055;
import net.minecraft.class_3882;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_583;
import net.minecraft.class_976;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/HeadFeatureRendererMixin.class */
@Mixin({class_976.class})
public abstract class HeadFeatureRendererMixin<S extends class_10042, M extends class_583<S> & class_3882> {
    @Inject(method = {"render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/LivingEntityRenderState;FF)V"}, at = {@At("HEAD")}, cancellable = true)
    private void onRender(class_4587 matrixStack, class_4597 vertexConsumerProvider, int i, S livingEntityRenderState, float f, float g, CallbackInfo ci) {
        if (!(livingEntityRenderState instanceof class_10055) || !((NoRender) Modules.get().get(NoRender.class)).noArmor()) {
            return;
        }
        ci.cancel();
    }
}
