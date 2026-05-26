package meteordevelopment.meteorclient.renderer.text;

import java.nio.ByteBuffer;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_310;
import org.lwjgl.BufferUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/text/CustomTextRenderer.class */
public class CustomTextRenderer implements TextRenderer {
    public static final Color SHADOW_COLOR = new Color(60, 60, 60, Opcode.GETFIELD);
    public final FontFace fontFace;
    private final Font[] fonts;
    private Font font;
    private boolean building;
    private boolean scaleOnly;
    private final MeshBuilder mesh = new MeshBuilder(MeteorRenderPipelines.UI_TEXT);
    private double fontScale = 1.0d;
    private double scale = 1.0d;

    public CustomTextRenderer(FontFace fontFace) {
        this.fontFace = fontFace;
        byte[] bytes = Utils.readBytes(fontFace.toStream());
        ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length).put(bytes).flip();
        this.fonts = new Font[5];
        for (int i = 0; i < this.fonts.length; i++) {
            this.fonts[i] = new Font(buffer, (int) Math.round(27.0d * ((((double) i) * 0.5d) + 1.0d)));
        }
    }

    @Override // meteordevelopment.meteorclient.renderer.text.TextRenderer
    public void setAlpha(double a) {
        this.mesh.alpha = a;
    }

    @Override // meteordevelopment.meteorclient.renderer.text.TextRenderer
    public void begin(double scale, boolean scaleOnly, boolean big) {
        int scaleI;
        if (this.building) {
            throw new RuntimeException("CustomTextRenderer.begin() called twice");
        }
        if (!scaleOnly) {
            this.mesh.begin();
        }
        if (big) {
            this.font = this.fonts[this.fonts.length - 1];
        } else {
            double scaleA = Math.floor(scale * 10.0d) / 10.0d;
            if (scaleA >= 3.0d) {
                scaleI = 5;
            } else if (scaleA >= 2.5d) {
                scaleI = 4;
            } else if (scaleA >= 2.0d) {
                scaleI = 3;
            } else {
                scaleI = scaleA >= 1.5d ? 2 : 1;
            }
            this.font = this.fonts[scaleI - 1];
        }
        this.building = true;
        this.scaleOnly = scaleOnly;
        this.fontScale = ((double) this.font.getHeight()) / 27.0d;
        this.scale = 1.0d + ((scale - this.fontScale) / this.fontScale);
    }

    @Override // meteordevelopment.meteorclient.renderer.text.TextRenderer
    public double getWidth(String text, int length, boolean shadow) {
        if (text.isEmpty()) {
            return 0.0d;
        }
        Font font = this.building ? this.font : this.fonts[0];
        return ((font.getWidth(text, length) + ((double) (shadow ? 1 : 0))) * this.scale) / 1.5d;
    }

    @Override // meteordevelopment.meteorclient.renderer.text.TextRenderer
    public double getHeight(boolean shadow) {
        Font font = this.building ? this.font : this.fonts[0];
        return (((double) ((font.getHeight() + 1) + (shadow ? 1 : 0))) * this.scale) / 1.5d;
    }

    @Override // meteordevelopment.meteorclient.renderer.text.TextRenderer
    public double render(String text, double x, double y, Color color, boolean shadow) {
        double width;
        boolean wasBuilding = this.building;
        if (!wasBuilding) {
            begin();
        }
        if (shadow) {
            int preShadowA = SHADOW_COLOR.a;
            SHADOW_COLOR.a = (int) ((((double) color.a) / 255.0d) * ((double) preShadowA));
            width = this.font.render(this.mesh, text, x + ((this.fontScale * this.scale) / 1.5d), y + ((this.fontScale * this.scale) / 1.5d), SHADOW_COLOR, this.scale / 1.5d);
            this.font.render(this.mesh, text, x, y, color, this.scale / 1.5d);
            SHADOW_COLOR.a = preShadowA;
        } else {
            width = this.font.render(this.mesh, text, x, y, color, this.scale / 1.5d);
        }
        if (!wasBuilding) {
            end();
        }
        return width;
    }

    @Override // meteordevelopment.meteorclient.renderer.text.TextRenderer
    public boolean isBuilding() {
        return this.building;
    }

    @Override // meteordevelopment.meteorclient.renderer.text.TextRenderer
    public void end() {
        if (!this.building) {
            throw new RuntimeException("CustomTextRenderer.end() called without calling begin()");
        }
        if (!this.scaleOnly) {
            this.mesh.end();
            MeshRenderer.begin().attachments(class_310.method_1551().method_1522()).pipeline(MeteorRenderPipelines.UI_TEXT).mesh(this.mesh).setupCallback(pass -> {
                this.font.texture.bind();
            }).end();
        }
        this.building = false;
        this.scale = 1.0d;
    }

    public void destroy() {
        for (Font font : this.fonts) {
            font.texture.dispose();
        }
    }
}
