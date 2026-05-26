package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.class_10444;
import net.minecraft.class_918;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ItemRendererMixin.class */
@Mixin({class_918.class})
public abstract class ItemRendererMixin {
    @ModifyVariable(method = {"renderItem(Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II[ILjava/util/List;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/item/ItemRenderState$Glint;)V"}, at = @At("HEAD"), argsOnly = true)
    private static class_10444.class_10445 modifyEnchant(class_10444.class_10445 glint) {
        return ((NoRender) Modules.get().get(NoRender.class)).noEnchantGlint() ? class_10444.class_10445.field_55341 : glint;
    }
}
