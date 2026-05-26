package meteordevelopment.meteorclient.gui.newgui.components;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.ModuleBindChangedEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/components/NewGuiBindCapture.class */
public final class NewGuiBindCapture {
    private static final NewGuiBindCapture INSTANCE = new NewGuiBindCapture();
    private static boolean subscribed = false;
    private KeybindSetting listeningSetting = null;

    private NewGuiBindCapture() {
    }

    public static NewGuiBindCapture get() {
        ensureSubscribed();
        return INSTANCE;
    }

    public static void ensureSubscribed() {
        if (subscribed) {
            return;
        }
        MeteorClient.EVENT_BUS.subscribe(INSTANCE);
        subscribed = true;
    }

    public void startListeningForSetting(KeybindSetting setting) {
        this.listeningSetting = setting;
    }

    public boolean isListeningForSetting() {
        return this.listeningSetting != null;
    }

    public KeybindSetting getListeningSetting() {
        return this.listeningSetting;
    }

    public void cancelSettingListen() {
        this.listeningSetting = null;
    }

    @EventHandler(priority = 200)
    private void onKey(KeyEvent event) {
        if (this.listeningSetting == null) {
            return;
        }
        if (event.action == KeyAction.Press && event.key == 256) {
            this.listeningSetting.get().set(true, -1, 0);
            this.listeningSetting = null;
            event.cancel();
        } else if (event.action == KeyAction.Release) {
            Keybind kb = this.listeningSetting.get();
            if (kb.canBindTo(true, event.key, event.modifiers)) {
                kb.set(true, event.key, event.modifiers);
                this.listeningSetting.onChanged();
            }
            this.listeningSetting = null;
            event.cancel();
        }
    }

    @EventHandler(priority = 200)
    private void onMouseButton(MouseButtonEvent event) {
        if (this.listeningSetting != null && event.action == KeyAction.Release) {
            Keybind kb = this.listeningSetting.get();
            if (kb.canBindTo(false, event.button, 0)) {
                kb.set(false, event.button, 0);
                this.listeningSetting.onChanged();
            }
            this.listeningSetting = null;
            event.cancel();
        }
    }

    @EventHandler
    private void onModuleBindChanged(ModuleBindChangedEvent event) {
        ModuleButton.clearKeyListeningIfMatches(event.module);
    }
}
