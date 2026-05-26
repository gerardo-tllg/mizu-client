package meteordevelopment.meteorclient.utils.render;

import com.mojang.blaze3d.platform.TextureUtil;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.class_3298;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/PlayerHeadTexture.class */
public class PlayerHeadTexture extends Texture {
    private boolean needsRotate;

    public PlayerHeadTexture(String url) {
        try {
            BufferedImage skin = ImageIO.read(Http.get(url).sendInputStream());
            byte[] head = new byte[256];
            int[] pixel = new int[4];
            int i = 0;
            for (int x = 8; x < 16; x++) {
                for (int y = 8; y < 16; y++) {
                    skin.getData().getPixel(x, y, pixel);
                    for (int j = 0; j < 4; j++) {
                        head[i] = (byte) pixel[j];
                        i++;
                    }
                }
            }
            int i2 = 0;
            for (int x2 = 40; x2 < 48; x2++) {
                for (int y2 = 8; y2 < 16; y2++) {
                    skin.getData().getPixel(x2, y2, pixel);
                    if (pixel[3] != 0) {
                        for (int j2 = 0; j2 < 4; j2++) {
                            head[i2] = (byte) pixel[j2];
                            i2++;
                        }
                    } else {
                        i2 += 4;
                    }
                }
            }
            ByteBuffer buffer = BufferUtils.createByteBuffer(head.length).put(head);
            upload(8, 8, buffer, Texture.Format.RGBA, Texture.Filter.Nearest, Texture.Filter.Nearest, false);
            this.needsRotate = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerHeadTexture() {
        try {
            InputStream inputStream = ((class_3298) MeteorClient.mc.method_1478().method_14486(MeteorClient.identifier("textures/steve.png")).get()).method_14482();
            try {
                ByteBuffer data = TextureUtil.readResource(inputStream);
                data.rewind();
                MemoryStack stack = MemoryStack.stackPush();
                try {
                    IntBuffer width = stack.mallocInt(1);
                    IntBuffer height = stack.mallocInt(1);
                    IntBuffer comp = stack.mallocInt(1);
                    ByteBuffer image = STBImage.stbi_load_from_memory(data, width, height, comp, 4);
                    upload(8, 8, image, Texture.Format.RGBA, Texture.Filter.Nearest, Texture.Filter.Nearest, false);
                    STBImage.stbi_image_free(image);
                    if (stack != null) {
                        stack.close();
                    }
                    MemoryUtil.memFree(data);
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Throwable th) {
                    if (stack != null) {
                        try {
                            stack.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } finally {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean needsRotate() {
        return this.needsRotate;
    }
}
