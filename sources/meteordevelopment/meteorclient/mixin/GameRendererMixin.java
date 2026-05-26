package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.GetFovEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.render.RenderAfterWorldEvent;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.LiquidInteract;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.systems.modules.render.Zoom;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import net.minecraft.class_10209;
import net.minecraft.class_1297;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_239;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_757;
import net.minecraft.class_9779;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/GameRendererMixin.class */
@Mixin({class_757.class})
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private class_310 field_4015;

    @Shadow
    @Final
    private class_4184 field_18765;

    @Unique
    private Renderer3D renderer;

    @Unique
    private Renderer3D depthRenderer;

    @Unique
    private final class_4587 matrices = new class_4587();

    @Unique
    private boolean freecamSet = false;

    @Shadow
    public abstract void method_3190(float f);

    @Shadow
    public abstract void method_3203();

    @Shadow
    protected abstract void method_3186(class_4587 class_4587Var, float f);

    @Shadow
    protected abstract void method_3198(class_4587 class_4587Var, float f);

    @Inject(method = {"renderWorld"}, at = {@At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = {"ldc=hand"})})
    private void onRenderWorld(class_9779 tickCounter, CallbackInfo ci, @Local(ordinal = 0) Matrix4f projection, @Local(ordinal = 2) Matrix4f view, @Local(ordinal = 1) float tickDelta, @Local class_4587 matrixStack) {
        if (Utils.canUpdate()) {
            class_10209.method_64146().method_15396("meteor-client_render");
            if (this.renderer == null) {
                this.renderer = new Renderer3D(MeteorRenderPipelines.WORLD_COLORED_LINES, MeteorRenderPipelines.WORLD_COLORED);
            }
            if (this.depthRenderer == null) {
                this.depthRenderer = new Renderer3D(MeteorRenderPipelines.WORLD_COLORED_LINES_DEPTH, MeteorRenderPipelines.WORLD_COLORED_DEPTH);
            }
            Render3DEvent event = Render3DEvent.get(matrixStack, this.renderer, this.depthRenderer, tickDelta, this.field_18765.method_19326().field_1352, this.field_18765.method_19326().field_1351, this.field_18765.method_19326().field_1350);
            RenderUtils.updateScreenCenter();
            NametagUtils.onRender(view);
            RenderSystem.getModelViewStack().pushMatrix().mul(view);
            this.matrices.method_22903();
            method_3198(this.matrices, this.field_18765.method_55437());
            if (((Boolean) this.field_4015.field_1690.method_42448().method_41753()).booleanValue()) {
                method_3186(this.matrices, this.field_18765.method_55437());
            }
            RenderSystem.getModelViewStack().mul(this.matrices.method_23760().method_23761().invert());
            this.matrices.method_22909();
            this.renderer.begin();
            this.depthRenderer.begin();
            MeteorClient.EVENT_BUS.post(event);
            this.renderer.render(matrixStack);
            this.depthRenderer.render(matrixStack);
            RenderSystem.getModelViewStack().popMatrix();
            class_10209.method_64146().method_15407();
        }
    }

    @Inject(method = {"renderWorld"}, at = {@At("TAIL")})
    private void onRenderWorldTail(CallbackInfo info) {
        MeteorClient.EVENT_BUS.post(RenderAfterWorldEvent.get());
    }

    @ModifyReturnValue(method = {"findCrosshairTarget"}, at = {@At("RETURN")})
    private class_239 onUpdateTargetedEntity(class_239 original, @Local class_239 hitResult) {
        return original;
    }

    @ModifyExpressionValue(method = {"findCrosshairTarget"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;")})
    private class_239 modifyRaycastResult(class_239 original, class_1297 entity, double blockInteractionRange, double entityInteractionRange, float tickProgress, @Local(ordinal = 0, argsOnly = true) double maxDistance) {
        if (Modules.get().isActive(LiquidInteract.class) && original.method_17783() == class_239.class_240.field_1333) {
            return entity.method_5745(maxDistance, tickProgress, true);
        }
        return original;
    }

    @Inject(method = {"tiltViewWhenHurt"}, at = {@At("HEAD")}, cancellable = true)
    private void onTiltViewWhenHurt(class_4587 matrices, float tickDelta, CallbackInfo ci) {
        if (((NoRender) Modules.get().get(NoRender.class)).noHurtCam()) {
            ci.cancel();
        }
    }

    @Inject(method = {"showFloatingItem"}, at = {@At("HEAD")}, cancellable = true)
    private void onShowFloatingItem(class_1799 floatingItem, CallbackInfo info) {
        if (floatingItem.method_7909() == class_1802.field_8288 && ((NoRender) Modules.get().get(NoRender.class)).noTotemAnimation()) {
            info.cancel();
        }
    }

    @ModifyExpressionValue(method = {"renderWorld"}, at = {@At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 0)})
    private float applyCameraTransformationsMathHelperLerpProxy(float original) {
        if (((NoRender) Modules.get().get(NoRender.class)).noNausea()) {
            return 0.0f;
        }
        return original;
    }

    @ModifyReturnValue(method = {"getFov"}, at = {@At("RETURN")})
    private float modifyFov(float original) {
        return ((GetFovEvent) MeteorClient.EVENT_BUS.post(GetFovEvent.get(original))).fov;
    }

    @Inject(method = {"updateCrosshairTarget"}, at = {@At("HEAD")}, cancellable = true)
    private void updateTargetedEntityInvoke(float tickDelta, CallbackInfo info) {
        Freecam freecam = (Freecam) Modules.get().get(Freecam.class);
        if (freecam.isActive() && this.field_4015.method_1560() != null && !this.freecamSet) {
            info.cancel();
            class_1297 cameraE = this.field_4015.method_1560();
            double x = cameraE.method_23317();
            double y = cameraE.method_23318();
            double z = cameraE.method_23321();
            double lastX = cameraE.field_6014;
            double lastY = cameraE.field_6036;
            double lastZ = cameraE.field_5969;
            float yaw = cameraE.method_36454();
            float pitch = cameraE.method_36455();
            float lastYaw = cameraE.field_5982;
            float lastPitch = cameraE.field_6004;
            cameraE.method_19538().meteor$set(freecam.pos.x, freecam.pos.y - ((double) cameraE.method_18381(cameraE.method_18376())), freecam.pos.z);
            cameraE.field_6014 = freecam.prevPos.x;
            cameraE.field_6036 = freecam.prevPos.y - ((double) cameraE.method_18381(cameraE.method_18376()));
            cameraE.field_5969 = freecam.prevPos.z;
            cameraE.method_36456(freecam.yaw);
            cameraE.method_36457(freecam.pitch);
            cameraE.field_5982 = freecam.prevYaw;
            cameraE.field_6004 = freecam.prevPitch;
            this.freecamSet = true;
            method_3190(tickDelta);
            this.freecamSet = false;
            cameraE.method_19538().meteor$set(x, y, z);
            cameraE.field_6014 = lastX;
            cameraE.field_6036 = lastY;
            cameraE.field_5969 = lastZ;
            cameraE.method_36456(yaw);
            cameraE.method_36457(pitch);
            cameraE.field_5982 = lastYaw;
            cameraE.field_6004 = lastPitch;
        }
    }

    @Inject(method = {"renderHand"}, at = {@At("HEAD")}, cancellable = true)
    private void renderHand(class_4184 camera, float tickDelta, Matrix4f matrix4f, CallbackInfo ci) {
        if (!((Freecam) Modules.get().get(Freecam.class)).renderHands() || !((Zoom) Modules.get().get(Zoom.class)).renderHands()) {
            ci.cancel();
        }
    }
}
