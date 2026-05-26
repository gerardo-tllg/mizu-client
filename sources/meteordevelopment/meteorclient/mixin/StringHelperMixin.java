package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import net.minecraft.class_3544;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/StringHelperMixin.class */
@Mixin({class_3544.class})
public abstract class StringHelperMixin {
    @ModifyArg(method = {"truncateChat"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringHelper;truncate(Ljava/lang/String;IZ)Ljava/lang/String;"), index = 1)
    private static int injected(int maxLength) {
        if (((BetterChat) Modules.get().get(BetterChat.class)).isInfiniteChatBox()) {
            return Integer.MAX_VALUE;
        }
        return maxLength;
    }
}
