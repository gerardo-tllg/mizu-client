package meteordevelopment.meteorclient.gui.newgui.util;

import java.awt.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/util/ColorUtils.class */
public class ColorUtils {
    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 16777215);
    }

    public static int lerp(int from, int to, float progress) {
        int fa = (from >> 24) & 255;
        int fr = (from >> 16) & 255;
        int fg = (from >> 8) & 255;
        int fb = from & 255;
        int ta = (to >> 24) & 255;
        int tr = (to >> 16) & 255;
        int tg = (to >> 8) & 255;
        int tb = to & 255;
        int a = (int) (fa + ((ta - fa) * progress));
        int r = (int) (fr + ((tr - fr) * progress));
        int g = (int) (fg + ((tg - fg) * progress));
        int b = (int) (fb + ((tb - fb) * progress));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int rainbow(float offset) {
        float hue = ((System.currentTimeMillis() % 3000) / 3000.0f) + offset;
        return Color.HSBtoRGB(hue % 1.0f, 0.6f, 1.0f);
    }

    public static int brighter(int color, float factor) {
        int r = Math.min(255, (int) (((color >> 16) & 255) * factor));
        int g = Math.min(255, (int) (((color >> 8) & 255) * factor));
        int b = Math.min(255, (int) ((color & 255) * factor));
        return (color & (-16777216)) | (r << 16) | (g << 8) | b;
    }
}
