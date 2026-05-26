package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.utils.tooltip.MeteorTooltipData;
import net.minecraft.class_5632;
import net.minecraft.class_5684;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/TooltipComponentMixin.class */
@Mixin({class_5684.class})
public interface TooltipComponentMixin {
    @Inject(method = {"of(Lnet/minecraft/item/tooltip/TooltipData;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;"}, at = {@At("HEAD")}, cancellable = true)
    private static void shortcutMeteorTooltipData(class_5632 tooltipData, CallbackInfoReturnable<class_5684> cir) {
        if (tooltipData instanceof MeteorTooltipData) {
            cir.setReturnValue((Object) null);
        }
    }
}
