package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.ICamera;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.class_1297;
import net.minecraft.class_1922;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_5636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/CameraMixin.class */
@Mixin({class_4184.class})
public abstract class CameraMixin implements ICamera {

    @Shadow
    private boolean field_18719;

    @Shadow
    private float field_18718;

    @Shadow
    private float field_18717;

    @Unique
    private float tickDelta;

    @Shadow
    protected abstract void method_19325(float f, float f2);

    @Inject(method = {"getSubmersionType"}, at = {@At("HEAD")}, cancellable = true)
    private void getSubmergedFluidState(CallbackInfoReturnable<class_5636> ci) {
        if (((NoRender) Modules.get().get(NoRender.class)).noLiquidOverlay()) {
            ci.setReturnValue(class_5636.field_27888);
        }
    }

    @ModifyVariable(method = {"clipToSpace"}, at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float modifyClipToSpace(float d) {
        if (((Freecam) Modules.get().get(Freecam.class)).isActive()) {
            return 0.0f;
        }
        return d;
    }

    @Inject(method = {"update"}, at = {@At("HEAD")})
    private void onUpdateHead(class_1922 area, class_1297 focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {
        this.tickDelta = tickDelta;
    }

    @Inject(method = {"update"}, at = {@At("TAIL")})
    private void onUpdateTail(class_1922 area, class_1297 focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {
        if (Modules.get().isActive(Freecam.class)) {
            this.field_18719 = true;
        }
    }

    @ModifyArgs(method = {"update"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void onUpdateSetPosArgs(Args args) {
        Freecam freecam = (Freecam) Modules.get().get(Freecam.class);
        if (freecam.isActive()) {
            args.set(0, Double.valueOf(freecam.getX(this.tickDelta)));
            args.set(1, Double.valueOf(freecam.getY(this.tickDelta)));
            args.set(2, Double.valueOf(freecam.getZ(this.tickDelta)));
        }
    }

    @ModifyArgs(method = {"update"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void onUpdateSetRotationArgs(Args args) {
        Freecam freecam = (Freecam) Modules.get().get(Freecam.class);
        FreeLook freeLook = (FreeLook) Modules.get().get(FreeLook.class);
        if (freecam.isActive()) {
            args.set(0, Float.valueOf((float) freecam.getYaw(this.tickDelta)));
            args.set(1, Float.valueOf((float) freecam.getPitch(this.tickDelta)));
        } else if (freeLook.isActive()) {
            args.set(0, Float.valueOf(freeLook.cameraYaw));
            args.set(1, Float.valueOf(freeLook.cameraPitch));
        }
    }

    @Override // meteordevelopment.meteorclient.mixininterface.ICamera
    public void meteor$setRot(double yaw, double pitch) {
        method_19325((float) yaw, (float) class_3532.method_15350(pitch, -90.0d, 90.0d));
    }
}
