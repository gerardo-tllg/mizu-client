package meteordevelopment.meteorclient.renderer.text;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.renderer.Mesh;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackRange;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/text/Font.class */
public class Font {
    public final Texture texture;
    private final int height;
    private final float scale;
    private final float ascent;
    private final Int2ObjectOpenHashMap<CharData> charMap = new Int2ObjectOpenHashMap<>();
    private static final int size = 2048;

    public Font(ByteBuffer buffer, int height) {
        this.height = height;
        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        STBTruetype.stbtt_InitFont(fontInfo, buffer);
        ByteBuffer bitmap = BufferUtils.createByteBuffer(4194304);
        STBTTPackedchar.Buffer[] cdata = {STBTTPackedchar.create(95), STBTTPackedchar.create(96), STBTTPackedchar.create(128), STBTTPackedchar.create(Opcode.D2F), STBTTPackedchar.create(256), STBTTPackedchar.create(1)};
        STBTTPackContext packContext = STBTTPackContext.create();
        STBTruetype.stbtt_PackBegin(packContext, bitmap, 2048, 2048, 0, 1);
        STBTTPackRange.Buffer packRange = STBTTPackRange.create(cdata.length);
        packRange.put(STBTTPackRange.create().set(height, 32, (IntBuffer) null, 95, cdata[0], (byte) 2, (byte) 2));
        packRange.put(STBTTPackRange.create().set(height, Opcode.IF_ICMPNE, (IntBuffer) null, 96, cdata[1], (byte) 2, (byte) 2));
        packRange.put(STBTTPackRange.create().set(height, 256, (IntBuffer) null, 128, cdata[2], (byte) 2, (byte) 2));
        packRange.put(STBTTPackRange.create().set(height, 880, (IntBuffer) null, Opcode.D2F, cdata[3], (byte) 2, (byte) 2));
        packRange.put(STBTTPackRange.create().set(height, 1024, (IntBuffer) null, 256, cdata[4], (byte) 2, (byte) 2));
        packRange.put(STBTTPackRange.create().set(height, 8734, (IntBuffer) null, 1, cdata[5], (byte) 2, (byte) 2));
        packRange.flip();
        STBTruetype.stbtt_PackFontRanges(packContext, buffer, 0, packRange);
        STBTruetype.stbtt_PackEnd(packContext);
        this.texture = new Texture();
        this.texture.upload(2048, 2048, bitmap, Texture.Format.A, Texture.Filter.Linear, Texture.Filter.Linear, false);
        this.scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, height);
        MemoryStack stack = MemoryStack.stackPush();
        try {
            IntBuffer ascent = stack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascent, (IntBuffer) null, (IntBuffer) null);
            this.ascent = ascent.get(0);
            if (stack != null) {
                stack.close();
            }
            for (int i = 0; i < cdata.length; i++) {
                STBTTPackedchar.Buffer cbuf = cdata[i];
                int offset = packRange.get(i).first_unicode_codepoint_in_range();
                for (int j = 0; j < cbuf.capacity(); j++) {
                    STBTTPackedchar packedChar = cbuf.get(j);
                    this.charMap.put(j + offset, new CharData(packedChar.xoff(), packedChar.yoff(), packedChar.xoff2(), packedChar.yoff2(), packedChar.x0() * 4.8828125E-4f, packedChar.y0() * 4.8828125E-4f, packedChar.x1() * 4.8828125E-4f, packedChar.y1() * 4.8828125E-4f, packedChar.xadvance()));
                }
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
    }

    public double getWidth(String string, int length) {
        double width = 0.0d;
        for (int i = 0; i < length; i++) {
            int cp = string.charAt(i);
            CharData c = (CharData) this.charMap.get(cp);
            if (c == null) {
                c = (CharData) this.charMap.get(32);
            }
            width += (double) c.xAdvance;
        }
        return width;
    }

    public int getHeight() {
        return this.height;
    }

