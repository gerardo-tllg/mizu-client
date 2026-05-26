package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_4587;
import net.minecraft.class_9848;
import org.joml.Matrix4f;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/MeshRenderer.class */
public class MeshRenderer {
    private static final MeshRenderer INSTANCE = new MeshRenderer();
    private static boolean taken;
    private GpuTexture colorAttachment;
    private GpuTexture depthAttachment;
    private Color clearColor;
    private RenderPipeline pipeline;
    private MeshBuilder mesh;
    private Matrix4f matrix;
    private Consumer<RenderPass> setupCallback;

    private MeshRenderer() {
    }

    public static MeshRenderer begin() {
        if (taken) {
            throw new IllegalStateException("Previous instance of MeshRenderer was not ended");
        }
        taken = true;
        return INSTANCE;
    }

    public MeshRenderer attachments(GpuTexture color, GpuTexture depth) {
        this.colorAttachment = color;
        this.depthAttachment = depth;
        return this;
    }

    public MeshRenderer attachments(class_276 framebuffer) {
        this.colorAttachment = framebuffer.method_30277();
        this.depthAttachment = framebuffer.method_30278();
        return this;
    }

    public MeshRenderer clearColor(Color color) {
        this.clearColor = color;
        return this;
    }

    public MeshRenderer pipeline(RenderPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    public MeshRenderer mesh(MeshBuilder mesh) {
        this.mesh = mesh;
        return this;
    }

    public MeshRenderer mesh(MeshBuilder mesh, Matrix4f matrix) {
        this.mesh = mesh;
        this.matrix = matrix;
        return this;
    }

    public MeshRenderer mesh(MeshBuilder mesh, class_4587 matrices) {
        this.mesh = mesh;
        this.matrix = matrices.method_23760().method_23761();
        return this;
    }

    public MeshRenderer setupCallback(Consumer<RenderPass> callback) {
        this.setupCallback = callback;
        return this;
    }

    public void end() {
        OptionalInt optionalIntEmpty;
        RenderPass renderPassCreateRenderPass;
        if (this.mesh.isBuilding()) {
            this.mesh.end();
        }
        if (this.mesh.getIndicesCount() > 0) {
            if (Utils.rendering3D || this.matrix != null) {
                RenderSystem.getModelViewStack().pushMatrix();
            }
            if (this.matrix != null) {
                RenderSystem.getModelViewStack().mul(this.matrix);
            }
            if (Utils.rendering3D) {
                applyCameraPos();
            }
            GpuBuffer vertexBuffer = this.mesh.getVertexBuffer();
            GpuBuffer indexBuffer = this.mesh.getIndexBuffer();
            if (this.clearColor != null) {
                optionalIntEmpty = OptionalInt.of(class_9848.method_61324(this.clearColor.a, this.clearColor.r, this.clearColor.g, this.clearColor.b));
            } else {
                optionalIntEmpty = OptionalInt.empty();
            }
            OptionalInt clearColor = optionalIntEmpty;
            if (this.depthAttachment != null && this.pipeline.wantsDepthTexture()) {
                renderPassCreateRenderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(this.colorAttachment, clearColor, this.depthAttachment, OptionalDouble.empty());
            } else {
                renderPassCreateRenderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(this.colorAttachment, clearColor);
            }
            RenderPass pass = renderPassCreateRenderPass;
            pass.setPipeline(this.pipeline);
            pass.setUniform("u_Proj", RenderSystem.getProjectionMatrix());
            pass.setUniform("u_ModelView", RenderSystem.getModelViewStack());
            if (this.setupCallback != null) {
                this.setupCallback.accept(pass);
            }
            pass.setVertexBuffer(0, vertexBuffer);
            pass.setIndexBuffer(indexBuffer, VertexFormat.class_5595.field_27373);
            pass.drawIndexed(0, this.mesh.getIndicesCount());
            pass.close();
            if (Utils.rendering3D || this.matrix != null) {
                RenderSystem.getModelViewStack().popMatrix();
            }
        }
        this.colorAttachment = null;
        this.depthAttachment = null;
        this.clearColor = null;
        this.pipeline = null;
        this.mesh = null;
        this.matrix = null;
        this.setupCallback = null;
        taken = false;
    }

    private static void applyCameraPos() {
        class_243 cameraPos = MeteorClient.mc.field_1773.method_19418().method_19326();
        RenderSystem.getModelViewStack().translate(0.0f, (float) (-cameraPos.field_1351), 0.0f);
    }
}
