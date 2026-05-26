package meteordevelopment.meteorclient.utils.misc.input;

import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiKeyEvents;
import meteordevelopment.meteorclient.mixin.KeyBindingAccessor;
import meteordevelopment.meteorclient.utils.misc.CursorStyle;
import net.minecraft.class_304;
import org.lwjgl.glfw.GLFW;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/input/Input.class */
public class Input {
    private static final boolean[] keys = new boolean[512];
    private static final boolean[] buttons = new boolean[16];
    private static CursorStyle lastCursorStyle = CursorStyle.Default;

    private Input() {
    }

    public static void setKeyState(int key, boolean pressed) {
        if (key < 0 || key >= keys.length) {
            return;
        }
        keys[key] = pressed;
    }

    public static void setButtonState(int button, boolean pressed) {
        if (button < 0 || button >= buttons.length) {
            return;
        }
        buttons[button] = pressed;
    }

    public static int getKey(class_304 bind) {
        return ((KeyBindingAccessor) bind).getKey().method_1444();
    }

    public static void setKeyState(class_304 bind, boolean pressed) {
        setKeyState(getKey(bind), pressed);
    }

    public static boolean isPressed(class_304 bind) {
        return isKeyPressed(getKey(bind));
    }

    public static boolean isKeyPressed(int key) {
        return GuiKeyEvents.canUseKeys && key != -1 && key < keys.length && keys[key];
    }

    public static boolean isButtonPressed(int button) {
        return button != -1 && button < buttons.length && buttons[button];
    }

    public static void setCursorStyle(CursorStyle style) {
        if (lastCursorStyle != style) {
            GLFW.glfwSetCursor(MeteorClient.mc.method_22683().method_4490(), style.getGlfwCursor());
            lastCursorStyle = style;
        }
    }

    public static int getModifier(int key) {
        switch (key) {
            case TokenId.THROW /* 340 */:
            case TokenId.VOID /* 344 */:
                return 1;
            case TokenId.THROWS /* 341 */:
            case TokenId.VOLATILE /* 345 */:
                return 2;
            case TokenId.TRANSIENT /* 342 */:
            case TokenId.WHILE /* 346 */:
                return 4;
            case TokenId.TRY /* 343 */:
            case TokenId.STRICT /* 347 */:
                return 8;
            default:
                return 0;
        }
    }
}
