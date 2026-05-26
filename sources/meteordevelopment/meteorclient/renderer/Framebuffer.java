package meteordevelopment.meteorclient.renderer;

import meteordevelopment.meteorclient.MeteorClient;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/Framebuffer.class */
public class Framebuffer {
    private int id;
    public int texture;
    public double sizeMulti;
    public int width;
    public int height;

    public Framebuffer(double sizeMulti) {
        this.sizeMulti = 1.0d;
        this.sizeMulti = sizeMulti;
        init();
    }

    public Framebuffer() {
        this.sizeMulti = 1.0d;
        init();
    }

    private void init() {
        this.id = GL.genFramebuffer();
        bind();
        this.texture = GL.genTexture();
        GL.bindTexture(this.texture);
        GL.defaultPixelStore();
        GL.textureParam(3553, 10242, 33071);
        GL.textureParam(3553, 10243, 33071);
        GL.textureParam(3553, 10241, 9729);
        GL.textureParam(3553, 10240, 9729);
        this.width = (int) (((double) MeteorClient.mc.method_22683().method_4489()) * this.sizeMulti);
        this.height = (int) (((double) MeteorClient.mc.method_22683().method_4506()) * this.sizeMulti);
        GL.textureImage2D(3553, 0, 6407, this.width, this.height, 0, 6407, 5121, null);
        GL.framebufferTexture2D(36160, 36064, 3553, this.texture, 0);
        unbind();
    }

    public void bind() {
        GL.bindFramebuffer(this.id);
    }

    public void setViewport() {
        GL.viewport(0, 0, this.width, this.height);
    }

    public void unbind() {
        GL.bindFramebuffer(0);
    }

    public void resize() {
        GL.deleteFramebuffer(this.id);
        GL.deleteTexture(this.texture);
        init();
    }
}
