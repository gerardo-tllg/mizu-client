package meteordevelopment.meteorclient.utils.render.postprocess;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.FullScreenRenderer;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import net.minecraft.class_1297;
import net.minecraft.class_276;
import net.minecraft.class_4618;
import net.minecraft.class_6367;
import org.lwjgl.glfw.GLFW;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/postprocess/PostProcessShader.class */
public abstract class PostProcessShader {
    public class_4618 vertexConsumerProvider;
    public class_276 framebuffer;
    protected RenderPipeline pipeline;

    protected abstract boolean shouldDraw();

    public abstract boolean shouldDraw(class_1297 class_1297Var);

    protected abstract void setupPass(RenderPass renderPass);

    public void init(RenderPipeline pipeline) {
        this.vertexConsumerProvider = new class_4618(MeteorClient.mc.method_22940().method_23000());
        this.framebuffer = new class_6367(MeteorClient.NAME + " PostProcessShader", MeteorClient.mc.method_22683().method_4489(), MeteorClient.mc.method_22683().method_4506(), true);
        this.pipeline = pipeline;
    }

    protected void preDraw() {
    }

    protected void postDraw() {
    }

    public boolean beginRender() {
        return shouldDraw();
    }

    public void endRender(Runnable draw) {
        if (shouldDraw()) {
            preDraw();
            draw.run();
            postDraw();
            MeshRenderer.begin().attachments(MeteorClient.mc.method_1522()).pipeline(this.pipeline).mesh(FullScreenRenderer.mesh).setupCallback(pass -> {
                pass.bindSampler("u_Texture", this.framebuffer.method_30277());
                pass.setUniform("u_Size", new float[]{MeteorClient.mc.method_22683().method_4489(), MeteorClient.mc.method_22683().method_4506()});
                pass.setUniform("u_Time", new float[]{(float) GLFW.glfwGetTime()});
                setupPass(pass);
            }).end();
        }
    }

    public void onResized(int width, int height) {
        if (this.framebuffer == null) {
            return;
        }
        this.framebuffer.method_1234(width, height);
    }
}
