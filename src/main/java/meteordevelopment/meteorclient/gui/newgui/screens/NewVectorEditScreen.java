/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.screens;

import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.BlockPosSetting;
import meteordevelopment.meteorclient.settings.Vector3dSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

/**
 * Edit screen for {@link BlockPosSetting} and {@link Vector3dSetting}.
 * Three labelled input fields for X / Y / Z, Tab cycles, Enter commits, "From Me"
 * fills from the player's current position (if available).
 */
public class NewVectorEditScreen extends Screen {
    private final BlockPosSetting blockPosSetting; // nullable
    private final Vector3dSetting vectorSetting;   // nullable

    private String[] fields = new String[3];
    private int focusIndex = 0;

    private int panelX, panelY, panelWidth, panelHeight;

    private static final int PADDING = 8;
    private static final int FIELD_HEIGHT = 22;

    public NewVectorEditScreen(BlockPosSetting setting) {
        super(Text.literal("Edit " + setting.name));
        this.blockPosSetting = setting;
        this.vectorSetting = null;
        BlockPos p = setting.get();
        fields[0] = String.valueOf(p.getX());
        fields[1] = String.valueOf(p.getY());
        fields[2] = String.valueOf(p.getZ());
    }

    public NewVectorEditScreen(Vector3dSetting setting) {
        super(Text.literal("Edit " + setting.name));
        this.blockPosSetting = null;
        this.vectorSetting = setting;
        Vector3d v = setting.get();
        int dp = Math.max(1, setting.decimalPlaces);
        String fmt = "%." + dp + "f";
        fields[0] = String.format(fmt, v.x);
        fields[1] = String.format(fmt, v.y);
        fields[2] = String.format(fmt, v.z);
    }

    @Override
    protected void init() {
        super.init();
        panelWidth = Math.min(360, this.width - 40);
        panelHeight = FIELD_HEIGHT * 3 + PADDING * 4 + FIELD_HEIGHT + 20; // fields + buttons row + title
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        FontManager fm = FontManager.get();
        super.render(context, mouseX, mouseY, delta); // 1.21.5 pipeline handles blur internally

        int lineColor = fm.primaryAlpha(180);
        int headerH = fm.getHeaderHeight();

        // Panel
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, fm.primaryAlpha(160));
        RenderUtils.drawThinOutline(context, panelX, panelY, panelWidth, panelHeight, lineColor);

        // Title
        context.fill(panelX, panelY, panelX + panelWidth, panelY + headerH, fm.secondaryAlpha(200));
        RenderUtils.drawThinHLine(context, panelX, panelY + headerH, panelWidth, lineColor);
        fm.drawText(context, this.title.getString(), panelX + PADDING, panelY + (headerH - fm.getTextHeight()) / 2, fm.getTextColor());

        String[] labels = { "X:", "Y:", "Z:" };
        int y = panelY + headerH + PADDING;
        int fx = panelX + PADDING;
        int fw = panelWidth - PADDING * 2;
        int labelW = fm.getTextWidth("Z:") + 6;

        for (int i = 0; i < 3; i++) {
            // Label
            fm.drawText(context, labels[i], fx, y + (FIELD_HEIGHT - fm.getTextHeight()) / 2, fm.getTextColor());

            // Field background
            int inputX = fx + labelW;
            int inputW = fw - labelW;
            boolean focused = focusIndex == i;
            context.fill(inputX, y, inputX + inputW, y + FIELD_HEIGHT, focused ? fm.secondaryAlpha(220) : fm.secondaryAlpha(150));
            RenderUtils.drawThinOutline(context, inputX, y, inputW, FIELD_HEIGHT, lineColor);

            // Value + caret
            int textY = y + (FIELD_HEIGHT - fm.getTextHeight()) / 2;
            fm.drawText(context, fields[i], inputX + 4, textY, fm.getTextColor());
            if (focused && ((System.currentTimeMillis() / 500) & 1) == 0) {
                int caretX = inputX + 4 + fm.getTextWidth(fields[i]);
                context.fill(caretX, y + 3, caretX + 1, y + FIELD_HEIGHT - 3, fm.getTextColor());
            }

            y += FIELD_HEIGHT + PADDING;
        }

        // Buttons row: [From Me] [Cancel] [Apply]
        int btnY = y + 4;
        String fromMe = "[From Me]";
        String cancel = "[Cancel]";
        String apply = "[Apply]";
        int fromMeW = fm.getTextWidth(fromMe) + 12;
        int cancelW = fm.getTextWidth(cancel) + 12;
        int applyW = fm.getTextWidth(apply) + 12;
        int fromMeX = fx;
        int applyX = fx + fw - applyW;
        int cancelX = applyX - cancelW - 8;

