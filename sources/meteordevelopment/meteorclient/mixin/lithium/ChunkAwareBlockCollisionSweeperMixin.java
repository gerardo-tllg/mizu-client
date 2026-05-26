package meteordevelopment.meteorclient.mixin.lithium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import net.caffeinemc.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/lithium/ChunkAwareBlockCollisionSweeperMixin.class */
@Mixin({ChunkAwareBlockCollisionSweeper.class})
public abstract class ChunkAwareBlockCollisionSweeperMixin {

    @Shadow
    @Final
    private class_1937 world;

    @Shadow
    @Final
    private class_2338.class_2339 pos;

    @ModifyExpressionValue(method = {"computeNext()Lnet/minecraft/util/shape/VoxelShape;"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/block/ShapeContext;getCollisionShape(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/CollisionView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;")})
    private class_265 modifyCollisionShape(class_265 original, @Local class_2680 state) {
        if (this.world != class_310.method_1551().field_1687) {
            return original;
        }
        CollisionShapeEvent event = (CollisionShapeEvent) MeteorClient.EVENT_BUS.post(CollisionShapeEvent.get(state, this.pos, original));
        return event.isCancelled() ? class_259.method_1073() : event.shape;
    }
}
