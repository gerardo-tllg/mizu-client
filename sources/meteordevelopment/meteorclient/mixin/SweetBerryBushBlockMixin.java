package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import net.minecraft.class_10774;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3830;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/SweetBerryBushBlockMixin.class */
@Mixin({class_3830.class})
public abstract class SweetBerryBushBlockMixin {
    @Inject(method = {"onEntityCollision"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;slowMovement(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Vec3d;)V")}, cancellable = true)
    private void onEntityCollision(class_2680 state, class_1937 world, class_2338 pos, class_1297 entity, class_10774 handler, CallbackInfo info) {
        if (entity != MeteorClient.mc.field_1724 || !((NoSlow) Modules.get().get(NoSlow.class)).berryBush()) {
            return;
        }
        info.cancel();
    }
}
