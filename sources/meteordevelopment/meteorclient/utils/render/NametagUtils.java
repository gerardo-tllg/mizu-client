package meteordevelopment.meteorclient.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Zoom;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3d;
import org.joml.Vector4f;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/NametagUtils.class */
public class NametagUtils {
    private static final Vector4f vec4 = new Vector4f();
    private static final Vector4f mmMat4 = new Vector4f();
    private static final Vector4f pmMat4 = new Vector4f();
    private static final Vector3d camera = new Vector3d();
    private static final Vector3d cameraNegated = new Vector3d();
    private static final Matrix4f model = new Matrix4f();
    private static final Matrix4f projection = new Matrix4f();
    private static double windowScale;
    public static double scale;

    private NametagUtils() {
    }

    public static void onRender(Matrix4f modelView) {
        model.set(modelView);
        projection.set(RenderSystem.getProjectionMatrix());
        Utils.set(camera, MeteorClient.mc.field_1773.method_19418().method_19326());
        cameraNegated.set(camera);
        cameraNegated.negate();
        windowScale = MeteorClient.mc.method_22683().method_4476(1, false);
    }

    public static boolean to2D(Vector3d pos, double scale2) {
        return to2D(pos, scale2, true);
    }

    public static boolean to2D(Vector3d pos, double scale2, boolean distanceScaling) {
        return to2D(pos, scale2, distanceScaling, false);
    }

    public static boolean to2D(Vector3d pos, double scale2, boolean distanceScaling, boolean allowBehind) {
        Zoom zoom = (Zoom) Modules.get().get(Zoom.class);
        scale = scale2 * zoom.getScaling();
        if (distanceScaling) {
            scale *= getScale(pos);
        }
        vec4.set(cameraNegated.x + pos.x, cameraNegated.y + pos.y, cameraNegated.z + pos.z, 1.0d);
        vec4.mul(model, mmMat4);
        mmMat4.mul(projection, pmMat4);
        boolean behind = pmMat4.w <= 0.0f;
        if (behind && !allowBehind) {
            return false;
        }
        toScreen(pmMat4);
        double x = pmMat4.x * MeteorClient.mc.method_22683().method_4489();
        double y = pmMat4.y * MeteorClient.mc.method_22683().method_4506();
        if (behind) {
            x = ((double) MeteorClient.mc.method_22683().method_4489()) - x;
            y = ((double) MeteorClient.mc.method_22683().method_4506()) - y;
        }
        if (Double.isInfinite(x) || Double.isInfinite(y)) {
            return false;
        }
        pos.set(x / windowScale, ((double) MeteorClient.mc.method_22683().method_4506()) - (y / windowScale), allowBehind ? pmMat4.w : pmMat4.z);
        return true;
    }

    public static void begin(Vector3d pos) {
        Matrix4fStack matrices = RenderSystem.getModelViewStack();
        begin(matrices, pos);
    }

    public static void begin(Vector3d pos, class_332 drawContext) {
        begin(pos);
        class_4587 matrices = drawContext.method_51448();
        matrices.method_22903();
        matrices.method_46416((float) pos.x, (float) pos.y, 0.0f);
        matrices.method_22905((float) scale, (float) scale, 1.0f);
    }

    private static void begin(Matrix4fStack matrices, Vector3d pos) {
        matrices.pushMatrix();
        matrices.translate((float) pos.x, (float) pos.y, 0.0f);
        matrices.scale((float) scale, (float) scale, 1.0f);
    }

    public static void end() {
        RenderSystem.getModelViewStack().popMatrix();
    }

    public static void end(class_332 drawContext) {
        end();
        drawContext.method_51448().method_22909();
    }

    private static double getScale(Vector3d pos) {
        double dist = camera.distance(pos);
        return class_3532.method_15350(1.0d - (dist * 0.01d), 0.5d, 2.147483647E9d);
    }

    private static void toScreen(Vector4f vec) {
        float newW = (1.0f / vec.w) * 0.5f;
        vec.x = (vec.x * newW) + 0.5f;
        vec.y = (vec.y * newW) + 0.5f;
        vec.z = (vec.z * newW) + 0.5f;
        vec.w = newW;
    }
}
