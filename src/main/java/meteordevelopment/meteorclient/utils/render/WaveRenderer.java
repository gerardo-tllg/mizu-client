/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.render;

import net.minecraft.client.gui.DrawContext;

public class WaveRenderer {
    public static void renderRipple(DrawContext context, int cx, int cy, long startMs, long currentMs) {
        long elapsed = currentMs - startMs;
        long duration = 2000L;
        if (elapsed > duration) return;

        float progress = elapsed / (float) duration;
        int radius = (int)(progress * 120);
        int alpha = (int)((1.0f - progress) * 0.3f * 255);
        if (alpha <= 0) return;
        int color = (alpha << 24) | 0x1D9E75;
        drawCircleOutline(context, cx, cy, radius, color, 1);
    }

    public static void renderContinuousRipples(DrawContext context, int cx, int cy, long timeMs) {
        long period = 2000L;
        for (int i = 0; i < 3; i++) {
            long phase = (timeMs + i * (period / 3)) % period;
            float progress = phase / (float) period;
            int radius = (int)(progress * 120);
            int alpha = (int)((1.0f - progress) * 0.3f * 255);
            if (alpha <= 0) continue;
            int color = (alpha << 24) | 0x1D9E75;
            drawCircleOutline(context, cx, cy, radius, color, 1);
        }
    }

    private static void drawCircleOutline(DrawContext context, int cx, int cy, int radius, int color, int thickness) {
        if (radius <= 0) return;
        int x = radius, y = 0, err = 0;
        while (x >= y) {
            fillPoint(context, cx + x, cy + y, thickness, color);
            fillPoint(context, cx + y, cy + x, thickness, color);
            fillPoint(context, cx - y, cy + x, thickness, color);
            fillPoint(context, cx - x, cy + y, thickness, color);
            fillPoint(context, cx - x, cy - y, thickness, color);
            fillPoint(context, cx - y, cy - x, thickness, color);
            fillPoint(context, cx + y, cy - x, thickness, color);
            fillPoint(context, cx + x, cy - y, thickness, color);
            y++;
            if (err <= 0) err += 2 * y + 1;
            else { x--; err += 2 * (y - x) + 1; }
        }
    }

    private static void fillPoint(DrawContext context, int x, int y, int size, int color) {
        context.fill(x, y, x + size, y + size, color);
    }
}
