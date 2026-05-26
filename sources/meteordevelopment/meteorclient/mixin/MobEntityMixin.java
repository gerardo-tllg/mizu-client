package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.EntityControl;
import net.minecraft.class_1308;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/MobEntityMixin.class */
@Mixin({class_1308.class})
public abstract class MobEntityMixin {
    @ModifyReturnValue(method = {"hasSaddleEquipped"}, at = {@At("RETURN")})
    private boolean hasSaddleEquipped(boolean original) {
        return ((EntityControl) Modules.get().get(EntityControl.class)).isActive() || original;
    }
}
