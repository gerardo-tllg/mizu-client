package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/KeybindSetting.class */
public class KeybindSetting extends Setting<Keybind> {
    private final Runnable action;
    public WKeybind widget;

    public KeybindSetting(String name, String description, Keybind defaultValue, Consumer<Keybind> onChanged, Consumer<Setting<Keybind>> onModuleActivated, IVisible visible, Runnable action) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.action = action;
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority = 200)
    private void onKeyBinding(KeyEvent event) {
        if (this.widget == null) {
            return;
        }
        if (event.action != KeyAction.Press || event.key != 256 || !this.widget.onClear()) {
            if (event.action != KeyAction.Release || !this.widget.onAction(true, event.key, event.modifiers)) {
                return;
            }
            event.cancel();
            return;
        }
        event.cancel();
    }

    @EventHandler(priority = 200)
    private void onMouseButtonBinding(MouseButtonEvent event) {
        if (event.action != KeyAction.Press || this.widget == null || !this.widget.onAction(false, event.button, 0)) {
            return;
        }
        event.cancel();
    }

    @EventHandler(priority = 100)
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Release && get().matches(true, event.key, event.modifiers)) {
            if ((this.module == null || this.module.isActive()) && this.action != null) {
                this.action.run();
            }
        }
    }

    @EventHandler(priority = 100)
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Release && get().matches(false, event.button, 0)) {
            if ((this.module == null || this.module.isActive()) && this.action != null) {
                this.action.run();
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v6, types: [T, meteordevelopment.meteorclient.utils.misc.Keybind] */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public void resetImpl() {
        if (this.value == 0) {
            this.value = ((Keybind) this.defaultValue).copy();
        } else {
            ((Keybind) this.value).set((Keybind) this.defaultValue);
        }
        if (this.widget != null) {
            this.widget.reset();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Keybind parseImpl(String str) {
        try {
            return Keybind.fromKey(Integer.parseInt(str.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(Keybind value) {
        return true;
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    public class_2487 save(class_2487 tag) {
        tag.method_10566("value", get().toTag());
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public Keybind load(class_2487 tag) {
        get().fromTag(tag.method_68568("value"));
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/KeybindSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, Keybind, KeybindSetting> {
        private Runnable action;

        public Builder() {
            super(Keybind.none());
        }

        public Builder action(Runnable action) {
            this.action = action;
            return this;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public KeybindSetting build() {
            return new KeybindSetting(this.name, this.description, (Keybind) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.action);
        }
    }
}
