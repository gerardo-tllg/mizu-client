package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.CanWalkOnFluidEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Sprint;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightModes;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Bounce;
import meteordevelopment.meteorclient.systems.modules.player.NoStatusEffects;
import meteordevelopment.meteorclient.systems.modules.player.OffhandCrash;
import meteordevelopment.meteorclient.systems.modules.render.HandView;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.class_1268;
import net.minecraft.class_1291;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_3610;
import net.minecraft.class_6880;
import net.minecraft.class_9334;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/LivingEntityMixin.class */
@Mixin({class_1309.class})
public abstract class LivingEntityMixin extends class_1297 {

    @Unique
    private boolean previousElytra;

    public LivingEntityMixin(class_1299<?> type, class_1937 world) {
        super(type, world);
        this.previousElytra = false;
    }

    @ModifyReturnValue(method = {"canWalkOnFluid"}, at = {@At("RETURN")})
    private boolean onCanWalkOnFluid(boolean original, class_3610 fluidState) {
        if (this != MeteorClient.mc.field_1724) {
            return original;
        }
        CanWalkOnFluidEvent event = (CanWalkOnFluidEvent) MeteorClient.EVENT_BUS.post(CanWalkOnFluidEvent.get(fluidState));
        return event.walkOnFluid;
    }

    @Inject(method = {"spawnItemParticles"}, at = {@At("HEAD")}, cancellable = true)
    private void spawnItemParticles(class_1799 stack, int count, CallbackInfo info) {
        NoRender noRender = (NoRender) Modules.get().get(NoRender.class);
        if (!noRender.noEatParticles() || !stack.method_57353().method_57832(class_9334.field_50075)) {
            return;
        }
        info.cancel();
    }

    @Inject(method = {"onEquipStack"}, at = {@At("HEAD")}, cancellable = true)
    private void onEquipStack(class_1304 slot, class_1799 oldStack, class_1799 newStack, CallbackInfo info) {
        if (this == MeteorClient.mc.field_1724 && ((OffhandCrash) Modules.get().get(OffhandCrash.class)).isAntiCrash()) {
            info.cancel();
        }
    }

    @ModifyArg(method = {"swingHand(Lnet/minecraft/util/Hand;)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;swingHand(Lnet/minecraft/util/Hand;Z)V"))
    private class_1268 setHand(class_1268 hand) {
        if (this != MeteorClient.mc.field_1724) {
            return hand;
        }
        HandView handView = (HandView) Modules.get().get(HandView.class);
        if (handView.isActive()) {
            return handView.swingMode.get() == HandView.SwingMode.None ? hand : handView.swingMode.get() == HandView.SwingMode.Offhand ? class_1268.field_5810 : class_1268.field_5808;
        }
        return hand;
    }

    @ModifyExpressionValue(method = {"getHandSwingDuration"}, at = {@At(value = "CONSTANT", args = {"intValue=6"})})
    private int getHandSwingDuration(int original) {
        return this != MeteorClient.mc.field_1724 ? original : (((HandView) Modules.get().get(HandView.class)).isActive() && MeteorClient.mc.field_1690.method_31044().method_31034()) ? ((HandView) Modules.get().get(HandView.class)).swingSpeed.get().intValue() : original;
    }

    @ModifyReturnValue(method = {"isGliding"}, at = {@At("RETURN")})
    private boolean isGlidingHook(boolean original) {
        if (this != MeteorClient.mc.field_1724) {
            return original;
        }
        if (((ElytraFly) Modules.get().get(ElytraFly.class)).canPacketEfly()) {
            return true;
        }
        return original;
    }

    @Inject(method = {"isGliding"}, at = {@At("TAIL")}, cancellable = true)
    public void recastOnLand(CallbackInfoReturnable<Boolean> cir) {
        boolean elytra = ((Boolean) cir.getReturnValue()).booleanValue();
        ElytraFly elytraFly = (ElytraFly) Modules.get().get(ElytraFly.class);
        if (this.previousElytra && !elytra && elytraFly.isActive() && elytraFly.flightMode.get() == ElytraFlightModes.Bounce) {
            cir.setReturnValue(Boolean.valueOf(Bounce.recastElytra(MeteorClient.mc.field_1724)));
        }
        this.previousElytra = elytra;
    }

    @ModifyReturnValue(method = {"hasStatusEffect"}, at = {@At("RETURN")})
    private boolean hasStatusEffect(boolean original, class_6880<class_1291> effect) {
        if (((NoStatusEffects) Modules.get().get(NoStatusEffects.class)).shouldBlock((class_1291) effect.comp_349())) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = {"jump"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F")})
    private float modifyGetYaw(float original) {
        if (this == MeteorClient.mc.field_1724 && ((Sprint) Modules.get().get(Sprint.class)).rageSprint()) {
            float forward = Math.signum(MeteorClient.mc.field_1724.field_6250);
            float strafe = 90.0f * Math.signum(MeteorClient.mc.field_1724.field_6212);
            if (forward != 0.0f) {
                strafe *= forward * 0.5f;
            }
            float original2 = original - strafe;
            if (forward < 0.0f) {
                original2 -= 180.0f;
            }
            return original2;
        }
        return original;
    }

    @ModifyExpressionValue(method = {"jump"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z")})
    private boolean modifyIsSprinting(boolean original) {
        if (this == MeteorClient.mc.field_1724 && ((Sprint) Modules.get().get(Sprint.class)).rageSprint()) {
            return original && (Math.abs(MeteorClient.mc.field_1724.field_6250) > 1.0E-5f || Math.abs(MeteorClient.mc.field_1724.field_6212) > 1.0E-5f);
        }
        return original;
    }
}
