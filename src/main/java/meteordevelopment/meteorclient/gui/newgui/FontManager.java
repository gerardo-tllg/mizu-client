/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui;

import meteordevelopment.meteorclient.gui.newgui.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Runtime holder for ClickGui theme (colors, fonts, animation flags).
 * Singleton. Session-local — values reset on game restart until a
 * Gui-configuration module is added to persist them.
 */
public class FontManager {
    private static FontManager instance;

    private float scale = 0.5f;
    private float guiScale = 0.8f;
    private String fontName = "Default";
    private Identifier fontId = null;

    // Theme colors — stored as 0xAARRGGBB (alpha byte preserved from user's SettingColor)
    private int primaryColor = 0xFF0A1E30;   // header/border: deep navy
    private int secondaryColor = 0xFF1D9E75; // active module highlight: teal
    private int textColor = 0xFFF0F0FA;      // primary text (active modules)
    private int textSecondary = 0xFF185FA5;  // secondary text (inactive modules)
    private boolean blur = true;
    private boolean animation = true;
    private boolean animationLTR = false;
    private int animationColor = 0xFF303030;
    private boolean textShadow = true;

    public static final String[] FONT_NAMES = {
        "Default", "Uniform", "Comfortaa", "Inter", "Lexend"
    };

    private static final Identifier[] FONT_IDS = {
        null,
        Identifier.of("minecraft", "uniform"),
        Identifier.of("meteor-client", "comfortaa"),
        Identifier.of("meteor-client", "inter"),
        Identifier.of("meteor-client", "lexend")
    };

    public static FontManager get() {
        if (instance == null) instance = new FontManager();
        return instance;
    }

