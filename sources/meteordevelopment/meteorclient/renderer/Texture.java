package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/Texture.class */
public class Texture {
    public int width;
    public int height;
    private int id;
    private boolean valid;

    public Texture(int width, int height, byte[] data, Format format, Filter filterMin, Filter filterMag) {
        if (RenderSystem.isOnRenderThread()) {
            upload(width, height, data, format, filterMin, filterMag);
        } else {
            RenderSystem.assertOnRenderThread();
            upload(width, height, data, format, filterMin, filterMag);
        }
    }

    public Texture() {
    }

    protected void upload(int width, int height, byte[] data, Format format, Filter filterMin, Filter filterMag) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(data.length).put(data);
        upload(width, height, buffer, format, filterMin, filterMag, false);
    }

    public void upload(int width, int height, ByteBuffer buffer, Format format, Filter filterMin, Filter filterMag, boolean wrapClamp) {
        this.width = width;
        this.height = height;
        if (!this.valid) {
            this.id = GL.genTexture();
            this.valid = true;
        }
        bind();
        GL.defaultPixelStore();
        GL.textureParam(3553, 10242, wrapClamp ? 33071 : 10497);
        GL.textureParam(3553, 10243, wrapClamp ? 33071 : 10497);
        GL.textureParam(3553, 10241, filterMin.toOpenGL());
        GL.textureParam(3553, 10240, filterMag.toOpenGL());
        buffer.rewind();
        GL.textureImage2D(3553, 0, format.toOpenGL(), width, height, 0, format.toOpenGL(), 5121, buffer);
        if (filterMin == Filter.LinearMipmapLinear || filterMag == Filter.LinearMipmapLinear) {
            GL.generateMipmap(3553);
        }
    }

    public boolean isValid() {
        return this.valid;
    }

    public void bind(int slot) {
        GL.bindTexture(this.id, slot);
    }

    public void bind() {
        bind(0);
    }

    public void dispose() {
        GL.deleteTexture(this.id);
        this.valid = false;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/Texture$Format.class */
    public enum Format {
        A,
        RGB,
        RGBA;

        /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
        public int toOpenGL() throws MatchException {
            switch (this) {
                case A:
                    return 6403;
                case RGB:
                    return 6407;
                case RGBA:
                    return 6408;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/Texture$Filter.class */
    public enum Filter {
        Nearest,
        Linear,
        LinearMipmapLinear;

        /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
        public int toOpenGL() throws MatchException {
            switch (this) {
                case Nearest:
                    return 9728;
                case Linear:
                    return 9729;
                case LinearMipmapLinear:
                    return 9987;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
        }
    }
}
