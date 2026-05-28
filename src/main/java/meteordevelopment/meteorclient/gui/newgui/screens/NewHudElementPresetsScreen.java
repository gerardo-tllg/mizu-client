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
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact preset picker styled as a clickgui panel — same layout as
 * {@link NewAddHudElementScreen} but with a flat preset list.
 */
public class NewHudElementPresetsScreen extends Screen {
    private final HudElementInfo<?> info;
    private final int spawnX, spawnY;
    private final Screen backTo;
    private final Screen afterAddTo;

    private String searchQuery = "";
    private int scroll = 0;

    private int panelX, panelY, panelWidth, panelHeight;

    private static final int PANEL_WIDTH = 170;
    private static final int PADDING = 2;
    private static final int SEARCH_ROW_GAP = 2;

    public NewHudElementPresetsScreen(HudElementInfo<?> info, int spawnX, int spawnY, Screen backTo, Screen afterAddTo) {
        super(Text.literal("Presets \u2014 " + info.title));
        this.info = info;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.backTo = backTo;
        this.afterAddTo = afterAddTo;
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
        int desiredH = rowH
            + (rowH + SEARCH_ROW_GAP)
            + filteredPresets().size() * rowH
            + PADDING * 2;
        panelHeight = Math.min(desiredH, this.height - 10);

        int targetX = (int) (spawnX / sf);
        int targetY = (int) (spawnY / sf);
        panelX = Math.max(4, Math.min(this.width - panelWidth - 4, targetX));
        panelY = Math.max(4, Math.min(this.height - panelHeight - 4, targetY));
    }

    private List<HudElementInfo<?>.Preset> filteredPresets() {
        List<HudElementInfo<?>.Preset> out = new ArrayList<>();
        for (HudElementInfo<?>.Preset p : info.presets) {
            if (Utils.searchTextDefault(p.title, searchQuery, false)) out.add(p);
        }
        return out;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        org.joml.Matrix4f savedProj = new org.joml.Matrix4f(com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix());
        Utils.unscaledProjection();
        HudEditorScreen.renderElements(context);
        com.mojang.blaze3d.systems.RenderSystem.setProjectionMatrix(savedProj, com.mojang.blaze3d.systems.ProjectionType.ORTHOGRAPHIC);

        positionPanel();

        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int outlineColor = fm.primaryAlpha(170);
        int headerColor = fm.primaryAlpha(220);

        RenderUtils.drawThickOutline(context, panelX, panelY, panelWidth, panelHeight, 3, outlineColor);
        RenderUtils.fillNative(context, panelX, panelY, panelWidth, rowH, headerColor);

        context.enableScissor(panelX, panelY, panelX + panelWidth, panelY + rowH);
        String title = "Presets: " + info.title;
        fm.drawText(context, title, panelX + 4, panelY + (rowH - fm.getTextHeight()) / 2, fm.getTextColor());
        String close = "\u2715";
        int closeX = panelX + panelWidth - fm.getTextWidth(close) - 4;
        fm.drawText(context, close, closeX, panelY + (rowH - fm.getTextHeight()) / 2, fm.getTextColor());
        context.disableScissor();

        // Search
        int searchY = panelY + rowH + SEARCH_ROW_GAP;
        int searchX = panelX + PADDING;
        int searchW = panelWidth - PADDING * 2;
        RenderUtils.fillNative(context, searchX, searchY, searchW, rowH, fm.secondaryAlpha(200));
        String label = searchQuery.isEmpty() ? "Search\u2026" : searchQuery;
        int tc = searchQuery.isEmpty() ? GuiColors.TEXT_DISABLED : fm.getTextColor();
        fm.drawText(context, label, searchX + 3, searchY + (rowH - fm.getTextHeight()) / 2, tc);
        if (!searchQuery.isEmpty() && ((System.currentTimeMillis() / 500) & 1) == 0) {
            int caretX = searchX + 3 + fm.getTextWidth(searchQuery);
            context.fill(caretX, searchY + 2, caretX + 1, searchY + rowH - 2, fm.getTextColor());
        }

        // List
        int listY = searchY + rowH + 1;
        int listBottom = panelY + panelHeight - PADDING;
        int listH = Math.max(rowH, listBottom - listY);
        int listX = panelX + PADDING;
        int listW = panelWidth - PADDING * 2;

        context.enableScissor(listX, listY, listX + listW, listY + listH);
        int drawY = listY - scroll;
        int totalH = 0;
        List<HudElementInfo<?>.Preset> list = filteredPresets();
        for (HudElementInfo<?>.Preset preset : list) {
            int btnSize = rowH;
            int btnX = listX + listW - btnSize;
            boolean hovered = mouseX >= listX && mouseX < listX + listW
                && mouseY >= drawY && mouseY < drawY + rowH;
            if (hovered) RenderUtils.fillNative(context, listX, drawY, listW, rowH, fm.secondaryAlpha(140));

            context.enableScissor(listX, drawY, btnX - 2, drawY + rowH);
            fm.drawText(context, preset.title, listX + 3, drawY + (rowH - fm.getTextHeight()) / 2, fm.getTextColor());
            context.disableScissor();

            boolean btnHovered = mouseX >= btnX && mouseX < btnX + btnSize
                && mouseY >= drawY && mouseY < drawY + rowH;
            int btnFill = btnHovered ? 0xFF3BC660 : 0xFF2F9E4D;
            RenderUtils.fillNative(context, btnX, drawY, btnSize, rowH, btnFill);
            fm.drawText(context, "+",
                btnX + (btnSize - fm.getTextWidth("+")) / 2,
                drawY + (rowH - fm.getTextHeight()) / 2,
                fm.getTextColor());

            drawY += rowH;
            totalH += rowH;
        }
        context.disableScissor();

        int maxScroll = Math.max(0, totalH - listH);
        if (maxScroll > 0) {
            int trackH = Math.max(1, listH - 2);
            int thumbH = Math.max(8, (int) ((long) listH * trackH / Math.max(1, totalH)));
            int thumbY = listY + 1 + (int) ((long) (trackH - thumbH) * scroll / maxScroll);
            int sbX = panelX + panelWidth - 2;
            context.fill(sbX, thumbY, sbX + 1, thumbY + thumbH, fm.primaryAlpha(220));
        }

        if (list.isEmpty()) {
            String msg = searchQuery.isEmpty() ? "(no presets)" : "No matches.";
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

        if (mouseX < panelX || mouseX >= panelX + panelWidth
            || mouseY < panelY || mouseY >= panelY + panelHeight) {
            MinecraftClient.getInstance().setScreen(backTo);
            return true;
        }

        String close = "\u2715";
        int closeX = panelX + panelWidth - fm.getTextWidth(close) - 4;
        if (mouseX >= closeX - 2 && mouseY >= panelY && mouseY < panelY + rowH) {
            MinecraftClient.getInstance().setScreen(backTo);
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
        for (HudElementInfo<?>.Preset preset : filteredPresets()) {
            if (mouseY >= drawY && mouseY < drawY + rowH) {
                Hud.get().add(preset, spawnX, spawnY);
                MinecraftClient.getInstance().setScreen(afterAddTo);
                return true;
            }
            drawY += rowH;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
            MinecraftClient.getInstance().setScreen(backTo);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
            searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            scroll = 0;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            List<HudElementInfo<?>.Preset> list = filteredPresets();
            if (!list.isEmpty()) {
                Hud.get().add(list.get(0), spawnX, spawnY);
                MinecraftClient.getInstance().setScreen(afterAddTo);
                return true;
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
        MinecraftClient.getInstance().setScreen(backTo);
    }
}
