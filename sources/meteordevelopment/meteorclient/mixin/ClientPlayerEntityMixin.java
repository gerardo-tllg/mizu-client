package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.DropItemsEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerTickMovementEvent;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.meteorclient.systems.modules.movement.Scaffold;
import meteordevelopment.meteorclient.systems.modules.movement.Sneak;
import meteordevelopment.meteorclient.systems.modules.movement.Sprint;
import meteordevelopment.meteorclient.systems.modules.movement.Velocity;
import net.minecraft.class_437;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_744;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ClientPlayerEntityMixin.class */
@Mixin({class_746.class})
public abstract class ClientPlayerEntityMixin extends class_742 {

    @Shadow
    public class_744 field_3913;

    public ClientPlayerEntityMixin(class_638 world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = {"dropSelectedItem"}, at = {@At("HEAD")}, cancellable = true)
    private void onDropSelectedItem(boolean dropEntireStack, CallbackInfoReturnable<Boolean> info) {
        if (((DropItemsEvent) MeteorClient.EVENT_BUS.post(DropItemsEvent.get(method_6047()))).isCancelled()) {
            info.setReturnValue(false);
        }
    }

    @ModifyExpressionValue(method = {"tickNausea"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;")})
    private class_437 modifyNauseaCurrentScreen(class_437 original) {
        return original;
    }

    @ModifyExpressionValue(method = {"applyMovementSpeedFactors"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z")})
    private boolean redirectUsingItem(boolean isUsingItem) {
        if (((NoSlow) Modules.get().get(NoSlow.class)).items()) {
            return false;
        }
        return isUsingItem;
    }

    @Inject(method = {"isSneaking"}, at = {@At("HEAD")}, cancellable = true)
    private void onIsSneaking(CallbackInfoReturnable<Boolean> info) {
        if (((Scaffold) Modules.get().get(Scaffold.class)).scaffolding()) {
            info.setReturnValue(false);
        }
        if (((Flight) Modules.get().get(Flight.class)).noSneak()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = {"shouldSlowDown"}, at = {@At("HEAD")}, cancellable = true)
    private void onShouldSlowDown(CallbackInfoReturnable<Boolean> info) {
        if (((NoSlow) Modules.get().get(NoSlow.class)).sneaking()) {
            info.setReturnValue(Boolean.valueOf(method_20448()));
        }
    }

    @Inject(method = {"pushOutOfBlocks"}, at = {@At("HEAD")}, cancellable = true)
    private void onPushOutOfBlocks(double x, double d, CallbackInfo info) {
        Velocity velocity = (Velocity) Modules.get().get(Velocity.class);
        if (velocity.isActive() && velocity.blocks.get().booleanValue()) {
            info.cancel();
        }
    }

    @ModifyExpressionValue(method = {"canSprint"}, at = {@At(value = "CONSTANT", args = {"floatValue=6.0f"})})
    private float onHunger(float constant) {
        if (((NoSlow) Modules.get().get(NoSlow.class)).hunger()) {
            return -1.0f;
        }
        return constant;
    }

    @ModifyExpressionValue(method = {"sendSneakingPacket"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSneaking()Z")})
    private boolean isSneaking(boolean sneaking) {
        return ((Sneak) Modules.get().get(Sneak.class)).doPacket() || ((NoSlow) Modules.get().get(NoSlow.class)).airStrict() || sneaking;
    }

    @Inject(method = {"tickMovement"}, at = {@At("HEAD")})
    private void preTickMovement(CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(PlayerTickMovementEvent.get());
    }

    @ModifyExpressionValue(method = {"canStartSprinting"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;hasForwardMovement()Z")})
    private boolean modifyIsWalking(boolean original) {
        if (!((Sprint) Modules.get().get(Sprint.class)).rageSprint()) {
            return original;
        }
        float forwards = Math.abs(this.field_6250);
        float sideways = Math.abs(this.field_6212);
        return method_5869() ? forwards > 1.0E-5f || sideways > 1.0E-5f : ((double) forwards) > 0.8d || ((double) sideways) > 0.8d;
    }

    @ModifyExpressionValue(method = {"tickMovement"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;hasForwardMovement()Z")})
    private boolean modifyMovement(boolean original) {
        return !((Sprint) Modules.get().get(Sprint.class)).rageSprint() ? original : Math.abs(this.field_6212) > 1.0E-5f || Math.abs(this.field_6250) > 1.0E-5f;
    }

    @WrapWithCondition(method = {"tickMovement"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setSprinting(Z)V", ordinal = 3)})
    private boolean wrapSetSprinting(class_746 instance, boolean b) {
        Sprint s = (Sprint) Modules.get().get(Sprint.class);
        return !s.rageSprint() || (s.unsprintInWater() && method_5799());
    }

    @Inject(method = {"sendMovementPackets"}, at = {@At("HEAD")})
    private void onSendMovementPacketsHead(CallbackInfo info) {
        MeteorClient.EVENT_BUS.post(SendMovementPacketsEvent.Pre.get());
    }

    @Inject(method = {"tick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 1)})
    private void onTickHasVehicleBeforeSendPackets(CallbackInfo info) {
        MeteorClient.EVENT_BUS.post(SendMovementPacketsEvent.Pre.get());
    }

    @Inject(method = {"sendMovementPackets"}, at = {@At("TAIL")})
    private void onSendMovementPacketsTail(CallbackInfo info) {
        MeteorClient.EVENT_BUS.post(SendMovementPacketsEvent.Post.get());
    }

    @Inject(method = {"tick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 1, shift = At.Shift.AFTER)})
    private void onTickHasVehicleAfterSendPackets(CallbackInfo info) {
        MeteorClient.EVENT_BUS.post(SendMovementPacketsEvent.Post.get());
    }
}
