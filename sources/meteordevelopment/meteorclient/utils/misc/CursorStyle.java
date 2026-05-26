package meteordevelopment.meteorclient.utils.misc;

import org.lwjgl.glfw.GLFW;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/CursorStyle.class */
public enum CursorStyle {
    Default,
    Click,
    Type;

    private boolean created;
    private long cursor;

    public long getGlfwCursor() {
        if (!this.created) {
            switch (ordinal()) {
                case 1:
                    this.cursor = GLFW.glfwCreateStandardCursor(221188);
                    break;
                case 2:
                    this.cursor = GLFW.glfwCreateStandardCursor(221186);
                    break;
            }
            this.created = true;
        }
        return this.cursor;
    }
}
