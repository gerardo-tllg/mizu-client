package meteordevelopment.meteorclient.utils.misc;

import java.util.Objects;
import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/Keybind.class */
public class Keybind implements ISerializable<Keybind>, ICopyable<Keybind> {
    private boolean isKey;
    private int value;
    private int modifiers;

    private Keybind(boolean isKey, int value, int modifiers) {
        set(isKey, value, modifiers);
    }

    public static Keybind none() {
        return new Keybind(true, -1, 0);
    }

    public static Keybind fromKey(int key) {
        return new Keybind(true, key, 0);
    }

    public static Keybind fromKeys(int key, int modifiers) {
        return new Keybind(true, key, modifiers);
    }

    public static Keybind fromButton(int button) {
        return new Keybind(false, button, 0);
    }

    public int getValue() {
        return this.value;
    }

    public boolean isSet() {
        return this.value != -1;
    }

    public boolean isKey() {
        return this.isKey;
    }

    public boolean hasMods() {
        return this.isKey && this.modifiers != 0;
    }

    public void set(boolean isKey, int value, int modifiers) {
        this.isKey = isKey;
        this.value = value;
        this.modifiers = modifiers;
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ICopyable
    public Keybind set(Keybind value) {
        this.isKey = value.isKey;
        this.value = value.value;
        this.modifiers = value.modifiers;
        return this;
    }

    public void reset() {
        set(true, -1, 0);
    }

    public boolean canBindTo(boolean isKey, int value, int modifiers) {
        return isKey ? ((modifiers != 0 && isKeyMod(value)) || value == -1 || value == 256) ? false : true : (value == 0 || value == 1) ? false : true;
    }

    public boolean matches(boolean isKey, int value, int modifiers) {
        if (isSet() && this.isKey == isKey) {
            return !hasMods() ? this.value == value : this.value == value && this.modifiers == modifiers;
        }
        return false;
    }

    public boolean isPressed() {
        return this.isKey ? modifiersPressed() && Input.isKeyPressed(this.value) : Input.isButtonPressed(this.value);
    }

    private boolean modifiersPressed() {
        if (hasMods()) {
            return isModPressed(2, TokenId.THROWS, TokenId.VOLATILE) && isModPressed(8, TokenId.TRY, TokenId.STRICT) && isModPressed(4, TokenId.TRANSIENT, TokenId.WHILE) && isModPressed(1, TokenId.THROW, TokenId.VOID);
        }
        return true;
    }

    private boolean isModPressed(int value, int... keys) {
        if ((this.modifiers & value) == 0) {
            return true;
        }
        for (int key : keys) {
            if (Input.isKeyPressed(key)) {
                return true;
            }
        }
        return false;
    }

    private boolean isKeyMod(int key) {
        return key >= 340 && key <= 347;
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ICopyable
    public Keybind copy() {
        return new Keybind(this.isKey, this.value, this.modifiers);
    }

    public String toString() {
        if (!isSet()) {
            return "None";
        }
        if (!this.isKey) {
            return Utils.getButtonName(this.value);
        }
        if (this.modifiers == 0) {
            return Utils.getKeyName(this.value);
        }
        StringBuilder label = new StringBuilder();
        if ((this.modifiers & 2) != 0) {
            label.append("Ctrl + ");
        }
        if ((this.modifiers & 8) != 0) {
            label.append("Cmd + ");
        }
        if ((this.modifiers & 4) != 0) {
            label.append("Alt + ");
        }
        if ((this.modifiers & 1) != 0) {
            label.append("Shift + ");
        }
        if ((this.modifiers & 16) != 0) {
            label.append("Caps Lock + ");
        }
        if ((this.modifiers & 32) != 0) {
            label.append("Num Lock + ");
        }
        label.append(Utils.getKeyName(this.value));
        return label.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Keybind keybind = (Keybind) o;
        return this.isKey == keybind.isKey && this.value == keybind.value && this.modifiers == keybind.modifiers;
    }

    public int hashCode() {
        return Objects.hash(Boolean.valueOf(this.isKey), Integer.valueOf(this.value), Integer.valueOf(this.modifiers));
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10556("isKey", this.isKey);
        tag.method_10569("value", this.value);
        tag.method_10569("modifiers", this.modifiers);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Keybind fromTag(class_2487 tag) {
        this.isKey = tag.method_68566("isKey", false);
        this.value = tag.method_68083("value", 0);
        this.modifiers = tag.method_68083("modifiers", 0);
        return this;
    }
}