    // --- Scale ---
    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = Math.max(0.3f, Math.min(1.5f, scale)); }
    public float getGuiScale() { return guiScale; }
    public void setGuiScale(float guiScale) { this.guiScale = Math.max(0.4f, Math.min(2.0f, guiScale)); }

    public int getRowHeight() { return (int) (12 * guiScale); }
    public int getHeaderHeight() { return (int) (12 * guiScale); }

    // --- Theme ---
    public int getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(int color) { this.primaryColor = color; }
    public int getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(int color) { this.secondaryColor = color; }
    public int getTextColor() { return textColor; }
    public void setTextColor(int color) { this.textColor = color; }
    public int getTextSecondary() { return textSecondary; }
    public void setTextSecondary(int color) { this.textSecondary = color; }
    public boolean isBlur() { return blur; }
    public void setBlur(boolean blur) { this.blur = blur; }
    public boolean isAnimation() { return animation; }
    public void setAnimation(boolean animation) { this.animation = animation; }
    public boolean isAnimationLTR() { return animationLTR; }
    public void setAnimationLTR(boolean ltr) { this.animationLTR = ltr; }
    public int getAnimationColor() { return animationColor; }
    public void setAnimationColor(int color) { this.animationColor = color; }
    public int animationAlpha(int alpha) {
        int userA = (animationColor >> 24) & 0xFF;
        int finalA = (userA * (alpha & 0xFF)) / 255;
        return (finalA << 24) | (animationColor & 0x00FFFFFF);
    }
    public boolean isTextShadow() { return textShadow; }
    public void setTextShadow(boolean textShadow) { this.textShadow = textShadow; }

    /** Primary color at a given alpha for lines. Multiplies by the user's stored alpha. */
    public int primaryAlpha(int alpha) {
        int userA = (primaryColor >> 24) & 0xFF;
        int finalA = (userA * (alpha & 0xFF)) / 255;
        return (finalA << 24) | (primaryColor & 0x00FFFFFF);
    }
    /** Secondary color at a given alpha for fills. Multiplies by the user's stored alpha. */
    public int secondaryAlpha(int alpha) {
        int userA = (secondaryColor >> 24) & 0xFF;
        int finalA = (userA * (alpha & 0xFF)) / 255;
        return (finalA << 24) | (secondaryColor & 0x00FFFFFF);
    }

    // --- Font ---
    public String getFontName() { return fontName; }

    public void setFont(String name) {
        for (int i = 0; i < FONT_NAMES.length; i++) {
            if (FONT_NAMES[i].equals(name)) {
                fontName = name;
                fontId = FONT_IDS[i];
                return;
            }
        }
    }

    public void cycleFont() {
        for (int i = 0; i < FONT_NAMES.length; i++) {
            if (FONT_NAMES[i].equals(fontName)) {
                int next = (i + 1) % FONT_NAMES.length;
                fontName = FONT_NAMES[next];
                fontId = FONT_IDS[next];
                return;
            }
        }
    }

    // --- Text ---
    public Text styledText(String text) {
        if (fontId == null) return Text.literal(text);
        return Text.literal(text).setStyle(Style.EMPTY.withFont(fontId));
    }

    public int getGuiTextWidth(String text) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        if (fontId == null) return (int) (tr.getWidth(text) * guiScale);
        return (int) (tr.getWidth(styledText(text)) * guiScale);
    }

    public int getTextWidth(String text) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        if (fontId == null) return (int) (tr.getWidth(text) * scale);
        return (int) (tr.getWidth(styledText(text)) * scale);
    }

    public int getTextHeight() {
        return (int) (8 * scale);
    }

    public void drawText(DrawContext context, String text, int x, int y, int color) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.scale(scale, scale, 1f);
        float invScale = 1f / scale;
        int dx = (int) (x * invScale);
        int dy = (int) (y * invScale);
        if (textShadow) {
            if (fontId == null) {
                context.drawTextWithShadow(tr, text, dx, dy, color);
            } else {
                context.drawTextWithShadow(tr, styledText(text), dx, dy, color);
            }
        } else {
            if (fontId == null) {
                context.drawText(tr, text, dx, dy, color, false);
            } else {
                context.drawText(tr, styledText(text), dx, dy, color, false);
            }
        }
        matrices.pop();
    }

    // Per-row hover-start timestamps so marquee starts at t=0 when hover begins.
    // Keyed by an opaque identity passed by the caller (usually the setting).
    private final java.util.WeakHashMap<Object, Long> marqueeStart = new java.util.WeakHashMap<>();

    /**
     * Draw text that auto-scrolls horizontally if it's wider than the available
     * space AND the row is currently hovered. Otherwise draws statically (the
     * caller's scissor clips overflow).
     *
     * Animation starts at t=0 when the row becomes hovered, so the text pauses
     * at the origin for 1.5s before scrolling left. Leaving hover resets to origin.
     *
     * @param id         identity key (typically the Setting or Module); used to track hover-start time
     * @param textStartX absolute x where the text starts
     * @param textEndX   absolute x of the right clip boundary
     * @param hovered    whether the mouse is over this row right now
     */
    public void drawTextMarquee(DrawContext context, Object id, String text, int textStartX, int y, int textEndX, int color, boolean hovered) {
        int textW = getTextWidth(text);
        int avail = textEndX - textStartX;

        if (!hovered) {
            // Not hovered — reset any stored start time and draw at origin.
            marqueeStart.remove(id);
            drawText(context, text, textStartX, y, color);
            return;
        }

        if (textW <= avail) {
            // Hovered but fits — no animation needed.
            drawText(context, text, textStartX, y, color);
            return;
        }

        // Record hover start if this is a fresh hover
        long now = System.currentTimeMillis();
        Long start = marqueeStart.get(id);
        if (start == null) {
            marqueeStart.put(id, now);
            start = now;
        }

        // Phases: pause-at-origin → scroll-left → pause-at-end → scroll-back → repeat
        int overflow = textW - avail;
        float pauseSec = 1.5f;
        float pxPerSec = 30f;
        float scrollSec = overflow / pxPerSec;
        float cycleSec = pauseSec + scrollSec + pauseSec + scrollSec;

        float elapsed = (now - start) / 1000f;
        float t = elapsed % cycleSec;

        float offset;
        if (t < pauseSec) {
            offset = 0;
        } else if (t < pauseSec + scrollSec) {
            offset = -((t - pauseSec) * pxPerSec);
        } else if (t < pauseSec + scrollSec + pauseSec) {
            offset = -overflow;
        } else {
            offset = -overflow + ((t - pauseSec - scrollSec - pauseSec) * pxPerSec);
        }

        drawText(context, text, textStartX + (int) offset, y, color);
    }

    /** Legacy 7-arg overload: uses the text string as identity (works but shared across rows with same text). */
    public void drawTextMarquee(DrawContext context, String text, int textStartX, int y, int textEndX, int color, boolean hovered) {
        drawTextMarquee(context, text, text, textStartX, y, textEndX, color, hovered);
    }
}
