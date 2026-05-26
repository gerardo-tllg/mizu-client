package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Chams;
import net.minecraft.class_10014;
import net.minecraft.class_3532;
import net.minecraft.class_9946;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/EndCrystalEntityModelMixin.class */
@Mixin({class_9946.class})
public abstract class EndCrystalEntityModelMixin {
    @ModifyExpressionValue(method = {"method_62083"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EndCrystalEntityRenderer;getYOffset(F)F")})
    private float setAngles$bounce(float original, class_10014 state) {
        Chams module = (Chams) Modules.get().get(Chams.class);
        if (!module.isActive() || !module.crystals.get().booleanValue()) {
            return original;
        }
        float g = (class_3532.method_15374(state.field_53328 * 0.2f) / 2.0f) + 0.5f;
        return ((((g * g) + g) * 0.4f) * module.crystalsBounce.get().floatValue()) - 1.4f;
    }

    @ModifyExpressionValue(method = {"method_62083"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/EndCrystalEntityRenderState;age:F", ordinal = 0)})
    private float modifySpeed(float original) {
        Chams module = (Chams) Modules.get().get(Chams.class);
        return (module.isActive() && module.crystals.get().booleanValue()) ? original * module.crystalsRotationSpeed.get().floatValue() : original;
    }
}
