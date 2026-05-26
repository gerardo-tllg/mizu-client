package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorclient.mixininterface.IEntityRenderState;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.Hitboxes;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.class_10017;
import net.minecraft.class_10042;
import net.minecraft.class_10932;
import net.minecraft.class_10933;
import net.minecraft.class_1297;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4538;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_897;
import net.minecraft.class_898;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/EntityRenderDispatcherMixin.class */
@Mixin({class_898.class})
public abstract class EntityRenderDispatcherMixin {

    @Shadow
    public class_4184 field_4686;

    @Unique
    private static class_1297 renderHitboxEntity;

    @Inject(method = {"render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V"}, at = {@At("HEAD")}, cancellable = true)
    private <E extends class_1297, S extends class_10017> void render(E entity, double x, double y, double z, float tickDelta, class_4587 matrices, class_4597 vertexConsumers, int light, class_897<? super E, S> renderer, CallbackInfo info) {
        if (entity instanceof FakePlayerEntity) {
            FakePlayerEntity player = (FakePlayerEntity) entity;
            if (player.hideWhenInsideCamera) {
                int cX = class_3532.method_15357(this.field_4686.method_19326().field_1352);
                int cY = class_3532.method_15357(this.field_4686.method_19326().field_1351);
                int cZ = class_3532.method_15357(this.field_4686.method_19326().field_1350);
                if (cX == entity.method_31477() && cZ == entity.method_31479()) {
                    if (cY == entity.method_31478() || cY == entity.method_31478() + 1) {
                        info.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = {"renderHitboxes(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/client/render/entity/state/EntityHitboxAndView;Lnet/minecraft/client/render/VertexConsumerProvider;)V"}, at = {@At("HEAD")})
    private void renderHitboxes$setEntity(class_4587 matrices, class_10017 state, class_10933 hitbox, class_4597 vertexConsumers, CallbackInfo info) {
        renderHitboxEntity = ((IEntityRenderState) state).meteor$getEntity();
    }

    @Inject(method = {"renderHitboxes(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/client/render/entity/state/EntityHitboxAndView;Lnet/minecraft/client/render/VertexConsumerProvider;)V"}, at = {@At("TAIL")})
    private void renderHitboxes$unsetEntity(class_4587 matrices, class_10017 state, class_10933 hitbox, class_4597 vertexConsumers, CallbackInfo info) {
        renderHitboxEntity = null;
    }

    @Inject(method = {"renderHitbox"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER)})
    private static void renderHitbox$scale(class_4587 matrices, class_4588 vertexConsumer, class_10932 hitbox, CallbackInfo info) {
        if (renderHitboxEntity == null) {
            return;
        }
        double v = ((Hitboxes) Modules.get().get(Hitboxes.class)).getEntityValue(renderHitboxEntity);
        if (v == 0.0d) {
            return;
        }
        double v2 = v + 1.0d;
        matrices.method_22905((float) v2, (float) v2, (float) v2);
    }

    @ModifyExpressionValue(method = {"render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;getAndUpdateRenderState(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/entity/state/EntityRenderState;")})
    private <E extends class_1297, S extends class_10017> S render$getAndUpdateRenderState(S state, E entity, double x, double y, double z, float tickDelta, class_4587 matrices, class_4597 vertexConsumers, int light, class_897<? super E, S> renderer) {
        ((IEntityRenderState) state).meteor$setEntity(entity);
        return state;
    }

    @Inject(method = {"renderShadow"}, at = {@At("HEAD")}, cancellable = true)
    private static void onRenderShadow(class_4587 matrices, class_4597 vertexConsumers, class_10017 renderState, float opacity, class_4538 world, float radius, CallbackInfo info) {
        if (PostProcessShaders.rendering) {
            info.cancel();
        }
        if (((NoRender) Modules.get().get(NoRender.class)).noDeadEntities() && (renderState instanceof class_10042)) {
            class_10042 livingEntityRenderState = (class_10042) renderState;
            if (livingEntityRenderState.field_53449 > 0.0f) {
                info.cancel();
            }
        }
    }

    @Inject(method = {"getSquaredDistanceToCamera(Lnet/minecraft/entity/Entity;)D"}, at = {@At("HEAD")}, cancellable = true)
    private void onGetSquaredDistanceToCameraEntity(class_1297 entity, CallbackInfoReturnable<Double> info) {
        if (this.field_4686 == null) {
            info.setReturnValue(Double.valueOf(0.0d));
        }
    }

    @Inject(method = {"getSquaredDistanceToCamera(DDD)D"}, at = {@At("HEAD")}, cancellable = true)
    private void onGetSquaredDistanceToCameraXYZ(double x, double y, double z, CallbackInfoReturnable<Double> info) {
        if (this.field_4686 == null) {
            info.setReturnValue(Double.valueOf(0.0d));
        }
    }
}
