/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.screens;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.components.ModuleButton;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.StatusEffectAmplifierMapSetting;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Editor for {@link StatusEffectAmplifierMapSetting}. All status effects are
 * shown in one scrollable list; each row has a potion icon, name, and
 * [-] / level / [+] buttons to edit the amplifier. Effects with amplifier &gt; 0
 * sort to the top.
 */
public class NewAmplifierMapScreen extends Screen {
    private final StatusEffectAmplifierMapSetting setting;
    private final Reference2IntMap<StatusEffect> map;

    private String filterText = "";
    private boolean filterFocused = true;
    private int scroll = 0;

    private int panelX, panelY, panelWidth, panelHeight;
    private int searchY;

    private static final int PADDING = 6;
    private static final int SEARCH_HEIGHT_MULT = 2;
    private static final int ICON_SIZE = 16;
    private static final int ROW_H_MIN = ICON_SIZE + 2;
    private static final int MAX_AMP = 255;

    public NewAmplifierMapScreen(StatusEffectAmplifierMapSetting setting) {
        super(Text.literal("Modify Amplifiers — " + setting.name));
        this.setting = setting;
        this.map = setting.get();
    }

    @Override
    protected void init() {
        super.init();
        int margin = Math.max(40, this.width / 4);
        panelX = margin;
        panelWidth = this.width - margin * 2;
        int searchH = FontManager.get().getRowHeight() * SEARCH_HEIGHT_MULT;
        searchY = 20;
        panelY = searchY + searchH + 8;
        panelHeight = this.height - panelY - 20;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        FontManager fm = FontManager.get();
        super.render(context, mouseX, mouseY, delta); // 1.21.5 pipeline handles blur internally

        int lineColor = fm.primaryAlpha(180);
        int rowH = Math.max(fm.getRowHeight(), ROW_H_MIN);
        int headerH = fm.getHeaderHeight();

        fm.drawText(context, this.title.getString(), panelX, 4, fm.getTextColor());

        // Search
        int searchH = rowH * SEARCH_HEIGHT_MULT;
        context.fill(panelX, searchY, panelX + panelWidth, searchY + searchH, filterFocused ? fm.secondaryAlpha(220) : fm.secondaryAlpha(150));
        RenderUtils.drawThinOutline(context, panelX, searchY, panelWidth, searchH, lineColor);
        String searchLabel = "Search: ";
        int labelW = fm.getTextWidth(searchLabel);
        int searchTextY = searchY + (searchH - fm.getTextHeight()) / 2;
        fm.drawText(context, searchLabel, panelX + PADDING, searchTextY, GuiColors.TEXT_SETTING_VALUE);
        fm.drawText(context, filterText, panelX + PADDING + labelW, searchTextY, fm.getTextColor());
        if (filterFocused && ((System.currentTimeMillis() / 500) & 1) == 0) {
            int caretX = panelX + PADDING + labelW + fm.getTextWidth(filterText);
            context.fill(caretX, searchY + 3, caretX + 1, searchY + searchH - 3, fm.getTextColor());
        }
        String clearHint = "[Clear]";
        fm.drawText(context, clearHint, panelX + panelWidth - fm.getTextWidth(clearHint) - PADDING, searchTextY, GuiColors.TEXT_SETTING_VALUE);

        // Panel
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, fm.primaryAlpha(120));
        RenderUtils.drawThinOutline(context, panelX, panelY, panelWidth, panelHeight, lineColor);

        List<StatusEffect> items = buildFilteredList();

        // Header
        context.fill(panelX, panelY, panelX + panelWidth, panelY + headerH, fm.secondaryAlpha(200));
        RenderUtils.drawThinHLine(context, panelX, panelY + headerH, panelWidth, lineColor);
        int activeCount = 0;
        for (StatusEffect e : map.keySet()) if (map.getInt(e) > 0) activeCount++;
        String heading = "Effects (" + activeCount + " active)";
        fm.drawText(context, heading, panelX + PADDING, panelY + (headerH - fm.getTextHeight()) / 2, fm.getTextColor());

