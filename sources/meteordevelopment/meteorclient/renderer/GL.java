package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.opengl.GlStateManager;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.class_1044;
import net.minecraft.class_10868;
import net.minecraft.class_2960;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL32C;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/GL.class */
public class GL {
    private static final FloatBuffer MAT = BufferUtils.createFloatBuffer(16);
    private static boolean depthSaved;
    private static boolean blendSaved;
    private static boolean cullSaved;
    private static boolean scissorSaved;
    public static int CURRENT_IBO;
    private static int prevIbo;

    private GL() {
    }

    public static int genVertexArray() {
        return GlStateManager._glGenVertexArrays();
    }

    public static int genBuffer() {
        return GlStateManager._glGenBuffers();
    }

    public static int genTexture() {
        return GlStateManager._genTexture();
    }

    public static int genFramebuffer() {
        return GlStateManager.glGenFramebuffers();
    }

    public static void deleteBuffer(int buffer) {
        GlStateManager._glDeleteBuffers(buffer);
    }

    public static void deleteVertexArray(int vao) {
        GL32C.glDeleteVertexArrays(vao);
    }

    public static void deleteShader(int shader) {
        GlStateManager.glDeleteShader(shader);
    }

    public static void deleteTexture(int id) {
        GlStateManager._deleteTexture(id);
    }

    public static void deleteFramebuffer(int fbo) {
        GlStateManager._glDeleteFramebuffers(fbo);
    }

    public static void deleteProgram(int program) {
        GlStateManager.glDeleteProgram(program);
    }

    public static void bindVertexArray(int vao) {
        GlStateManager._glBindVertexArray(vao);
    }

    public static void bindVertexBuffer(int vbo) {
        GlStateManager._glBindBuffer(34962, vbo);
    }

    public static void bindIndexBuffer(int ibo) {
        if (ibo != 0) {
            prevIbo = CURRENT_IBO;
        }
        GlStateManager._glBindBuffer(34963, ibo != 0 ? ibo : prevIbo);
    }

    public static void bindFramebuffer(int fbo) {
        GlStateManager._glBindFramebuffer(36160, fbo);
    }

    public static void bufferData(int target, ByteBuffer data, int usage) {
        GlStateManager._glBufferData(target, data, usage);
    }

    public static void drawElements(int mode, int first, int type) {
        GlStateManager._drawElements(mode, first, type, 0L);
    }

    public static void enableVertexAttribute(int i) {
        GlStateManager._enableVertexAttribArray(i);
    }

    public static void vertexAttribute(int index, int size, int type, boolean normalized, int stride, long pointer) {
        GlStateManager._vertexAttribPointer(index, size, type, normalized, stride, pointer);
    }

    public static int createShader(int type) {
        return GlStateManager.glCreateShader(type);
    }

    public static void shaderSource(int shader, String source) {
        GlStateManager.glShaderSource(shader, source);
    }

    public static String compileShader(int shader) {
        GlStateManager.glCompileShader(shader);
        if (GlStateManager.glGetShaderi(shader, 35713) == 0) {
            return GlStateManager.glGetShaderInfoLog(shader, 512);
        }
        return null;
    }

    public static int createProgram() {
        return GlStateManager.glCreateProgram();
    }

    public static String linkProgram(int program, int vertShader, int fragShader) {
        GlStateManager.glAttachShader(program, vertShader);
        GlStateManager.glAttachShader(program, fragShader);
        GlStateManager.glLinkProgram(program);
        if (GlStateManager.glGetProgrami(program, 35714) == 0) {
            return GlStateManager.glGetProgramInfoLog(program, 512);
        }
        return null;
    }

    public static void useProgram(int program) {
        GlStateManager._glUseProgram(program);
    }

    public static void viewport(int x, int y, int width, int height) {
        GlStateManager._viewport(x, y, width, height);
    }

    public static int getUniformLocation(int program, String name) {
        return GlStateManager._glGetUniformLocation(program, name);
    }

    public static void uniformInt(int location, int v) {
        GlStateManager._glUniform1i(location, v);
    }

    public static void uniformFloat(int location, float v) {
        GL32C.glUniform1f(location, v);
    }

