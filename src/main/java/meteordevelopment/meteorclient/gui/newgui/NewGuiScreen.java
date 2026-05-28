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

import java.util.ArrayList;
import java.util.List;

public class NewGuiScreen extends Screen {
    private final List<CategoryPanel> panels = new ArrayList<>();
    private boolean initialized = false;
    private long openTime;

    public NewGuiScreen() {
        super(Text.literal("ReviveClient"));
    }

    @Override
    protected void init() {
        super.init();
        // Ensure the keybind-capture handlers are on the event bus before any
        // module/setting bind can start.
        meteordevelopment.meteorclient.gui.newgui.components.NewGuiBindCapture.ensureSubscribed();

        // Reset animation timer each time the GUI opens
        openTime = System.currentTimeMillis();

        // Re-sync FontManager from the persisted Gui module settings each
        // time the GUI opens. Handles the case where Meteor loads NBT after
        // the Gui module constructor has already run with defaults.
        try {
            meteordevelopment.meteorclient.systems.modules.gui.Gui guiMod =
                meteordevelopment.meteorclient.systems.modules.Modules.get()
                    .get(meteordevelopment.meteorclient.systems.modules.gui.Gui.class);
            if (guiMod != null) guiMod.applyToFontManager();
        } catch (Throwable ignored) {}

        if (!initialized) {
            int gap = CategoryPanel.getNativeGap();
            int startX = 4;
            int startY = 10;
            int rowSpacing = 4;

            for (Category category : Modules.loopCategories()) {
                panels.add(new CategoryPanel(category, 0, startY));
            }

            // Panels now show their full module list (no height cap). Layout just
            // needs to lay them out left-to-right, wrapping when they'd exceed
            // the screen width. Each wrapped row's starting Y uses the tallest
            // panel from the previous row.
            int maxWidth = this.width - startX;
            int currentX = startX;
            int currentY = startY;
            int rowTallest = 0;

            for (CategoryPanel panel : panels) {
                int pw = panel.getWidth();
                if (currentX != startX && currentX + pw > maxWidth) {
                    // Wrap to next row using the previous row's tallest panel
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
        // IMPORTANT: super.render in 1.21.5 sets up the framebuffer + blur via the
        // vanilla pipeline. Calling applyBlur() directly outside that path triggers
        // GL_INVALID_ENUM because the post-effect pipeline was rewritten in 1.21.5.
        super.render(context, mouseX, mouseY, delta);

        float animProgress = getAnimProgress();

        for (int i = panels.size() - 1; i >= 0; i--) {
            panels.get(i).render(context, mouseX, mouseY, animProgress);
        }
    }

    /** Animation progress: 0 to 1 over ~1.5 seconds, stays at 1 after. */
    private float getAnimProgress() {
        long elapsed = System.currentTimeMillis() - openTime;
        return Math.min(1f, elapsed / 1500f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseClicked((int) mouseX, (int) mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels) {
            panel.mouseReleased((int) mouseX, (int) mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseScrolled((int) mouseX, (int) mouseY, verticalAmount)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModuleButton.onKeyPressed(keyCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (ModuleButton.onCharTyped(chr)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        // Don't leak a pending keybind capture — if the user dismissed the gui
        // while "listening" for a module bind, any subsequent in-game key would
        // otherwise rebind that module.
        meteordevelopment.meteorclient.systems.modules.Modules modules =
            meteordevelopment.meteorclient.systems.modules.Modules.get();
        if (modules != null && modules.isBinding()) modules.setModuleToBind(null);
        meteordevelopment.meteorclient.gui.newgui.components.NewGuiBindCapture.get().cancelSettingListen();
        meteordevelopment.meteorclient.gui.newgui.components.SettingGroupRenderer.commitStringEdit();
        super.close();
    }
}
