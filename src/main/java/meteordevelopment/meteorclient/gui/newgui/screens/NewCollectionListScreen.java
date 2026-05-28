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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Generic Available/Added selector screen, styled to match the new ClickGui.
 * Handles any collection type (Set, List) given a registry iterable + name function.
 *
 * Left panel = items not yet added, right panel = items currently in collection.
 * Click left-panel row to add, click right-panel row to remove. Search box filters both.
 * Esc closes.
 *
 * Optional: supply an iconFn to draw an ItemStack icon on each row, and a
 * skipFn to exclude registry entries (e.g. ParticleType non-ParticleEffects).
 */
public class NewCollectionListScreen<T> extends Screen {
    private final Setting<?> setting;
    private final Collection<T> collection;
    private final Iterable<T> registry;
    private final Function<T, String> nameFn;
    /** Nullable — if non-null, rendered as an ItemStack icon at the start of each row. */
    private final Function<T, ItemStack> iconFn;
    /** Nullable — if non-null and returns true, the registry entry is excluded entirely. */
    private final Predicate<T> skipFn;

    // Search
    private String filterText = "";
    private boolean filterFocused = true;

    // Scroll state
    private int availableScroll = 0;
    private int addedScroll = 0;

    // Layout (populated in init)
    private int panelWidth;
    private int panelY, panelHeight;
    private int searchY;
    private int availableX;
    private int addedX;

    private static final int PADDING = 6;
    private static final int SEARCH_HEIGHT_MULT = 2; // search bar is 2x row height
    private static final int ICON_SIZE = 16;

    public NewCollectionListScreen(String title, Setting<?> setting, Collection<T> collection, Iterable<T> registry, Function<T, String> nameFn) {
        this(title, setting, collection, registry, nameFn, null, null);
    }

    public NewCollectionListScreen(String title, Setting<?> setting, Collection<T> collection, Iterable<T> registry,
                                   Function<T, String> nameFn, Function<T, ItemStack> iconFn, Predicate<T> skipFn) {
        super(Text.literal(title));
        this.setting = setting;
        this.collection = collection;
        this.registry = registry;
        this.nameFn = nameFn;
        this.iconFn = iconFn;
        this.skipFn = skipFn;
    }

    @Override
    protected void init() {
        super.init();
        // Lay out two panels side by side with a search bar at top
        int screenW = this.width;
        int screenH = this.height;

        int gap = 8;
        int margin = Math.max(20, screenW / 10);
        panelWidth = (screenW - margin * 2 - gap) / 2;

        int searchH = FontManager.get().getRowHeight() * SEARCH_HEIGHT_MULT;
        searchY = 20;
        panelY = searchY + searchH + gap;
        panelHeight = screenH - panelY - 20;

        availableX = margin;
        addedX = margin + panelWidth + gap;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        FontManager fm = FontManager.get();
        super.render(context, mouseX, mouseY, delta); // 1.21.5 pipeline handles blur internally

        int lineColor = fm.primaryAlpha(180);
        int fillColor = fm.secondaryAlpha(200);
        int headerHeight = fm.getHeaderHeight();
        int rowH = fm.getRowHeight();

        // --- Title bar ---
        fm.drawText(context, this.title.getString(), availableX, 4, fm.getTextColor());

        // --- Search bar ---
        int searchH = rowH * SEARCH_HEIGHT_MULT;
        int searchW = panelWidth * 2 + 8;
        boolean searchHovered = hit(mouseX, mouseY, availableX, searchY, searchW, searchH);
        context.fill(availableX, searchY, availableX + searchW, searchY + searchH, filterFocused ? fm.secondaryAlpha(220) : fm.secondaryAlpha(150));
        RenderUtils.drawThinOutline(context, availableX, searchY, searchW, searchH, lineColor);
        String searchLabel = "Search: ";
        int labelW = fm.getTextWidth(searchLabel);
        fm.drawText(context, searchLabel, availableX + PADDING, searchY + (searchH - fm.getTextHeight()) / 2, GuiColors.TEXT_SETTING_VALUE);
        fm.drawText(context, filterText, availableX + PADDING + labelW, searchY + (searchH - fm.getTextHeight()) / 2, fm.getTextColor());
        if (filterFocused && ((System.currentTimeMillis() / 500) & 1) == 0) {
            int caretX = availableX + PADDING + labelW + fm.getTextWidth(filterText);
            context.fill(caretX, searchY + 3, caretX + 1, searchY + searchH - 3, fm.getTextColor());
        }
        // Clear hint
        String clearHint = "[Clear]";
        fm.drawText(context, clearHint, availableX + searchW - fm.getTextWidth(clearHint) - PADDING, searchY + (searchH - fm.getTextHeight()) / 2, GuiColors.TEXT_SETTING_VALUE);

        // --- Build filtered lists ---
        List<Ranked<T>> availableList = new ArrayList<>();
        List<Ranked<T>> addedList = new ArrayList<>();
        for (T value : registry) {
            if (skipFn != null && skipFn.test(value)) continue;
            String name = nameFn.apply(value);
            int rank = rankMatch(name, filterText);
            if (rank == Integer.MIN_VALUE) continue;
            if (collection.contains(value)) {
                addedList.add(new Ranked<>(value, name, rank));
            } else {
                availableList.add(new Ranked<>(value, name, rank));
            }
        }
        // If filter is empty, sort alphabetically; else sort by rank
        if (filterText.isEmpty()) {
            availableList.sort(Comparator.comparing(r -> r.name));
            addedList.sort(Comparator.comparing(r -> r.name));
        } else {
            availableList.sort((a, b) -> b.rank - a.rank);
            addedList.sort((a, b) -> b.rank - a.rank);
        }

        // --- Panels ---
        renderPanel(context, fm, "Available (" + availableList.size() + ")", availableX, panelY, panelWidth, panelHeight, availableList, availableScroll, mouseX, mouseY, fillColor, lineColor, headerHeight, rowH, true);
        renderPanel(context, fm, "Added (" + addedList.size() + ")", addedX, panelY, panelWidth, panelHeight, addedList, addedScroll, mouseX, mouseY, fillColor, lineColor, headerHeight, rowH, false);

    }

