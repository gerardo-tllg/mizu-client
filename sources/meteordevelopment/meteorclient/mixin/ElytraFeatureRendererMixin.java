package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorclient.mixininterface.IEntityRenderState;
import meteordevelopment.meteorclient.utils.network.Capes;
import net.minecraft.class_10034;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_583;
import net.minecraft.class_979;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ElytraFeatureRendererMixin.class */
@Mixin({class_979.class})
public abstract class ElytraFeatureRendererMixin<S extends class_10034, M extends class_583<S>> extends class_3887<S, M> {
    public ElytraFeatureRendererMixin(class_3883<S, M> context) {
        super(context);
    }

    @ModifyExpressionValue(method = {"render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/ElytraFeatureRenderer;getTexture(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)Lnet/minecraft/util/Identifier;")})
    private class_2960 modifyCapeTexture(class_2960 original, class_4587 matrices, class_4597 consumers, int i, S state, float f, float g) {
        class_1657 class_1657VarMeteor$getEntity = ((IEntityRenderState) state).meteor$getEntity();
        if (class_1657VarMeteor$getEntity instanceof class_1657) {
            class_1657 player = class_1657VarMeteor$getEntity;
            class_2960 id = Capes.get(player);
            return id == null ? original : id;
        }
        return original;
    }
}
