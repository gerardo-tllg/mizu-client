package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Velocity;
import net.minecraft.class_1297;
import net.minecraft.class_1536;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/FishingBobberEntityMixin.class */
@Mixin({class_1536.class})
public abstract class FishingBobberEntityMixin {
    @WrapOperation(method = {"handleStatus"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;pullHookedEntity(Lnet/minecraft/entity/Entity;)V")})
    private void preventFishingRodPull(class_1536 instance, class_1297 entity, Operation<Void> original) {
        if (!instance.method_37908().field_9236 || entity != MeteorClient.mc.field_1724) {
            original.call(new Object[]{instance, entity});
        }
        Velocity velocity = (Velocity) Modules.get().get(Velocity.class);
        if (!velocity.isActive() || !velocity.fishing.get().booleanValue()) {
            original.call(new Object[]{instance, entity});
        }
    }
}
