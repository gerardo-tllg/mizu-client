package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_557;
import net.minecraft.class_828;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/EnchantingTableBlockEntityRendererMixin.class */
@Mixin({class_828.class})
public abstract class EnchantingTableBlockEntityRendererMixin {
    @WrapWithCondition(method = {"render(Lnet/minecraft/block/entity/EnchantingTableBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BookModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V")})
    private boolean onRenderBookModelRenderProxy(class_557 instance, class_4587 matrixStack, class_4588 vertexConsumer, int light, int overlay) {
        return !((NoRender) Modules.get().get(NoRender.class)).noEnchTableBook();
    }
}
