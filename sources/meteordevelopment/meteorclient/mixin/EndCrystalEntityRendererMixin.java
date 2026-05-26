package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Chams;
import net.minecraft.class_10014;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_892;
import net.minecraft.class_9946;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/EndCrystalEntityRendererMixin.class */
@Mixin({class_892.class})
public abstract class EndCrystalEntityRendererMixin {

    @Unique
    private Chams chams;

    @Shadow
    @Mutable
    @Final
    private static class_1921 field_21736;

    @Shadow
    @Final
    private static class_2960 field_4663;

    @Shadow
    @Final
    private class_9946 field_53187;

    @Inject(method = {"<init>"}, at = {@At("RETURN")})
    private void onInit(CallbackInfo info) {
        this.chams = (Chams) Modules.get().get(Chams.class);
    }

    @Inject(method = {"render(Lnet/minecraft/client/render/entity/state/EndCrystalEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at = {@At("HEAD")})
    private void render$renderLayer(class_10014 endCrystalEntityRenderState, class_4587 matrixStack, class_4597 vertexConsumerProvider, int i, CallbackInfo ci) {
        field_21736 = class_1921.method_23580((this.chams.isActive() && this.chams.crystals.get().booleanValue() && !this.chams.crystalsTexture.get().booleanValue()) ? Chams.BLANK : field_4663);
    }

    @Inject(method = {"render(Lnet/minecraft/client/render/entity/state/EndCrystalEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V")})
    private void render$scale(class_10014 endCrystalEntityRenderState, class_4587 matrixStack, class_4597 vertexConsumerProvider, int i, CallbackInfo info) {
        if (this.chams.isActive() && this.chams.crystals.get().booleanValue()) {
            float v = this.chams.crystalsScale.get().floatValue();
            matrixStack.method_22905(v, v, v);
        }
    }

    @WrapWithCondition(method = {"method_3908"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EndCrystalEntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V")})
    private boolean render$color(class_9946 instance, class_4587 matrices, class_4588 vertices, int light, int overlay) {
        if (this.chams.isActive() && this.chams.crystals.get().booleanValue()) {
            this.field_53187.method_62100(matrices, vertices, light, overlay, this.chams.crystalsCoreColor.get().getPacked());
            return false;
        }
        return true;
    }
}
