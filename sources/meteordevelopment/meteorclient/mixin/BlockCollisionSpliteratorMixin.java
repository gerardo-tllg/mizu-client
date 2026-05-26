package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import net.minecraft.class_1941;
import net.minecraft.class_2338;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3726;
import net.minecraft.class_5329;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/BlockCollisionSpliteratorMixin.class */
@Mixin({class_5329.class})
public abstract class BlockCollisionSpliteratorMixin {
    @WrapOperation(method = {"computeNext"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/block/ShapeContext;getCollisionShape(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/CollisionView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;")})
    private class_265 onComputeNextCollisionBox(class_3726 instance, class_2680 blockState, class_1941 collisionView, class_2338 blockPos, Operation<class_265> original) {
        class_265 shape = (class_265) original.call(new Object[]{instance, blockState, collisionView, blockPos});
        if (collisionView != class_310.method_1551().field_1687) {
            return shape;
        }
        CollisionShapeEvent event = (CollisionShapeEvent) MeteorClient.EVENT_BUS.post(CollisionShapeEvent.get(blockState, blockPos, shape));
        return event.isCancelled() ? class_259.method_1073() : event.shape;
    }
}
