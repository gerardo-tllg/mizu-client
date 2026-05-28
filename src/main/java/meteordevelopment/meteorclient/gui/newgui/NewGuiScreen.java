/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui;

import meteordevelopment.meteorclient.gui.newgui.components.CategoryPanel;
import meteordevelopment.meteorclient.gui.newgui.components.ModuleButton;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class NewGuiScreen extends Screen {
    private final List<CategoryPanel> panels = new ArrayList<>();
    private boolean initialized = false;
    private long openTime;

    // Search bar state (static so panels can read it without a back-reference)
    public static String searchQuery = "";
    private static final int SEARCH_BAR_H = 16;
    private static final int SEARCH_BAR_W = 180;

    public NewGuiScreen() {
        super(Text.literal("Mizu"));
    }

    @Override
    protected void init() {
        super.init();
        meteordevelopment.meteorclient.gui.newgui.components.NewGuiBindCapture.ensureSubscribed();

        openTime = System.currentTimeMillis();
        searchQuery = "";

        try {
            meteordevelopment.meteorclient.systems.modules.gui.Gui guiMod =
                meteordevelopment.meteorclient.systems.modules.Modules.get()
                    .get(meteordevelopment.meteorclient.systems.modules.gui.Gui.class);
            if (guiMod != null) guiMod.applyToFontManager();
        } catch (Throwable ignored) {}

        if (!initialized) {
            int gap = CategoryPanel.getNativeGap();
            int startX = 4;
            int startY = SEARCH_BAR_H + 6; // leave room for search bar
            int rowSpacing = 4;

            for (Category category : Modules.loopCategories()) {
                panels.add(new CategoryPanel(category, 0, startY));
            }

            int maxWidth = this.width - startX;
            int currentX = startX;
            int currentY = startY;
            int rowTallest = 0;

            for (CategoryPanel panel : panels) {
                int pw = panel.getWidth();
                if (currentX != startX && currentX + pw > maxWidth) {
                    currentX = startX;
                    currentY += rowTallest + rowSpacing;
                    rowTallest = 0;
                }
                panel.setX(currentX);
                panel.setY(currentY);
                currentX += pw + gap;

                int ph = panel.getTotalHeight();
                if (ph > rowTallest) rowTallest = ph;
            }

            initialized = true;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        float animProgress = getAnimProgress();
        for (int i = panels.size() - 1; i >= 0; i--) {
            panels.get(i).render(context, mouseX, mouseY, animProgress);
        }

        drawSearchBar(context, mouseX, mouseY);
    }

    private void drawSearchBar(DrawContext context, int mouseX, int mouseY) {
        int barX = (this.width - SEARCH_BAR_W) / 2;
        int barY = 2;

        // Background
        context.fill(barX, barY, barX + SEARCH_BAR_W, barY + SEARCH_BAR_H, 0xFF060d18);

        // Border — teal when query active, dark teal when empty
        boolean active = !searchQuery.isEmpty();
        int borderColor = active ? 0xFF1D9E75 : 0xFF0D3A5C;
        context.drawBorder(barX, barY, SEARCH_BAR_W, SEARCH_BAR_H, borderColor);

        // Text
        String display = searchQuery.isEmpty() ? "search modules..." : searchQuery;
        int textColor = searchQuery.isEmpty() ? 0xFF185FA5 : 0xFFF0F0FA;
        context.drawText(client.textRenderer, display, barX + 5, barY + 4, textColor, false);

        // Blinking cursor
        if (active && (System.currentTimeMillis() / 500) % 2 == 0) {
            int cursorX = barX + 5 + client.textRenderer.getWidth(searchQuery);
            context.fill(cursorX, barY + 3, cursorX + 1, barY + SEARCH_BAR_H - 3, 0xFF1D9E75);
        }
    }

    private float getAnimProgress() {
        long elapsed = System.currentTimeMillis() - openTime;
        return Math.min(1f, elapsed / 1500f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseClicked((int) mouseX, (int) mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels) panel.mouseReleased((int) mouseX, (int) mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseScrolled((int) mouseX, (int) mouseY, verticalAmount)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModuleButton.onKeyPressed(keyCode)) return true;

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
            searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (!searchQuery.isEmpty()) { searchQuery = ""; return true; }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (ModuleButton.onCharTyped(chr)) return true;
        if (chr >= 32 && chr != 127) {
            searchQuery += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        meteordevelopment.meteorclient.systems.modules.Modules modules =
            meteordevelopment.meteorclient.systems.modules.Modules.get();
        if (modules != null && modules.isBinding()) modules.setModuleToBind(null);
        meteordevelopment.meteorclient.gui.newgui.components.NewGuiBindCapture.get().cancelSettingListen();
        meteordevelopment.meteorclient.gui.newgui.components.SettingGroupRenderer.commitStringEdit();
        searchQuery = "";
        super.close();
    }
}
