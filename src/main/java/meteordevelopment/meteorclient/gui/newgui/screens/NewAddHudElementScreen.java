/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.screens;

import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * New-theme add-element picker. Renders as a floating clickgui-style panel
 * (header + search + scrollable list) positioned near the right-click spot,
 * matching the look of {@link meteordevelopment.meteorclient.gui.newgui.components.CategoryPanel}.
 */
public class NewAddHudElementScreen extends Screen {
    private final int spawnX, spawnY; // native pixels
    private final Screen returnTo;

    private String searchQuery = "";
    private int scroll = 0;

    // Panel rect (GUI pixels)
    private int panelX, panelY, panelWidth, panelHeight;

    private static final int PANEL_WIDTH = 170;
    private static final int PADDING = 2;
    private static final int SEARCH_ROW_GAP = 2;

    public NewAddHudElementScreen(int spawnX, int spawnY, Screen returnTo) {
        super(Text.literal("Add HUD element"));
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.returnTo = returnTo;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    protected void init() {
        super.init();
        positionPanel();
    }

    private void positionPanel() {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        double sf = MinecraftClient.getInstance().getWindow().getScaleFactor();

        panelWidth = PANEL_WIDTH;

        // Desired height = header + search + all rows, capped to screen
        int totalRows = countTotalRows();
        int desiredH = rowH // header
            + (rowH + SEARCH_ROW_GAP)   // search
            + totalRows * rowH          // list
            + PADDING * 2;
        int maxH = this.height - 10;
        panelHeight = Math.min(desiredH, maxH);

        // Position near the click (native → gui), clamped to screen
        int targetX = (int) (spawnX / sf);
        int targetY = (int) (spawnY / sf);
        panelX = Math.max(4, Math.min(this.width - panelWidth - 4, targetX));
        panelY = Math.max(4, Math.min(this.height - panelHeight - 4, targetY));
    }

    private int countTotalRows() {
        int total = 0;
        for (Map.Entry<HudGroup, List<Item>> e : buildGrouped().entrySet()) {
            total += 1;                   // section header row
            total += e.getValue().size(); // items
        }
        return total;
    }

    private Map<HudGroup, List<Item>> buildGrouped() {
        Map<HudGroup, List<Item>> grouped = new LinkedHashMap<>();
        for (HudElementInfo<?> info : Hud.get().infos.values()) {
            if (info.hasPresets() && !searchQuery.isEmpty()) {
                for (HudElementInfo<?>.Preset preset : info.presets) {
                    String title = info.title + "  -  " + preset.title;
                    if (Utils.searchTextDefault(title, searchQuery, false))
                        grouped.computeIfAbsent(info.group, g -> new ArrayList<>()).add(new Item(title, preset));
                }
            } else if (Utils.searchTextDefault(info.title, searchQuery, false)) {
                grouped.computeIfAbsent(info.group, g -> new ArrayList<>()).add(new Item(info.title, info));
            }
        }
        return grouped;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Live HUD underneath (native projection), then restore the GUI ortho.
        org.joml.Matrix4f savedProj = new org.joml.Matrix4f(com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix());
        Utils.unscaledProjection();
        HudEditorScreen.renderElements(context);
        com.mojang.blaze3d.systems.RenderSystem.setProjectionMatrix(savedProj, com.mojang.blaze3d.systems.ProjectionType.ORTHOGRAPHIC);

        // Re-position every frame so that if content/screen size changes, the
        // panel stays on-screen with the right height.
        positionPanel();

        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int outlineColor = fm.primaryAlpha(170);
        int headerColor = fm.primaryAlpha(220);

        // Thick outline + header (mirrors CategoryPanel)
        RenderUtils.drawThickOutline(context, panelX, panelY, panelWidth, panelHeight, 3, outlineColor);
        RenderUtils.fillNative(context, panelX, panelY, panelWidth, rowH, headerColor);
        context.enableScissor(panelX, panelY, panelX + panelWidth, panelY + rowH);
        String title = "Add HUD element";
        fm.drawText(context, title, panelX + 4, panelY + (rowH - fm.getTextHeight()) / 2, fm.getTextColor());
        // A small close affordance on the right of the header.
        String close = "\u2715";
        int closeX = panelX + panelWidth - fm.getTextWidth(close) - 4;
        fm.drawText(context, close, closeX, panelY + (rowH - fm.getTextHeight()) / 2, fm.getTextColor());
        context.disableScissor();

        // Search bar
        int searchY = panelY + rowH + SEARCH_ROW_GAP;
        int searchX = panelX + PADDING;
        int searchW = panelWidth - PADDING * 2;
        RenderUtils.fillNative(context, searchX, searchY, searchW, rowH, fm.secondaryAlpha(200));
        String label = searchQuery.isEmpty() ? "Search\u2026" : searchQuery;
        int textColor = searchQuery.isEmpty() ? GuiColors.TEXT_DISABLED : fm.getTextColor();
        fm.drawText(context, label, searchX + 3, searchY + (rowH - fm.getTextHeight()) / 2, textColor);
        if (!searchQuery.isEmpty() && ((System.currentTimeMillis() / 500) & 1) == 0) {
            int caretX = searchX + 3 + fm.getTextWidth(searchQuery);
            context.fill(caretX, searchY + 2, caretX + 1, searchY + rowH - 2, fm.getTextColor());
        }

        // Scrollable list (section headers + rows)
        int listY = searchY + rowH + 1;
        int listBottom = panelY + panelHeight - PADDING;
        int listH = Math.max(rowH, listBottom - listY);
        int listX = panelX + PADDING;
        int listW = panelWidth - PADDING * 2;

        context.enableScissor(listX, listY, listX + listW, listY + listH);
        int drawY = listY - scroll;
        int totalH = 0;
        Map<HudGroup, List<Item>> grouped = buildGrouped();
        for (Map.Entry<HudGroup, List<Item>> entry : grouped.entrySet()) {
            // Section header
            RenderUtils.fillNative(context, listX, drawY, listW, rowH, fm.secondaryAlpha(230));
            fm.drawText(context, entry.getKey().title(), listX + 3, drawY + (rowH - fm.getTextHeight()) / 2, fm.getTextColor());
            drawY += rowH;
            totalH += rowH;

            for (Item item : entry.getValue()) {
                boolean isInfoWithPresets = item.object() instanceof HudElementInfo<?> info && info.hasPresets();
                int btnSize = rowH;
                int btnX = listX + listW - btnSize;
                boolean rowHovered = mouseX >= listX && mouseX < listX + listW
                    && mouseY >= drawY && mouseY < drawY + rowH;
                if (rowHovered) {
                    RenderUtils.fillNative(context, listX, drawY, listW, rowH, fm.secondaryAlpha(140));
                }

                // Name text, scissor so it doesn't overlap the button
                context.enableScissor(listX, drawY, btnX - 2, drawY + rowH);
                fm.drawText(context, item.title(), listX + 3, drawY + (rowH - fm.getTextHeight()) / 2, fm.getTextColor());
                context.disableScissor();

                // Button
                String btnLabel = isInfoWithPresets ? ">" : "+";
                boolean btnHovered = mouseX >= btnX && mouseX < btnX + btnSize
                    && mouseY >= drawY && mouseY < drawY + rowH;
                int btnFill = isInfoWithPresets
                    ? (btnHovered ? fm.secondaryAlpha(240) : fm.secondaryAlpha(200))
                    : (btnHovered ? 0xFF3BC660 : 0xFF2F9E4D);
                RenderUtils.fillNative(context, btnX, drawY, btnSize, rowH, btnFill);
                fm.drawText(context, btnLabel,
                    btnX + (btnSize - fm.getTextWidth(btnLabel)) / 2,
                    drawY + (rowH - fm.getTextHeight()) / 2,
                    fm.getTextColor());

                drawY += rowH;
                totalH += rowH;
            }
        }
        context.disableScissor();

        // Scrollbar
        int maxScroll = Math.max(0, totalH - listH);
        if (maxScroll > 0) {
            int trackH = Math.max(1, listH - 2);
            int thumbH = Math.max(8, (int) ((long) listH * trackH / Math.max(1, totalH)));
            int thumbY = listY + 1 + (int) ((long) (trackH - thumbH) * scroll / maxScroll);
            int sbX = panelX + panelWidth - 2;
            context.fill(sbX, thumbY, sbX + 1, thumbY + thumbH, fm.primaryAlpha(220));
        }

        if (grouped.isEmpty()) {
            String msg = searchQuery.isEmpty() ? "(no elements)" : "No matches.";
            fm.drawText(context, msg,
                listX + (listW - fm.getTextWidth(msg)) / 2,
                listY + (listH - fm.getTextHeight()) / 2,
                GuiColors.TEXT_DISABLED);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();

        // Click outside the panel → close.
        if (mouseX < panelX || mouseX >= panelX + panelWidth
            || mouseY < panelY || mouseY >= panelY + panelHeight) {
            MinecraftClient.getInstance().setScreen(returnTo);
            return true;
        }

        // Close "X" in header
        String close = "\u2715";
        int closeX = panelX + panelWidth - fm.getTextWidth(close) - 4;
        if (mouseX >= closeX - 2 && mouseY >= panelY && mouseY < panelY + rowH) {
            MinecraftClient.getInstance().setScreen(returnTo);
            return true;
        }

        int searchY = panelY + rowH + SEARCH_ROW_GAP;
        int listY = searchY + rowH + 1;
        int listBottom = panelY + panelHeight - PADDING;
        int listH = Math.max(rowH, listBottom - listY);
        int listX = panelX + PADDING;
        int listW = panelWidth - PADDING * 2;

        if (mouseX < listX || mouseX >= listX + listW || mouseY < listY || mouseY >= listY + listH) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int drawY = listY - scroll;
        for (Map.Entry<HudGroup, List<Item>> entry : buildGrouped().entrySet()) {
            drawY += rowH; // section header
            for (Item item : entry.getValue()) {
                if (mouseY >= drawY && mouseY < drawY + rowH) {
                    runObject(item.object());
                    return true;
                }
                drawY += rowH;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void runObject(Object object) {
        if (object == null) return;
        if (object instanceof HudElementInfo.Preset preset) {
            Hud.get().add(preset, spawnX, spawnY);
            MinecraftClient.getInstance().setScreen(returnTo);
        } else {
            HudElementInfo<?> info = (HudElementInfo<?>) object;
            if (info.hasPresets()) {
                MinecraftClient.getInstance().setScreen(new NewHudElementPresetsScreen(info, spawnX, spawnY, this, returnTo));
            } else {
                Hud.get().add(info, spawnX, spawnY);
                MinecraftClient.getInstance().setScreen(returnTo);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX < panelX || mouseX >= panelX + panelWidth
            || mouseY < panelY || mouseY >= panelY + panelHeight) return false;
        FontManager fm = FontManager.get();
        scroll = Math.max(0, scroll - (int) (verticalAmount * fm.getRowHeight() * 2));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            MinecraftClient.getInstance().setScreen(returnTo);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
            searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            scroll = 0;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            Map<HudGroup, List<Item>> grouped = buildGrouped();
            for (List<Item> items : grouped.values()) {
                if (!items.isEmpty()) { runObject(items.get(0).object()); return true; }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (chr >= 32 && chr != 127) {
            searchQuery += chr;
            scroll = 0;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(returnTo);
    }

    private record Item(String title, Object object) {}
}
