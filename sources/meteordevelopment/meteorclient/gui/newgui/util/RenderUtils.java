package meteordevelopment.meteorclient.gui.newgui.util;

import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4587;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/util/RenderUtils.class */
public class RenderUtils {
    private static float getGuiScale() {
        return (float) class_310.method_1551().method_22683().method_4495();
    }

    public static void drawThinHLine(class_332 context, int x, int y, int length, int color) {
        float scale = getGuiScale();
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_22905(1.0f / scale, 1.0f / scale, 1.0f);
        int sx = (int) (x * scale);
        int sy = (int) (y * scale);
        int sw = (int) (length * scale);
        context.method_25294(sx, sy, sx + sw, sy + 1, color);
        matrices.method_22909();
    }

    public static void drawThinVLine(class_332 context, int x, int y, int length, int color) {
        float scale = getGuiScale();
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_22905(1.0f / scale, 1.0f / scale, 1.0f);
        int sx = (int) (x * scale);
        int sy = (int) (y * scale);
        int sh = (int) (length * scale);
        context.method_25294(sx, sy, sx + 1, sy + sh, color);
        matrices.method_22909();
    }

    public static void drawThinOutline(class_332 context, int x, int y, int w, int h, int color) {
        float scale = getGuiScale();
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_22905(1.0f / scale, 1.0f / scale, 1.0f);
        int sx = (int) (x * scale);
        int sy = (int) (y * scale);
        int sw = (int) (w * scale);
        int sh = (int) (h * scale);
        context.method_25294(sx, sy, sx + sw, sy + 1, color);
        context.method_25294(sx, (sy + sh) - 1, sx + sw, sy + sh, color);
        context.method_25294(sx, sy, sx + 1, sy + sh, color);
        context.method_25294((sx + sw) - 1, sy, sx + sw, sy + sh, color);
        matrices.method_22909();
    }

    public static void drawThickOutline(class_332 context, int x, int y, int w, int h, int thicknessPx, int color) {
        float scale = getGuiScale();
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_22905(1.0f / scale, 1.0f / scale, 1.0f);
        int sx = (int) (x * scale);
        int sy = (int) (y * scale);
        int sw = (int) (w * scale);
        int sh = (int) (h * scale);
        int t = Math.max(1, thicknessPx);
        context.method_25294(sx, sy, sx + sw, sy + t, color);
        context.method_25294(sx, (sy + sh) - t, sx + sw, sy + sh, color);
        context.method_25294(sx, sy, sx + t, sy + sh, color);
        context.method_25294((sx + sw) - t, sy, sx + sw, sy + sh, color);
        matrices.method_22909();
    }

    public static void fillNative(class_332 context, int x, int y, int w, int h, int color) {
        float scale = getGuiScale();
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_22905(1.0f / scale, 1.0f / scale, 1.0f);
        int sx = (int) (x * scale);
        int sy = (int) (y * scale);
        int sw = (int) (w * scale);
        int sh = (int) (h * scale);
        context.method_25294(sx, sy, sx + sw, sy + sh, color);
        matrices.method_22909();
    }

    public static void fillNativeHInset(class_332 context, int x, int y, int w, int h, int sideInsetPx, int color) {
        float scale = getGuiScale();
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_22905(1.0f / scale, 1.0f / scale, 1.0f);
        int sx = ((int) (x * scale)) + sideInsetPx;
        int sy = (int) (y * scale);
        int sw = ((int) (w * scale)) - (2 * sideInsetPx);
        int sh = (int) (h * scale);
        if (sw > 0 && sh > 0) {
            context.method_25294(sx, sy, sx + sw, sy + sh, color);
        }
        matrices.method_22909();
    }

    public static void drawRoundedRect(class_332 context, int x, int y, int width, int height, int radius, int color) {
        context.method_25294(x + radius, y, (x + width) - radius, y + height, color);
        context.method_25294(x, y + radius, x + radius, (y + height) - radius, color);
        context.method_25294((x + width) - radius, y + radius, x + width, (y + height) - radius, color);
        drawCorner(context, x, y, radius, color, true, true);
        drawCorner(context, (x + width) - radius, y, radius, color, false, true);
        drawCorner(context, x, (y + height) - radius, radius, color, true, false);
        drawCorner(context, (x + width) - radius, (y + height) - radius, radius, color, false, false);
    }

    private static void drawCorner(class_332 context, int x, int y, int radius, int color, boolean left, boolean top) {
        for (int cx = 0; cx < radius; cx++) {
            for (int cy = 0; cy < radius; cy++) {
                double dx = left ? ((double) (radius - cx)) - 0.5d : ((double) cx) + 0.5d;
                double dy = top ? ((double) (radius - cy)) - 0.5d : ((double) cy) + 0.5d;
                if ((dx * dx) + (dy * dy) <= ((double) radius) * ((double) radius)) {
                    context.method_25294(x + cx, y + cy, x + cx + 1, y + cy + 1, color);
                }
            }
        }
    }

    public static void drawHorizontalLine(class_332 context, int x, int y, int width, int color) {
        context.method_25294(x, y, x + width, y + 1, color);
    }

    public static void drawVerticalGradient(class_332 context, int x, int y, int width, int height, int colorTop, int colorBottom) {
        context.method_25296(x, y, x + width, y + height, colorTop, colorBottom);
    }

    public static void drawOutlineRect(class_332 context, int x, int y, int width, int height, int color) {
        context.method_25294(x, y, x + width, y + 1, color);
        context.method_25294(x, (y + height) - 1, x + width, y + height, color);
        context.method_25294(x, y, x + 1, y + height, color);
        context.method_25294((x + width) - 1, y, x + width, y + height, color);
    }
}
