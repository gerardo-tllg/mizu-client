package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.vertex.VertexFormat;
import meteordevelopment.meteorclient.utils.PreInit;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/FullScreenRenderer.class */
public class FullScreenRenderer {
    public static MeshBuilder mesh;

    private FullScreenRenderer() {
    }

    @PreInit
    public static void init() {
        mesh = new MeshBuilder(MeteorVertexFormats.POS2, VertexFormat.class_5596.field_27379, 4, 6);
        mesh.begin();
        mesh.ensureQuadCapacity();
        mesh.quad(mesh.vec2(-1.0d, -1.0d).next(), mesh.vec2(-1.0d, 1.0d).next(), mesh.vec2(1.0d, 1.0d).next(), mesh.vec2(1.0d, -1.0d).next());
        mesh.end();
    }
}
