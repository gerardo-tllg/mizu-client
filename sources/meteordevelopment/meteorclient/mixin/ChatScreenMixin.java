package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import net.minecraft.class_342;
import net.minecraft.class_408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ChatScreenMixin.class */
@Mixin(value = {class_408.class}, priority = 1001)
public abstract class ChatScreenMixin {

    @Shadow
    protected class_342 field_2382;

    @Inject(method = {"init"}, at = {@At("RETURN")})
    private void onInit(CallbackInfo info) {
        if (((BetterChat) Modules.get().get(BetterChat.class)).isInfiniteChatBox()) {
            this.field_2382.method_1880(Integer.MAX_VALUE);
        }
    }
}
