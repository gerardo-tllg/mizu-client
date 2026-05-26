package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import meteordevelopment.meteorclient.mixininterface.IRenderPipeline;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/ExtendedRenderPipelineBuilder.class */
public class ExtendedRenderPipelineBuilder extends RenderPipeline.Builder {
    private boolean lineSmooth;

    public ExtendedRenderPipelineBuilder(RenderPipeline.Snippet... snippets) {
        for (RenderPipeline.Snippet snippet : snippets) {
            withSnippet(snippet);
        }
    }

    public ExtendedRenderPipelineBuilder withLineSmooth() {
        this.lineSmooth = true;
        return this;
    }

    public RenderPipeline build() {
        IRenderPipeline iRenderPipelineBuild = super.build();
        iRenderPipelineBuild.meteor$setLineSmooth(this.lineSmooth);
        return iRenderPipelineBuild;
    }
}
