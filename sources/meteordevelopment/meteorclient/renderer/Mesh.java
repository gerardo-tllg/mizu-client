package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import org.joml.Matrix4fStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/Mesh.class */
public class Mesh {
    public boolean depthTest = false;
    public double alpha = 1.0d;
    private final DrawMode drawMode;
    private final int primitiveVerticesSize;
    private final int vao;
    private final int vbo;
    private final int ibo;
    private ByteBuffer vertices;
    private long verticesPointerStart;
    private long verticesPointer;
    private ByteBuffer indices;
    private long indicesPointer;
    private int vertexI;
    private int indicesCount;
    private boolean building;
    private boolean rendering3D;
    private double cameraX;
    private double cameraZ;
    private boolean beganRendering;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/Mesh$Attrib.class */
    public enum Attrib {
        Float(1, 4, false),
        Vec2(2, 4, false),
        Vec3(3, 4, false),
        Color(4, 1, true);

        public final int count;
        public final int size;
        public final boolean normalized;

        Attrib(int count, int componentSize, boolean normalized) {
            this.count = count;
            this.size = count * componentSize;
            this.normalized = normalized;
        }

        public int getType() {
            return this == Color ? 5121 : 5126;
        }
    }

    public Mesh(DrawMode drawMode, Attrib... attributes) {
        int stride = 0;
        for (Attrib attribute : attributes) {
            stride += attribute.size;
        }
        this.drawMode = drawMode;
        this.primitiveVerticesSize = stride * drawMode.indicesCount;
        this.vertices = BufferUtils.createByteBuffer(this.primitiveVerticesSize * 256 * 4);
        this.verticesPointerStart = MemoryUtil.memAddress0(this.vertices);
        this.indices = BufferUtils.createByteBuffer(drawMode.indicesCount * 512 * 4);
        this.indicesPointer = MemoryUtil.memAddress0(this.indices);
        this.vao = GL.genVertexArray();
        GL.bindVertexArray(this.vao);
        this.vbo = GL.genBuffer();
        GL.bindVertexBuffer(this.vbo);
        this.ibo = GL.genBuffer();
        GL.bindIndexBuffer(this.ibo);
        int offset = 0;
        for (int i = 0; i < attributes.length; i++) {
            Attrib attrib = attributes[i];
            GL.enableVertexAttribute(i);
            GL.vertexAttribute(i, attrib.count, attrib.getType(), attrib.normalized, stride, offset);
            offset += attrib.size;
        }
        GL.bindVertexArray(0);
        GL.bindVertexBuffer(0);
        GL.bindIndexBuffer(0);
    }

    public void destroy() {
        GL.deleteBuffer(this.ibo);
        GL.deleteBuffer(this.vbo);
        GL.deleteVertexArray(this.vao);
    }

    public void begin() {
        if (this.building) {
            throw new IllegalStateException("Mesh.begin() called while already building.");
        }
        this.verticesPointer = this.verticesPointerStart;
        this.vertexI = 0;
        this.indicesCount = 0;
        this.building = true;
        this.rendering3D = Utils.rendering3D;
        if (this.rendering3D) {
            class_243 camera = MeteorClient.mc.field_1773.method_19418().method_19326();
            this.cameraX = camera.field_1352;
            this.cameraZ = camera.field_1350;
        } else {
            this.cameraX = 0.0d;
            this.cameraZ = 0.0d;
        }
    }

    public Mesh vec3(double x, double y, double z) {
        long p = this.verticesPointer;
        MemoryUtil.memPutFloat(p, (float) (x - this.cameraX));
        MemoryUtil.memPutFloat(p + 4, (float) y);
        MemoryUtil.memPutFloat(p + 8, (float) (z - this.cameraZ));
        this.verticesPointer += 12;
        return this;
    }

    public Mesh vec2(double x, double y) {
        long p = this.verticesPointer;
        MemoryUtil.memPutFloat(p, (float) x);
        MemoryUtil.memPutFloat(p + 4, (float) y);
        this.verticesPointer += 8;
        return this;
    }

    public Mesh color(Color c) {
        long p = this.verticesPointer;
        MemoryUtil.memPutByte(p, (byte) c.r);
        MemoryUtil.memPutByte(p + 1, (byte) c.g);
        MemoryUtil.memPutByte(p + 2, (byte) c.b);
        MemoryUtil.memPutByte(p + 3, (byte) (c.a * ((float) this.alpha)));
        this.verticesPointer += 4;
        return this;
    }

    public int next() {
        int i = this.vertexI;
        this.vertexI = i + 1;
        return i;
    }

    public void line(int i1, int i2) {
        long p = this.indicesPointer + (((long) this.indicesCount) * 4);
        MemoryUtil.memPutInt(p, i1);
        MemoryUtil.memPutInt(p + 4, i2);
        this.indicesCount += 2;
        growIfNeeded();
    }

