package meteordevelopment.meteorclient.renderer;

import meteordevelopment.meteorclient.renderer.Mesh;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/ShaderMesh.class */
public class ShaderMesh extends Mesh {
    private final Shader shader;

    public ShaderMesh(Shader shader, DrawMode drawMode, Mesh.Attrib... attributes) {
        super(drawMode, attributes);
        this.shader = shader;
    }

    @Override // meteordevelopment.meteorclient.renderer.Mesh
    protected void beforeRender() {
        this.shader.bind();
    }
}
