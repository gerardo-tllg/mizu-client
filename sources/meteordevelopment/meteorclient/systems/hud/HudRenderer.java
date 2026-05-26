package meteordevelopment.meteorclient.systems.hud;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.CustomFontChangedEvent;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.CustomTextRenderer;
import meteordevelopment.meteorclient.renderer.text.Font;
import meteordevelopment.meteorclient.renderer.text.VanillaTextRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.lwjgl.BufferUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/HudRenderer.class */
public class HudRenderer {
    public static final HudRenderer INSTANCE = new HudRenderer();
    private static final double SCALE_TO_HEIGHT = 0.05555555555555555d;
    private final Hud hud = Hud.get();
    private final List<Runnable> postTasks = new ArrayList();
    private final Int2ObjectMap<FontHolder> fontsInUse = new Int2ObjectOpenHashMap();
    private final LoadingCache<Integer, FontHolder> fontCache = CacheBuilder.newBuilder().maximumSize(4).expireAfterAccess(Duration.ofMinutes(10)).removalListener(notification -> {
        if (notification.wasEvicted()) {
            ((FontHolder) notification.getValue()).destroy();
        }
    }).build(CacheLoader.from((v0) -> {
        return loadFont(v0);
    }));
    public class_332 drawContext;
    public double delta;

    private HudRenderer() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public void begin(class_332 drawContext) {
        Renderer2D.COLOR.begin();
        this.drawContext = drawContext;
        this.delta = Utils.frameTime;
        if (!this.hud.hasCustomFont()) {
            VanillaTextRenderer.INSTANCE.scaleIndividually = true;
            VanillaTextRenderer.INSTANCE.begin();
        }
    }

    public void end() {
        Renderer2D.COLOR.render();
        if (this.hud.hasCustomFont()) {
            ObjectIterator it = this.fontsInUse.values().iterator();
            while (it.hasNext()) {
                FontHolder fontHolder = (FontHolder) it.next();
                if (fontHolder.visited) {
                    fontHolder.font.texture.bind();
                    MeshRenderer.begin().attachments(class_310.method_1551().method_1522()).pipeline(MeteorRenderPipelines.UI_TEXT).mesh(fontHolder.getMesh()).end();
                } else {
                    it.remove();
                    this.fontCache.put(Integer.valueOf(fontHolder.font.getHeight()), fontHolder);
                }
                fontHolder.visited = false;
            }
        } else {
            VanillaTextRenderer.INSTANCE.end();
            VanillaTextRenderer.INSTANCE.scaleIndividually = false;
        }
        for (Runnable task : this.postTasks) {
            task.run();
        }
        this.postTasks.clear();
        this.drawContext = null;
    }

    public void line(double x1, double y1, double x2, double y2, Color color) {
        Renderer2D.COLOR.line(x1, y1, x2, y2, color);
    }

    public void quad(double x, double y, double width, double height, Color color) {
        Renderer2D.COLOR.quad(x, y, width, height, color);
    }

    public void quad(double x, double y, double width, double height, Color cTopLeft, Color cTopRight, Color cBottomRight, Color cBottomLeft) {
        Renderer2D.COLOR.quad(x, y, width, height, cTopLeft, cTopRight, cBottomRight, cBottomLeft);
    }

    public void triangle(double x1, double y1, double x2, double y2, double x3, double y3, Color color) {
        Renderer2D.COLOR.triangle(x1, y1, x2, y2, x3, y3, color);
    }

    public void texture(class_2960 id, double x, double y, double width, double height, Color color) {
        GL.bindTexture(id);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(x, y, width, height, color);
        Renderer2D.TEXTURE.render();
    }

    public double text(String text, double x, double y, Color color, boolean shadow, double scale) {
        double width;
        if (scale == -1.0d) {
            scale = this.hud.getTextScale();
        }
        if (!this.hud.hasCustomFont()) {
            VanillaTextRenderer.INSTANCE.scale = scale * 2.0d;
            return VanillaTextRenderer.INSTANCE.render(text, x, y, color, shadow);
        }
        FontHolder fontHolder = getFontHolder(scale, true);
        Font font = fontHolder.font;
        MeshBuilder mesh = fontHolder.getMesh();
        if (shadow) {
            int preShadowA = CustomTextRenderer.SHADOW_COLOR.a;
            CustomTextRenderer.SHADOW_COLOR.a = (int) ((((double) color.a) / 255.0d) * ((double) preShadowA));
            width = font.render(mesh, text, x + 1.0d, y + 1.0d, CustomTextRenderer.SHADOW_COLOR, scale);
            font.render(mesh, text, x, y, color, scale);
            CustomTextRenderer.SHADOW_COLOR.a = preShadowA;
        } else {
            width = font.render(mesh, text, x, y, color, scale);
        }
        return width;
    }

