/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.screens;

import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.components.CapturingGuiTheme;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.BlockDataSetting;
import meteordevelopment.meteorclient.settings.IBlockData;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * New-theme block picker for {@link BlockDataSetting}. Shows every registered
 * block; blocks that currently have a changed override are pinned to the top
 * and marked with "*".
 *
 * Clicking a block row opens Meteor's per-block edit screen (the widget-based
 * editor built by {@code T.createScreen()}) because {@link IBlockData} exposes
 * its editor as a {@code WidgetScreen}, not as a {@code SettingGroup}. This is
 * still a UX improvement over the fallback — users get our theme for the
 * picker instead of Meteor's old-style {@code BlockDataSettingScreen}.
 *
 * Clicking the [X] at the row's right edge removes that block's override.
 */
public class NewBlockDataScreen<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBlockData<T>> extends Screen {
    private final BlockDataSetting<T> setting;

    // Search
    private String filterText = "";
    private boolean filterFocused = true;

    // Scroll
    private int scroll = 0;

    // Layout (populated in init)
    private int panelX;
    private int panelWidth;
    private int panelY, panelHeight;
    private int searchY;

    private static final int PADDING = 6;
    private static final int SEARCH_HEIGHT_MULT = 2;
    private static final int ICON_SIZE = 16;
    private static final int REMOVE_BTN_WIDTH = 18;

    public NewBlockDataScreen(BlockDataSetting<T> setting) {
        super(Text.literal("Configure Blocks \u2014 " + setting.name));
        this.setting = setting;
    }

    @Override
    protected void init() {
        super.init();
        int screenW = this.width;
        int screenH = this.height;

        int margin = Math.max(20, screenW / 6);
        panelWidth = screenW - margin * 2;
        panelX = margin;

        int searchH = FontManager.get().getRowHeight() * SEARCH_HEIGHT_MULT;
        searchY = 20;
        panelY = searchY + searchH + 8;
        panelHeight = screenH - panelY - 20;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        FontManager fm = FontManager.get();
        super.render(context, mouseX, mouseY, delta);

        int lineColor = fm.primaryAlpha(180);
        int fillColor = fm.secondaryAlpha(200);
        int headerH = fm.getHeaderHeight();
        int rowH = fm.getRowHeight();

        // --- Title ---
        fm.drawText(context, this.title.getString(), panelX, 4, fm.getTextColor());

        // --- Search bar ---
        int searchH = rowH * SEARCH_HEIGHT_MULT;
        context.fill(panelX, searchY, panelX + panelWidth, searchY + searchH,
            filterFocused ? fm.secondaryAlpha(220) : fm.secondaryAlpha(150));
        RenderUtils.drawThinOutline(context, panelX, searchY, panelWidth, searchH, lineColor);
        String searchLabel = "Search: ";
        int labelW = fm.getTextWidth(searchLabel);
        fm.drawText(context, searchLabel, panelX + PADDING, searchY + (searchH - fm.getTextHeight()) / 2, GuiColors.TEXT_SETTING_VALUE);
        fm.drawText(context, filterText, panelX + PADDING + labelW, searchY + (searchH - fm.getTextHeight()) / 2, fm.getTextColor());
        if (filterFocused && ((System.currentTimeMillis() / 500) & 1) == 0) {
            int caretX = panelX + PADDING + labelW + fm.getTextWidth(filterText);
            context.fill(caretX, searchY + 3, caretX + 1, searchY + searchH - 3, fm.getTextColor());
        }
        String clearHint = "[Clear]";
        fm.drawText(context, clearHint,
            panelX + panelWidth - fm.getTextWidth(clearHint) - PADDING,
            searchY + (searchH - fm.getTextHeight()) / 2,
            GuiColors.TEXT_SETTING_VALUE);

        // --- Panel ---
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, fm.primaryAlpha(120));
        RenderUtils.drawThinOutline(context, panelX, panelY, panelWidth, panelHeight, lineColor);

        // Panel header shows count of changed overrides
        int overrideCount = countOverrides();
        String heading = overrideCount == 0
            ? "Blocks (no overrides yet)"
            : "Blocks (" + overrideCount + " override" + (overrideCount == 1 ? "" : "s") + ")";
        context.fill(panelX, panelY, panelX + panelWidth, panelY + headerH, fillColor);
        RenderUtils.drawThinHLine(context, panelX, panelY + headerH, panelWidth, lineColor);
        fm.drawText(context, heading, panelX + PADDING, panelY + (headerH - fm.getTextHeight()) / 2, fm.getTextColor());

