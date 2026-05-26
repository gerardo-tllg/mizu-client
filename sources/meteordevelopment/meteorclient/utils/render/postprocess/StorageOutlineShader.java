package meteordevelopment.meteorclient.utils.render.postprocess;

import com.mojang.blaze3d.systems.RenderPass;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.StorageESP;
import net.minecraft.class_1297;
import net.minecraft.class_276;
import net.minecraft.class_310;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/postprocess/StorageOutlineShader.class */
public class StorageOutlineShader extends PostProcessShader {
    private static StorageESP storageESP;
    private class_276 mcFramebuffer;

    public StorageOutlineShader() {
        init(MeteorRenderPipelines.POST_OUTLINE);
    }

    @Override // meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader
    protected void preDraw() {
        this.mcFramebuffer = class_310.method_1551().method_1522();
        class_310.method_1551().meteor$setFramebuffer(this.framebuffer);
    }

    @Override // meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader
    protected void postDraw() {
        class_310.method_1551().meteor$setFramebuffer(this.mcFramebuffer);
        this.mcFramebuffer = null;
    }

    @Override // meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader
    protected boolean shouldDraw() {
        if (storageESP == null) {
            storageESP = (StorageESP) Modules.get().get(StorageESP.class);
        }
        return storageESP.isShader();
    }

    @Override // meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader
    public boolean shouldDraw(class_1297 entity) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader
    protected void setupPass(RenderPass pass) {
        pass.setUniform("u_Width", new int[]{storageESP.outlineWidth.get().intValue()});
        pass.setUniform("u_FillOpacity", new float[]{storageESP.fillOpacity.get().intValue() / 255.0f});
        pass.setUniform("u_ShapeMode", new int[]{storageESP.shapeMode.get().ordinal()});
        pass.setUniform("u_GlowMultiplier", new float[]{storageESP.glowMultiplier.get().floatValue()});
    }
}
