package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.DropItemsEvent;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.BlockBreakingCooldownEvent;
import meteordevelopment.meteorclient.events.entity.player.BreakBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractItemEvent;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.mixininterface.IClientPlayerInteractionManager;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.BreakDelay;
import meteordevelopment.meteorclient.systems.modules.player.SpeedMine;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2846;
import net.minecraft.class_3965;
import net.minecraft.class_634;
import net.minecraft.class_636;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ClientPlayerInteractionManagerMixin.class */
@Mixin({class_636.class})
public abstract class ClientPlayerInteractionManagerMixin implements IClientPlayerInteractionManager {

    @Shadow
    private int field_3716;

    @Shadow
    @Final
    private class_634 field_3720;

    @Shadow
    protected abstract void method_2911();

    @Shadow
    public abstract boolean method_2899(class_2338 class_2338Var);

    @Inject(method = {"clickSlot"}, at = {@At("HEAD")}, cancellable = true)
    private void onClickSlot(int syncId, int slotId, int button, class_1713 actionType, class_1657 player, CallbackInfo info) {
        if (actionType == class_1713.field_7795 && slotId >= 0 && slotId < player.field_7512.field_7761.size()) {
            if (((DropItemsEvent) MeteorClient.EVENT_BUS.post(DropItemsEvent.get(((class_1735) player.field_7512.field_7761.get(slotId)).method_7677()))).isCancelled()) {
                info.cancel();
            }
        } else {
            if (slotId != -999 || !((DropItemsEvent) MeteorClient.EVENT_BUS.post(DropItemsEvent.get(player.field_7512.method_34255()))).isCancelled()) {
                return;
            }
            info.cancel();
        }
    }

    @Inject(method = {"attackBlock"}, at = {@At("HEAD")}, cancellable = true)
    private void onAttackBlock(class_2338 blockPos, class_2350 direction, CallbackInfoReturnable<Boolean> info) {
        if (!((StartBreakingBlockEvent) MeteorClient.EVENT_BUS.post(StartBreakingBlockEvent.get(blockPos, direction))).isCancelled()) {
            SpeedMine sm = (SpeedMine) Modules.get().get(SpeedMine.class);
            class_2680 state = MeteorClient.mc.field_1687.method_8320(blockPos);
            if (sm.instamine() && sm.filter(state.method_26204()) && state.method_26165(MeteorClient.mc.field_1724, MeteorClient.mc.field_1687, blockPos) > 0.5f) {
                method_2899(blockPos);
                this.field_3720.method_52787(new class_2846(class_2846.class_2847.field_12968, blockPos, direction));
                this.field_3720.method_52787(new class_2846(class_2846.class_2847.field_12973, blockPos, direction));
                info.setReturnValue(true);
                return;
            }
            return;
        }
        info.cancel();
    }

    @Inject(method = {"interactBlock"}, at = {@At("HEAD")}, cancellable = true)
    public void interactBlock(class_746 player, class_1268 hand, class_3965 hitResult, CallbackInfoReturnable<class_1269> cir) {
        if (((InteractBlockEvent) MeteorClient.EVENT_BUS.post(InteractBlockEvent.get(player.method_6047().method_7960() ? class_1268.field_5810 : hand, hitResult))).isCancelled()) {
            cir.setReturnValue(class_1269.field_5814);
        }
    }

    @Inject(method = {"attackEntity"}, at = {@At("HEAD")}, cancellable = true)
    private void onAttackEntity(class_1657 player, class_1297 target, CallbackInfo info) {
        if (((AttackEntityEvent) MeteorClient.EVENT_BUS.post(AttackEntityEvent.get(target))).isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method = {"interactEntity"}, at = {@At("HEAD")}, cancellable = true)
    private void onInteractEntity(class_1657 player, class_1297 entity, class_1268 hand, CallbackInfoReturnable<class_1269> info) {
        if (((InteractEntityEvent) MeteorClient.EVENT_BUS.post(InteractEntityEvent.get(entity, hand))).isCancelled()) {
            info.setReturnValue(class_1269.field_5814);
        }
    }

    @Inject(method = {"dropCreativeStack"}, at = {@At("HEAD")}, cancellable = true)
    private void onDropCreativeStack(class_1799 stack, CallbackInfo info) {
        if (((DropItemsEvent) MeteorClient.EVENT_BUS.post(DropItemsEvent.get(stack))).isCancelled()) {
            info.cancel();
        }
    }

    @Redirect(method = {"updateBlockBreakingProgress"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode = Opcode.PUTFIELD, ordinal = 1))
    private void creativeBreakDelayChange(class_636 interactionManager, int value) {
        BlockBreakingCooldownEvent event = (BlockBreakingCooldownEvent) MeteorClient.EVENT_BUS.post(BlockBreakingCooldownEvent.get(value));
        this.field_3716 = event.cooldown;
    }

    @Redirect(method = {"updateBlockBreakingProgress"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode = Opcode.PUTFIELD, ordinal = 2))
    private void survivalBreakDelayChange(class_636 interactionManager, int value) {
        BlockBreakingCooldownEvent event = (BlockBreakingCooldownEvent) MeteorClient.EVENT_BUS.post(BlockBreakingCooldownEvent.get(value));
        this.field_3716 = event.cooldown;
    }

    @Redirect(method = {"attackBlock"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode = Opcode.PUTFIELD))
    private void creativeBreakDelayChange2(class_636 interactionManager, int value) {
        BlockBreakingCooldownEvent event = (BlockBreakingCooldownEvent) MeteorClient.EVENT_BUS.post(BlockBreakingCooldownEvent.get(value));
        this.field_3716 = event.cooldown;
    }

    @ModifyExpressionValue(method = {"method_41930"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F")})
    private float modifyBlockBreakingDelta(float original) {
        if (((BreakDelay) Modules.get().get(BreakDelay.class)).preventInstaBreak() && original >= 1.0f) {
            BlockBreakingCooldownEvent event = (BlockBreakingCooldownEvent) MeteorClient.EVENT_BUS.post(BlockBreakingCooldownEvent.get(this.field_3716));
            this.field_3716 = event.cooldown;
            return 0.0f;
        }
        return original;
    }

    @Inject(method = {"breakBlock"}, at = {@At("HEAD")}, cancellable = true)
    private void onBreakBlock(class_2338 blockPos, CallbackInfoReturnable<Boolean> info) {
        if (((BreakBlockEvent) MeteorClient.EVENT_BUS.post(BreakBlockEvent.get(blockPos))).isCancelled()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = {"interactItem"}, at = {@At("HEAD")}, cancellable = true)
    private void onInteractItem(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> info) {
        InteractItemEvent event = (InteractItemEvent) MeteorClient.EVENT_BUS.post(InteractItemEvent.get(hand));
        if (event.toReturn != null) {
            info.setReturnValue(event.toReturn);
        }
    }

    @Inject(method = {"cancelBlockBreaking"}, at = {@At("HEAD")}, cancellable = true)
    private void onCancelBlockBreaking(CallbackInfo info) {
        if (BlockUtils.breaking) {
            info.cancel();
        }
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IClientPlayerInteractionManager
    public void meteor$syncSelected() {
        method_2911();
    }
}
