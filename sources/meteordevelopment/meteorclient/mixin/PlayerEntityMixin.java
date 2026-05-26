package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.DropItemsEvent;
import meteordevelopment.meteorclient.events.entity.player.ClipAtLedgeEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.meteorclient.systems.modules.movement.Sprint;
import meteordevelopment.meteorclient.systems.modules.player.SpeedMine;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1542;
import net.minecraft.class_1656;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3965;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/PlayerEntityMixin.class */
@Mixin({class_1657.class})
public abstract class PlayerEntityMixin extends class_1309 {
    @Shadow
    public abstract class_1656 method_31549();

    protected PlayerEntityMixin(class_1299<? extends class_1309> entityType, class_1937 world) {
        super(entityType, world);
    }

    @Inject(method = {"clipAtLedge"}, at = {@At("HEAD")}, cancellable = true)
    protected void clipAtLedge(CallbackInfoReturnable<Boolean> info) {
        if (method_37908().field_9236) {
            ClipAtLedgeEvent event = (ClipAtLedgeEvent) MeteorClient.EVENT_BUS.post(ClipAtLedgeEvent.get());
            if (event.isSet()) {
                info.setReturnValue(Boolean.valueOf(event.isClip()));
            }
        }
    }

    @Inject(method = {"dropItem"}, at = {@At("HEAD")}, cancellable = true)
    private void onDropItem(class_1799 stack, boolean retainOwnership, CallbackInfoReturnable<class_1542> cir) {
        if (!method_37908().field_9236 || stack.method_7960() || !((DropItemsEvent) MeteorClient.EVENT_BUS.post(DropItemsEvent.get(stack))).isCancelled()) {
            return;
        }
        cir.setReturnValue((Object) null);
    }

    @Inject(method = {"isSpectator"}, at = {@At("HEAD")}, cancellable = true)
    private void onIsSpectator(CallbackInfoReturnable<Boolean> info) {
        if (MeteorClient.mc.method_1562() == null) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = {"isCreative"}, at = {@At("HEAD")}, cancellable = true)
    private void onIsCreative(CallbackInfoReturnable<Boolean> info) {
        if (MeteorClient.mc.method_1562() == null) {
            info.setReturnValue(false);
        }
    }

    @ModifyReturnValue(method = {"getBlockBreakingSpeed"}, at = {@At("RETURN")})
    public float onGetBlockBreakingSpeed(float breakSpeed, class_2680 block) {
        if (!method_37908().field_9236) {
            return breakSpeed;
        }
        SpeedMine speedMine = (SpeedMine) Modules.get().get(SpeedMine.class);
        if (!speedMine.isActive() || speedMine.mode.get() != SpeedMine.Mode.Normal || !speedMine.filter(block.method_26204())) {
            return breakSpeed;
        }
        float breakSpeedMod = (float) (((double) breakSpeed) * speedMine.modifier.get().doubleValue());
        class_3965 class_3965Var = MeteorClient.mc.field_1765;
        if (class_3965Var instanceof class_3965) {
            class_3965 bhr = class_3965Var;
            class_2338 pos = bhr.method_17777();
            if (speedMine.modifier.get().doubleValue() < 1.0d || BlockUtils.canInstaBreak(pos, breakSpeed) == BlockUtils.canInstaBreak(pos, breakSpeedMod)) {
                return breakSpeedMod;
            }
            return 0.9f / BlockUtils.calcBlockBreakingDelta2(pos, 1.0f);
        }
        return breakSpeed;
    }

    @ModifyReturnValue(method = {"getMovementSpeed"}, at = {@At("RETURN")})
    private float onGetMovementSpeed(float original) {
        if (method_37908().field_9236 && ((NoSlow) Modules.get().get(NoSlow.class)).slowness()) {
            float walkSpeed = method_31549().method_7253();
            if (original < walkSpeed) {
                return method_5624() ? (float) (((double) walkSpeed) * 1.300000011920929d) : walkSpeed;
            }
            return original;
        }
        return original;
    }

    @Inject(method = {"getOffGroundSpeed"}, at = {@At("HEAD")}, cancellable = true)
    private void onGetOffGroundSpeed(CallbackInfoReturnable<Float> info) {
        if (method_37908().field_9236) {
            float speed = ((Flight) Modules.get().get(Flight.class)).getOffGroundSpeed();
            if (speed != -1.0f) {
                info.setReturnValue(Float.valueOf(speed));
            }
        }
    }

    @WrapWithCondition(method = {"attack"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V")})
    private boolean keepSprint$setVelocity(class_1657 instance, class_243 vec3d) {
        return ((Sprint) Modules.get().get(Sprint.class)).stopSprinting();
    }

    @WrapWithCondition(method = {"attack"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V")})
    private boolean keepSprint$setSprinting(class_1657 instance, boolean b) {
        return ((Sprint) Modules.get().get(Sprint.class)).stopSprinting();
    }
}
