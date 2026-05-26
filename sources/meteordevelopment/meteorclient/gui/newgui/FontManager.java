package meteordevelopment.meteorclient.gui.newgui;

import java.util.WeakHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4587;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/FontManager.class */
public class FontManager {
    private static FontManager instance;
    public static final String[] FONT_NAMES = {"Default", "Uniform", "Comfortaa", "Inter", "Lexend"};
    private static final class_2960[] FONT_IDS = {null, class_2960.method_60655("minecraft", "uniform"), class_2960.method_60655(MeteorClient.MOD_ID, "comfortaa"), class_2960.method_60655(MeteorClient.MOD_ID, "inter"), class_2960.method_60655(MeteorClient.MOD_ID, "lexend")};
    private float scale = 0.5f;
    private float guiScale = 0.8f;
    private String fontName = "Default";
    private class_2960 fontId = null;
    private int primaryColor = -16777216;
    private int secondaryColor = -13619152;
    private int textColor = -986886;
    private int textSecondary = -7566171;
    private boolean blur = true;
    private boolean animation = true;
    private boolean animationLTR = false;
    private int animationColor = -13619152;
    private boolean textShadow = true;
    private final WeakHashMap<Object, Long> marqueeStart = new WeakHashMap<>();

    public static FontManager get() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }

    public float getScale() {
        return this.scale;
    }

    public void setScale(float scale) {
        this.scale = Math.max(0.3f, Math.min(1.5f, scale));
    }

    public float getGuiScale() {
        return this.guiScale;
    }

    public void setGuiScale(float guiScale) {
        this.guiScale = Math.max(0.4f, Math.min(2.0f, guiScale));
    }

    public int getRowHeight() {
        return (int) (12.0f * this.guiScale);
    }

    public int getHeaderHeight() {
        return (int) (12.0f * this.guiScale);
    }

    public int getPrimaryColor() {
        return this.primaryColor;
    }

    public void setPrimaryColor(int color) {
        this.primaryColor = color;
    }

    public int getSecondaryColor() {
        return this.secondaryColor;
    }

    public void setSecondaryColor(int color) {
        this.secondaryColor = color;
    }

    public int getTextColor() {
        return this.textColor;
    }

    public void setTextColor(int color) {
        this.textColor = color;
    }

    public int getTextSecondary() {
        return this.textSecondary;
    }

    public void setTextSecondary(int color) {
        this.textSecondary = color;
    }

    public boolean isBlur() {
        return this.blur;
    }

    public void setBlur(boolean blur) {
        this.blur = blur;
    }

    public boolean isAnimation() {
        return this.animation;
    }

    public void setAnimation(boolean animation) {
        this.animation = animation;
    }

    public boolean isAnimationLTR() {
        return this.animationLTR;
    }

    public void setAnimationLTR(boolean ltr) {
        this.animationLTR = ltr;
    }

    public int getAnimationColor() {
        return this.animationColor;
    }

    public void setAnimationColor(int color) {
        this.animationColor = color;
    }

    public int animationAlpha(int alpha) {
        int userA = (this.animationColor >> 24) & 255;
        int finalA = (userA * (alpha & 255)) / 255;
        return (finalA << 24) | (this.animationColor & 16777215);
    }

    public boolean isTextShadow() {
        return this.textShadow;
    }

    public void setTextShadow(boolean textShadow) {
        this.textShadow = textShadow;
    }

    public int primaryAlpha(int alpha) {
        int userA = (this.primaryColor >> 24) & 255;
        int finalA = (userA * (alpha & 255)) / 255;
        return (finalA << 24) | (this.primaryColor & 16777215);
    }

    public int secondaryAlpha(int alpha) {
        int userA = (this.secondaryColor >> 24) & 255;
        int finalA = (userA * (alpha & 255)) / 255;
        return (finalA << 24) | (this.secondaryColor & 16777215);
    }

    public String getFontName() {
        return this.fontName;
    }

    public void setFont(String name) {
        for (int i = 0; i < FONT_NAMES.length; i++) {
            if (FONT_NAMES[i].equals(name)) {
                this.fontName = name;
                this.fontId = FONT_IDS[i];
                return;
            }
        }
    }

    public void cycleFont() {
        for (int i = 0; i < FONT_NAMES.length; i++) {
            if (FONT_NAMES[i].equals(this.fontName)) {
                int next = (i + 1) % FONT_NAMES.length;
                this.fontName = FONT_NAMES[next];
                this.fontId = FONT_IDS[next];
                return;
            }
        }
    }

    public class_2561 styledText(String text) {
        return this.fontId == null ? class_2561.method_43470(text) : class_2561.method_43470(text).method_10862(class_2583.field_24360.method_27704(this.fontId));
    }

    public int getGuiTextWidth(String text) {
        class_327 tr = class_310.method_1551().field_1772;
        return this.fontId == null ? (int) (tr.method_1727(text) * this.guiScale) : (int) (tr.method_27525(styledText(text)) * this.guiScale);
    }

    public int getTextWidth(String text) {
        class_327 tr = class_310.method_1551().field_1772;
        return this.fontId == null ? (int) (tr.method_1727(text) * this.scale) : (int) (tr.method_27525(styledText(text)) * this.scale);
    }

    public int getTextHeight() {
        return (int) (8.0f * this.scale);
    }

    public void drawText(class_332 context, String text, int x, int y, int color) {
        class_327 tr = class_310.method_1551().field_1772;
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_22905(this.scale, this.scale, 1.0f);
        float invScale = 1.0f / this.scale;
        int dx = (int) (x * invScale);
        int dy = (int) (y * invScale);
        if (this.textShadow) {
            if (this.fontId == null) {
                context.method_25303(tr, text, dx, dy, color);
            } else {
                context.method_27535(tr, styledText(text), dx, dy, color);
            }
        } else if (this.fontId == null) {
            context.method_51433(tr, text, dx, dy, color, false);
        } else {
            context.method_51439(tr, styledText(text), dx, dy, color, false);
        }
        matrices.method_22909();
    }

    public void drawTextMarquee(class_332 context, Object id, String text, int textStartX, int y, int textEndX, int color, boolean hovered) {
        float offset;
        int textW = getTextWidth(text);
        int avail = textEndX - textStartX;
        if (!hovered) {
            this.marqueeStart.remove(id);
            drawText(context, text, textStartX, y, color);
            return;
        }
        if (textW <= avail) {
            drawText(context, text, textStartX, y, color);
            return;
        }
        long now = System.currentTimeMillis();
        Long start = this.marqueeStart.get(id);
        if (start == null) {
            this.marqueeStart.put(id, Long.valueOf(now));
            start = Long.valueOf(now);
        }
        int overflow = textW - avail;
        float scrollSec = overflow / 30.0f;
        float cycleSec = 1.5f + scrollSec + 1.5f + scrollSec;
        float elapsed = (now - start.longValue()) / 1000.0f;
        float t = elapsed % cycleSec;
        if (t < 1.5f) {
            offset = 0.0f;
        } else if (t < 1.5f + scrollSec) {
            offset = -((t - 1.5f) * 30.0f);
        } else {
            offset = t < (1.5f + scrollSec) + 1.5f ? -overflow : (-overflow) + ((((t - 1.5f) - scrollSec) - 1.5f) * 30.0f);
        }
        drawText(context, text, textStartX + ((int) offset), y, color);
    }

    public void drawTextMarquee(class_332 context, String text, int textStartX, int y, int textEndX, int color, boolean hovered) {
        drawTextMarquee(context, text, text, textStartX, y, textEndX, color, hovered);
    }
}
