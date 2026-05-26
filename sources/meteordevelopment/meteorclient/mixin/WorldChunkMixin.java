package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/WorldChunkMixin.class */
@Mixin({class_2818.class})
public abstract class WorldChunkMixin {

    @Shadow
    @Final
    class_1937 field_12858;

    @Inject(method = {"setBlockState"}, at = {@At("TAIL")})
    private void onSetBlockState(class_2338 pos, class_2680 state, int flags, CallbackInfoReturnable<class_2680> info) {
        if (this.field_12858.field_9236) {
            MeteorClient.EVENT_BUS.post(BlockUpdateEvent.get(pos, (class_2680) info.getReturnValue(), state));
        }
    }
}
