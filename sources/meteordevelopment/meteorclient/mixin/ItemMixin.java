package meteordevelopment.meteorclient.mixin;

import java.util.Optional;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.TooltipDataEvent;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_5632;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ItemMixin.class */
@Mixin({class_1792.class})
public abstract class ItemMixin {
    @Inject(method = {"getTooltipData"}, at = {@At("HEAD")}, cancellable = true)
    private void onTooltipData(class_1799 stack, CallbackInfoReturnable<Optional<class_5632>> cir) {
        TooltipDataEvent event = (TooltipDataEvent) MeteorClient.EVENT_BUS.post(TooltipDataEvent.get(stack));
        if (event.tooltipData != null) {
            cir.setReturnValue(Optional.of(event.tooltipData));
        }
    }
}
