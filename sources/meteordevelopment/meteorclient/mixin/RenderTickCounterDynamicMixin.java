package meteordevelopment.meteorclient.mixin;

import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/RenderTickCounterDynamicMixin.class */
@Mixin({class_9779.class_9781.class})
public abstract class RenderTickCounterDynamicMixin {

    @Shadow
    private float field_51958;

    @Inject(method = {"beginRenderTick(J)I"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;lastTimeMillis:J", opcode = Opcode.PUTFIELD)})
    private void onBeingRenderTick(long a, CallbackInfoReturnable<Integer> info) {
        this.field_51958 *= (float) ((Timer) Modules.get().get(Timer.class)).getMultiplier();
    }
}
