package meteordevelopment.meteorclient.mixin;

import com.mojang.authlib.GameProfile;
import java.time.Instant;
import meteordevelopment.meteorclient.mixininterface.IMessageHandler;
import net.minecraft.class_2556;
import net.minecraft.class_2561;
import net.minecraft.class_7471;
import net.minecraft.class_7594;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/MessageHandlerMixin.class */
@Mixin({class_7594.class})
public abstract class MessageHandlerMixin implements IMessageHandler {

    @Unique
    private GameProfile sender;

    @Inject(method = {"processChatMessageInternal"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", shift = At.Shift.BEFORE)})
    private void onProcessChatMessageInternal_beforeAddMessage(class_2556.class_7602 params, class_7471 message, class_2561 decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> info) {
        this.sender = sender;
    }

    @Inject(method = {"processChatMessageInternal"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", shift = At.Shift.AFTER)})
    private void onProcessChatMessageInternal_afterAddMessage(class_2556.class_7602 params, class_7471 message, class_2561 decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> info) {
        this.sender = null;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IMessageHandler
    public GameProfile meteor$getSender() {
        return this.sender;
    }
}