        drawButton(context, fm, fromMe, fromMeX, btnY, fromMeW, FIELD_HEIGHT, lineColor);
        drawButton(context, fm, cancel, cancelX, btnY, cancelW, FIELD_HEIGHT, lineColor);
        drawButton(context, fm, apply, applyX, btnY, applyW, FIELD_HEIGHT, lineColor);

    }

    private void drawButton(DrawContext context, FontManager fm, String label, int x, int y, int w, int h, int lineColor) {
        context.fill(x, y, x + w, y + h, fm.secondaryAlpha(200));
        RenderUtils.drawThinOutline(context, x, y, w, h, lineColor);
        int tx = x + (w - fm.getTextWidth(label)) / 2;
        int ty = y + (h - fm.getTextHeight()) / 2;
        fm.drawText(context, label, tx, ty, fm.getTextColor());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int headerH = fm.getHeaderHeight();
        int y0 = panelY + headerH + PADDING;
        int fx = panelX + PADDING;
        int fw = panelWidth - PADDING * 2;
        int labelW = fm.getTextWidth("Z:") + 6;
        int inputX = fx + labelW;
        int inputW = fw - labelW;

        // Field hit tests
        for (int i = 0; i < 3; i++) {
            int y = y0 + i * (FIELD_HEIGHT + PADDING);
            if (mouseX >= inputX && mouseX < inputX + inputW && mouseY >= y && mouseY < y + FIELD_HEIGHT) {
                focusIndex = i;
                return true;
            }
        }

        // Buttons
        int btnY = y0 + 3 * (FIELD_HEIGHT + PADDING) + 4;
        String fromMe = "[From Me]";
        String cancel = "[Cancel]";
        String apply = "[Apply]";
        int fromMeW = fm.getTextWidth(fromMe) + 12;
        int cancelW = fm.getTextWidth(cancel) + 12;
        int applyW = fm.getTextWidth(apply) + 12;
        int fromMeX = fx;
        int applyX = fx + fw - applyW;
        int cancelX = applyX - cancelW - 8;

        if (inRect(mouseX, mouseY, fromMeX, btnY, fromMeW, FIELD_HEIGHT)) {
            fillFromPlayer();
            return true;
        }
        if (inRect(mouseX, mouseY, cancelX, btnY, cancelW, FIELD_HEIGHT)) {
            this.close();
            return true;
        }
        if (inRect(mouseX, mouseY, applyX, btnY, applyW, FIELD_HEIGHT)) {
            if (commit()) this.close();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (commit()) this.close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            focusIndex = (focusIndex + (hasShift(modifiers) ? 2 : 1)) % 3;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!fields[focusIndex].isEmpty()) fields[focusIndex] = fields[focusIndex].substring(0, fields[focusIndex].length() - 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (chr >= 32 && chr != 127) {
            boolean acceptsFloat = vectorSetting != null;
            boolean ok = (chr >= '0' && chr <= '9')
                || chr == '-'
                || (acceptsFloat && (chr == '.' || chr == 'e' || chr == 'E' || chr == '+'));
            if (ok && fields[focusIndex].length() < 16) {
                fields[focusIndex] += chr;
            }
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private boolean commit() {
        try {
            if (blockPosSetting != null) {
                int x = Integer.parseInt(fields[0].trim());
                int y = Integer.parseInt(fields[1].trim());
                int z = Integer.parseInt(fields[2].trim());
                blockPosSetting.set(new BlockPos(x, y, z));
                return true;
            } else if (vectorSetting != null) {
                double x = Double.parseDouble(fields[0].trim());
                double y = Double.parseDouble(fields[1].trim());
                double z = Double.parseDouble(fields[2].trim());
                // Clamp to setting min/max
                x = Math.max(vectorSetting.min, Math.min(vectorSetting.max, x));
                y = Math.max(vectorSetting.min, Math.min(vectorSetting.max, y));
                z = Math.max(vectorSetting.min, Math.min(vectorSetting.max, z));
                vectorSetting.set(new Vector3d(x, y, z));
                return true;
            }
        } catch (NumberFormatException e) {
            // Leave fields alone so the user can see what they typed.
        }
        return false;
    }

    private void fillFromPlayer() {
        PlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null) return;
        if (blockPosSetting != null) {
            BlockPos pos = p.getBlockPos();
            fields[0] = String.valueOf(pos.getX());
            fields[1] = String.valueOf(pos.getY());
            fields[2] = String.valueOf(pos.getZ());
        } else if (vectorSetting != null) {
            int dp = Math.max(1, vectorSetting.decimalPlaces);
            String fmt = "%." + dp + "f";
            fields[0] = String.format(fmt, p.getX());
            fields[1] = String.format(fmt, p.getY());
            fields[2] = String.format(fmt, p.getZ());
        }
    }

    private static boolean inRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static boolean hasShift(int modifiers) {
        return (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
    }
}
