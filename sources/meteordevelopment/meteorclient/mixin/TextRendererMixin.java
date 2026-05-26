package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/TextRendererMixin.class */
@Mixin(targets = {"net.minecraft.client.font.TextRenderer$Drawer"})
public abstract class TextRendererMixin {
    @ModifyExpressionValue(method = {"accept"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/text/Style;isObfuscated()Z")})
    private boolean onRenderObfuscatedStyle(boolean original) {
        if (Modules.get() == null || Modules.get().get(NoRender.class) == null) {
            return original;
        }
        return !((NoRender) Modules.get().get(NoRender.class)).noObfuscation() && original;
    }
}
