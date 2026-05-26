package meteordevelopment.meteorclient.utils.render;

import meteordevelopment.meteorclient.renderer.Mesh;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_1921;
import net.minecraft.class_4588;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/MeshVertexConsumerProvider.class */
public class MeshVertexConsumerProvider implements IVertexConsumerProvider {
    private final MeshVertexConsumer vertexConsumer;

    public MeshVertexConsumerProvider(Mesh mesh) {
        this.vertexConsumer = new MeshVertexConsumer(mesh);
    }

    public class_4588 getBuffer(class_1921 layer) {
        return this.vertexConsumer;
    }

    public void setColor(Color color) {
        this.vertexConsumer.fixedColor(color.r, color.g, color.b, color.a);
    }

    @Override // meteordevelopment.meteorclient.utils.render.IVertexConsumerProvider
    public void setOffset(int offsetX, int offsetY, int offsetZ) {
        this.vertexConsumer.setOffset(offsetX, offsetY, offsetZ);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/MeshVertexConsumerProvider$MeshVertexConsumer.class */
    public static class MeshVertexConsumer implements class_4588 {
        private final Mesh mesh;
        private int offsetX;
        private int offsetY;
        private int offsetZ;
        private final double[] xs = new double[4];
        private final double[] ys = new double[4];
        private final double[] zs = new double[4];
        private final Color color = new Color();
        private int i;

        public MeshVertexConsumer(Mesh mesh) {
            this.mesh = mesh;
        }

        public void setOffset(int offsetX, int offsetY, int offsetZ) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }

        public class_4588 method_22912(float x, float y, float z) {
            this.xs[this.i] = ((double) this.offsetX) + ((double) x);
            this.ys[this.i] = ((double) this.offsetY) + ((double) y);
            this.zs[this.i] = ((double) this.offsetZ) + ((double) z);
            int i = this.i + 1;
            this.i = i;
            if (i >= 4) {
                this.mesh.quad(this.mesh.vec3(this.xs[0], this.ys[0], this.zs[0]).color(this.color).next(), this.mesh.vec3(this.xs[1], this.ys[1], this.zs[1]).color(this.color).next(), this.mesh.vec3(this.xs[2], this.ys[2], this.zs[2]).color(this.color).next(), this.mesh.vec3(this.xs[3], this.ys[3], this.zs[3]).color(this.color).next());
                this.i = 0;
            }
            return this;
        }

        public class_4588 method_1336(int red, int green, int blue, int alpha) {
            return this;
        }

        public class_4588 method_22913(float u, float v) {
            return this;
        }

        public class_4588 method_60796(int u, int v) {
            return this;
        }

        public class_4588 method_22921(int u, int v) {
            return this;
        }

        public class_4588 method_22914(float x, float y, float z) {
            return null;
        }

        public void fixedColor(int red, int green, int blue, int alpha) {
            this.color.set(red, green, blue, alpha);
        }
    }
}
