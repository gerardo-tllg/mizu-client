/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.components;

import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class CategoryPanel {
    private final Category category;
    private final List<ModuleButton> buttons = new ArrayList<>();

    private int x, y;
    private int width;
    private static final int PADDING = 2;

    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;
    private int scrollOffset = 0;
    /** Max drawn content height (excluding header). 0 = auto-fit to screen. Set by NewGuiScreen. */
    private int heightCap = 0;

    // Collapsed state persists across GUI opens (static so it survives new Screen instances)
    private static final java.util.Set<String> COLLAPSED_CATS = new java.util.HashSet<>();

    public CategoryPanel(Category category, int x, int y) {
        this.category = category;
        this.x = x;
        this.y = y;

        List<Module> modules = Modules.get().getGroup(category);
        for (Module module : modules) {
            buttons.add(new ModuleButton(module, category));
        }

        recalcWidth();
    }

    public void recalcWidth() {
        FontManager fm = FontManager.get();
        int max = fm.getGuiTextWidth(category.name);

        for (ModuleButton btn : buttons) {
            Module mod = btn.getModule();
            int w = fm.getGuiTextWidth(mod.title);
            if (w > max) max = w;
        }

        // Hard cap: never exceed 1/4 of the screen width, and never less than
        // something reasonable for the category name.
        int sw = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int hardCap = Math.max(80, sw / 4);
        int minWidth = Math.max(60, fm.getGuiTextWidth(category.name) + PADDING * 4);

        // Add some breathing room so settings inside have space to display
        int computed = max + PADDING * 2 + 20;
        width = Math.max(minWidth, Math.min(computed, hardCap));
    }

    // Setting-width measurement is kept around for future use but no longer
    // drives the panel width. Settings fit within whatever the category's
    // fixed width is (content gets clipped by scissor if too long).
    @SuppressWarnings("unused")
    private int getSettingTextWidth(FontManager fm, Setting<?> setting) {
        if (setting instanceof ColorSetting cs) {
            return fm.getGuiTextWidth(cs.name) + 16;
        } else if (setting instanceof EnumSetting<?> es) {
            return fm.getGuiTextWidth(es.name + ": " + es.get().toString()) + 6;
        } else if (setting instanceof BoolSetting bs) {
            return fm.getGuiTextWidth(bs.name) + 14;
        } else if (setting instanceof IntSetting is) {
            int displayMax = Math.min(is.sliderMax, 99999);
            return fm.getGuiTextWidth(is.name + ": " + displayMax) + 6;
        } else if (setting instanceof DoubleSetting ds) {
            double displayMax = Math.min(ds.sliderMax, 99999);
            String longest = ds.name + ": " + String.format("%." + Math.max(1, Math.min(2, ds.decimalPlaces)) + "f", displayMax);
            return fm.getGuiTextWidth(longest) + 6;
        } else if (setting instanceof KeybindSetting ks) {
            return fm.getGuiTextWidth(ks.name + ": " + ks.get().toString()) + 6;
        } else if (setting instanceof ProvidedStringSetting pss) {
            return fm.getGuiTextWidth(pss.name + ": " + (pss.get() == null ? "" : pss.get())) + 6;
        } else if (setting instanceof FontFaceSetting fs) {
            return fm.getGuiTextWidth(fs.name + ": " + (fs.get() == null ? "(none)" : fs.get().toString())) + 6;
        } else if (setting instanceof StringSetting ss) {
            return fm.getGuiTextWidth(ss.name + ": ") + 40;
        }
        return fm.getGuiTextWidth(setting.name) + 40;
    }

    public int getWidth() {
        return width;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float animProgress) {
        if (dragging) {
            x = mouseX - dragOffsetX;
            y = mouseY - dragOffsetY;
            // Clamp so the header always stays visible on screen
            int sw = MinecraftClient.getInstance().getWindow().getScaledWidth();
            int sh = MinecraftClient.getInstance().getWindow().getScaledHeight();
            int headerH = FontManager.get().getHeaderHeight();
            if (x < 0) x = 0;
            if (x > sw - width) x = sw - width;
            if (y < 0) y = 0;
            if (y > sh - headerH) y = sh - headerH;
        }

        recalcWidth();

        FontManager fm = FontManager.get();
        int headerH = fm.getHeaderHeight();
        int contentHeight = getVisibleContentHeight();
        int totalHeight = headerH + contentHeight;

        // Re-clamp scroll in case the viewport or content changed
        scrollOffset = Math.max(0, Math.min(scrollOffset, getMaxScroll()));

        // Water-theme: deep navy header, dark teal border
        int outlineColor = GuiColors.OUTLINE_COLOR;   // #0d3a5c at 67% alpha
        int headerColor  = GuiColors.HEADER_FILL;     // #0a1e30 at 86% alpha

        boolean collapsed = COLLAPSED_CATS.contains(category.name);

        // Full-panel outline
        RenderUtils.drawThickOutline(context, x, y, width, totalHeight, 3, outlineColor);

        // Header fill
        RenderUtils.fillNative(context, x, y, width, headerH, headerColor);

        // Header: category name + collapse arrow on the right
        context.enableScissor(x, y, x + width, y + headerH);
        int headerTextY = y + (headerH - fm.getTextHeight()) / 2;
        fm.drawText(context, category.name, x + PADDING + 2, headerTextY, GuiColors.ACCENT_LIGHT);
        String arrow = collapsed ? ">" : "v";
        fm.drawText(context, arrow, x + width - fm.getGuiTextWidth(arrow) - PADDING - 2, headerTextY, GuiColors.ACCENT_LIGHT);
        context.disableScissor();

        if (!collapsed) {
            // Module list — scrollable, filtered by search query
            int contentTop = y + headerH;
            int contentBottom = y + totalHeight;
            context.enableScissor(x, contentTop, x + width, contentBottom);
            int moduleY = contentTop - scrollOffset;
            String query = meteordevelopment.meteorclient.gui.newgui.NewGuiScreen.searchQuery;
            for (ModuleButton button : buttons) {
                boolean matches = query.isEmpty() ||
                    button.getModule().title.toLowerCase().contains(query.toLowerCase());
                if (matches) {
                    button.render(context, x, moduleY, width, mouseX, mouseY);
                    moduleY += button.getHeight();
                }
            }
            context.disableScissor();

            // Scrollbar
            int maxScroll = getMaxScroll();
            if (maxScroll > 0) {
                int visibleH = contentHeight;
                int totalContent = getContentHeight();
                int trackH = Math.max(1, visibleH - 2);
                int thumbH = Math.max(8, (int) ((long) visibleH * trackH / totalContent));
                int thumbY = y + headerH + 1 + (int) ((long) (trackH - thumbH) * scrollOffset / maxScroll);
                int sbX = x + width - 2;
                fillNative(context, sbX, thumbY, sbX + 1, thumbY + thumbH, GuiColors.OUTLINE_HEADER);
            }
        }

        // Border trace animation
        if (fm.isAnimation() && animProgress < 1f) {
            drawBorderTrace(context, x, y, width, totalHeight, animProgress, fm);
        }
    }

    /**
     * Draws a glowing segment that traces the border starting from top-right.
     * Path: top-right → down right side → across bottom → up left side → across top.
     */
    private void drawBorderTrace(DrawContext context, int bx, int by, int bw, int bh, float progress, FontManager fm) {
        int animColor = fm.animationAlpha(220);

        // Work in native screen pixels via matrix scaling
        float scale = (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
        int sx = (int) (bx * scale);
        int sy = (int) (by * scale);
        int sw = (int) (bw * scale);
        int sh = (int) (bh * scale);

        // 3 sides only: skip the top edge
        int perimeter = 2 * sh + sw;
        int segLen = Math.max(8, perimeter / 8);

        int headPos = (int) (progress * (perimeter + segLen));
        int tailPos = headPos - segLen;

        var matrices = context.getMatrices();
        matrices.push();
        matrices.scale(1f / scale, 1f / scale, 1f);

        boolean ltr = fm.isAnimationLTR();

        for (int p = Math.max(0, tailPos); p < Math.min(perimeter, headPos); p++) {
            int px, py;
            if (ltr) {
                // LTR: down left side → across bottom → up right side
                if (p < sh) {
                    px = sx;
                    py = sy + p;
                } else if (p < sh + sw) {
                    px = sx + (p - sh);
                    py = sy + sh - 1;
                } else {
                    px = sx + sw - 1;
                    py = sy + sh - 1 - (p - sh - sw);
                }
            } else {
                // RTL: down right side → across bottom → up left side
                if (p < sh) {
                    px = sx + sw - 1;
                    py = sy + p;
                } else if (p < sh + sw) {
                    px = sx + sw - 1 - (p - sh);
                    py = sy + sh - 1;
                } else {
                    px = sx;
                    py = sy + sh - 1 - (p - sh - sw);
                }
            }

            context.fill(px - 1, py - 1, px + 2, py + 2, animColor);
        }

        matrices.pop();
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        FontManager fm = FontManager.get();
        int headerH = fm.getHeaderHeight();
        int totalHeight = headerH + getVisibleContentHeight();

        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + headerH) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
                return true;
            }
        }

        if (mouseX >= x && mouseX <= x + width
            && mouseY >= y + headerH && mouseY < y + totalHeight) {
            int moduleY = y + headerH - scrollOffset;
            for (ModuleButton moduleButton : buttons) {
                int btnHeight = moduleButton.getHeight();
                if (mouseY >= moduleY && mouseY < moduleY + btnHeight) {
                    if (moduleButton.mouseClicked(x, moduleY, width, mouseX, mouseY, button)) {
                        return true;
                    }
                }
                moduleY += btnHeight;
            }
        }
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        dragging = false;
        for (ModuleButton moduleButton : buttons) {
            moduleButton.mouseReleased(mouseX, mouseY, button);
        }
    }

    public boolean mouseScrolled(int mouseX, int mouseY, double amount) {
        int totalHeight = FontManager.get().getHeaderHeight() + getVisibleContentHeight();
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + totalHeight) {
            scrollOffset -= (int) (amount * 14);
            scrollOffset = Math.max(0, Math.min(scrollOffset, getMaxScroll()));
            return true;
        }
        return false;
    }

    /** Natural height if everything were shown — before screen clamp. */
    private int getContentHeight() {
        int h = 2;
        for (ModuleButton button : buttons) {
            h += button.getHeight();
        }
        return h;
    }

    /**
     * Drawn content height. Capped so the panel never extends past the bottom of
     * the screen; the list scrolls inside the capped region when content is taller.
     */
    private int getVisibleContentHeight() {
        int screenH = MinecraftClient.getInstance().getWindow().getScaledHeight();
        int headerH = FontManager.get().getHeaderHeight();
        int bottomMargin = 2;
        int maxAvail = Math.max(FontManager.get().getRowHeight() * 2, screenH - y - headerH - bottomMargin);
        return Math.min(getContentHeight(), maxAvail);
    }

    /** Retained for API compatibility with NewGuiScreen; no longer does anything
     *  since panels always show their full content height. */
    public void setHeightCap(int cap) {
        this.heightCap = cap;
    }

    private int getMaxScroll() {
        return Math.max(0, getContentHeight() - getVisibleContentHeight());
    }

    /** Draw a filled rect at native pixel resolution. */
    private void fillNative(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        float scale = (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
        var matrices = context.getMatrices();
        matrices.push();
        matrices.scale(1f / scale, 1f / scale, 1f);
        context.fill((int)(x1 * scale), (int)(y1 * scale),
                      (int)(x2 * scale), (int)(y2 * scale), color);
        matrices.pop();
    }

    /** Get the gap between category panels in GUI coords (1 native pixel). */
    public static int getNativeGap() {
        float scale = (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
        return Math.max(1, (int) Math.ceil(1.0 / scale));
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    /** Full drawn height including header, clamped to screen. Used by row-wrap layout. */
    public int getTotalHeight() {
        return FontManager.get().getHeaderHeight() + getVisibleContentHeight();
    }

    public Category getCategory() {
        return category;
    }
}
