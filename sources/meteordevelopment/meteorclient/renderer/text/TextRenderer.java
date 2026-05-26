package meteordevelopment.meteorclient.renderer.text;

import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/text/TextRenderer.class */
public interface TextRenderer {
    void setAlpha(double d);

    void begin(double d, boolean z, boolean z2);

    double getWidth(String str, int i, boolean z);

    double getHeight(boolean z);

    double render(String str, double d, double d2, Color color, boolean z);

    boolean isBuilding();

    void end();

    static TextRenderer get() {
        return Config.get().customFont.get().booleanValue() ? Fonts.RENDERER : VanillaTextRenderer.INSTANCE;
    }

    default void begin(double scale) {
        begin(scale, false, false);
    }

    default void begin() {
        begin(1.0d, false, false);
    }

    default void beginBig() {
        begin(1.0d, false, true);
    }

    default double getWidth(String text, boolean shadow) {
        return getWidth(text, text.length(), shadow);
    }

    default double getWidth(String text) {
        return getWidth(text, text.length(), false);
    }

    default double getHeight() {
        return getHeight(false);
    }

    default double render(String text, double x, double y, Color color) {
        return render(text, x, y, color, false);
    }
}
