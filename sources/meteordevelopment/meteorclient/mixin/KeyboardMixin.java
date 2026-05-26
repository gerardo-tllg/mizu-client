package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.CharTypedEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.gui.GuiKeyEvents;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import net.minecraft.class_309;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/KeyboardMixin.class */
@Mixin({class_309.class})
public abstract class KeyboardMixin {

    @Shadow
    @Final
    private class_310 field_1678;

    @Inject(method = {"onKey"}, at = {@At("HEAD")}, cancellable = true)
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo info) {
        if (key != -1) {
            if (action == 1) {
                modifiers |= Input.getModifier(key);
            } else if (action == 0) {
                modifiers &= Input.getModifier(key) ^ (-1);
            }
            if ((this.field_1678.field_1755 instanceof WidgetScreen) && action == 2) {
                ((WidgetScreen) this.field_1678.field_1755).keyRepeated(key, modifiers);
            }
            if (GuiKeyEvents.canUseKeys) {
                Input.setKeyState(key, action != 0);
                if (((KeyEvent) MeteorClient.EVENT_BUS.post(KeyEvent.get(key, modifiers, KeyAction.get(action)))).isCancelled()) {
                    info.cancel();
                }
            }
        }
    }

    @Inject(method = {"onChar"}, at = {@At("HEAD")}, cancellable = true)
    private void onChar(long window, int i, int j, CallbackInfo info) {
        if (Utils.canUpdate() && !this.field_1678.method_1493()) {
            if ((this.field_1678.field_1755 == null || (this.field_1678.field_1755 instanceof WidgetScreen)) && ((CharTypedEvent) MeteorClient.EVENT_BUS.post(CharTypedEvent.get((char) i))).isCancelled()) {
                info.cancel();
            }
        }
    }
}
