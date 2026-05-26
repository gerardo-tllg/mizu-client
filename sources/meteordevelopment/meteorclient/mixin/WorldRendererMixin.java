package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import meteordevelopment.meteorclient.mixininterface.IWorldRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BlockSelection;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.postprocess.EntityShader;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_276;
import net.minecraft.class_2784;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4618;
import net.minecraft.class_757;
import net.minecraft.class_761;
import net.minecraft.class_9779;
import net.minecraft.class_9922;
import net.minecraft.class_9925;
import net.minecraft.class_9960;
import net.minecraft.class_9976;
import net.minecraft.class_9978;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/WorldRendererMixin.class */
@Mixin({class_761.class})
public abstract class WorldRendererMixin implements IWorldRenderer {

    @Unique
    private ESP esp;

    @Shadow
    private class_276 field_53080;

    @Shadow
    @Final
    private class_9960 field_53081;

    @Unique
    private Stack<class_276> framebufferStack;

    @Unique
    private Stack<class_9925<class_276>> framebufferHandleStack;

    @Shadow
    protected abstract void method_22977(class_1297 class_1297Var, double d, double d2, double d3, float f, class_4587 class_4587Var, class_4597 class_4597Var);

    @Inject(method = {"checkEmpty"}, at = {@At("HEAD")}, cancellable = true)
    private void onCheckEmpty(class_4587 matrixStack, CallbackInfo info) {
        info.cancel();
    }

    @Inject(method = {"drawBlockOutline"}, at = {@At("HEAD")}, cancellable = true)
    private void onDrawHighlightedBlockOutline(class_4587 matrices, class_4588 vertexConsumer, class_1297 entity, double cameraX, double cameraY, double cameraZ, class_2338 pos, class_2680 state, int i, CallbackInfo ci) {
        if (Modules.get().isActive(BlockSelection.class)) {
            ci.cancel();
        }
    }

    @ModifyArg(method = {"render"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"), index = 3)
    private boolean renderSetupTerrainModifyArg(boolean spectator) {
        return Modules.get().isActive(Freecam.class) || spectator;
    }

    @WrapWithCondition(method = {"method_62216"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/WeatherRendering;renderPrecipitation(Lnet/minecraft/world/World;Lnet/minecraft/client/render/VertexConsumerProvider;IFLnet/minecraft/util/math/Vec3d;)V")})
    private boolean shouldRenderPrecipitation(class_9976 instance, class_1937 world, class_4597 vertexConsumers, int ticks, float tickProgress, class_243 pos) {
        return !((NoRender) Modules.get().get(NoRender.class)).noWeather();
    }

    @WrapWithCondition(method = {"method_62216"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldBorderRendering;render(Lnet/minecraft/world/border/WorldBorder;Lnet/minecraft/util/math/Vec3d;DD)V")})
    private boolean shouldRenderWorldBorder(class_9978 instance, class_2784 border, class_243 cameraPos, double viewDistanceBlocks, double farPlaneDistance) {
        return !((NoRender) Modules.get().get(NoRender.class)).noWorldBorder();
    }

    @Inject(method = {"hasBlindnessOrDarkness(Lnet/minecraft/client/render/Camera;)Z"}, at = {@At("HEAD")}, cancellable = true)
    private void hasBlindnessOrDarkness(class_4184 camera, CallbackInfoReturnable<Boolean> info) {
        if (((NoRender) Modules.get().get(NoRender.class)).noBlindness() || ((NoRender) Modules.get().get(NoRender.class)).noDarkness()) {
            info.setReturnValue((Object) null);
        }
    }

    @Inject(method = {"render"}, at = {@At("HEAD")})
    private void onRenderHead(class_9922 allocator, class_9779 tickCounter, boolean renderBlockOutline, class_4184 camera, class_757 gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        PostProcessShaders.beginRender();
    }

    @Inject(method = {"renderEntity"}, at = {@At("HEAD")})
    private void renderEntity(class_1297 entity, double cameraX, double cameraY, double cameraZ, float tickDelta, class_4587 matrices, class_4597 vertexConsumers, CallbackInfo info) {
        draw(entity, cameraX, cameraY, cameraZ, tickDelta, vertexConsumers, matrices, PostProcessShaders.CHAMS, Color.WHITE);
        draw(entity, cameraX, cameraY, cameraZ, tickDelta, vertexConsumers, matrices, PostProcessShaders.ENTITY_OUTLINE, ((ESP) Modules.get().get(ESP.class)).getColor(entity));
    }

    @Unique
    private void draw(class_1297 entity, double cameraX, double cameraY, double cameraZ, float tickDelta, class_4597 vertexConsumers, class_4587 matrices, EntityShader shader, Color color) {
        if (shader.shouldDraw(entity) && !PostProcessShaders.isCustom(vertexConsumers) && color != null) {
            meteor$pushEntityOutlineFramebuffer(shader.framebuffer);
            PostProcessShaders.rendering = true;
            shader.vertexConsumerProvider.method_23286(color.r, color.g, color.b, color.a);
            method_22977(entity, cameraX, cameraY, cameraZ, tickDelta, matrices, shader.vertexConsumerProvider);
            PostProcessShaders.rendering = false;
            meteor$popEntityOutlineFramebuffer();
        }
    }

    @Inject(method = {"method_62214"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;draw()V")})
    private void onRender(CallbackInfo ci) {
        PostProcessShaders.endRender();
    }

    @WrapOperation(method = {"renderEntities"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;setColor(IIII)V")})
    private void setGlowColor(class_4618 instance, int red, int green, int blue, int alpha, Operation<Void> original, @Local LocalRef<class_1297> entity) {
        if (getESP().isGlow() && !getESP().shouldSkip((class_1297) entity.get())) {
            Color color = getESP().getColor((class_1297) entity.get());
            if (color != null) {
                instance.method_23286(color.r, color.g, color.b, color.a);
                return;
            } else {
                original.call(new Object[]{instance, Integer.valueOf(red), Integer.valueOf(green), Integer.valueOf(blue), Integer.valueOf(alpha)});
                return;
            }
        }
        original.call(new Object[]{instance, Integer.valueOf(red), Integer.valueOf(green), Integer.valueOf(blue), Integer.valueOf(alpha)});
    }

    @Inject(method = {"onResized"}, at = {@At("HEAD")})
    private void onResized(int width, int height, CallbackInfo info) {
        PostProcessShaders.onResized(width, height);
    }

    @Unique
    private ESP getESP() {
        if (this.esp == null) {
            this.esp = (ESP) Modules.get().get(ESP.class);
        }
        return this.esp;
    }

    @Inject(method = {"<init>"}, at = {@At("TAIL")})
    private void init$IWorldRenderer(CallbackInfo info) {
        this.framebufferStack = new ObjectArrayList();
        this.framebufferHandleStack = new ObjectArrayList();
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IWorldRenderer
    public void meteor$pushEntityOutlineFramebuffer(class_276 framebuffer) {
        this.framebufferStack.push(this.field_53080);
        this.field_53080 = framebuffer;
        this.framebufferHandleStack.push(this.field_53081.field_53097);
        this.field_53081.field_53097 = () -> {
            return framebuffer;
        };
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IWorldRenderer
    public void meteor$popEntityOutlineFramebuffer() {
        this.field_53080 = (class_276) this.framebufferStack.pop();
        this.field_53081.field_53097 = (class_9925) this.framebufferHandleStack.pop();
    }
}
