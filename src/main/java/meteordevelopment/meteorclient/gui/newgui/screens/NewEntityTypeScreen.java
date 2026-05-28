/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.screens;

import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity-type selector with collapsible sections grouped by spawn category:
 * Animals, Water Animals, Monsters, Ambient, Misc. Matches the layout of
 * Meteor's own {@code EntityTypeListSettingScreen}, styled in the new theme.
 *
 * Each section header has a master checkbox (checked if any entity in the
 * section is currently in the collection; toggling affects all entities in
 * that section). Individual rows have per-entity checkboxes.
 */
public class NewEntityTypeScreen extends Screen {
    private static final String S_ANIMALS = "Animals";
    private static final String S_WATER   = "Water Animals";
    private static final String S_MONSTERS = "Monsters";
    private static final String S_AMBIENT = "Ambient";
    private static final String S_MISC    = "Misc";

    /** Fixed display order. */
    private static final String[] SECTION_ORDER = { S_ANIMALS, S_WATER, S_MONSTERS, S_AMBIENT, S_MISC };

    private final EntityTypeListSetting setting;
    /** Section → ordered list of entity types in that section (post-filter). */
    private final Map<String, List<EntityType<?>>> sections = new LinkedHashMap<>();
    /** Section → expanded state. */
    private final Map<String, Boolean> expanded = new LinkedHashMap<>();

    private String filterText = "";
    private boolean filterFocused = true;
    private int scroll = 0;

    private int panelX, panelY, panelWidth, panelHeight;
    private int searchY;

    private static final int PADDING = 6;
    private static final int SEARCH_HEIGHT_MULT = 2;
    private static final int CHECKBOX_SIZE = 10;

    public NewEntityTypeScreen(EntityTypeListSetting setting) {
        super(Text.literal("Select Entities \u2014 " + setting.name));
        this.setting = setting;
        for (String s : SECTION_ORDER) expanded.put(s, false);
        rebuildSections();
        // Auto-expand sections if the total count is modest
        int total = 0;
        for (List<EntityType<?>> l : sections.values()) total += l.size();
        if (total <= 30) {
            for (String s : SECTION_ORDER) {
                if (!sections.get(s).isEmpty()) expanded.put(s, true);
            }
        }
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
        int rowH = fm.getRowHeight();
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

        // Title row
        context.fill(panelX, panelY, panelX + panelWidth, panelY + headerH, fm.secondaryAlpha(200));
        RenderUtils.drawThinHLine(context, panelX, panelY + headerH, panelWidth, lineColor);
        int addedCount = setting.get().size();
        fm.drawText(context, "Entities (" + addedCount + " added)", panelX + PADDING, panelY + (headerH - fm.getTextHeight()) / 2, fm.getTextColor());

        // Sections
        int curY = panelY + headerH + 1 - scroll;
        int rowEnd = panelY + panelHeight;
        context.enableScissor(panelX + 1, panelY + headerH + 1, panelX + panelWidth - 1, rowEnd - 1);
        for (String secName : SECTION_ORDER) {
            List<EntityType<?>> entries = sections.get(secName);
            if (entries.isEmpty()) continue;
            boolean isExpanded = expanded.getOrDefault(secName, false);

            // Section header
            if (curY + rowH >= panelY + headerH + 1 && curY <= rowEnd) {
                boolean hovered = hit(mouseX, mouseY, panelX + 1, curY, panelWidth - 2, rowH);
                context.fill(panelX + 1, curY, panelX + panelWidth - 1, curY + rowH, fm.secondaryAlpha(hovered ? 180 : 140));

                // Expand/collapse indicator + name
                String indicator = isExpanded ? "-" : "+";
                fm.drawText(context, indicator, panelX + PADDING, curY + (rowH - fm.getTextHeight()) / 2, fm.getTextColor());
                fm.drawText(context, secName + " (" + entries.size() + ")",
                    panelX + PADDING + fm.getTextWidth("+") + 6,
                    curY + (rowH - fm.getTextHeight()) / 2,
                    fm.getTextColor());

                // Master checkbox: checked if ANY entity in this section is added; partial if some
                int addedInSec = 0;
                for (EntityType<?> e : entries) if (setting.get().contains(e)) addedInSec++;
                boolean allAdded = addedInSec == entries.size();
                boolean someAdded = addedInSec > 0;
                drawCheckbox(context, fm, panelX + panelWidth - CHECKBOX_SIZE - PADDING, curY + (rowH - CHECKBOX_SIZE) / 2,
                    allAdded, someAdded && !allAdded, lineColor);
            }
            curY += rowH;

            // Section rows (if expanded)
            if (isExpanded) {
                for (EntityType<?> e : entries) {
                    if (curY + rowH < panelY + headerH + 1) { curY += rowH; continue; }
                    if (curY > rowEnd) break;
                    boolean hovered = hit(mouseX, mouseY, panelX + 1, curY, panelWidth - 2, rowH);
                    boolean inColl = setting.get().contains(e);
                    if (hovered || inColl) {
                        context.fill(panelX + 1, curY, panelX + panelWidth - 1, curY + rowH, fm.secondaryAlpha(hovered ? 130 : 70));
                    }
                    int tcolor = (hovered || inColl) ? fm.getTextColor() : fm.getTextSecondary();
                    // Indent entity rows slightly
                    fm.drawText(context, Names.get(e), panelX + PADDING + 14, curY + (rowH - fm.getTextHeight()) / 2, tcolor);
                    drawCheckbox(context, fm, panelX + panelWidth - CHECKBOX_SIZE - PADDING, curY + (rowH - CHECKBOX_SIZE) / 2, inColl, false, fm.primaryAlpha(180));
                    curY += rowH;
                }
            }
        }
        context.disableScissor();

    }