    public void quad(int i1, int i2, int i3, int i4) {
        long p = this.indicesPointer + (((long) this.indicesCount) * 4);
        MemoryUtil.memPutInt(p, i1);
        MemoryUtil.memPutInt(p + 4, i2);
        MemoryUtil.memPutInt(p + 8, i3);
        MemoryUtil.memPutInt(p + 12, i3);
        MemoryUtil.memPutInt(p + 16, i4);
        MemoryUtil.memPutInt(p + 20, i1);
        this.indicesCount += 6;
        growIfNeeded();
    }

    public void triangle(int i1, int i2, int i3) {
        long p = this.indicesPointer + (((long) this.indicesCount) * 4);
        MemoryUtil.memPutInt(p, i1);
        MemoryUtil.memPutInt(p + 4, i2);
        MemoryUtil.memPutInt(p + 8, i3);
        this.indicesCount += 3;
        growIfNeeded();
    }

    public void ensureCapacity(int vertexCount, int indexCount) {
        growIfNeeded();
    }

    public void growIfNeeded() {
        if ((this.vertexI + 1) * this.primitiveVerticesSize >= this.vertices.capacity()) {
            int offset = getVerticesOffset();
            int newSize = this.vertices.capacity() * 2;
            if (newSize % this.primitiveVerticesSize != 0) {
                newSize += newSize % this.primitiveVerticesSize;
            }
            ByteBuffer newVertices = BufferUtils.createByteBuffer(newSize);
            MemoryUtil.memCopy(MemoryUtil.memAddress0(this.vertices), MemoryUtil.memAddress0(newVertices), offset);
            this.vertices = newVertices;
            this.verticesPointerStart = MemoryUtil.memAddress0(this.vertices);
            this.verticesPointer = this.verticesPointerStart + ((long) offset);
        }
        if (this.indicesCount * 4 >= this.indices.capacity()) {
            int newSize2 = this.indices.capacity() * 2;
            if (newSize2 % this.drawMode.indicesCount != 0) {
                newSize2 += newSize2 % (this.drawMode.indicesCount * 4);
            }
            ByteBuffer newIndices = BufferUtils.createByteBuffer(newSize2);
            MemoryUtil.memCopy(MemoryUtil.memAddress0(this.indices), MemoryUtil.memAddress0(newIndices), ((long) this.indicesCount) * 4);
            this.indices = newIndices;
            this.indicesPointer = MemoryUtil.memAddress0(this.indices);
        }
    }

    public void end() {
        if (!this.building) {
            throw new IllegalStateException("Mesh.end() called while not building.");
        }
        if (this.indicesCount > 0) {
            GL.bindVertexBuffer(this.vbo);
            GL.bufferData(34962, this.vertices.limit(getVerticesOffset()), 35048);
            GL.bindVertexBuffer(0);
            GL.bindIndexBuffer(this.ibo);
            GL.bufferData(34963, this.indices.limit(this.indicesCount * 4), 35048);
            GL.bindIndexBuffer(0);
        }
        this.building = false;
    }

    public void beginRender(class_4587 matrices) {
        GL.saveState();
        if (this.depthTest) {
            GL.enableDepth();
        } else {
            GL.disableDepth();
        }
        GL.enableBlend();
        GL.disableCull();
        GL.enableLineSmooth();
        if (this.rendering3D) {
            Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
            matrixStack.pushMatrix();
            if (matrices != null) {
                matrixStack.mul(matrices.method_23760().method_23761());
            }
            class_243 cameraPos = MeteorClient.mc.field_1773.method_19418().method_19326();
            matrixStack.translate(0.0f, (float) (-cameraPos.field_1351), 0.0f);
        }
        this.beganRendering = true;
    }

    public void render(class_4587 matrices) {
        if (this.building) {
            end();
        }
        if (this.indicesCount > 0) {
            boolean wasBeganRendering = this.beganRendering;
            if (!wasBeganRendering) {
                beginRender(matrices);
            }
            beforeRender();
            Shader.BOUND.setDefaults();
            GL.bindVertexArray(this.vao);
            GL.drawElements(this.drawMode.getGL(), this.indicesCount, 5125);
            GL.bindVertexArray(0);
            if (!wasBeganRendering) {
                endRender();
            }
        }
    }

    public void endRender() {
        if (this.rendering3D) {
            RenderSystem.getModelViewStack().popMatrix();
        }
        GL.restoreState();
        this.beganRendering = false;
    }

    public boolean isBuilding() {
        return this.building;
    }

    protected void beforeRender() {
    }

    private int getVerticesOffset() {
        return (int) (this.verticesPointer - this.verticesPointerStart);
    }
}
