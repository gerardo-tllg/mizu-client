package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.LivingEntityMoveEvent;
import meteordevelopment.meteorclient.events.entity.player.JumpVelocityMultiplierEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.Hitboxes;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.movement.NoFall;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.meteorclient.systems.modules.movement.Velocity;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1313;
import net.minecraft.class_1657;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_243;
import net.minecraft.class_4050;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/EntityMixin.class */
@Mixin({class_1297.class})
public abstract class EntityMixin {
    @ModifyExpressionValue(method = {"updateMovementInFluid"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;getVelocity(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/Vec3d;")})
    private class_243 updateMovementInFluidFluidStateGetVelocity(class_243 vec) {
        if (this != MeteorClient.mc.field_1724) {
            return vec;
        }
        Velocity velocity = (Velocity) Modules.get().get(Velocity.class);
        if (velocity.isActive() && velocity.liquids.get().booleanValue()) {
            vec = vec.method_18805(velocity.getHorizontal(velocity.liquidsHorizontal), velocity.getVertical(velocity.liquidsVertical), velocity.getHorizontal(velocity.liquidsHorizontal));
        }
        return vec;
    }

    @Inject(method = {"isTouchingWater"}, at = {@At("HEAD")}, cancellable = true)
    private void isTouchingWater(CallbackInfoReturnable<Boolean> info) {
        if (this != MeteorClient.mc.field_1724) {
            return;
        }
        if (((Flight) Modules.get().get(Flight.class)).isActive()) {
            info.setReturnValue(false);
        }
        if (((NoSlow) Modules.get().get(NoSlow.class)).fluidDrag()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = {"isInLava"}, at = {@At("HEAD")}, cancellable = true)
    private void isInLava(CallbackInfoReturnable<Boolean> info) {
        if (this != MeteorClient.mc.field_1724) {
            return;
        }
        if (((Flight) Modules.get().get(Flight.class)).isActive()) {
            info.setReturnValue(false);
        }
        if (((NoSlow) Modules.get().get(NoSlow.class)).fluidDrag()) {
            info.setReturnValue(false);
        }
    }

    @ModifyExpressionValue(method = {"updateSwimming"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSubmergedInWater()Z")})
    private boolean isSubmergedInWater(boolean submerged) {
        if (this != MeteorClient.mc.field_1724) {
            return submerged;
        }
        if (((NoSlow) Modules.get().get(NoSlow.class)).fluidDrag() || ((Flight) Modules.get().get(Flight.class)).isActive()) {
            return false;
        }
        return submerged;
    }

    @ModifyArgs(method = {"pushAwayFrom(Lnet/minecraft/entity/Entity;)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    private void onPushAwayFrom(Args args, class_1297 entity) {
        Velocity velocity = (Velocity) Modules.get().get(Velocity.class);
        if (this == MeteorClient.mc.field_1724 && velocity.isActive() && velocity.entityPush.get().booleanValue()) {
            double multiplier = velocity.entityPushAmount.get().doubleValue();
            args.set(0, Double.valueOf(((Double) args.get(0)).doubleValue() * multiplier));
            args.set(2, Double.valueOf(((Double) args.get(2)).doubleValue() * multiplier));
        } else if (entity instanceof FakePlayerEntity) {
            FakePlayerEntity player = (FakePlayerEntity) entity;
            if (player.doNotPush) {
                args.set(0, Double.valueOf(0.0d));
                args.set(2, Double.valueOf(0.0d));
            }
        }
    }

    @ModifyReturnValue(method = {"getJumpVelocityMultiplier"}, at = {@At("RETURN")})
    private float onGetJumpVelocityMultiplier(float original) {
        if (this == MeteorClient.mc.field_1724) {
            JumpVelocityMultiplierEvent event = (JumpVelocityMultiplierEvent) MeteorClient.EVENT_BUS.post(JumpVelocityMultiplierEvent.get());
            return original * event.multiplier;
        }
        return original;
    }

    @Inject(method = {"move"}, at = {@At("HEAD")})
    private void onMove(class_1313 type, class_243 movement, CallbackInfo info) {
        if (this == MeteorClient.mc.field_1724) {
            MeteorClient.EVENT_BUS.post(PlayerMoveEvent.get(type, movement));
        } else if (this instanceof class_1309) {
            MeteorClient.EVENT_BUS.post(LivingEntityMoveEvent.get((class_1309) this, movement));
        }
    }

    @Inject(method = {"getTeamColorValue"}, at = {@At("HEAD")}, cancellable = true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> info) {
        Color color;
        if (!PostProcessShaders.rendering || (color = ((ESP) Modules.get().get(ESP.class)).getColor((class_1297) this)) == null) {
            return;
        }
        info.setReturnValue(Integer.valueOf(color.getPacked()));
    }

    @ModifyExpressionValue(method = {"getVelocityMultiplier"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;")})
    private class_2248 modifyVelocityMultiplierBlock(class_2248 original) {
        return this != MeteorClient.mc.field_1724 ? original : (original == class_2246.field_10114 && ((NoSlow) Modules.get().get(NoSlow.class)).soulSand()) ? class_2246.field_10340 : (original == class_2246.field_21211 && ((NoSlow) Modules.get().get(NoSlow.class)).honeyBlock()) ? class_2246.field_10340 : original;
    }

    @ModifyReturnValue(method = {"isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"}, at = {@At("RETURN")})
    private boolean isInvisibleToCanceller(boolean original) {
        if (!Utils.canUpdate()) {
            return original;
        }
        ESP esp = (ESP) Modules.get().get(ESP.class);
        if (((NoRender) Modules.get().get(NoRender.class)).noInvisibility()) {
            return false;
        }
        if (!esp.isActive() || esp.shouldSkip((class_1297) this)) {
            return original;
        }
        return false;
    }

    @Inject(method = {"isGlowing"}, at = {@At("HEAD")}, cancellable = true)
    private void isGlowing(CallbackInfoReturnable<Boolean> info) {
        if (((NoRender) Modules.get().get(NoRender.class)).noGlowing()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = {"getTargetingMargin"}, at = {@At("HEAD")}, cancellable = true)
    private void onGetTargetingMargin(CallbackInfoReturnable<Float> info) {
        double v = ((Hitboxes) Modules.get().get(Hitboxes.class)).getEntityValue((class_1297) this);
        if (v != 0.0d) {
            info.setReturnValue(Float.valueOf((float) v));
        }
    }

    @Inject(method = {"isInvisibleTo"}, at = {@At("HEAD")}, cancellable = true)
    private void onIsInvisibleTo(class_1657 player, CallbackInfoReturnable<Boolean> info) {
        if (player == null) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = {"getPose"}, at = {@At("HEAD")}, cancellable = true)
    private void getPoseHook(CallbackInfoReturnable<class_4050> info) {
        if (this == MeteorClient.mc.field_1724 && ((ElytraFly) Modules.get().get(ElytraFly.class)).canPacketEfly()) {
            info.setReturnValue(class_4050.field_18077);
        }
    }

    @ModifyReturnValue(method = {"getPose"}, at = {@At("RETURN")})
    private class_4050 modifyGetPose(class_4050 original) {
        return this != MeteorClient.mc.field_1724 ? original : (original == class_4050.field_18081 && !MeteorClient.mc.field_1724.method_5715() && MeteorClient.mc.field_1724.meteor$canChangeIntoPose(class_4050.field_18076)) ? class_4050.field_18076 : original;
    }

    @ModifyReturnValue(method = {"bypassesLandingEffects"}, at = {@At("RETURN")})
    private boolean cancelBounce(boolean original) {
        return ((NoFall) Modules.get().get(NoFall.class)).cancelBounce() || original;
    }

    @Inject(method = {"changeLookDirection"}, at = {@At("HEAD")}, cancellable = true)
    private void updateChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (this != MeteorClient.mc.field_1724) {
            return;
        }
        Freecam freecam = (Freecam) Modules.get().get(Freecam.class);
        FreeLook freeLook = (FreeLook) Modules.get().get(FreeLook.class);
        if (freecam.isActive()) {
            freecam.changeLookDirection(cursorDeltaX * 0.15d, cursorDeltaY * 0.15d);
            ci.cancel();
        } else if (freeLook.cameraMode()) {
            freeLook.cameraYaw += (float) (cursorDeltaX / ((double) freeLook.sensitivity.get().floatValue()));
            freeLook.cameraPitch += (float) (cursorDeltaY / ((double) freeLook.sensitivity.get().floatValue()));
            if (Math.abs(freeLook.cameraPitch) > 90.0f) {
                freeLook.cameraPitch = freeLook.cameraPitch > 0.0f ? 90.0f : -90.0f;
            }
            ci.cancel();
        }
    }
}