        // Rows
        List<Block> blocks = buildFilteredList();
        int effectiveRowH = Math.max(rowH, ICON_SIZE + 2);
        int rowY = panelY + headerH + 1 - scroll;
        int rowEnd = panelY + panelHeight;
        context.enableScissor(panelX + 1, panelY + headerH + 1, panelX + panelWidth - 1, rowEnd - 1);

        if (blocks.isEmpty()) {
            String msg = filterText.isEmpty() ? "(no blocks)" : "(no matches)";
            int tx = panelX + (panelWidth - fm.getTextWidth(msg)) / 2;
            int ty = panelY + headerH + (panelHeight - headerH - fm.getTextHeight()) / 2;
            fm.drawText(context, msg, tx, ty, GuiColors.TEXT_DISABLED);
        }

        for (Block block : blocks) {
            if (rowY + effectiveRowH < panelY + headerH + 1) {
                rowY += effectiveRowH;
                continue;
            }
            if (rowY > rowEnd) break;

            T data = setting.get().get(block);
            boolean hasOverride = data != null && data.isChanged();

            boolean rowHovered = mouseX >= panelX + 1 && mouseX < panelX + panelWidth - 1
                && mouseY >= rowY && mouseY < rowY + effectiveRowH
                && mouseY >= panelY + headerH + 1 && mouseY <= rowEnd - 1;
            if (rowHovered) {
                context.fill(panelX + 1, rowY, panelX + panelWidth - 1, rowY + effectiveRowH, fm.secondaryAlpha(120));
            }

            int tcolor = rowHovered ? fm.getTextColor() : (hasOverride ? fm.getTextColor() : fm.getTextSecondary());

            // Block icon
            ItemStack icon = block.asItem().getDefaultStack();
            int iconY = rowY + (effectiveRowH - ICON_SIZE) / 2;
            context.drawItem(icon, panelX + PADDING, iconY);

            // Name + "*" marker for changed
            String label = Names.get(block) + (hasOverride ? "  *" : "");
            int nameX = panelX + PADDING + ICON_SIZE + 4;
            fm.drawText(context, label, nameX, rowY + (effectiveRowH - fm.getTextHeight()) / 2, tcolor);

            // [X] remove-override button (only shown when there's an override)
            if (hasOverride) {
                int xBtnX = panelX + panelWidth - REMOVE_BTN_WIDTH - 2;
                boolean xBtnHovered = mouseX >= xBtnX && mouseX < xBtnX + REMOVE_BTN_WIDTH
                    && mouseY >= rowY && mouseY < rowY + effectiveRowH;
                int xColor = xBtnHovered ? 0xFFFF6060 : GuiColors.TEXT_DISABLED;
                String xStr = "reset";
                int xw = fm.getTextWidth(xStr);
                fm.drawText(context, xStr, xBtnX + (REMOVE_BTN_WIDTH - xw) / 2,
                    rowY + (effectiveRowH - fm.getTextHeight()) / 2, xColor);
            }

            rowY += effectiveRowH;
        }
        context.disableScissor();