    private void drawCheckbox(DrawContext context, FontManager fm, int x, int y, boolean checked, boolean partial, int lineColor) {
        context.fill(x, y, x + CHECKBOX_SIZE, y + CHECKBOX_SIZE, fm.primaryAlpha(200));
        RenderUtils.drawThinOutline(context, x, y, CHECKBOX_SIZE, CHECKBOX_SIZE, lineColor);
        if (checked) {
            context.fill(x + 2, y + 2, x + CHECKBOX_SIZE - 2, y + CHECKBOX_SIZE - 2, fm.getTextColor());
        } else if (partial) {
            // Dashed fill — smaller inner rect
            context.fill(x + 3, y + 4, x + CHECKBOX_SIZE - 3, y + CHECKBOX_SIZE - 4, fm.getTextSecondary());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int headerH = fm.getHeaderHeight();

        // Search / clear
        int searchH = rowH * SEARCH_HEIGHT_MULT;
        if (hit((int) mouseX, (int) mouseY, panelX, searchY, panelWidth, searchH)) {
            filterFocused = true;
            String clearHint = "[Clear]";
            int clearX = panelX + panelWidth - fm.getTextWidth(clearHint) - PADDING;
            if (mouseX >= clearX) {
                filterText = "";
                rebuildSections();
                scroll = 0;
            }
            return true;
        } else {
            filterFocused = false;
        }

        if (!hit((int) mouseX, (int) mouseY, panelX, panelY, panelWidth, panelHeight)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int curY = panelY + headerH + 1 - scroll;
        int rowEnd = panelY + panelHeight;
        int checkboxX = panelX + panelWidth - CHECKBOX_SIZE - PADDING;

        for (String secName : SECTION_ORDER) {
            List<EntityType<?>> entries = sections.get(secName);
            if (entries.isEmpty()) continue;

            // Section header click?
            if (mouseY >= curY && mouseY < curY + rowH && curY >= panelY + headerH + 1 && curY + rowH <= rowEnd) {
                // Master checkbox region?
                if (mouseX >= checkboxX - 2 && mouseX <= checkboxX + CHECKBOX_SIZE + 2) {
                    // Toggle: if any are added, remove all; else add all
                    boolean anyAdded = false;
                    for (EntityType<?> e : entries) if (setting.get().contains(e)) { anyAdded = true; break; }
                    if (anyAdded) setting.get().removeAll(entries);
                    else setting.get().addAll(entries);
                    setting.onChanged();
                    return true;
                }
                // Otherwise: toggle expanded
                expanded.put(secName, !expanded.get(secName));
                return true;
            }
            curY += rowH;

            if (expanded.getOrDefault(secName, false)) {
                for (EntityType<?> e : entries) {
                    if (mouseY >= curY && mouseY < curY + rowH && curY >= panelY + headerH + 1 && curY + rowH <= rowEnd) {
                        // Anywhere in the row toggles membership
                        if (setting.get().contains(e)) setting.get().remove(e);
                        else setting.get().add(e);
                        setting.onChanged();
                        return true;
                    }
                    curY += rowH;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (hit((int) mouseX, (int) mouseY, panelX, panelY, panelWidth, panelHeight)) {
            FontManager fm = FontManager.get();
            int rowH = fm.getRowHeight();
            int headerH = fm.getHeaderHeight();
            int total = 0;
            for (String secName : SECTION_ORDER) {
                List<EntityType<?>> entries = sections.get(secName);
                if (entries.isEmpty()) continue;
                total++; // section header
                if (expanded.getOrDefault(secName, false)) total += entries.size();
            }
            int max = Math.max(0, total * rowH - (panelHeight - headerH));
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
                if (!filterText.isEmpty()) {
                    filterText = filterText.substring(0, filterText.length() - 1);
                    rebuildSections();
                    scroll = 0;
                }
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
            rebuildSections();
            scroll = 0;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /** Compute sections from the registry with current filter applied. */
    private void rebuildSections() {
        for (String s : SECTION_ORDER) sections.put(s, new ArrayList<>());
        List<EntityType<?>> flat = new ArrayList<>();
        for (EntityType<?> e : Registries.ENTITY_TYPE) {
            if (setting.filter != null && !setting.filter.test(e)) continue;
            String name = Names.get(e);
            if (!filterText.isEmpty()) {
                int words = Utils.searchInWords(name, filterText);
                int diff = Utils.searchLevenshteinDefault(name, filterText, false);
                if (words == 0 && diff > name.length() / 2) continue;
            }
            flat.add(e);
        }
        flat.sort(Comparator.comparing(Names::get, String.CASE_INSENSITIVE_ORDER));
        for (EntityType<?> e : flat) {
            String sec = sectionFor(e);
            sections.get(sec).add(e);
        }
    }

    private static String sectionFor(EntityType<?> e) {
        SpawnGroup g = e.getSpawnGroup();
        return switch (g) {
            case CREATURE -> S_ANIMALS;
            case WATER_AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE, AXOLOTLS -> S_WATER;
            case MONSTER -> S_MONSTERS;
            case AMBIENT -> S_AMBIENT;
            default -> S_MISC;
        };
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