    public double text(String text, double x, double y, Color color, boolean shadow) {
        return text(text, x, y, color, shadow, -1.0d);
    }

    public double textWidth(String text, boolean shadow, double scale) {
        if (text.isEmpty()) {
            return 0.0d;
        }
        if (this.hud.hasCustomFont()) {
            double width = getFont(scale).getWidth(text, text.length());
            return ((width + ((double) (shadow ? 1 : 0))) * (scale == -1.0d ? this.hud.getTextScale() : scale)) + ((double) (shadow ? 1 : 0));
        }
        VanillaTextRenderer.INSTANCE.scale = (scale == -1.0d ? this.hud.getTextScale() : scale) * 2.0d;
        return VanillaTextRenderer.INSTANCE.getWidth(text, shadow);
    }

    public double textWidth(String text, boolean shadow) {
        return textWidth(text, shadow, -1.0d);
    }

    public double textWidth(String text, double scale) {
        return textWidth(text, false, scale);
    }

    public double textWidth(String text) {
        return textWidth(text, false, -1.0d);
    }

    public double textHeight(boolean shadow, double scale) {
        if (this.hud.hasCustomFont()) {
            double height = getFont(scale).getHeight() + 1;
            return (height + ((double) (shadow ? 1 : 0))) * (scale == -1.0d ? this.hud.getTextScale() : scale);
        }
        VanillaTextRenderer.INSTANCE.scale = (scale == -1.0d ? this.hud.getTextScale() : scale) * 2.0d;
        return VanillaTextRenderer.INSTANCE.getHeight(shadow);
    }

    public double textHeight(boolean shadow) {
        return textHeight(shadow, -1.0d);
    }

    public double textHeight() {
        return textHeight(false, -1.0d);
    }

    public void post(Runnable task) {
        this.postTasks.add(task);
    }

    public void item(class_1799 itemStack, int x, int y, float scale, boolean overlay, String countOverlay) {
        RenderUtils.drawItem(this.drawContext, itemStack, x, y, scale, overlay, countOverlay);
    }

    public void item(class_1799 itemStack, int x, int y, float scale, boolean overlay) {
        RenderUtils.drawItem(this.drawContext, itemStack, x, y, scale, overlay);
    }

    private FontHolder getFontHolder(double scale, boolean render) {
        if (scale == -1.0d) {
            scale = this.hud.getTextScale();
        }
        int height = (int) Math.round(scale / SCALE_TO_HEIGHT);
        FontHolder fontHolder = (FontHolder) this.fontsInUse.get(height);
        if (fontHolder != null) {
            if (render) {
                fontHolder.visited = true;
            }
            return fontHolder;
        }
        if (render) {
            FontHolder fontHolder2 = (FontHolder) this.fontCache.getIfPresent(Integer.valueOf(height));
            if (fontHolder2 == null) {
                fontHolder2 = loadFont(height);
            } else {
                this.fontCache.invalidate(Integer.valueOf(height));
            }
            this.fontsInUse.put(height, fontHolder2);
            fontHolder2.visited = true;
            return fontHolder2;
        }
        return (FontHolder) this.fontCache.getUnchecked(Integer.valueOf(height));
    }

    private Font getFont(double scale) {
        return getFontHolder(scale, false).font;
    }

    @EventHandler
    private void onCustomFontChanged(CustomFontChangedEvent event) {
        ObjectIterator it = this.fontsInUse.values().iterator();
        while (it.hasNext()) {
            FontHolder fontHolder = (FontHolder) it.next();
            fontHolder.destroy();
        }
        for (FontHolder fontHolder2 : this.fontCache.asMap().values()) {
            fontHolder2.destroy();
        }
        this.fontsInUse.clear();
        this.fontCache.invalidateAll();
    }

    private static FontHolder loadFont(int height) {
        byte[] data = Utils.readBytes(Fonts.RENDERER.fontFace.toStream());
        ByteBuffer buffer = BufferUtils.createByteBuffer(data.length).put(data).flip();
        return new FontHolder(new Font(buffer, height));
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/HudRenderer$FontHolder.class */
    private static class FontHolder {
        public final Font font;
        public boolean visited;
        private MeshBuilder mesh;

        public FontHolder(Font font) {
            this.font = font;
        }

        public MeshBuilder getMesh() {
            if (this.mesh == null) {
                this.mesh = new MeshBuilder(MeteorRenderPipelines.UI_TEXT);
            }
            if (!this.mesh.isBuilding()) {
                this.mesh.begin();
            }
            return this.mesh;
        }

        public void destroy() {
            this.font.texture.dispose();
        }
    }
}
