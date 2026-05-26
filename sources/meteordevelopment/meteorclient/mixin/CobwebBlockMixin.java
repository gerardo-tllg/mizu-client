package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import net.minecraft.class_10774;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2560;
import net.minecraft.class_2680;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/CobwebBlockMixin.class */
@Mixin({class_2560.class})
public abstract class CobwebBlockMixin {
    @Inject(method = {"onEntityCollision"}, at = {@At("HEAD")}, cancellable = true)
    private void onEntityCollision(class_2680 state, class_1937 world, class_2338 pos, class_1297 entity, class_10774 handler, CallbackInfo info) {
        if (entity != MeteorClient.mc.field_1724 || !((NoSlow) Modules.get().get(NoSlow.class)).cobweb()) {
            return;
        }
        info.cancel();
    }
}