    private void renderPanel(DrawContext context, FontManager fm, String heading, int px, int py, int pw, int ph,
                             List<Ranked<T>> items, int scroll,
                             int mouseX, int mouseY,
                             int fillColor, int lineColor, int headerH, int rowH, boolean isAvailable) {
        // Panel background + border
        context.fill(px, py, px + pw, py + ph, fm.primaryAlpha(120));
        RenderUtils.drawThinOutline(context, px, py, pw, ph, lineColor);

        // Header
        context.fill(px, py, px + pw, py + headerH, fillColor);
        RenderUtils.drawThinHLine(context, px, py + headerH, pw, lineColor);
        fm.drawText(context, heading, px + PADDING, py + (headerH - fm.getTextHeight()) / 2, fm.getTextColor());

        // Rows
        // When an icon is present, each row is taller to accommodate the 16x16 icon.
        int effectiveRowH = iconFn != null ? Math.max(rowH, ICON_SIZE + 2) : rowH;
        int rowY = py + headerH + 1 - scroll;
        int rowEnd = py + ph;
        context.enableScissor(px + 1, py + headerH + 1, px + pw - 1, rowEnd - 1);
        if (items.isEmpty()) {
            // Empty-state message so the panel doesn't look broken when a registry
            // is unavailable (e.g. enchantments from the main menu).
            String msg = isAvailable
                ? (filterText.isEmpty() ? "(no items to add)" : "(no matches)")
                : (filterText.isEmpty() ? "(nothing added)" : "(no matches)");
            int tx = px + (pw - fm.getTextWidth(msg)) / 2;
            int ty = py + headerH + (ph - headerH - fm.getTextHeight()) / 2;
            fm.drawText(context, msg, tx, ty, GuiColors.TEXT_DISABLED);
        }
        for (Ranked<T> r : items) {
            if (rowY + effectiveRowH < py + headerH + 1) {
                rowY += effectiveRowH;
                continue;
            }
            if (rowY > rowEnd) break;
            boolean rowHovered = hit(mouseX, mouseY, px + 1, rowY, pw - 2, effectiveRowH) && mouseY >= py + headerH + 1 && mouseY <= rowEnd - 1;
            if (rowHovered) {
                context.fill(px + 1, rowY, px + pw - 1, rowY + effectiveRowH, fm.secondaryAlpha(120));
            }
            int tcolor = rowHovered ? fm.getTextColor() : fm.getTextSecondary();

            int textX = px + PADDING;

            // Draw icon if provided
            if (iconFn != null) {
                ItemStack icon = iconFn.apply(r.value);
                if (icon != null && !icon.isEmpty()) {
                    int iconY = rowY + (effectiveRowH - ICON_SIZE) / 2;
                    context.drawItem(icon, textX, iconY);
                    textX += ICON_SIZE + 4;
                }
            }

            // Prefix with + or - indicator (only when no icon — with icons the visual is clear enough)
            String label = iconFn != null ? r.name : ((isAvailable ? "+ " : "- ") + r.name);
            fm.drawText(context, label, textX, rowY + (effectiveRowH - fm.getTextHeight()) / 2, tcolor);
            rowY += effectiveRowH;
        }
        context.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int headerH = fm.getHeaderHeight();

        // Search bar focus toggle
        int searchH = rowH * SEARCH_HEIGHT_MULT;
        int searchW = panelWidth * 2 + 8;
        if (hit((int) mouseX, (int) mouseY, availableX, searchY, searchW, searchH)) {
            filterFocused = true;
            // Clear-hint hit region
            String clearHint = "[Clear]";
            int clearX = availableX + searchW - fm.getTextWidth(clearHint) - PADDING;
            if (mouseX >= clearX) {
                filterText = "";
                availableScroll = 0;
                addedScroll = 0;
            }
            return true;
        } else {
            filterFocused = false;
        }

        // Figure out which panel was clicked
        boolean inAvailable = hit((int) mouseX, (int) mouseY, availableX, panelY, panelWidth, panelHeight);
        boolean inAdded = hit((int) mouseX, (int) mouseY, addedX, panelY, panelWidth, panelHeight);

        if (!inAvailable && !inAdded) return super.mouseClicked(mouseX, mouseY, button);

        int px = inAvailable ? availableX : addedX;
        int scroll = inAvailable ? availableScroll : addedScroll;
        int effectiveRowH = iconFn != null ? Math.max(rowH, ICON_SIZE + 2) : rowH;
        int rowY = panelY + headerH + 1 - scroll;
        int rowEnd = panelY + panelHeight;

        // Re-build filtered list to find what was clicked (cheap enough at 1–2000 items)
        List<Ranked<T>> items = new ArrayList<>();
        for (T value : registry) {
            if (skipFn != null && skipFn.test(value)) continue;
            String name = nameFn.apply(value);
            int rank = rankMatch(name, filterText);
            if (rank == Integer.MIN_VALUE) continue;
            boolean inColl = collection.contains(value);
            if (inAvailable && !inColl) items.add(new Ranked<>(value, name, rank));
            else if (inAdded && inColl) items.add(new Ranked<>(value, name, rank));
        }
        if (filterText.isEmpty()) items.sort(Comparator.comparing(r -> r.name));
        else items.sort((a, b) -> b.rank - a.rank);

        for (Ranked<T> r : items) {
            if (mouseY >= rowY && mouseY < rowY + effectiveRowH && mouseY >= panelY + headerH + 1 && mouseY <= rowEnd - 1) {
                if (inAvailable) {
                    collection.add(r.value);
                } else {
                    collection.remove(r.value);
                }
                setting.onChanged();
                return true;
            }
            rowY += effectiveRowH;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        FontManager fm = FontManager.get();
        int headerH = fm.getHeaderHeight();
        int rowH = fm.getRowHeight();
        int effectiveRowH = iconFn != null ? Math.max(rowH, ICON_SIZE + 2) : rowH;
        // Estimate scrollable content so we can clamp
        int avail = 0, added = 0;
        for (T v : registry) {
            if (skipFn != null && skipFn.test(v)) continue;
            String name = nameFn.apply(v);
            if (rankMatch(name, filterText) == Integer.MIN_VALUE) continue;
            if (collection.contains(v)) added++;
            else avail++;
        }
        int contentAvail = Math.max(0, avail * effectiveRowH - (panelHeight - headerH));
        int contentAdded = Math.max(0, added * effectiveRowH - (panelHeight - headerH));

        if (hit((int) mouseX, (int) mouseY, availableX, panelY, panelWidth, panelHeight)) {
            availableScroll -= (int) (verticalAmount * 14);
            availableScroll = Math.max(0, Math.min(availableScroll, contentAvail));
            return true;
        }
        if (hit((int) mouseX, (int) mouseY, addedX, panelY, panelWidth, panelHeight)) {
            addedScroll -= (int) (verticalAmount * 14);
            addedScroll = Math.max(0, Math.min(addedScroll, contentAdded));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (filterFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!filterText.isEmpty()) filterText = filterText.substring(0, filterText.length() - 1);
                availableScroll = 0;
                addedScroll = 0;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                filterFocused = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
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
            availableScroll = 0;
            addedScroll = 0;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // --- Helpers ---

    private static boolean hit(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    /** Rank a name vs filter; higher = better match. MIN_VALUE means filtered out. */
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
