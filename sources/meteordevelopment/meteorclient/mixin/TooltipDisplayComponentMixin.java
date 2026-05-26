package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips;
import net.minecraft.class_10712;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/TooltipDisplayComponentMixin.class */
@Mixin({class_10712.class})
public abstract class TooltipDisplayComponentMixin {
    @ModifyExpressionValue(method = {"shouldDisplay"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/component/type/TooltipDisplayComponent;hideTooltip:Z")})
    private boolean modifyHideTooltip(boolean original) {
        return original && !((BetterTooltips) Modules.get().get(BetterTooltips.class)).tooltip.get().booleanValue();
    }

    @ModifyExpressionValue(method = {"shouldDisplay"}, at = {@At(value = "INVOKE", target = "Ljava/util/SequencedSet;contains(Ljava/lang/Object;)Z")})
    private boolean modifyHiddenComponents(boolean original) {
        return original && !((BetterTooltips) Modules.get().get(BetterTooltips.class)).additional.get().booleanValue();
    }
}
