package meteordevelopment.meteorclient.utils.render.postprocess;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderPass;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Optional;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ResourcePacksReloadedEvent;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Chams;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_3298;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/postprocess/ChamsShader.class */
public class ChamsShader extends EntityShader {
    private static final String[] FILE_FORMATS = {"png", "jpg"};
    private static Texture IMAGE_TEX;
    private static Chams chams;

    public ChamsShader() {
        init(MeteorRenderPipelines.POST_IMAGE);
        MeteorClient.EVENT_BUS.subscribe(ChamsShader.class);
    }

    @PostInit
    public static void load() {
        try {
            ByteBuffer data = null;
            String[] strArr = FILE_FORMATS;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String fileFormat = strArr[i];
                Optional<class_3298> optional = MeteorClient.mc.method_1478().method_14486(MeteorClient.identifier("textures/chams." + fileFormat));
                if (!optional.isEmpty() && optional.get().method_14482() != null) {
                    data = TextureUtil.readResource(optional.get().method_14482());
                    break;
                }
                i++;
            }
            if (data == null) {
                return;
            }
            data.rewind();
            MemoryStack stack = MemoryStack.stackPush();
            try {
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);
                STBImage.stbi_set_flip_vertically_on_load(true);
                ByteBuffer image = STBImage.stbi_load_from_memory(data, width, height, comp, 4);
                IMAGE_TEX = new Texture();
                IMAGE_TEX.upload(width.get(0), height.get(0), image, Texture.Format.RGBA, Texture.Filter.Nearest, Texture.Filter.Nearest, false);
                STBImage.stbi_image_free(image);
                STBImage.stbi_set_flip_vertically_on_load(false);
                if (stack != null) {
                    stack.close();
                }
            } finally {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    private static void onResourcePacksReloaded(ResourcePacksReloadedEvent event) {
        load();
    }

    @Override // meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader
    protected void setupPass(RenderPass pass) {
        Color color = chams.shaderColor.get();
        pass.setUniform("u_Color", new float[]{color.r / 255.0f, color.g / 255.0f, color.b / 255.0f, color.a / 255.0f});
        if (chams.isShader() && chams.shader.get() == Chams.Shader.Image && IMAGE_TEX != null) {
            IMAGE_TEX.bind();
        }
    }

    @Override // meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader
    protected boolean shouldDraw() {
        if (chams == null) {
            chams = (Chams) Modules.get().get(Chams.class);
        }
        return chams.isShader();
    }

    @Override // meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader
    public boolean shouldDraw(class_1297 entity) {
        return shouldDraw() && chams.entities.get().contains(entity.method_5864()) && !(entity == MeteorClient.mc.field_1724 && chams.ignoreSelfDepth.get().booleanValue());
    }
}
