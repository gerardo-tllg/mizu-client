package meteordevelopment.meteorclient.mixin.indigo;

import meteordevelopment.meteorclient.systems.modules.render.Xray;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractTerrainRenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/indigo/AbstractTerrainRenderContextMixin.class */
@Mixin({AbstractTerrainRenderContext.class})
public abstract class AbstractTerrainRenderContextMixin {

    @Shadow(remap = false)
    @Final
    protected BlockRenderInfo blockInfo;

    @Inject(method = {"bufferQuad"}, at = {@At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/client/indigo/renderer/render/AbstractTerrainRenderContext;bufferQuad(Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;Lnet/minecraft/client/render/VertexConsumer;)V")}, cancellable = true)
    private void onBufferQuad(MutableQuadViewImpl quad, CallbackInfo ci) {
        int alpha = Xray.getAlpha(this.blockInfo.blockState, this.blockInfo.blockPos);
        if (alpha != 0) {
            if (alpha != -1) {
                for (int i = 0; i < 4; i++) {
                    quad.color(i, rewriteQuadAlpha(quad.color(i), alpha));
                }
                return;
            }
            return;
        }
        ci.cancel();
    }

    @Unique
    private int rewriteQuadAlpha(int color, int alpha) {
        return ((alpha & 255) << 24) | (color & 16777215);
    }
}