        // Rows
        int rowY = panelY + headerH + 1 - scroll;
        int rowEnd = panelY + panelHeight;
        context.enableScissor(panelX + 1, panelY + headerH + 1, panelX + panelWidth - 1, rowEnd - 1);
        for (StatusEffect effect : items) {
            if (rowY + rowH < panelY + headerH + 1) { rowY += rowH; continue; }
            if (rowY > rowEnd) break;
            boolean hovered = hit(mouseX, mouseY, panelX + 1, rowY, panelWidth - 2, rowH)
                && mouseY >= panelY + headerH + 1 && mouseY <= rowEnd - 1;
            int amp = map.getInt(effect);
            boolean active = amp > 0;
            if (hovered || active) {
                context.fill(panelX + 1, rowY, panelX + panelWidth - 1, rowY + rowH, fm.secondaryAlpha(hovered ? 150 : 90));
            }

            // Icon
            int iconX = panelX + PADDING;
            int iconY = rowY + (rowH - ICON_SIZE) / 2;
            ItemStack icon = ModuleButton.statusEffectIcon(effect);
            if (icon != null && !icon.isEmpty()) context.drawItem(icon, iconX, iconY);

            // Name
            int textX = iconX + ICON_SIZE + 4;
            int tcolor = active ? fm.getTextColor() : fm.getTextSecondary();
            fm.drawText(context, Names.get(effect), textX, rowY + (rowH - fm.getTextHeight()) / 2, tcolor);

            // Amplifier controls on the right: [-] N [+]
            String minus = "[-]";
            String plus = "[+]";
            String val = String.valueOf(amp);
            int plusX = panelX + panelWidth - fm.getTextWidth(plus) - PADDING;
            int valX = plusX - 4 - fm.getTextWidth(val);
            int minusX = valX - 4 - fm.getTextWidth(minus);
            int ampColor = active ? fm.getTextColor() : GuiColors.TEXT_DISABLED;
            fm.drawText(context, minus, minusX, rowY + (rowH - fm.getTextHeight()) / 2, amp > 0 ? fm.getTextColor() : GuiColors.TEXT_DISABLED);
            fm.drawText(context, val, valX, rowY + (rowH - fm.getTextHeight()) / 2, ampColor);
            fm.drawText(context, plus, plusX, rowY + (rowH - fm.getTextHeight()) / 2, amp < MAX_AMP ? fm.getTextColor() : GuiColors.TEXT_DISABLED);

            rowY += rowH;
        }
        context.disableScissor();

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = Math.max(fm.getRowHeight(), ROW_H_MIN);
        int headerH = fm.getHeaderHeight();

        // Search bar / clear
        int searchH = rowH * SEARCH_HEIGHT_MULT;
        if (hit((int) mouseX, (int) mouseY, panelX, searchY, panelWidth, searchH)) {
            filterFocused = true;
            String clearHint = "[Clear]";
            int clearX = panelX + panelWidth - fm.getTextWidth(clearHint) - PADDING;
            if (mouseX >= clearX) {
                filterText = "";
                scroll = 0;
            }
            return true;
        } else {
            filterFocused = false;
        }

        if (!hit((int) mouseX, (int) mouseY, panelX, panelY, panelWidth, panelHeight)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int rowY = panelY + headerH + 1 - scroll;
        int rowEnd = panelY + panelHeight;
        for (StatusEffect effect : buildFilteredList()) {
            if (mouseY >= rowY && mouseY < rowY + rowH && mouseY >= panelY + headerH + 1 && mouseY <= rowEnd - 1) {
                int amp = map.getInt(effect);
                String minus = "[-]";
                String plus = "[+]";
                String val = String.valueOf(amp);
                int plusX = panelX + panelWidth - fm.getTextWidth(plus) - PADDING;
                int valX = plusX - 4 - fm.getTextWidth(val);
                int minusX = valX - 4 - fm.getTextWidth(minus);

                if (mouseX >= minusX && mouseX < minusX + fm.getTextWidth(minus) + 2) {
                    if (amp > 0) {
                        map.put(effect, amp - 1);
                        setting.onChanged();
                    }
                    return true;
                }
                if (mouseX >= plusX - 2 && mouseX <= panelX + panelWidth - PADDING) {
                    if (amp < MAX_AMP) {
                        map.put(effect, amp + 1);
                        setting.onChanged();
                    }
                    return true;
                }
                // Click on value itself toggles: if 0 → 1, else keeps the current (fast on/off)
                if (mouseX >= valX - 2 && mouseX < plusX - 2) {
                    if (amp == 0) map.put(effect, 1);
                    else map.put(effect, 0);
                    setting.onChanged();
                    return true;
                }
                return true;
            }
            rowY += rowH;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (hit((int) mouseX, (int) mouseY, panelX, panelY, panelWidth, panelHeight)) {
            FontManager fm = FontManager.get();
            int rowH = Math.max(fm.getRowHeight(), ROW_H_MIN);
            int headerH = fm.getHeaderHeight();
            int count = buildFilteredList().size();
            int max = Math.max(0, count * rowH - (panelHeight - headerH));
            scroll -= (int) (verticalAmount * 14);
            scroll = Math.max(0, Math.min(scroll, max));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (filterFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!filterText.isEmpty()) filterText = filterText.substring(0, filterText.length() - 1);
                scroll = 0;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                filterFocused = false;
                return true;
            }
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (filterFocused && chr >= 32 && chr != 127) {
            filterText += chr;
            scroll = 0;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /** All effects matching the filter, sorted active-first then by name. */
    private List<StatusEffect> buildFilteredList() {
        List<StatusEffect> list = new ArrayList<>();
        for (StatusEffect e : Registries.STATUS_EFFECT) {
            String name = Names.get(e);
            if (!filterText.isEmpty()) {
                int words = Utils.searchInWords(name, filterText);
                int diff = Utils.searchLevenshteinDefault(name, filterText, false);
                if (words == 0 && diff > name.length() / 2) continue;
            }
            list.add(e);
        }
        list.sort((a, b) -> {
            int ampA = map.getInt(a);
            int ampB = map.getInt(b);
            boolean aActive = ampA > 0;
            boolean bActive = ampB > 0;
            if (aActive != bActive) return aActive ? -1 : 1;
            return Names.get(a).compareToIgnoreCase(Names.get(b));
        });
        return list;
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