    public static void uniformFloat2(int location, float v1, float v2) {
        GL32C.glUniform2f(location, v1, v2);
    }

    public static void uniformFloat3(int location, float v1, float v2, float v3) {
        GL32C.glUniform3f(location, v1, v2, v3);
    }

    public static void uniformFloat4(int location, float v1, float v2, float v3, float v4) {
        GL32C.glUniform4f(location, v1, v2, v3, v4);
    }

    public static void uniformFloat3Array(int location, float[] v) {
        GL32C.glUniform3fv(location, v);
    }

    public static void uniformMatrix(int location, Matrix4f v) {
        v.get(MAT);
        GL32C.glUniformMatrix4fv(location, false, MAT);
    }

    public static void pixelStore(int name, int param) {
        GlStateManager._pixelStore(name, param);
    }

    public static void textureParam(int target, int name, int param) {
        GlStateManager._texParameter(target, name, param);
    }

    public static void textureImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
        GL32C.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
    }

    public static void defaultPixelStore() {
        pixelStore(3312, 0);
        pixelStore(3313, 0);
        pixelStore(3314, 0);
        pixelStore(32878, 0);
        pixelStore(3315, 0);
        pixelStore(3316, 0);
        pixelStore(32877, 0);
        pixelStore(3317, 4);
    }

    public static void generateMipmap(int target) {
        GL32C.glGenerateMipmap(target);
    }

    public static void framebufferTexture2D(int target, int attachment, int textureTarget, int texture, int level) {
        GlStateManager._glFramebufferTexture2D(target, attachment, textureTarget, texture, level);
    }

    public static void clear(int mask) {
        GL32C.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL32C.glClear(mask);
    }

    public static void saveState() {
        depthSaved = GL32C.glIsEnabled(2929);
        blendSaved = GL32C.glIsEnabled(3042);
        cullSaved = GL32C.glIsEnabled(2884);
        scissorSaved = GL32C.glIsEnabled(3089);
    }

    public static void restoreState() {
        if (depthSaved) {
            enableDepth();
        } else {
            disableDepth();
        }
        if (blendSaved) {
            enableBlend();
        } else {
            disableBlend();
        }
        if (cullSaved) {
            enableCull();
        } else {
            disableCull();
        }
        if (scissorSaved) {
            enableScissorTest();
        } else {
            disableScissorTest();
        }
        disableLineSmooth();
    }

    public static void enableDepth() {
        GlStateManager._enableDepthTest();
    }

    public static void disableDepth() {
        GlStateManager._disableDepthTest();
    }

    public static void enableBlend() {
        GlStateManager._enableBlend();
        GL32C.glBlendFunc(770, 771);
    }

    public static void disableBlend() {
        GlStateManager._disableBlend();
    }

    public static void enableCull() {
        GlStateManager._enableCull();
    }

    public static void disableCull() {
        GlStateManager._disableCull();
    }

    public static void enableScissorTest() {
        GlStateManager._enableScissorTest();
    }

    public static void disableScissorTest() {
        GlStateManager._disableScissorTest();
    }

    public static void enableLineSmooth() {
        GL32C.glEnable(2848);
        GL32C.glLineWidth(1.0f);
    }

    public static void disableLineSmooth() {
        GL32C.glDisable(2848);
    }

    public static void bindTexture(class_2960 id) {
        GlStateManager._activeTexture(33984);
        class_1044 texture = MeteorClient.mc.method_1531().method_4619(id);
        class_10868 class_10868VarMethod_68004 = texture.method_68004();
        if (class_10868VarMethod_68004 instanceof class_10868) {
            class_10868 glTexture = class_10868VarMethod_68004;
            GlStateManager._bindTexture(glTexture.method_68427());
        }
    }

    public static void bindTexture(int i, int slot) {
        GlStateManager._activeTexture(33984 + slot);
        GlStateManager._bindTexture(i);
    }

    public static void bindTexture(int i) {
        bindTexture(i, 0);
    }

    public static void resetTextureSlot() {
        GlStateManager._activeTexture(33984);
    }
}
