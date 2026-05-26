package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.class_10529;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/AbstractSignBlockEntityRendererMixin.class */
@Mixin({class_10529.class})
public abstract class AbstractSignBlockEntityRendererMixin {
    @ModifyExpressionValue(method = {"renderText"}, at = {@At(value = "CONSTANT", args = {"intValue=4", "ordinal=1"})})
    private int loopTextLengthProxy(int i) {
        if (((NoRender) Modules.get().get(NoRender.class)).noSignText()) {
            return 0;
        }
        return i;
    }
}
