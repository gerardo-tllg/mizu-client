package meteordevelopment.meteorclient.mixin;

import it.unimi.dsi.fastutil.Pair;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import net.minecraft.class_2561;
import net.minecraft.class_412;
import net.minecraft.class_4185;
import net.minecraft.class_419;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_639;
import net.minecraft.class_642;
import net.minecraft.class_8667;
import net.minecraft.class_9112;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/DisconnectedScreenMixin.class */
@Mixin({class_419.class})
public abstract class DisconnectedScreenMixin extends class_437 {

    @Shadow
    @Final
    private class_8667 field_44552;

    @Unique
    private class_4185 reconnectBtn;

    @Unique
    private double time;

    protected DisconnectedScreenMixin(class_2561 title) {
        super(title);
        this.time = ((AutoReconnect) Modules.get().get(AutoReconnect.class)).time.get().doubleValue() * 20.0d;
    }

    @Inject(method = {"init"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/DirectionalLayoutWidget;refreshPositions()V", shift = At.Shift.BEFORE)})
    private void addButtons(CallbackInfo ci) {
        AutoReconnect autoReconnect = (AutoReconnect) Modules.get().get(AutoReconnect.class);
        if (autoReconnect.lastServerConnection != null && !autoReconnect.button.get().booleanValue()) {
            this.reconnectBtn = new class_4185.class_7840(class_2561.method_43470(getText()), button -> {
                tryConnecting();
            }).method_46431();
            this.field_44552.method_52736(this.reconnectBtn);
            this.field_44552.method_52736(new class_4185.class_7840(class_2561.method_43470("Toggle Auto Reconnect"), button2 -> {
                autoReconnect.toggle();
                this.reconnectBtn.method_25355(class_2561.method_43470(getText()));
                this.time = autoReconnect.time.get().doubleValue() * 20.0d;
            }).method_46431());
        }
    }

    public void method_25393() {
        AutoReconnect autoReconnect = (AutoReconnect) Modules.get().get(AutoReconnect.class);
        if (!autoReconnect.isActive() || autoReconnect.lastServerConnection == null) {
            return;
        }
        if (this.time <= 0.0d) {
            tryConnecting();
            return;
        }
        this.time -= 1.0d;
        if (this.reconnectBtn != null) {
            this.reconnectBtn.method_25355(class_2561.method_43470(getText()));
        }
    }

    @Unique
    private String getText() {
        String reconnectText;
        reconnectText = "Reconnect";
        return Modules.get().isActive(AutoReconnect.class) ? reconnectText + " " + String.format("(%.1f)", Double.valueOf(this.time / 20.0d)) : "Reconnect";
    }

    @Unique
    private void tryConnecting() {
        Pair<class_639, class_642> lastServer = ((AutoReconnect) Modules.get().get(AutoReconnect.class)).lastServerConnection;
        class_412.method_36877(new class_442(), MeteorClient.mc, (class_639) lastServer.left(), (class_642) lastServer.right(), false, (class_9112) null);
    }
}
