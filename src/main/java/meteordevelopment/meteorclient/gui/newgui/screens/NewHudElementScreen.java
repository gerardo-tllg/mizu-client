/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.screens;

import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.components.SettingGroupRenderer;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.XAnchor;
import meteordevelopment.meteorclient.systems.hud.YAnchor;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Per-element settings screen as a clickgui {@link
 * meteordevelopment.meteorclient.gui.newgui.components.CategoryPanel}-style
 * floating panel: header + settings list + an Active/Remove row pinned inside
 * the panel body. Nothing darkens the rest of the screen — the live HUD
 * renders through under the unscaled projection.
 */
public class NewHudElementScreen extends Screen {
    private final HudElement element;
    private final Screen returnTo;
    private final Settings anchorSettings;

    private int panelX, panelY, panelWidth, panelHeight;
    private int listX, listY, listW, listH;
    private int barY, barHeight;
    private int scroll = 0;

    private static final int PANEL_WIDTH = 200;

    public NewHudElementScreen(HudElement element, Screen returnTo) {
        super(Text.literal(element.info.title));
        this.element = element;
        this.returnTo = returnTo;

        this.anchorSettings = new Settings();
        SettingGroup sg = anchorSettings.createGroup("Anchors");
        sg.add(new BoolSetting.Builder()
            .name("auto-anchors")
            .description("Automatically assigns anchors based on the position.")
            .defaultValue(true)
            .onModuleActivated(s -> s.set(element.autoAnchors))
            .onChanged(v -> {
                if (v) element.box.updateAnchors();
                element.autoAnchors = v;
            })
            .build()
        );
        sg.add(new EnumSetting.Builder<XAnchor>()
            .name("x-anchor")
            .description("Horizontal anchor.")
            .defaultValue(XAnchor.Left)
            .visible(() -> !element.autoAnchors)
            .onModuleActivated(s -> s.set(element.box.xAnchor))
            .onChanged(element.box::setXAnchor)
            .build()
        );
        sg.add(new EnumSetting.Builder<YAnchor>()
            .name("y-anchor")
            .description("Vertical anchor.")
            .defaultValue(YAnchor.Top)
            .visible(() -> !element.autoAnchors)
            .onModuleActivated(s -> s.set(element.box.yAnchor))
            .onChanged(element.box::setYAnchor)
            .build()
        );
        anchorSettings.onActivated();
        element.settings.onActivated();
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    protected void init() {
        super.init();
        layoutPanel();
    }

    private void layoutPanel() {
        FontManager fm = FontManager.get();
        int headerH = fm.getHeaderHeight();
        int rowH = fm.getRowHeight();
        barHeight = rowH + 2;

        panelWidth = Math.min(PANEL_WIDTH, this.width - 20);

        int totalContent = SettingGroupRenderer.getGroupsHeight(anchorSettings)
            + SettingGroupRenderer.getGroupsHeight(element.settings);
        int desiredListH = Math.max(rowH, totalContent);
        int desiredH = headerH + desiredListH + barHeight;
        int maxH = this.height - 20;
        panelHeight = Math.min(desiredH, maxH);

        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        listX = panelX;
        listY = panelY + headerH;
        listW = panelWidth;
        listH = panelHeight - headerH - barHeight;

        barY = panelY + panelHeight - barHeight;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Live HUD underneath; no dimming, no vanilla renderBackground.
        org.joml.Matrix4f savedProj = new org.joml.Matrix4f(com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix());
        meteordevelopment.meteorclient.utils.Utils.unscaledProjection();
        HudEditorScreen.renderElements(context);
        com.mojang.blaze3d.systems.RenderSystem.setProjectionMatrix(savedProj, com.mojang.blaze3d.systems.ProjectionType.ORTHOGRAPHIC);

        layoutPanel();

        FontManager fm = FontManager.get();
        int headerH = fm.getHeaderHeight();
        int rowH = fm.getRowHeight();
        int outlineColor = fm.primaryAlpha(170);
        int headerColor = fm.primaryAlpha(220);

        // Back button (top-left, outside the panel).
        int backY = 4;
        int backX = 10;
        int backW = 60;
        boolean backHovered = mouseX >= backX && mouseX < backX + backW
            && mouseY >= backY && mouseY < backY + rowH + 2;
        int backFill = backHovered ? fm.secondaryAlpha(220) : fm.secondaryAlpha(160);
        context.fill(backX, backY, backX + backW, backY + rowH + 2, backFill);
        RenderUtils.drawThinOutline(context, backX, backY, backW, rowH + 2, outlineColor);
        String backLabel = "\u2190 Back";
        fm.drawText(context, backLabel,
            backX + (backW - fm.getTextWidth(backLabel)) / 2,
            backY + (rowH + 2 - fm.getTextHeight()) / 2,
            fm.getTextColor());

        // Floating panel frame + header with element title.
        RenderUtils.drawThickOutline(context, panelX, panelY, panelWidth, panelHeight, 3, outlineColor);
        RenderUtils.fillNative(context, panelX, panelY, panelWidth, headerH, headerColor);

        context.enableScissor(panelX, panelY, panelX + panelWidth, panelY + headerH);
        fm.drawText(context, element.info.title,
            panelX + 4, panelY + (headerH - fm.getTextHeight()) / 2,
            fm.getTextColor());
        context.disableScissor();

        // Settings list.
        context.enableScissor(listX, listY, listX + listW, listY + listH);
        int drawY = listY - scroll;
        drawY = SettingGroupRenderer.renderGroups(context, anchorSettings, listX, drawY, listW, mouseX, mouseY);
        if (element.settings.sizeGroups() > 0) {
            drawY = SettingGroupRenderer.renderGroups(context, element.settings, listX, drawY, listW, mouseX, mouseY);
        }
        context.disableScissor();

        int totalH = SettingGroupRenderer.getGroupsHeight(anchorSettings)
            + SettingGroupRenderer.getGroupsHeight(element.settings);
        int maxScroll = Math.max(0, totalH - listH);
        if (maxScroll > 0) {
            int trackH = Math.max(1, listH - 2);
            int thumbH = Math.max(8, (int) ((long) listH * trackH / Math.max(1, totalH)));
            int thumbY = listY + 1 + (int) ((long) (trackH - thumbH) * scroll / maxScroll);
            int sbX = panelX + panelWidth - 2;
            context.fill(sbX, thumbY, sbX + 1, thumbY + thumbH, fm.primaryAlpha(220));
        }

        if (totalH == 0) {
            String msg = "(no settings)";
            fm.drawText(context, msg,
                listX + (listW - fm.getTextWidth(msg)) / 2,
                listY + (listH - fm.getTextHeight()) / 2,
                GuiColors.TEXT_DISABLED);
        }

        // Active / Remove row pinned inside the panel body, right above the outline.
        int halfW = panelWidth / 2;
        int activeX = panelX;
        int removeX = panelX + halfW;
        int activeW = halfW;
        int removeW = panelWidth - halfW;

        boolean activeHover = mouseX >= activeX && mouseX < activeX + activeW
            && mouseY >= barY && mouseY < barY + barHeight;
        boolean removeHover = mouseX >= removeX && mouseX < removeX + removeW
            && mouseY >= barY && mouseY < barY + barHeight;

        int activeFill = element.isActive()
            ? (activeHover ? 0xFF3BC660 : 0xFF2F9E4D)
            : (activeHover ? fm.secondaryAlpha(240) : fm.secondaryAlpha(180));
        RenderUtils.fillNative(context, activeX, barY, activeW, barHeight, activeFill);

        int removeFill = removeHover ? 0xFFC83232 : 0xFF9E2A2A;
        RenderUtils.fillNative(context, removeX, barY, removeW, barHeight, removeFill);

        // Thin divider between the two halves.
        RenderUtils.fillNative(context, removeX, barY, 1, barHeight, outlineColor);
        // Thin separator from the settings list above.
        RenderUtils.fillNative(context, panelX, barY, panelWidth, 1, outlineColor);

        String activeLabel = element.isActive() ? "Active: yes" : "Active: no";
        fm.drawText(context, activeLabel,
            activeX + (activeW - fm.getTextWidth(activeLabel)) / 2,
            barY + (barHeight - fm.getTextHeight()) / 2,
            fm.getTextColor());
        String removeLabel = "Remove";
        fm.drawText(context, removeLabel,
            removeX + (removeW - fm.getTextWidth(removeLabel)) / 2,
            barY + (barHeight - fm.getTextHeight()) / 2,
            fm.getTextColor());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();

        int backX = 10;
        int backY = 4;
        int backW = 60;
        if (mouseX >= backX && mouseX < backX + backW
            && mouseY >= backY && mouseY < backY + rowH + 2) {
            MinecraftClient.getInstance().setScreen(returnTo);
            return true;
        }

        int halfW = panelWidth / 2;
        int activeX = panelX;
        int removeX = panelX + halfW;
        int activeW = halfW;
        int removeW = panelWidth - halfW;
        if (mouseX >= activeX && mouseX < activeX + activeW
            && mouseY >= barY && mouseY < barY + barHeight) {
            element.toggle();
            return true;
        }
        if (mouseX >= removeX && mouseX < removeX + removeW
            && mouseY >= barY && mouseY < barY + barHeight) {
            element.remove();
            MinecraftClient.getInstance().setScreen(returnTo);
            return true;
        }

        SettingGroupRenderer.commitStringEdit();

        if (mouseX >= listX && mouseX < listX + listW
            && mouseY >= listY && mouseY < listY + listH) {
            int drawY = listY - scroll;
            if (SettingGroupRenderer.mouseClickedGroups(
                    anchorSettings, listX, drawY, listW,
                    (int) mouseX, (int) mouseY, button, () -> null)) return true;
            drawY += SettingGroupRenderer.getGroupsHeight(anchorSettings);
            if (SettingGroupRenderer.mouseClickedGroups(
                    element.settings, listX, drawY, listW,
                    (int) mouseX, (int) mouseY, button, () -> null)) return true;
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
        if (mouseX < listX || mouseX >= listX + listW || mouseY < listY || mouseY >= listY + listH) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        FontManager fm = FontManager.get();
        int totalH = SettingGroupRenderer.getGroupsHeight(anchorSettings)
            + SettingGroupRenderer.getGroupsHeight(element.settings);
        int maxScroll = Math.max(0, totalH - listH);
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (verticalAmount * fm.getRowHeight() * 2)));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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
