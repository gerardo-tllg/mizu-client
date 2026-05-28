/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.screens;

import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.ColorListSetting;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

/**
 * List editor for {@link ColorListSetting}. Each row shows a color swatch +
 * editable hex field + rainbow toggle + delete button. Header has Add.
 */
public class NewColorListScreen extends Screen {
    private final ColorListSetting setting;
    private final ArrayList<SettingColor> entries;
    private int editingIndex = -1;
    private String editBuffer = "";
    private int scroll = 0;

    private int listX, listY, listWidth, listHeight;

    private static final int PADDING = 6;
    private static final int SWATCH_SIZE = 14;
    private static final int ROW_HEIGHT_MIN = 18;

    public NewColorListScreen(ColorListSetting setting) {
        super(Text.literal("Edit " + setting.name));
        this.setting = setting;
        this.entries = new ArrayList<>();
        for (SettingColor c : setting.get()) this.entries.add(new SettingColor(c));
    }

    @Override
    protected void init() {
        super.init();
        int margin = Math.max(40, this.width / 6);
        listX = margin;
        listY = 30;
        listWidth = this.width - margin * 2;
        listHeight = this.height - 60;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        FontManager fm = FontManager.get();
        super.render(context, mouseX, mouseY, delta); // 1.21.5 pipeline handles blur internally

        int lineColor = fm.primaryAlpha(180);
        int headerH = fm.getHeaderHeight();
        int rowH = Math.max(fm.getRowHeight(), ROW_HEIGHT_MIN);

        fm.drawText(context, this.title.getString(), listX, 8, fm.getTextColor());

        // Panel
        context.fill(listX, listY, listX + listWidth, listY + listHeight, fm.primaryAlpha(120));
        RenderUtils.drawThinOutline(context, listX, listY, listWidth, listHeight, lineColor);

        // Header
        context.fill(listX, listY, listX + listWidth, listY + headerH, fm.secondaryAlpha(200));
        RenderUtils.drawThinHLine(context, listX, listY + headerH, listWidth, lineColor);
        String heading = setting.name + " (" + entries.size() + ")";
        fm.drawText(context, heading, listX + PADDING, listY + (headerH - fm.getTextHeight()) / 2, fm.getTextColor());
        String addLabel = "[+ Add]";
        fm.drawText(context, addLabel, listX + listWidth - fm.getTextWidth(addLabel) - PADDING, listY + (headerH - fm.getTextHeight()) / 2, GuiColors.TEXT_SETTING_VALUE);

        // Rows
        int rowY = listY + headerH + 1 - scroll;
        int rowEnd = listY + listHeight;
        context.enableScissor(listX + 1, listY + headerH + 1, listX + listWidth - 1, rowEnd - 1);
        for (int i = 0; i < entries.size(); i++) {
            if (rowY + rowH < listY + headerH + 1) { rowY += rowH; continue; }
            if (rowY > rowEnd) break;
            SettingColor c = entries.get(i);
            boolean editing = editingIndex == i;
            boolean hovered = hit(mouseX, mouseY, listX + 1, rowY, listWidth - 2, rowH);
            if (hovered || editing) {
                context.fill(listX + 1, rowY, listX + listWidth - 1, rowY + rowH, fm.secondaryAlpha(120));
            }

            // Swatch
            int swatchX = listX + PADDING;
            int swatchY = rowY + (rowH - SWATCH_SIZE) / 2;
            int argb = ((c.a & 0xFF) << 24) | ((c.r & 0xFF) << 16) | ((c.g & 0xFF) << 8) | (c.b & 0xFF);
            context.fill(swatchX, swatchY, swatchX + SWATCH_SIZE, swatchY + SWATCH_SIZE, argb);
            RenderUtils.drawThinOutline(context, swatchX, swatchY, SWATCH_SIZE, SWATCH_SIZE, lineColor);

            // Hex field
            int hexX = swatchX + SWATCH_SIZE + 6;
            String hex = editing ? editBuffer : toHex(c);
            int hexTextColor = editing ? fm.getTextColor() : fm.getTextSecondary();
            fm.drawText(context, hex, hexX, rowY + (rowH - fm.getTextHeight()) / 2, hexTextColor);
            if (editing && ((System.currentTimeMillis() / 500) & 1) == 0) {
                int caretX = hexX + fm.getTextWidth(hex);
                context.fill(caretX, rowY + 3, caretX + 1, rowY + rowH - 3, fm.getTextColor());
            }

            // Rainbow toggle ("R" when on, dim "r" when off)
            String rainbowLabel = c.rainbow ? "[R]" : "[r]";
            int rainbowColor = c.rainbow ? fm.getTextColor() : GuiColors.TEXT_DISABLED;
            int rainbowX = listX + listWidth - fm.getTextWidth("[x]") - PADDING - 6 - fm.getTextWidth(rainbowLabel);
            fm.drawText(context, rainbowLabel, rainbowX, rowY + (rowH - fm.getTextHeight()) / 2, rainbowColor);

            // Delete
            String delLabel = "[x]";
            fm.drawText(context, delLabel, listX + listWidth - fm.getTextWidth(delLabel) - PADDING, rowY + (rowH - fm.getTextHeight()) / 2, GuiColors.TEXT_DISABLED);

            rowY += rowH;
        }
        context.disableScissor();

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = Math.max(fm.getRowHeight(), ROW_HEIGHT_MIN);
        int headerH = fm.getHeaderHeight();

        // Add button
        String addLabel = "[+ Add]";
        int addX = listX + listWidth - fm.getTextWidth(addLabel) - PADDING;
        if (mouseY >= listY && mouseY < listY + headerH && mouseX >= addX && mouseX <= listX + listWidth - 2) {
            commitEdit();
            entries.add(new SettingColor(255, 255, 255, 255));
            setting.get().clear();
            setting.get().addAll(entries);
            setting.onChanged();
            return true;
        }

        // Find clicked row
        int rowY = listY + headerH + 1 - scroll;
        for (int i = 0; i < entries.size(); i++) {
            if (mouseY >= rowY && mouseY < rowY + rowH) {
                // Delete
                String delLabel = "[x]";
                int delX = listX + listWidth - fm.getTextWidth(delLabel) - PADDING;
                if (mouseX >= delX) {
                    commitEdit();
                    entries.remove(i);
                    setting.get().clear();
                    setting.get().addAll(entries);
                    setting.onChanged();
                    if (editingIndex == i) editingIndex = -1;
                    else if (editingIndex > i) editingIndex--;
                    return true;
                }
                // Rainbow toggle
                String rainbowLabel = entries.get(i).rainbow ? "[R]" : "[r]";
                int rainbowX = listX + listWidth - fm.getTextWidth("[x]") - PADDING - 6 - fm.getTextWidth(rainbowLabel);
                int rainbowEndX = listX + listWidth - fm.getTextWidth("[x]") - PADDING - 6;
                if (mouseX >= rainbowX && mouseX < rainbowEndX) {
                    commitEdit();
                    entries.get(i).rainbow = !entries.get(i).rainbow;
                    setting.get().clear();
                    setting.get().addAll(entries);
                    setting.onChanged();
                    return true;
                }
                // Start editing hex
                commitEdit();
                editingIndex = i;
                editBuffer = toHex(entries.get(i));
                return true;
            }
            rowY += rowH;
        }

        // Outside — stop editing
        commitEdit();
        editingIndex = -1;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (hit((int) mouseX, (int) mouseY, listX, listY, listWidth, listHeight)) {
            FontManager fm = FontManager.get();
            int rowH = Math.max(fm.getRowHeight(), ROW_HEIGHT_MIN);
            int headerH = fm.getHeaderHeight();
            int max = Math.max(0, entries.size() * rowH - (listHeight - headerH));
            scroll -= (int) (verticalAmount * 14);
            scroll = Math.max(0, Math.min(scroll, max));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editingIndex >= 0) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                commitEdit();
                editingIndex = -1;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                editingIndex = -1;
                editBuffer = "";
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!editBuffer.isEmpty()) editBuffer = editBuffer.substring(0, editBuffer.length() - 1);
                return true;
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            commitEdit();
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (editingIndex >= 0 && chr >= 32 && chr != 127) {
            // Accept hex digits + leading '#'; cap length at 9 (#RRGGBBAA)
            boolean ok = (chr >= '0' && chr <= '9') || (chr >= 'a' && chr <= 'f') || (chr >= 'A' && chr <= 'F') || chr == '#';
            if (ok && editBuffer.length() < 9) editBuffer += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        commitEdit();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void commitEdit() {
        if (editingIndex < 0) return;
        SettingColor parsed = parseHex(editBuffer);
        if (parsed != null) {
            // Preserve rainbow flag
            boolean wasRainbow = entries.get(editingIndex).rainbow;
            parsed.rainbow = wasRainbow;
            entries.set(editingIndex, parsed);
            setting.get().clear();
            setting.get().addAll(entries);
            setting.onChanged();
        }
        editBuffer = "";
    }

    private static String toHex(SettingColor c) {
        return String.format("#%02X%02X%02X%02X", c.r & 0xFF, c.g & 0xFF, c.b & 0xFF, c.a & 0xFF);
    }

    /** Parse #RGB, #RGBA, #RRGGBB, or #RRGGBBAA. Returns null if unparseable. */
    private static SettingColor parseHex(String s) {
        String t = s.startsWith("#") ? s.substring(1) : s;
        try {
            int r, g, b, a = 255;
            if (t.length() == 3) {
                r = Integer.parseInt("" + t.charAt(0) + t.charAt(0), 16);
                g = Integer.parseInt("" + t.charAt(1) + t.charAt(1), 16);
                b = Integer.parseInt("" + t.charAt(2) + t.charAt(2), 16);
            } else if (t.length() == 4) {
                r = Integer.parseInt("" + t.charAt(0) + t.charAt(0), 16);
                g = Integer.parseInt("" + t.charAt(1) + t.charAt(1), 16);
                b = Integer.parseInt("" + t.charAt(2) + t.charAt(2), 16);
                a = Integer.parseInt("" + t.charAt(3) + t.charAt(3), 16);
            } else if (t.length() == 6) {
                r = Integer.parseInt(t.substring(0, 2), 16);
                g = Integer.parseInt(t.substring(2, 4), 16);
                b = Integer.parseInt(t.substring(4, 6), 16);
            } else if (t.length() == 8) {
                r = Integer.parseInt(t.substring(0, 2), 16);
                g = Integer.parseInt(t.substring(2, 4), 16);
                b = Integer.parseInt(t.substring(4, 6), 16);
                a = Integer.parseInt(t.substring(6, 8), 16);
            } else {
                return null;
            }
            return new SettingColor(r, g, b, a);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
