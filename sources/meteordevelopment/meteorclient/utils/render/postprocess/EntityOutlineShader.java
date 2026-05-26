package meteordevelopment.meteorclient.utils.render.postprocess;

import com.mojang.blaze3d.systems.RenderPass;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import net.minecraft.class_1297;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/postprocess/EntityOutlineShader.class */
public class EntityOutlineShader extends EntityShader {
    private static ESP esp;

    public EntityOutlineShader() {
        init(MeteorRenderPipelines.POST_OUTLINE);
    }

    @Override // meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader
    protected boolean shouldDraw() {
        if (esp == null) {
            esp = (ESP) Modules.get().get(ESP.class);
        }
        return esp.isShader();
    }

    @Override // meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader
    public boolean shouldDraw(class_1297 entity) {
        return shouldDraw() && !esp.shouldSkip(entity);
    }

    @Override // meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader
    protected void setupPass(RenderPass pass) {
        pass.setUniform("u_Width", new int[]{esp.outlineWidth.get().intValue()});
        pass.setUniform("u_FillOpacity", new float[]{esp.fillOpacity.get().floatValue()});
        pass.setUniform("u_ShapeMode", new int[]{esp.shapeMode.get().ordinal()});
        pass.setUniform("u_GlowMultiplier", new float[]{esp.glowMultiplier.get().floatValue()});
    }
}
