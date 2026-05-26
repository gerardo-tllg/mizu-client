package meteordevelopment.meteorclient.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import meteordevelopment.meteorclient.mixininterface.IRenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/RenderPipelineMixin.class */
@Mixin({RenderPipeline.class})
public abstract class RenderPipelineMixin implements IRenderPipeline {

    @Unique
    private boolean lineSmooth;

    @Override // meteordevelopment.meteorclient.mixininterface.IRenderPipeline
    public void meteor$setLineSmooth(boolean lineSmooth) {
        this.lineSmooth = lineSmooth;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IRenderPipeline
    public boolean meteor$getLineSmooth() {
        return this.lineSmooth;
    }
}