        // Scroll hint
        int totalRowsH = blocks.size() * effectiveRowH;
        int visibleRowsH = panelHeight - headerH - 2;
        if (totalRowsH > visibleRowsH) {
            int sx = panelX + panelWidth - 3;
            int scrollH = Math.max(10, visibleRowsH * visibleRowsH / totalRowsH);
            int scrollY = panelY + headerH + 2
                + (scroll * (visibleRowsH - scrollH)) / Math.max(1, totalRowsH - visibleRowsH);
            context.fill(sx, scrollY, sx + 2, scrollY + scrollH, fm.secondaryAlpha(220));
        }
    }

    private int countOverrides() {
        int n = 0;
        for (Map.Entry<Block, T> e : setting.get().entrySet()) {
            if (e.getValue() != null && e.getValue().isChanged()) n++;
        }
        return n;
    }

    /** Returns blocks in display order: changed-overrides first, then filter-matching alphabetical. */
    private List<Block> buildFilteredList() {
        List<Block> changed = new ArrayList<>();
        List<Block> rest = new ArrayList<>();
        for (Block b : Registries.BLOCK) {
            String name = Names.get(b);
            if (!matchesFilter(name, filterText)) continue;
            T data = setting.get().get(b);
            if (data != null && data.isChanged()) changed.add(b);
            else rest.add(b);
        }
        changed.sort(Comparator.comparing(Names::get));
        rest.sort(Comparator.comparing(Names::get));
        changed.addAll(rest);
        return changed;
    }

    private static boolean matchesFilter(String name, String filter) {
        if (filter.isEmpty()) return true;
        String a = name.toLowerCase();
        String b = filter.toLowerCase();
        // Simple substring match is enough here; picker is coarse.
        return a.contains(b);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int headerH = fm.getHeaderHeight();

        // Search bar
        int searchH = rowH * SEARCH_HEIGHT_MULT;
        if (mouseX >= panelX && mouseX < panelX + panelWidth
            && mouseY >= searchY && mouseY < searchY + searchH) {
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

        // Rows
        if (!(mouseX >= panelX && mouseX < panelX + panelWidth
            && mouseY >= panelY && mouseY < panelY + panelHeight)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int effectiveRowH = Math.max(rowH, ICON_SIZE + 2);
        int rowY = panelY + headerH + 1 - scroll;
        int rowEnd = panelY + panelHeight;
        List<Block> blocks = buildFilteredList();

        for (Block block : blocks) {
            if (mouseY >= rowY && mouseY < rowY + effectiveRowH
                && mouseY >= panelY + headerH + 1 && mouseY <= rowEnd - 1) {

                T data = setting.get().get(block);
                boolean hasOverride = data != null && data.isChanged();

                // Was it the [reset] button?
                if (hasOverride && button == 0) {
                    int xBtnX = panelX + panelWidth - REMOVE_BTN_WIDTH - 2;
                    if (mouseX >= xBtnX && mouseX < xBtnX + REMOVE_BTN_WIDTH) {
                        setting.get().remove(block);
                        setting.onChanged();
                        return true;
                    }
                }

                // Otherwise: open the per-block edit screen.
                if (button == 0) {
                    openBlockEdit(block, data);
                    return true;
                }
                // Right-click to quickly reset when override exists
                if (button == 1 && hasOverride) {
                    setting.get().remove(block);
                    setting.onChanged();
                    return true;
                }
                return true;
            }
            rowY += effectiveRowH;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Open the per-block editor. Uses a {@link CapturingGuiTheme} to intercept
     * Meteor's {@code Settings} object from the T's {@code createScreen()} and
     * render it in our theme via {@link NewBlockEditScreen}. If the T's screen
     * doesn't use {@code theme.settings(...)} (rare — every first-party
     * implementation does), falls back to the original widget-themed screen.
     */
    @SuppressWarnings("unchecked")
    private void openBlockEdit(Block block, T existing) {
        MinecraftClient mc = MinecraftClient.getInstance();

        T data = existing;
        if (data == null) data = (T) setting.defaultData.get().copy();
        // Put it in the map now so edits persist from the moment the user changes anything.
        setting.get().put(block, data);

        CapturingGuiTheme capture = new CapturingGuiTheme();
        Screen widgetScreen = null;
        try {
            widgetScreen = data.createScreen(capture, block, setting);
            // Trigger the settings-building logic. WidgetScreen.initWidgets() is public.
            if (widgetScreen instanceof meteordevelopment.meteorclient.gui.WidgetScreen ws) {
                ws.initWidgets();
            }
        } catch (Throwable ignored) {
            // If construction failed, we'll fall through to either the captured
            // Settings (if any) or the widget screen fallback.
        }

        if (capture.captured != null) {
            String title = "Configure " + Names.get(block);
            mc.setScreen(new NewBlockEditScreen(title, capture.captured,
                /* returnTo */ new NewBlockDataScreen<>(setting)));
        } else if (widgetScreen != null) {
            // Rare path: T built its UI without theme.settings(...). Use the old screen.
            mc.setScreen(widgetScreen);
        } else {
            // Absolute last resort — something went very wrong. Use Meteor's default editor.
            mc.setScreen(data.createScreen(GuiThemes.get(), block, setting));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        FontManager fm = FontManager.get();
        int effectiveRowH = Math.max(fm.getRowHeight(), ICON_SIZE + 2);
        int totalRowsH = buildFilteredList().size() * effectiveRowH;
        int visibleRowsH = panelHeight - fm.getHeaderHeight() - 2;
        int maxScroll = Math.max(0, totalRowsH - visibleRowsH);
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (verticalAmount * effectiveRowH * 2)));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (filterFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!filterText.isEmpty()) {
                    filterText = filterText.substring(0, filterText.length() - 1);
                    scroll = 0;
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                filterFocused = false;
                return true;
            }
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
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
}
