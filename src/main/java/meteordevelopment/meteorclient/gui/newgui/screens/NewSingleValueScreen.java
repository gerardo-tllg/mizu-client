/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.screens;

import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Single-value picker: one centered panel with a search bar and a scrollable
 * list of choices. Clicking a row sets the setting value and closes the screen.
 *
 * Shares the aesthetic of {@link NewCollectionListScreen} but with a single panel.
 */
public class NewSingleValueScreen<T> extends Screen {
    private final Setting<T> setting;
    private final Iterable<T> registry;
    private final Function<T, String> nameFn;
    private final Function<T, ItemStack> iconFn;
    private final Predicate<T> skipFn;
    private final Consumer<T> onPick;

    private String filterText = "";
    private boolean filterFocused = true;
    private int scroll = 0;

    private int panelX, panelY, panelWidth, panelHeight;
    private int searchY;

    private static final int PADDING = 6;
    private static final int SEARCH_HEIGHT_MULT = 2;
    private static final int ICON_SIZE = 16;

    public NewSingleValueScreen(String title, Setting<T> setting, Iterable<T> registry,
                                Function<T, String> nameFn, Function<T, ItemStack> iconFn,
                                Predicate<T> skipFn, Consumer<T> onPick) {
        super(Text.literal(title));
        this.setting = setting;
        this.registry = registry;
        this.nameFn = nameFn;
        this.iconFn = iconFn;
        this.skipFn = skipFn;
        this.onPick = onPick;
    }

    @Override
    protected void init() {
        super.init();
        int marginX = Math.max(40, this.width / 4);
        panelX = marginX;
        panelWidth = this.width - marginX * 2;

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
        int headerH = fm.getHeaderHeight();
        int rowH = fm.getRowHeight();
        int effectiveRowH = iconFn != null ? Math.max(rowH, ICON_SIZE + 2) : rowH;

        // Title
        fm.drawText(context, this.title.getString(), panelX, 4, fm.getTextColor());

        // Search bar
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

        // List panel
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, fm.primaryAlpha(120));
        RenderUtils.drawThinOutline(context, panelX, panelY, panelWidth, panelHeight, lineColor);

        // Header
        List<Ranked<T>> items = buildFilteredList();
        context.fill(panelX, panelY, panelX + panelWidth, panelY + headerH, fm.secondaryAlpha(200));
        RenderUtils.drawThinHLine(context, panelX, panelY + headerH, panelWidth, lineColor);
        String heading = "Choose (" + items.size() + ")";
        fm.drawText(context, heading, panelX + PADDING, panelY + (headerH - fm.getTextHeight()) / 2, fm.getTextColor());

        // Rows
        int rowY = panelY + headerH + 1 - scroll;
        int rowEnd = panelY + panelHeight;
        context.enableScissor(panelX + 1, panelY + headerH + 1, panelX + panelWidth - 1, rowEnd - 1);
        T current = setting.get();
        for (Ranked<T> r : items) {
            if (rowY + effectiveRowH < panelY + headerH + 1) { rowY += effectiveRowH; continue; }
            if (rowY > rowEnd) break;
            boolean hovered = hit(mouseX, mouseY, panelX + 1, rowY, panelWidth - 2, effectiveRowH)
                && mouseY >= panelY + headerH + 1 && mouseY <= rowEnd - 1;
            boolean isCurrent = current != null && current.equals(r.value);
            if (hovered || isCurrent) {
                context.fill(panelX + 1, rowY, panelX + panelWidth - 1, rowY + effectiveRowH, fm.secondaryAlpha(hovered ? 150 : 90));
            }
            int tcolor = (hovered || isCurrent) ? fm.getTextColor() : fm.getTextSecondary();

            int textX = panelX + PADDING;
            if (iconFn != null) {
                ItemStack icon = iconFn.apply(r.value);
                if (icon != null && !icon.isEmpty()) {
                    int iconY = rowY + (effectiveRowH - ICON_SIZE) / 2;
                    context.drawItem(icon, textX, iconY);
                    textX += ICON_SIZE + 4;
                }
            }
            fm.drawText(context, r.name, textX, rowY + (effectiveRowH - fm.getTextHeight()) / 2, tcolor);
            rowY += effectiveRowH;
        }
        context.disableScissor();

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int headerH = fm.getHeaderHeight();
        int effectiveRowH = iconFn != null ? Math.max(rowH, ICON_SIZE + 2) : rowH;

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
        for (Ranked<T> r : buildFilteredList()) {
            if (mouseY >= rowY && mouseY < rowY + effectiveRowH && mouseY >= panelY + headerH + 1 && mouseY <= rowEnd - 1) {
                setting.set(r.value);
                if (onPick != null) onPick.accept(r.value);
                this.close();
                return true;
            }
            rowY += effectiveRowH;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (hit((int) mouseX, (int) mouseY, panelX, panelY, panelWidth, panelHeight)) {
            FontManager fm = FontManager.get();
            int rowH = fm.getRowHeight();
            int effectiveRowH = iconFn != null ? Math.max(rowH, ICON_SIZE + 2) : rowH;
            int headerH = fm.getHeaderHeight();
            int count = 0;
            for (T v : registry) {
                if (skipFn != null && skipFn.test(v)) continue;
                if (rankMatch(nameFn.apply(v), filterText) == Integer.MIN_VALUE) continue;
                count++;
            }
            int max = Math.max(0, count * effectiveRowH - (panelHeight - headerH));
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

    private List<Ranked<T>> buildFilteredList() {
        List<Ranked<T>> list = new ArrayList<>();
        for (T value : registry) {
            if (skipFn != null && skipFn.test(value)) continue;
            String name = nameFn.apply(value);
            int rank = rankMatch(name, filterText);
            if (rank == Integer.MIN_VALUE) continue;
            list.add(new Ranked<>(value, name, rank));
        }
        if (filterText.isEmpty()) list.sort(Comparator.comparing(r -> r.name));
        else list.sort((a, b) -> b.rank - a.rank);
        return list;
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static int rankMatch(String name, String filter) {
        if (filter.isEmpty()) return 0;
        int words = Utils.searchInWords(name, filter);
        int diff = Utils.searchLevenshteinDefault(name, filter, false);
        if (words > 0 || diff <= name.length() / 2) return -diff;
        return Integer.MIN_VALUE;
    }

    private static class Ranked<T> {
        final T value;
        final String name;
        final int rank;
        Ranked(T value, String name, int rank) {
            this.value = value;
            this.name = name;
            this.rank = rank;
        }
    }
}
