package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import net.minecraft.class_10473;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/CompassStateMixin.class */
@Mixin({class_10473.class})
public abstract class CompassStateMixin {
    @ModifyExpressionValue(method = {"getBodyYaw"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getBodyYaw()F")})
    private static float callLivingEntityGetYaw(float original) {
        return Modules.get().isActive(Freecam.class) ? MeteorClient.mc.field_1773.method_19418().method_19330() : original;
    }

    @ModifyReturnValue(method = {"getAngleTo(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;)D"}, at = {@At("RETURN")})
    private static double modifyGetAngleTo(double original, class_1297 entity, class_2338 pos) {
        if (Modules.get().isActive(Freecam.class)) {
            class_243 vec3d = class_243.method_24953(pos);
            class_4184 camera = MeteorClient.mc.field_1773.method_19418();
            return Math.atan2(vec3d.method_10215() - camera.method_19326().field_1350, vec3d.method_10216() - camera.method_19326().field_1352) / 6.2831854820251465d;
        }
        return original;
    }
}
