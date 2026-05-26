package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_1920;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3486;
import net.minecraft.class_3610;
import net.minecraft.class_4588;
import net.minecraft.class_775;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/FluidRendererMixin.class */
@Mixin({class_775.class})
public abstract class FluidRendererMixin {

    @Unique
    private final ThreadLocal<Integer> alphas = new ThreadLocal<>();

    @Unique
    private final ThreadLocal<Boolean> ambient = ThreadLocal.withInitial(() -> {
        return false;
    });

    @Inject(method = {"render"}, at = {@At("HEAD")}, cancellable = true)
    private void onRender(class_1920 world, class_2338 pos, class_4588 vertexConsumer, class_2680 blockState, class_3610 fluidState, CallbackInfo info) {
        Ambience ambience = (Ambience) Modules.get().get(Ambience.class);
        this.ambient.set(Boolean.valueOf(ambience.isActive() && ambience.customLavaColor.get().booleanValue() && fluidState.method_15767(class_3486.field_15518)));
        int alpha = Xray.getAlpha(fluidState.method_15759(), pos);
        if (alpha != 0) {
            this.alphas.set(Integer.valueOf(alpha));
        } else {
            info.cancel();
        }
    }

    @Inject(method = {"vertex"}, at = {@At("HEAD")}, cancellable = true)
    private void onVertex(class_4588 vertexConsumer, float x, float y, float z, float red, float green, float blue, float u, float v, int light, CallbackInfo info) {
        int alpha = this.alphas.get().intValue();
        if (this.ambient.get().booleanValue()) {
            Color color = ((Ambience) Modules.get().get(Ambience.class)).lavaColor.get();
            vertex(vertexConsumer, x, y, z, color.r, color.g, color.b, alpha != -1 ? alpha : color.a, u, v, light);
            info.cancel();
        } else if (alpha != -1) {
            vertex(vertexConsumer, x, y, z, (int) (red * 255.0f), (int) (green * 255.0f), (int) (blue * 255.0f), alpha, u, v, light);
            info.cancel();
        }
    }

    @Unique
    private void vertex(class_4588 vertexConsumer, float x, float y, float z, int red, int green, int blue, int alpha, float u, float v, int light) {
        vertexConsumer.method_22912(x, y, z).method_1336(red, green, blue, alpha).method_22913(u, v).method_60803(light).method_22914(0.0f, 1.0f, 0.0f);
    }
}
