package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.class_10789;
import net.minecraft.class_290;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_4013;
import org.apache.commons.io.IOUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/MeteorRenderPipelines.class */
public abstract class MeteorRenderPipelines {
    private static final List<RenderPipeline> PIPELINES = new ArrayList();
    private static final RenderPipeline.Snippet UNIFORMS = RenderPipeline.builder(new RenderPipeline.Snippet[0]).withUniform("u_Proj", class_10789.field_56747).withUniform("u_ModelView", class_10789.field_56747).buildSnippet();
    public static final RenderPipeline WORLD_COLORED = add(new ExtendedRenderPipelineBuilder(UNIFORMS).withLocation(MeteorClient.identifier("pipeline/world_colored")).withVertexFormat(class_290.field_1576, VertexFormat.class_5596.field_27379).withVertexShader(MeteorClient.identifier("shaders/pos_color.vert")).withFragmentShader(MeteorClient.identifier("shaders/pos_color.frag")).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
    public static final RenderPipeline WORLD_COLORED_LINES = add(new ExtendedRenderPipelineBuilder(UNIFORMS).withLineSmooth().withLocation(MeteorClient.identifier("pipeline/world_colored_lines")).withVertexFormat(class_290.field_1576, VertexFormat.class_5596.field_29344).withVertexShader(MeteorClient.identifier("shaders/pos_color.vert")).withFragmentShader(MeteorClient.identifier("shaders/pos_color.frag")).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
    public static final RenderPipeline WORLD_COLORED_DEPTH = add(new ExtendedRenderPipelineBuilder(UNIFORMS).withLocation(MeteorClient.identifier("pipeline/world_colored_depth")).withVertexFormat(class_290.field_1576, VertexFormat.class_5596.field_27379).withVertexShader(MeteorClient.identifier("shaders/pos_color.vert")).withFragmentShader(MeteorClient.identifier("shaders/pos_color.frag")).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
    public static final RenderPipeline WORLD_COLORED_LINES_DEPTH = add(new ExtendedRenderPipelineBuilder(UNIFORMS).withLineSmooth().withLocation(MeteorClient.identifier("pipeline/world_colored_lines_depth")).withVertexFormat(class_290.field_1576, VertexFormat.class_5596.field_29344).withVertexShader(MeteorClient.identifier("shaders/pos_color.vert")).withFragmentShader(MeteorClient.identifier("shaders/pos_color.frag")).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
    public static final RenderPipeline UI_COLORED = add(new ExtendedRenderPipelineBuilder(UNIFORMS).withLocation(MeteorClient.identifier("pipeline/ui_colored")).withVertexFormat(MeteorVertexFormats.POS2_COLOR, VertexFormat.class_5596.field_27379).withVertexShader(MeteorClient.identifier("shaders/pos_color.vert")).withFragmentShader(MeteorClient.identifier("shaders/pos_color.frag")).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(true).build());
    public static final RenderPipeline UI_COLORED_LINES = add(new ExtendedRenderPipelineBuilder(UNIFORMS).withLocation(MeteorClient.identifier("pipeline/ui_colored_lines")).withVertexFormat(MeteorVertexFormats.POS2_COLOR, VertexFormat.class_5596.field_29344).withVertexShader(MeteorClient.identifier("shaders/pos_color.vert")).withFragmentShader(MeteorClient.identifier("shaders/pos_color.frag")).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(true).build());
    public static final RenderPipeline UI_TEXTURED = add(new ExtendedRenderPipelineBuilder(UNIFORMS).withLocation(MeteorClient.identifier("pipeline/ui_textured")).withVertexFormat(MeteorVertexFormats.POS2_TEXTURE_COLOR, VertexFormat.class_5596.field_27379).withVertexShader(MeteorClient.identifier("shaders/pos_tex_color.vert")).withFragmentShader(MeteorClient.identifier("shaders/pos_tex_color.frag")).withSampler("u_Texture").withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(true).build());
    public static final RenderPipeline UI_TEXT = add(new ExtendedRenderPipelineBuilder(UNIFORMS).withLocation(MeteorClient.identifier("pipeline/ui_text")).withVertexFormat(MeteorVertexFormats.POS2_TEXTURE_COLOR, VertexFormat.class_5596.field_27379).withVertexShader(MeteorClient.identifier("shaders/text.vert")).withFragmentShader(MeteorClient.identifier("shaders/text.frag")).withSampler("u_Texture").withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(true).build());
    public static final RenderPipeline POST_OUTLINE = add(new ExtendedRenderPipelineBuilder(new RenderPipeline.Snippet[0]).withLocation(MeteorClient.identifier("pipeline/post/outline")).withVertexFormat(MeteorVertexFormats.POS2, VertexFormat.class_5596.field_27379).withVertexShader(MeteorClient.identifier("shaders/post-process/base.vert")).withFragmentShader(MeteorClient.identifier("shaders/post-process/outline.frag")).withUniform("u_Size", class_10789.field_56744).withSampler("u_Texture").withUniform("u_Width", class_10789.field_56741).withUniform("u_FillOpacity", class_10789.field_56743).withUniform("u_ShapeMode", class_10789.field_56741).withUniform("u_GlowMultiplier", class_10789.field_56743).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
    public static final RenderPipeline POST_IMAGE = add(new ExtendedRenderPipelineBuilder(UNIFORMS).withLocation(MeteorClient.identifier("pipeline/post/image")).withVertexFormat(MeteorVertexFormats.POS2, VertexFormat.class_5596.field_27379).withVertexShader(MeteorClient.identifier("shaders/post-process/base.vert")).withFragmentShader(MeteorClient.identifier("shaders/post-process/image.frag")).withUniform("u_Size", class_10789.field_56744).withSampler("u_Texture").withSampler("u_TextureI").withUniform("u_Color", class_10789.field_56746).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
    public static final RenderPipeline BLUR_DOWN = add(new ExtendedRenderPipelineBuilder(UNIFORMS).withLocation(MeteorClient.identifier("pipeline/blur/down")).withVertexFormat(MeteorVertexFormats.POS2, VertexFormat.class_5596.field_27379).withVertexShader(MeteorClient.identifier("shaders/blur.vert")).withFragmentShader(MeteorClient.identifier("shaders/blur_down.frag")).withSampler("uTexture").withUniform("uHalfTexelSize", class_10789.field_56744).withUniform("uOffset", class_10789.field_56743).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
    public static final RenderPipeline BLUR_UP = add(new ExtendedRenderPipelineBuilder(UNIFORMS).withLocation(MeteorClient.identifier("pipeline/blur/up")).withVertexFormat(MeteorVertexFormats.POS2, VertexFormat.class_5596.field_27379).withVertexShader(MeteorClient.identifier("shaders/blur.vert")).withFragmentShader(MeteorClient.identifier("shaders/blur_up.frag")).withSampler("uTexture").withUniform("uHalfTexelSize", class_10789.field_56744).withUniform("uOffset", class_10789.field_56743).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
    public static final RenderPipeline BLUR_PASSTHROUGH = add(new ExtendedRenderPipelineBuilder(UNIFORMS).withLocation(MeteorClient.identifier("pipeline/blur/up")).withVertexFormat(MeteorVertexFormats.POS2, VertexFormat.class_5596.field_27379).withVertexShader(MeteorClient.identifier("shaders/passthrough.vert")).withFragmentShader(MeteorClient.identifier("shaders/passthrough.frag")).withSampler("uTexture").withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());

    private static RenderPipeline add(RenderPipeline pipeline) {
        PIPELINES.add(pipeline);
        return pipeline;
    }

    private MeteorRenderPipelines() {
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/MeteorRenderPipelines$Reloader.class */
    public static class Reloader implements class_4013 {
        public void method_14491(class_3300 manager) {
            GpuDevice device = RenderSystem.getDevice();
            for (RenderPipeline pipeline : MeteorRenderPipelines.PIPELINES) {
                device.precompilePipeline(pipeline, (identifier, shaderType) -> {
                    class_3298 resource = (class_3298) manager.method_14486(identifier).get();
                    try {
                        InputStream in = resource.method_14482();
                        try {
                            String string = IOUtils.toString(in, StandardCharsets.UTF_8);
                            if (in != null) {
                                in.close();
                            }
                            return string;
                        } finally {
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }
}
