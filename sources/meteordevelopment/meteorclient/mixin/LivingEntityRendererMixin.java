package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.IEntityRenderState;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Chams;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.class_10042;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_268;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_583;
import net.minecraft.class_746;
import net.minecraft.class_922;
import org.lwjgl.opengl.GL11C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/LivingEntityRendererMixin.class */
@Mixin({class_922.class})
public abstract class LivingEntityRendererMixin<T extends class_1309, S extends class_10042, M extends class_583<? super S>> {

    @Unique
    private Chams chams;

    @ModifyExpressionValue(method = {"hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;")})
    private class_1297 hasLabelGetCameraEntityProxy(class_1297 cameraEntity) {
        if (Modules.get().isActive(Freecam.class)) {
            return null;
        }
        return cameraEntity;
    }

    @ModifyExpressionValue(method = {"hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getScoreboardTeam()Lnet/minecraft/scoreboard/Team;")})
    private class_268 hasLabelClientPlayerEntityGetScoreboardTeamProxy(class_268 team) {
        if (MeteorClient.mc.field_1724 == null) {
            return null;
        }
        return team;
    }

    @Inject(method = {"<init>"}, at = {@At("RETURN")})
    private void init$chams(CallbackInfo info) {
        this.chams = (Chams) Modules.get().get(Chams.class);
    }

    @WrapWithCondition(method = {"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V")})
    private boolean render$render(M instance, class_4587 matrixStack, class_4588 vertexConsumer, int light, int overlay, int color, S state, class_4587 matrices, class_4597 consumers, int i) {
        if (!this.chams.isActive() || !this.chams.players.get().booleanValue()) {
            return true;
        }
        class_746 class_746VarMeteor$getEntity = ((IEntityRenderState) state).meteor$getEntity();
        if (!(class_746VarMeteor$getEntity instanceof class_1657)) {
            return true;
        }
        class_746 class_746Var = (class_1657) class_746VarMeteor$getEntity;
        if (this.chams.ignoreSelf.get().booleanValue() && class_746Var == MeteorClient.mc.field_1724) {
            return true;
        }
        instance.method_62100(matrixStack, vertexConsumer, light, overlay, PlayerUtils.getPlayerColor(class_746Var, this.chams.playersColor.get()).getPacked());
        return false;
    }

    @ModifyReturnValue(method = {"getRenderLayer"}, at = {@At("RETURN")})
    private class_1921 getRenderPlayer(class_1921 original, S state, boolean showBody, boolean translucent, boolean showOutline) {
        if (this.chams.isActive()) {
            class_746 class_746VarMeteor$getEntity = ((IEntityRenderState) state).meteor$getEntity();
            if (class_746VarMeteor$getEntity instanceof class_1657) {
                class_746 class_746Var = (class_1657) class_746VarMeteor$getEntity;
                if (!this.chams.players.get().booleanValue() || this.chams.playersTexture.get().booleanValue()) {
                    return original;
                }
                if (this.chams.ignoreSelf.get().booleanValue() && class_746Var == MeteorClient.mc.field_1724) {
                    return original;
                }
                return class_1921.method_29379(Chams.BLANK);
            }
        }
        return original;
    }

    @Inject(method = {"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at = {@At("HEAD")}, cancellable = true)
    private void render$Head(S state, class_4587 matrixStack, class_4597 vertexConsumerProvider, int i, CallbackInfo info) {
        class_1297 entity = ((IEntityRenderState) state).meteor$getEntity();
        if (entity instanceof class_1309) {
            class_1309 livingEntity = (class_1309) entity;
            if (((NoRender) Modules.get().get(NoRender.class)).noDeadEntities() && livingEntity.method_29504()) {
                info.cancel();
            }
            if (this.chams.shouldRender(entity)) {
                GL11C.glEnable(32823);
                GL11C.glPolygonOffset(1.0f, -1100000.0f);
            }
        }
    }

    @Inject(method = {"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at = {@At("TAIL")})
    private void render$Tail(S state, class_4587 matrixStack, class_4597 vertexConsumerProvider, int i, CallbackInfo info) {
        class_1297 entity = ((IEntityRenderState) state).meteor$getEntity();
        if (entity instanceof class_1309) {
            if (this.chams.shouldRender((class_1309) entity)) {
                GL11C.glPolygonOffset(1.0f, 1100000.0f);
                GL11C.glDisable(32823);
            }
        }
    }
}
