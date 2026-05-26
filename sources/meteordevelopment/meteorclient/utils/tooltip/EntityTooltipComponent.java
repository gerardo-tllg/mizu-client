package meteordevelopment.meteorclient.utils.tooltip;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1477;
import net.minecraft.class_308;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5684;
import net.minecraft.class_6053;
import net.minecraft.class_7833;
import net.minecraft.class_898;
import org.joml.Quaternionf;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/tooltip/EntityTooltipComponent.class */
public class EntityTooltipComponent implements MeteorTooltipData, class_5684 {
    protected final class_1297 entity;

    public EntityTooltipComponent(class_1297 entity) {
        this.entity = entity;
    }

    @Override // meteordevelopment.meteorclient.utils.tooltip.MeteorTooltipData
    public class_5684 getComponent() {
        return this;
    }

    public int method_32661(class_327 textRenderer) {
        return 24;
    }

    public int method_32664(class_327 textRenderer) {
        return 60;
    }

    public void method_32666(class_327 textRenderer, int x, int y, int width, int height, class_332 context) {
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_46416(15.0f, 2.0f, 0.0f);
        this.entity.method_18800(1.0d, 1.0d, 1.0d);
        renderEntity(matrices, x, y);
        matrices.method_22909();
    }

    protected void renderEntity(class_4587 matrices, int x, int y) {
        if (MeteorClient.mc.field_1724 == null) {
            return;
        }
        float size = 24.0f;
        if (Math.max(this.entity.method_17681(), this.entity.method_17682()) > 1.0d) {
            size = 24.0f / Math.max(this.entity.method_17681(), this.entity.method_17682());
        }
        class_308.method_24210();
        matrices.method_22903();
        int yOffset = 16;
        if (this.entity instanceof class_1477) {
            size = 16.0f;
            yOffset = 2;
        }
        matrices.method_46416(x + 10, y + yOffset, 1050.0f);
        matrices.method_22905(1.0f, 1.0f, -1.0f);
        matrices.method_46416(0.0f, 0.0f, 1000.0f);
        matrices.method_22905(size, size, size);
        Quaternionf quaternion = class_7833.field_40718.rotationDegrees(180.0f);
        Quaternionf quaternion2 = class_7833.field_40714.rotationDegrees(-10.0f);
        hamiltonProduct(quaternion, quaternion2);
        matrices.method_22907(quaternion);
        setupAngles();
        class_898 entityRenderDispatcher = MeteorClient.mc.method_1561();
        quaternion2.conjugate();
        entityRenderDispatcher.method_24196(quaternion2);
        entityRenderDispatcher.method_3948(false);
        class_4597.class_4598 immediate = MeteorClient.mc.method_22940().method_23000();
        this.entity.field_6012 = MeteorClient.mc.field_1724.field_6012;
        this.entity.method_5880(false);
        entityRenderDispatcher.method_62424(this.entity, 0.0d, 0.0d, 0.0d, 1.0f, matrices, immediate, 15728880);
        immediate.method_22993();
        entityRenderDispatcher.method_3948(true);
        matrices.method_22909();
        class_308.method_24211();
    }

    public void hamiltonProduct(Quaternionf q, Quaternionf other) {
        float f = q.x();
        float g = q.y();
        float h = q.z();
        float i = q.w();
        float j = other.x();
        float k = other.y();
        float l = other.z();
        float m = other.w();
        q.x = (((i * j) + (f * m)) + (g * l)) - (h * k);
        q.y = ((i * k) - (f * l)) + (g * m) + (h * j);
        q.z = (((i * l) + (f * k)) - (g * j)) + (h * m);
        q.w = (((i * m) - (f * j)) - (g * k)) - (h * l);
    }

    protected void setupAngles() {
        float yaw = (System.currentTimeMillis() / 10.0f) % 360.0f;
        this.entity.method_36456(yaw);
        this.entity.method_5847(yaw);
        this.entity.method_36457(0.0f);
        class_1309 class_1309Var = this.entity;
        if (class_1309Var instanceof class_1309) {
            class_1309 livingEntity = class_1309Var;
            if (this.entity instanceof class_6053) {
                livingEntity.field_6241 = yaw;
            }
            livingEntity.field_6283 = yaw;
        }
    }
}
