package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.utils.tooltip.MeteorTooltipData;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5632;
import net.minecraft.class_5684;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/DrawContextMixin.class */
@Mixin({class_332.class})
public abstract class DrawContextMixin {
    @Inject(method = {"drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/util/Identifier;)V"}, at = {@At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V", shift = At.Shift.BEFORE)})
    private void onDrawTooltip(class_327 textRenderer, List<class_2561> text, Optional<class_5632> data, int x, int y, @Nullable class_2960 texture, CallbackInfo info, @Local(ordinal = 1) List<class_5684> list) {
        if (data.isPresent()) {
            class_5632 class_5632Var = data.get();
            if (class_5632Var instanceof MeteorTooltipData) {
                MeteorTooltipData meteorTooltipData = (MeteorTooltipData) class_5632Var;
                list.add(meteorTooltipData.getComponent());
            }
        }
    }

    @ModifyReceiver(method = {"drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/util/Identifier;)V"}, at = {@At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V")})
    private Optional<class_5632> onDrawTooltip_modifyIfPresentReceiver(Optional<class_5632> data, Consumer<class_5632> consumer) {
        return (data.isPresent() && (data.get() instanceof MeteorTooltipData)) ? Optional.empty() : data;
    }
}
