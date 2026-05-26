package meteordevelopment.meteorclient.utils.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.IBakedQuad;
import net.minecraft.class_1087;
import net.minecraft.class_10889;
import net.minecraft.class_1921;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2464;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_5819;
import net.minecraft.class_777;
import net.minecraft.class_827;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/SimpleBlockRenderer.class */
public abstract class SimpleBlockRenderer {
    private static final class_4587 MATRICES = new class_4587();
    private static final List<class_10889> PARTS = new ArrayList();
    private static final class_2350[] DIRECTIONS = class_2350.values();
    private static final class_5819 RANDOM = class_5819.method_43047();

    private SimpleBlockRenderer() {
    }

    public static void renderWithBlockEntity(class_2586 blockEntity, float tickDelta, IVertexConsumerProvider vertexConsumerProvider) {
        vertexConsumerProvider.setOffset(blockEntity.method_11016().method_10263(), blockEntity.method_11016().method_10264(), blockEntity.method_11016().method_10260());
        render(blockEntity.method_11016(), blockEntity.method_11010(), vertexConsumerProvider);
        class_827<class_2586> renderer = MeteorClient.mc.method_31975().method_3550(blockEntity);
        if (renderer != null && blockEntity.method_11002() && blockEntity.method_11017().method_20526(blockEntity.method_11010())) {
            class_243 camera = MeteorClient.mc.field_1773.method_19418().method_19326();
            renderer.method_3569(blockEntity, tickDelta, MATRICES, vertexConsumerProvider, 15728880, class_4608.field_21444, camera);
        }
        vertexConsumerProvider.setOffset(0, 0, 0);
    }

    public static void render(class_2338 pos, class_2680 state, class_4597 consumerProvider) {
        if (state.method_26217() != class_2464.field_11458) {
            return;
        }
        class_4588 consumer = consumerProvider.getBuffer(class_1921.method_23577());
        class_1087 model = MeteorClient.mc.method_1541().method_3349(state);
        model.method_68513(RANDOM, PARTS);
        class_243 offset = state.method_26226(pos);
        float offsetX = (float) offset.field_1352;
        float offsetY = (float) offset.field_1351;
        float offsetZ = (float) offset.field_1350;
        for (class_10889 part : PARTS) {
            for (class_2350 direction : DIRECTIONS) {
                List<class_777> quads = part.method_68509(direction);
                if (!quads.isEmpty()) {
                    renderQuads(quads, offsetX, offsetY, offsetZ, consumer);
                }
            }
            List<class_777> quads2 = part.method_68509((class_2350) null);
            if (!quads2.isEmpty()) {
                renderQuads(quads2, offsetX, offsetY, offsetZ, consumer);
            }
        }
        PARTS.clear();
    }

    private static void renderQuads(List<class_777> quads, float offsetX, float offsetY, float offsetZ, class_4588 consumer) {
        Iterator<class_777> it = quads.iterator();
        while (it.hasNext()) {
            IBakedQuad quad = (class_777) it.next();
            for (int j = 0; j < 4; j++) {
                float x = quad.meteor$getX(j);
                float y = quad.meteor$getY(j);
                float z = quad.meteor$getZ(j);
                consumer.method_22912(offsetX + x, offsetY + y, offsetZ + z);
            }
        }
    }
}
