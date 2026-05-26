package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.vertex.VertexFormatElement;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/MeteorVertexFormatElements.class */
public abstract class MeteorVertexFormatElements {
    public static final VertexFormatElement POS2 = VertexFormatElement.register(getNextVertexFormatElementId(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 2);

    private MeteorVertexFormatElements() {
    }

    private static int getNextVertexFormatElementId() {
        int id = 0;
        while (VertexFormatElement.byId(id) != null) {
            id++;
            if (id >= 32) {
                throw new RuntimeException("Too many mods registering VertexFormatElements");
            }
        }
        return id;
    }
}
