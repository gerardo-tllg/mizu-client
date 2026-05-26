package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.class_10378;
import net.minecraft.class_1088;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_2573;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_7833;
import net.minecraft.class_823;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/BannerBlockEntityRendererMixin.class */
@Mixin({class_823.class})
public abstract class BannerBlockEntityRendererMixin {
    @Shadow
    public abstract void method_3546(class_2573 class_2573Var, float f, class_4587 class_4587Var, class_4597 class_4597Var, int i, int i2, class_243 class_243Var);

    @Inject(method = {"render(Lnet/minecraft/block/entity/BannerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V"}, at = {@At("HEAD")}, cancellable = true)
    private void injectRender1(class_2573 bannerBlockEntity, float tickDelta, class_4587 matrixStack, class_4597 vertexConsumerProvider, int light, int overlay, class_243 vec3d, CallbackInfo ci) {
        if (((NoRender) Modules.get().get(NoRender.class)).getBannerRenderMode() == NoRender.BannerRenderMode.None) {
            ci.cancel();
        }
    }

    @Inject(method = {"render(Lnet/minecraft/block/entity/BannerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BannerBlockEntityRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIFLnet/minecraft/client/render/block/entity/model/BannerBlockModel;Lnet/minecraft/client/render/block/entity/model/BannerFlagBlockModel;FLnet/minecraft/util/DyeColor;Lnet/minecraft/component/type/BannerPatternsComponent;)V")}, cancellable = true)
    private void injectRender2(class_2573 bannerBlockEntity, float tickDelta, class_4587 matrixStack, class_4597 vertexConsumerProvider, int light, int overlay, class_243 vec3d, CallbackInfo ci, @Local(ordinal = 1) float rotation, @Local class_10378 bannerBlockModel) {
        if (((NoRender) Modules.get().get(NoRender.class)).getBannerRenderMode() == NoRender.BannerRenderMode.Pillar) {
            renderPillar(matrixStack, vertexConsumerProvider, light, overlay, rotation, bannerBlockModel);
            ci.cancel();
        }
    }

    @Unique
    private static void renderPillar(class_4587 matrices, class_4597 vertexConsumers, int light, int overlay, float rotation, class_10378 model) {
        matrices.method_22903();
        matrices.method_46416(0.5f, 0.0f, 0.5f);
        matrices.method_22907(class_7833.field_40716.rotationDegrees(rotation));
        matrices.method_22905(0.6666667f, -0.6666667f, -0.6666667f);
        model.method_60879(matrices, class_1088.field_20847.method_24145(vertexConsumers, class_1921::method_23572), light, overlay);
        matrices.method_22909();
    }
}
