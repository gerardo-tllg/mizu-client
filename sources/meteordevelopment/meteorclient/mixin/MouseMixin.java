package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.meteor.MouseScrollEvent;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/MouseMixin.class */
@Mixin({class_312.class})
public abstract class MouseMixin {
    @Inject(method = {"onMouseButton"}, at = {@At("HEAD")}, cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo info) {
        Input.setButtonState(button, action != 0);
        if (((MouseButtonEvent) MeteorClient.EVENT_BUS.post(MouseButtonEvent.get(button, KeyAction.get(action)))).isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method = {"onMouseScroll"}, at = {@At("HEAD")}, cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo info) {
        if (((MouseScrollEvent) MeteorClient.EVENT_BUS.post(MouseScrollEvent.get(vertical))).isCancelled()) {
            info.cancel();
        }
    }
}
