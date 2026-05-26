package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Chams;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_630;
import net.minecraft.class_742;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/PlayerEntityRendererMixin.class */
@Mixin({class_1007.class})
public abstract class PlayerEntityRendererMixin {

    @Unique
    private Chams chams;

    @Inject(method = {"<init>"}, at = {@At("RETURN")})
    private void init$chams(CallbackInfo info) {
        this.chams = (Chams) Modules.get().get(Chams.class);
    }

    @Inject(method = {"updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V"}, at = {@At("RETURN")})
    private void updateRenderState$scale(class_742 player, class_10055 state, float f, CallbackInfo info) {
        if (this.chams.isActive() && this.chams.players.get().booleanValue()) {
            if (this.chams.ignoreSelf.get().booleanValue() && player == MeteorClient.mc.field_1724) {
                return;
            }
            float v = this.chams.playersScale.get().floatValue();
            state.field_53453 *= v;
            if (state.field_53338 != null) {
                state.field_53338.meteor$setY(state.field_53338.field_1351 + ((double) ((player.method_17682() * v) - player.method_17682())));
            }
        }
    }

    @ModifyExpressionValue(method = {"renderArm"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getEntityTranslucent(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;")})
    private class_1921 renderArm$texture(class_1921 original, class_4587 matrices, class_4597 vertexConsumers, int light, class_2960 skinTexture, class_630 arm, boolean sleeveVisible) {
        if (this.chams.isActive() && this.chams.hand.get().booleanValue()) {
            class_2960 texture = this.chams.handTexture.get().booleanValue() ? skinTexture : Chams.BLANK;
            return class_1921.method_23580(texture);
        }
        return original;
    }

    @WrapWithCondition(method = {"renderArm"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V")})
    private boolean renderArm$color(class_630 instance, class_4587 matrices, class_4588 vertices, int light, int overlay) {
        if (this.chams.isActive() && this.chams.hand.get().booleanValue()) {
            instance.method_22699(matrices, vertices, light, overlay, this.chams.handColor.get().getPacked());
            return false;
        }
        return true;
    }

    @Inject(method = {"updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V"}, at = {@At("RETURN")})
    private void updateRenderState$rotations(class_742 player, class_10055 state, float f, CallbackInfo info) {
        if (Rotations.rotating && player == MeteorClient.mc.field_1724) {
            state.field_53446 = Rotations.serverYaw;
            state.field_53448 = Rotations.serverPitch;
        }
    }
}
