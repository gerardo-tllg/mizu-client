/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.render;

import net.minecraft.client.gui.DrawContext;

public class WaveRenderer {
    // Wave colors as packed ARGB
    private static final int WAVE_BACK  = 0xFF0a2a45;
    private static final int WAVE_MID   = 0xFF0d3a5e;
    private static final int WAVE_FRONT = 0xFF0f4a78;

    public static void renderWaves(DrawContext context, int screenWidth, int screenHeight, long timeMs) {
        int baseY = (int)(screenHeight * 0.8);
        double t = timeMs / 1000.0;

        // Wave 1 (back): amplitude 15, period 400, slow
        renderWave(context, screenWidth, screenHeight, baseY, 15, 400, t * 0.5, WAVE_BACK);
        // Wave 2 (mid): amplitude 12, period 320, medium
        renderWave(context, screenWidth, screenHeight, baseY - 4, 12, 320, t * 0.8, WAVE_MID);
        // Wave 3 (front): amplitude 8, period 240, fast
        renderWave(context, screenWidth, screenHeight, baseY - 8, 8, 240, t * 1.2, WAVE_FRONT);
    }

    private static void renderWave(DrawContext context, int screenWidth, int screenHeight,
                                   int baseY, int amplitude, int period, double phase, int color) {
        for (int x = 0; x < screenWidth; x++) {
            int waveTop = baseY + (int)(Math.sin((x / (double) period) * Math.PI * 2.0 + phase) * amplitude);
            context.fill(x, waveTop, x + 1, screenHeight, color);
        }
    }

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
