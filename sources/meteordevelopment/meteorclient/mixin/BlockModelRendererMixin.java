package meteordevelopment.meteorclient.mixin;

import java.util.List;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import net.minecraft.class_10889;
import net.minecraft.class_1920;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_778;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/BlockModelRendererMixin.class */
@Mixin({class_778.class})
public abstract class BlockModelRendererMixin {

    @Unique
    private final ThreadLocal<Integer> alphas = new ThreadLocal<>();

    @Inject(method = {"renderSmooth", "renderFlat"}, at = {@At("HEAD")}, cancellable = true)
    private void onRenderSmooth(class_1920 world, List<class_10889> parts, class_2680 state, class_2338 pos, class_4587 matrices, class_4588 vertexConsumer, boolean cull, int overlay, CallbackInfo ci) {
        int alpha = Xray.getAlpha(state, pos);
        if (alpha != 0) {
            this.alphas.set(Integer.valueOf(alpha));
        } else {
            ci.cancel();
        }
    }

    @ModifyArgs(method = {"renderQuad"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;quad(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;[FFFFF[IIZ)V"))
    private void modifyXrayAlpha(Args args) {
        int alpha = this.alphas.get().intValue();
        args.set(6, alpha == -1 ? (Float) args.get(6) : Float.valueOf(alpha / 255.0f));
    }
}
