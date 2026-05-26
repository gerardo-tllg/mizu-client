package meteordevelopment.meteorclient.utils.render.postprocess;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.OptionalInt;
import meteordevelopment.meteorclient.MeteorClient;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/postprocess/EntityShader.class */
public abstract class EntityShader extends PostProcessShader {
    @Override // meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader
    public boolean beginRender() {
        if (super.beginRender()) {
            RenderSystem.getDevice().createCommandEncoder().createRenderPass(this.framebuffer.method_30277(), OptionalInt.of(0)).close();
            return true;
        }
        return false;
    }

    @Override // meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader
    protected void preDraw() {
        MeteorClient.mc.field_1769.meteor$pushEntityOutlineFramebuffer(this.framebuffer);
    }

    @Override // meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader
    protected void postDraw() {
        MeteorClient.mc.field_1769.meteor$popEntityOutlineFramebuffer();
    }

    public void endRender() {
        endRender(() -> {
            this.vertexConsumerProvider.method_23285();
        });
    }
}
