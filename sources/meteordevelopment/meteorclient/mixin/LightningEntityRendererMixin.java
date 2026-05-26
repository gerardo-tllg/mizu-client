package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_4588;
import net.minecraft.class_919;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/LightningEntityRendererMixin.class */
@Mixin({class_919.class})
public abstract class LightningEntityRendererMixin {
    @Inject(method = {"drawBranch"}, at = {@At("HEAD")}, cancellable = true)
    private static void onSetLightningVertex(Matrix4f matrix4f, class_4588 vertexConsumer, float f, float g, int i, float h, float j, float k, float l, float m, float n, float o, boolean bl, boolean bl2, boolean bl3, boolean bl4, CallbackInfo ci) {
        Ambience ambience = (Ambience) Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.changeLightningColor.get().booleanValue()) {
            Color color = ambience.lightningColor.get();
            vertexConsumer.method_22918(matrix4f, f + (bl ? o : -o), i * 16, g + (bl2 ? o : -o)).method_22915(color.r / 255.0f, color.g / 255.0f, color.b / 255.0f, 0.3f);
            vertexConsumer.method_22918(matrix4f, h + (bl ? n : -n), (i + 1) * 16, j + (bl2 ? n : -n)).method_22915(color.r / 255.0f, color.g / 255.0f, color.b / 255.0f, 0.3f);
            vertexConsumer.method_22918(matrix4f, h + (bl3 ? n : -n), (i + 1) * 16, j + (bl4 ? n : -n)).method_22915(color.r / 255.0f, color.g / 255.0f, color.b / 255.0f, 0.3f);
            vertexConsumer.method_22918(matrix4f, f + (bl3 ? o : -o), i * 16, g + (bl4 ? o : -o)).method_22915(color.r / 255.0f, color.g / 255.0f, color.b / 255.0f, 0.3f);
            ci.cancel();
        }
    }
}
