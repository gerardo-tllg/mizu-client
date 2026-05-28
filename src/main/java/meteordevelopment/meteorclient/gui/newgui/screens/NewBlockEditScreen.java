/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.screens;

import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.components.SettingGroupRenderer;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.Settings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Renders a captured {@link Settings} (one block's settings from a
 * {@link meteordevelopment.meteorclient.settings.BlockDataSetting}, or any
 * other settings object) in our theme via {@link SettingGroupRenderer}.
 *
 * <p>The Settings is obtained by {@link NewBlockDataScreen} via
 * {@code CapturingGuiTheme} — it reuses Meteor's existing T-specific
 * {@code createScreen()} builder logic, but renders the result in our theme.</p>
 *
 * <p>Back button at top-left returns to the provided previous screen.</p>
 */
public class NewBlockEditScreen extends Screen {
    private final Settings settings;
    private final Screen returnTo;

    // Layout
    private int contentX;
    private int contentY;
    private int contentWidth;
    private int contentHeight;

    // Scroll
    private int scroll = 0;

    private static final int PADDING = 8;
    private static final int TITLE_HEIGHT = 20;
    private static final int BACK_BUTTON_WIDTH = 60;

    public NewBlockEditScreen(String title, Settings settings, Screen returnTo) {
        super(Text.literal(title));
        this.settings = settings;
        this.returnTo = returnTo;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    protected void init() {
        super.init();
        int margin = Math.max(20, this.width / 6);
        contentX = margin;
        contentWidth = this.width - margin * 2;
        contentY = TITLE_HEIGHT + PADDING;
        contentHeight = this.height - contentY - PADDING;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        FontManager fm = FontManager.get();
        super.render(context, mouseX, mouseY, delta);

        int lineColor = fm.primaryAlpha(180);
        int fillColor = fm.secondaryAlpha(200);
        int rowH = fm.getRowHeight();

        // --- Title bar ---
        // Back button
        int backY = 4;
        int backX = contentX;
        boolean backHovered = mouseX >= backX && mouseX < backX + BACK_BUTTON_WIDTH
            && mouseY >= backY && mouseY < backY + rowH + 2;
        int backFill = backHovered ? fm.secondaryAlpha(220) : fm.secondaryAlpha(160);
        context.fill(backX, backY, backX + BACK_BUTTON_WIDTH, backY + rowH + 2, backFill);
        RenderUtils.drawThinOutline(context, backX, backY, BACK_BUTTON_WIDTH, rowH + 2, lineColor);
        String backLabel = "\u2190 Back";
        fm.drawText(context, backLabel,
            backX + (BACK_BUTTON_WIDTH - fm.getTextWidth(backLabel)) / 2,
            backY + (rowH + 2 - fm.getTextHeight()) / 2,
            fm.getTextColor());

        // Title
        fm.drawText(context, this.title.getString(),
            backX + BACK_BUTTON_WIDTH + PADDING, backY + (rowH + 2 - fm.getTextHeight()) / 2,
            fm.getTextColor());

        // --- Content panel ---
        context.fill(contentX, contentY, contentX + contentWidth, contentY + contentHeight, fm.primaryAlpha(120));
        RenderUtils.drawThinOutline(context, contentX, contentY, contentWidth, contentHeight, lineColor);

        // Settings rendered inside a clipped region, vertically scrollable
        int innerX = contentX + PADDING;
        int innerY = contentY + PADDING;
        int innerWidth = contentWidth - PADDING * 2;
        int innerHeight = contentHeight - PADDING * 2;

        int totalH = SettingGroupRenderer.getGroupsHeight(settings);
        int drawY = innerY - scroll;

        context.enableScissor(innerX, innerY, innerX + innerWidth, innerY + innerHeight);
        SettingGroupRenderer.renderGroups(context, settings, innerX, drawY, innerWidth, mouseX, mouseY);
        context.disableScissor();

        // Scroll indicator
        if (totalH > innerHeight) {
            int sx = contentX + contentWidth - 3;
            int scrollH = Math.max(10, innerHeight * innerHeight / totalH);
            int scrollY = innerY + (scroll * (innerHeight - scrollH)) / Math.max(1, totalH - innerHeight);
            context.fill(sx, scrollY, sx + 2, scrollY + scrollH, fm.secondaryAlpha(220));
        }

        // Empty-state hint
        if (totalH == 0) {
            String msg = "(no settings)";
            fm.drawText(context, msg,
                contentX + (contentWidth - fm.getTextWidth(msg)) / 2,
                contentY + (contentHeight - fm.getTextHeight()) / 2,
                GuiColors.TEXT_DISABLED);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();

        // Back button
        int backY = 4;
        int backX = contentX;
        if (mouseX >= backX && mouseX < backX + BACK_BUTTON_WIDTH
            && mouseY >= backY && mouseY < backY + rowH + 2) {
            MinecraftClient.getInstance().setScreen(returnTo);
            return true;
        }

        // Commit any inline string edit before a settings-area click
        SettingGroupRenderer.commitStringEdit();

        // Content area — delegate to renderer
        int innerX = contentX + PADDING;
        int innerY = contentY + PADDING;
        int innerWidth = contentWidth - PADDING * 2;
        int innerHeight = contentHeight - PADDING * 2;

        if (mouseX >= innerX && mouseX < innerX + innerWidth
            && mouseY >= innerY && mouseY < innerY + innerHeight) {
            // Translate mouseY for scroll
            int drawY = innerY - scroll;
            boolean handled = SettingGroupRenderer.mouseClickedGroups(
                settings, innerX, drawY, innerWidth,
                (int) mouseX, (int) mouseY, button,
                () -> null);  // no fallback — unknown setting types simply do nothing
            if (handled) return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        SettingGroupRenderer.mouseReleasedGroups();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        FontManager fm = FontManager.get();
        int innerHeight = contentHeight - PADDING * 2;
        int totalH = SettingGroupRenderer.getGroupsHeight(settings);
        int maxScroll = Math.max(0, totalH - innerHeight);
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (verticalAmount * fm.getRowHeight() * 2)));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Let the renderer consume the keystroke (string edit, keybind capture, etc.)
        if (SettingGroupRenderer.onKeyPressed(keyCode)) return true;
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            MinecraftClient.getInstance().setScreen(returnTo);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (SettingGroupRenderer.onCharTyped(chr)) return true;
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        SettingGroupRenderer.commitStringEdit();
        MinecraftClient.getInstance().setScreen(returnTo);
    }
}