    public double render(Mesh mesh, String string, double x, double y, Color color, double scale) {
        double y2 = y + (((double) (this.ascent * this.scale)) * scale);
        int length = string.length();
        mesh.ensureCapacity(length * 4, length * 6);
        for (int i = 0; i < length; i++) {
            int cp = string.charAt(i);
            CharData c = (CharData) this.charMap.get(cp);
            if (c == null) {
                c = (CharData) this.charMap.get(32);
            }
            mesh.quad(mesh.vec2(x + (((double) c.x0) * scale), y2 + (((double) c.y0) * scale)).vec2(c.u0, c.v0).color(color).next(), mesh.vec2(x + (((double) c.x0) * scale), y2 + (((double) c.y1) * scale)).vec2(c.u0, c.v1).color(color).next(), mesh.vec2(x + (((double) c.x1) * scale), y2 + (((double) c.y1) * scale)).vec2(c.u1, c.v1).color(color).next(), mesh.vec2(x + (((double) c.x1) * scale), y2 + (((double) c.y0) * scale)).vec2(c.u1, c.v0).color(color).next());
            x += ((double) c.xAdvance) * scale;
        }
        return x;
    }

    public double render(MeshBuilder mesh, String string, double x, double y, Color color, double scale) {
        double y2 = y + (((double) (this.ascent * this.scale)) * scale);
        int length = string.length();
        mesh.ensureCapacity(length * 4, length * 6);
        for (int i = 0; i < length; i++) {
            int cp = string.charAt(i);
            CharData c = (CharData) this.charMap.get(cp);
            if (c == null) {
                c = (CharData) this.charMap.get(32);
            }
            mesh.quad(mesh.vec2(x + (((double) c.x0) * scale), y2 + (((double) c.y0) * scale)).vec2(c.u0, c.v0).color(color).next(), mesh.vec2(x + (((double) c.x0) * scale), y2 + (((double) c.y1) * scale)).vec2(c.u0, c.v1).color(color).next(), mesh.vec2(x + (((double) c.x1) * scale), y2 + (((double) c.y1) * scale)).vec2(c.u1, c.v1).color(color).next(), mesh.vec2(x + (((double) c.x1) * scale), y2 + (((double) c.y0) * scale)).vec2(c.u1, c.v0).color(color).next());
            x += ((double) c.xAdvance) * scale;
        }
        return x;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/text/Font$CharData.class */
    private static final class CharData extends Record {
        private final float x0;
        private final float y0;
        private final float x1;
        private final float y1;
        private final float u0;
        private final float v0;
        private final float u1;
        private final float v1;
        private final float xAdvance;

        private CharData(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, float xAdvance) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
            this.xAdvance = xAdvance;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, CharData.class), CharData.class, "x0;y0;x1;y1;u0;v0;u1;v1;xAdvance", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->x0:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->y0:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->x1:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->y1:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->u0:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->v0:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->u1:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->v1:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->xAdvance:F").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, CharData.class), CharData.class, "x0;y0;x1;y1;u0;v0;u1;v1;xAdvance", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->x0:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->y0:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->x1:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->y1:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->u0:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->v0:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->u1:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->v1:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->xAdvance:F").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, CharData.class, Object.class), CharData.class, "x0;y0;x1;y1;u0;v0;u1;v1;xAdvance", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->x0:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->y0:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->x1:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->y1:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->u0:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->v0:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->u1:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->v1:F", "FIELD:Lmeteordevelopment/meteorclient/renderer/text/Font$CharData;->xAdvance:F").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public float x0() {
            return this.x0;
        }

        public float y0() {
            return this.y0;
        }

        public float x1() {
            return this.x1;
        }

        public float y1() {
            return this.y1;
        }

        public float u0() {
            return this.u0;
        }

        public float v0() {
            return this.v0;
        }

        public float u1() {
            return this.u1;
        }

        public float v1() {
            return this.v1;
        }

        public float xAdvance() {
            return this.xAdvance;
        }
    }
}
