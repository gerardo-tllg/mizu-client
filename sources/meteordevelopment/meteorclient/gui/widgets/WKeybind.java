package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Keybind;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/WKeybind.class */
public class WKeybind extends WHorizontalList {
    public Runnable action;
    public Runnable actionOnSet;
    private WButton button;
    private final Keybind keybind;
    private final Keybind defaultValue;
    private boolean listening;

    public WKeybind(Keybind keybind, Keybind defaultValue) {
        this.keybind = keybind;
        this.defaultValue = defaultValue;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void init() {
        this.button = (WButton) add(this.theme.button("")).widget();
        this.button.action = () -> {
            this.listening = true;
            this.button.set("...");
            if (this.actionOnSet != null) {
                this.actionOnSet.run();
            }
        };
        refreshLabel();
    }

    public boolean onClear() {
        if (this.listening) {
            this.keybind.reset();
            reset();
            return true;
        }
        return false;
    }

    public boolean onAction(boolean isKey, int value, int modifiers) {
        if (this.listening && this.keybind.canBindTo(isKey, value, modifiers)) {
            this.keybind.set(isKey, value, modifiers);
            reset();
            return true;
        }
        return false;
    }

    public void resetBind() {
        this.keybind.set(this.defaultValue);
        reset();
    }

    public void reset() {
        this.listening = false;
        refreshLabel();
        if (Modules.get().isBinding()) {
            Modules.get().setModuleToBind(null);
        }
    }

    private void refreshLabel() {
        this.button.set(this.keybind.toString());
    }
}
