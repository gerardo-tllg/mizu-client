/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.screens;

import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.settings.StringListSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

/**
 * Edit-in-place list editor for {@link StringListSetting}. Each row is a text
 * box with a delete button; an Add button appends a new row.
 */
public class NewStringListScreen extends Screen {
    private final StringListSetting setting;
    private final ArrayList<String> entries;
    private int editingIndex = -1;
    private int scroll = 0;

    private int margin;
    private int listX, listY, listWidth, listHeight;

    private static final int PADDING = 6;

    public NewStringListScreen(StringListSetting setting) {
        super(Text.literal("Edit " + setting.name));
        this.setting = setting;
        this.entries = new ArrayList<>(setting.get());
    }

    @Override
    protected void init() {
        super.init();
        margin = Math.max(40, this.width / 6);
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
        int rowH = fm.getRowHeight();
        int headerH = fm.getHeaderHeight();

        // Title
        fm.drawText(context, this.title.getString(), listX, 8, fm.getTextColor());

        // Panel
        context.fill(listX, listY, listX + listWidth, listY + listHeight, fm.primaryAlpha(120));
        RenderUtils.drawThinOutline(context, listX, listY, listWidth, listHeight, lineColor);

        // Header
        context.fill(listX, listY, listX + listWidth, listY + headerH, fm.secondaryAlpha(200));
        RenderUtils.drawThinHLine(context, listX, listY + headerH, listWidth, lineColor);
        String heading = setting.name + " (" + entries.size() + ")";
        fm.drawText(context, heading, listX + PADDING, listY + (headerH - fm.getTextHeight()) / 2, fm.getTextColor());

        // Add button (right side of header)
        String addLabel = "[+ Add]";
        fm.drawText(context, addLabel, listX + listWidth - fm.getTextWidth(addLabel) - PADDING, listY + (headerH - fm.getTextHeight()) / 2, GuiColors.TEXT_SETTING_VALUE);

        // Rows (entries)
        int rowY = listY + headerH + 1 - scroll;
        int rowEnd = listY + listHeight;
        context.enableScissor(listX + 1, listY + headerH + 1, listX + listWidth - 1, rowEnd - 1);
        for (int i = 0; i < entries.size(); i++) {
            if (rowY + rowH < listY + headerH + 1) { rowY += rowH; continue; }
            if (rowY > rowEnd) break;
            boolean hovered = hit(mouseX, mouseY, listX + 1, rowY, listWidth - 2, rowH);
            boolean editing = editingIndex == i;
            if (hovered || editing) {
                context.fill(listX + 1, rowY, listX + listWidth - 1, rowY + rowH, fm.secondaryAlpha(120));
            }
            int tcolor = editing ? fm.getTextColor() : fm.getTextSecondary();
            String text = entries.get(i);
            fm.drawText(context, text, listX + PADDING, rowY + (rowH - fm.getTextHeight()) / 2, tcolor);
            // Caret when editing
            if (editing && ((System.currentTimeMillis() / 500) & 1) == 0) {
                int caretX = listX + PADDING + fm.getTextWidth(text);
                context.fill(caretX, rowY + 2, caretX + 1, rowY + rowH - 2, fm.getTextColor());
            }
            // Delete button (right side)
            String delLabel = "[x]";
            fm.drawText(context, delLabel, listX + listWidth - fm.getTextWidth(delLabel) - PADDING, rowY + (rowH - fm.getTextHeight()) / 2, GuiColors.TEXT_DISABLED);
            rowY += rowH;
        }
        context.disableScissor();

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int headerH = fm.getHeaderHeight();

        // Add button?
        String addLabel = "[+ Add]";
        int addX = listX + listWidth - fm.getTextWidth(addLabel) - PADDING;
        if (mouseY >= listY && mouseY < listY + headerH && mouseX >= addX && mouseX <= listX + listWidth - 2) {
            commitEdit();
            entries.add("");
            editingIndex = entries.size() - 1;
            setting.set(new ArrayList<>(entries));
            return true;
        }

        // Row click (edit or delete)
        int rowY = listY + headerH + 1 - scroll;
        for (int i = 0; i < entries.size(); i++) {
            if (mouseY >= rowY && mouseY < rowY + rowH) {
                // Delete button region
                String delLabel = "[x]";
                int delX = listX + listWidth - fm.getTextWidth(delLabel) - PADDING;
                if (mouseX >= delX) {
                    commitEdit();
                    entries.remove(i);
                    setting.set(new ArrayList<>(entries));
                    if (editingIndex == i) editingIndex = -1;
                    else if (editingIndex > i) editingIndex--;
                    return true;
                }
                // Otherwise: start editing this row
                commitEdit();
                editingIndex = i;
                return true;
            }
            rowY += rowH;
        }

        // Click outside any row: stop editing
        commitEdit();
        editingIndex = -1;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (hit((int) mouseX, (int) mouseY, listX, listY, listWidth, listHeight)) {
            int rowH = FontManager.get().getRowHeight();
            int headerH = FontManager.get().getHeaderHeight();
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
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                String cur = entries.get(editingIndex);
                if (!cur.isEmpty()) entries.set(editingIndex, cur.substring(0, cur.length() - 1));
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
            String cur = entries.get(editingIndex);
            String next = cur + chr;
            CharFilter f = setting.filter;
            if (f == null || f.filter(next, chr)) {
                entries.set(editingIndex, next);
            }
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        commitEdit();
        super.close();
    }

    private void commitEdit() {
        // Persist the current entries to the setting
        setting.set(new ArrayList<>(entries));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
