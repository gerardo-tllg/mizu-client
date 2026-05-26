package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.mixininterface.IChatHudLine;
import meteordevelopment.meteorclient.mixininterface.IChatHudLineVisible;
import meteordevelopment.meteorclient.mixininterface.IMessageHandler;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.class_2561;
import net.minecraft.class_303;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_338;
import net.minecraft.class_5481;
import net.minecraft.class_7469;
import net.minecraft.class_7591;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ChatHudMixin.class */
@Mixin({class_338.class})
public abstract class ChatHudMixin implements IChatHud {

    @Shadow
    @Final
    private class_310 field_2062;

    @Shadow
    @Final
    private List<class_303.class_7590> field_2064;

    @Shadow
    @Final
    private List<class_303> field_2061;

    @Unique
    private BetterChat betterChat;

    @Unique
    private int nextId;

    @Unique
    private boolean skipOnAddMessage;

    @Shadow
    public abstract void method_44811(class_2561 class_2561Var, @Nullable class_7469 class_7469Var, @Nullable class_7591 class_7591Var);

    @Shadow
    public abstract void method_1812(class_2561 class_2561Var);

    @Override // meteordevelopment.meteorclient.mixininterface.IChatHud
    public void meteor$add(class_2561 message, int id) {
        this.nextId = id;
        method_1812(message);
        this.nextId = 0;
    }

    @Inject(method = {"addVisibleMessage"}, at = {@At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", shift = At.Shift.AFTER)})
    private void onAddMessageAfterNewChatHudLineVisible(class_303 message, CallbackInfo ci) {
        ((IChatHudLine) this.field_2064.getFirst()).meteor$setId(this.nextId);
    }

    @Inject(method = {"addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V"}, at = {@At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", shift = At.Shift.AFTER)})
    private void onAddMessageAfterNewChatHudLine(class_303 message, CallbackInfo ci) {
        ((IChatHudLine) this.field_2061.getFirst()).meteor$setId(this.nextId);
    }

    @ModifyExpressionValue(method = {"addVisibleMessage"}, at = {@At(value = "NEW", target = "(ILnet/minecraft/text/OrderedText;Lnet/minecraft/client/gui/hud/MessageIndicator;Z)Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;")})
    private class_303.class_7590 onAddMessage_modifyChatHudLineVisible(class_303.class_7590 line, @Local(ordinal = 1) int j) {
        IMessageHandler handler = this.field_2062.method_44714();
        if (handler == null) {
            return line;
        }
        IChatHudLineVisible meteorLine = (IChatHudLineVisible) line;
        meteorLine.meteor$setSender(handler.meteor$getSender());
        meteorLine.meteor$setStartOfEntry(j == 0);
        return line;
    }

    @ModifyExpressionValue(method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V"}, at = {@At(value = "NEW", target = "(ILnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)Lnet/minecraft/client/gui/hud/ChatHudLine;")})
    private class_303 onAddMessage_modifyChatHudLine(class_303 line) {
        IMessageHandler handler = this.field_2062.method_44714();
        if (handler == null) {
            return line;
        }
        ((IChatHudLine) line).meteor$setSender(handler.meteor$getSender());
        return line;
    }

    @Inject(at = {@At("HEAD")}, method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V"}, cancellable = true)
    private void onAddMessage(class_2561 message, class_7469 signatureData, class_7591 indicator, CallbackInfo ci) {
        if (this.skipOnAddMessage) {
            return;
        }
        ReceiveMessageEvent event = (ReceiveMessageEvent) MeteorClient.EVENT_BUS.post(ReceiveMessageEvent.get(message, indicator, this.nextId));
        if (!event.isCancelled()) {
            this.field_2064.removeIf(msg -> {
                return ((IChatHudLine) msg).meteor$getId() == this.nextId && this.nextId != 0;
            });
            for (int i = this.field_2061.size() - 1; i > -1; i--) {
                if (this.field_2061.get(i).meteor$getId() == this.nextId && this.nextId != 0) {
                    this.field_2061.remove(i);
                    getBetterChat().removeLine(i);
                }
            }
            if (event.isModified()) {
                ci.cancel();
                this.skipOnAddMessage = true;
                method_44811(event.getMessage(), signatureData, event.getIndicator());
                this.skipOnAddMessage = false;
                return;
            }
            return;
        }
        ci.cancel();
    }

    @ModifyExpressionValue(method = {"addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V"}, at = {@At(value = "CONSTANT", args = {"intValue=100"})})
    private int maxLength(int size) {
        return (Modules.get() == null || !getBetterChat().isLongerChat()) ? size : size + this.betterChat.getExtraChatLines();
    }

    @ModifyExpressionValue(method = {"addVisibleMessage"}, at = {@At(value = "CONSTANT", args = {"intValue=100"})})
    private int maxLengthVisible(int size) {
        return (Modules.get() == null || !getBetterChat().isLongerChat()) ? size : size + this.betterChat.getExtraChatLines();
    }

    @ModifyExpressionValue(method = {"render"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;ceil(F)I")})
    private int onRender_modifyWidth(int width) {
        return getBetterChat().modifyChatWidth(width);
    }

    @ModifyReceiver(method = {"render"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I")})
    private class_332 onRender_beforeDrawTextWithShadow(class_332 context, class_327 textRenderer, class_5481 text, int x, int y, int color, @Local class_303.class_7590 line) {
        getBetterChat().drawPlayerHead(context, line, y, color);
        return context;
    }

    @ModifyExpressionValue(method = {"render"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;indicator()Lnet/minecraft/client/gui/hud/MessageIndicator;")})
    private class_7591 onRender_modifyIndicator(class_7591 indicator) {
        if (((NoRender) Modules.get().get(NoRender.class)).noMessageSignatureIndicator()) {
            return null;
        }
        return indicator;
    }

    @Inject(method = {"addVisibleMessage"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;isChatFocused()Z")})
    private void onBreakChatMessageLines(class_303 message, CallbackInfo ci, @Local List<class_5481> list) {
        if (Modules.get() == null) {
            return;
        }
        getBetterChat().lines.addFirst(Integer.valueOf(list.size()));
    }

    @Inject(method = {"addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V"}, at = {@At(value = "INVOKE", target = "Ljava/util/List;remove(I)Ljava/lang/Object;")})
    private void onRemoveMessage(class_303 message, CallbackInfo ci) {
        if (Modules.get() == null) {
            return;
        }
        int extra = getBetterChat().isLongerChat() ? getBetterChat().getExtraChatLines() : 0;
        for (int size = this.betterChat.lines.size(); size > 100 + extra; size--) {
            this.betterChat.lines.removeLast();
        }
    }

    @Inject(method = {"clear"}, at = {@At("HEAD")})
    private void onClear(boolean clearHistory, CallbackInfo ci) {
        getBetterChat().lines.clear();
    }

    @Inject(method = {"refresh"}, at = {@At("HEAD")})
    private void onRefresh(CallbackInfo ci) {
        getBetterChat().lines.clear();
    }

    @Unique
    private BetterChat getBetterChat() {
        if (this.betterChat == null) {
            this.betterChat = (BetterChat) Modules.get().get(BetterChat.class);
        }
        return this.betterChat;
    }
}
