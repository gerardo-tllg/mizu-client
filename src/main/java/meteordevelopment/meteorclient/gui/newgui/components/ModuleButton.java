/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.components;

import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/**
 * Renders a module's title row and — when expanded — delegates setting
 * rendering and input to {@link SettingGroupRenderer}.
 *
 * <p>Module-specific responsibilities that stay here:</p>
 * <ul>
 *   <li>Module title row rendering (active/inactive colors, marquee on hover)</li>
 *   <li>Expand animation</li>
 *   <li>Module-keybind capture (the last row inside the expanded module, separate
 *       from individual {@code KeybindSetting}s)</li>
 *   <li>Left-click toggles module, right-click expands</li>
 * </ul>
 */
public class ModuleButton {
    private final Module module;
    private final Category category;

    private static final int ACCENT_X = 3;
    private static ModuleButton listeningForKey = null;

    private boolean expanded = false;
    private float expandAnimation = 0f;

    public ModuleButton(Module module, Category category) {
        this.module = module;
        this.category = category;
    }

    private int getRowHeight() { return FontManager.get().getRowHeight(); }

    private int nativePixelGap() {
        float scale = (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
        return Math.max(1, (int) Math.ceil(1.0 / scale));
    }

    private int textY(int y, int rowH) {
        return y + (rowH - FontManager.get().getTextHeight()) / 2;
    }

    private void fillNative(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        float scale = (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
        var matrices = context.getMatrices();
        matrices.push();
        matrices.scale(1f / scale, 1f / scale, 1f);
        context.fill((int)(x1 * scale), (int)(y1 * scale),
                     (int)(x2 * scale), (int)(y2 * scale), color);
        matrices.pop();
    }

    // ---- RENDER ----

    public void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY) {
        FontManager fm = FontManager.get();
        int rowH = getRowHeight();

        float target = expanded ? 1f : 0f;
        expandAnimation += (target - expandAnimation) * 0.35f;
        if (Math.abs(expandAnimation - target) < 0.01f) expandAnimation = target;

        if (module.isActive()) {
            // Enabled-module highlight uses SECONDARY color with mild transparency.
            // Inset in NATIVE pixels so the gap-to-outline is precise regardless of
            // Minecraft's GUI scale: 3 native px for the outline + 1 native px for
            // the visible gap = 4 native px on each side.
            RenderUtils.fillNativeHInset(context, x, y, width, rowH, 4, fm.secondaryAlpha(160));
        }

        int textColor = module.isActive() ? fm.getTextColor() : fm.getTextSecondary();
        boolean titleHovered = mouseY >= y && mouseY < y + rowH && mouseX >= x && mouseX < x + width;
        context.enableScissor(x + 3, y, x + width - 3, y + rowH);
        fm.drawTextMarquee(context, module, module.title, x + 5, textY(y, rowH), x + width - 5, textColor, titleHovered);
        context.disableScissor();

        if (expandAnimation > 0.01f && hasAnyVisibleSetting()) {
            renderSettings(context, x, y + rowH + nativePixelGap(), width, mouseX, mouseY);
        }
    }

    private boolean hasAnyVisibleSetting() {
        for (SettingGroup group : module.settings) {
            for (Setting<?> setting : group) {
                if (setting.isVisible()) return true;
            }
        }
        return false;
    }

    private void renderSettings(DrawContext context, int x, int y, int width, int mouseX, int mouseY) {
        FontManager fm = FontManager.get();
        int settingsHeight = getSettingsHeight();
        int visibleHeight = (int) (settingsHeight * expandAnimation);

        context.enableScissor(x, y, x + width, y + visibleHeight);
        RenderUtils.drawThinVLine(context, x + ACCENT_X, y, settingsHeight, fm.secondaryAlpha(140));

        int currentY = SettingGroupRenderer.renderGroups(context, module.settings, x, y, width, mouseX, mouseY);

        renderModuleKeybind(context, fm, x, currentY, width);
        context.disableScissor();
    }

    /** Bottom row showing the module's own keybind (separate from per-setting KeybindSettings). */
    private void renderModuleKeybind(DrawContext context, FontManager fm, int x, int y, int width) {
        int rowH = getRowHeight();
        context.enableScissor(x, y, x + width, y + rowH);
        String text;
        int color;
        if (listeningForKey == this) {
            text = "Key ...";
            color = fm.getTextColor();
        } else {
            text = "Key " + getKeyName(module.keybind.getValue());
            color = GuiColors.TEXT_DISABLED;
        }
        fm.drawText(context, text, x + 6, textY(y, rowH), color);
        context.disableScissor();
    }

    private static String getKeyName(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return "NONE";
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null) return name.toUpperCase();
        return switch (key) {
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LShift";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RShift";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCtrl";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCtrl";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LAlt";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RAlt";
            case GLFW.GLFW_KEY_TAB -> "Tab";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "Caps";
            case GLFW.GLFW_KEY_SPACE -> "Space";
            case GLFW.GLFW_KEY_ENTER -> "Enter";
            case GLFW.GLFW_KEY_BACKSPACE -> "Back";
            case GLFW.GLFW_KEY_DELETE -> "Del";
            case GLFW.GLFW_KEY_INSERT -> "Ins";
            case GLFW.GLFW_KEY_HOME -> "Home";
            case GLFW.GLFW_KEY_END -> "End";
            case GLFW.GLFW_KEY_PAGE_UP -> "PgUp";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PgDn";
            case GLFW.GLFW_KEY_UP -> "Up";
            case GLFW.GLFW_KEY_DOWN -> "Down";
            case GLFW.GLFW_KEY_LEFT -> "Left";
            case GLFW.GLFW_KEY_RIGHT -> "Right";
            case GLFW.GLFW_KEY_ESCAPE -> "Esc";
            case GLFW.GLFW_KEY_F1 -> "F1";
            case GLFW.GLFW_KEY_F2 -> "F2";
            case GLFW.GLFW_KEY_F3 -> "F3";
            case GLFW.GLFW_KEY_F4 -> "F4";
            case GLFW.GLFW_KEY_F5 -> "F5";
            case GLFW.GLFW_KEY_F6 -> "F6";
            case GLFW.GLFW_KEY_F7 -> "F7";
            case GLFW.GLFW_KEY_F8 -> "F8";
            case GLFW.GLFW_KEY_F9 -> "F9";
            case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11";
            case GLFW.GLFW_KEY_F12 -> "F12";
            default -> "KEY " + key;
        };
    }

    /** Called from NewGuiScreen on key press. Module-keybind capture is handled
     *  on key RELEASE by {@link Modules#onKeyBinding}; this method only forwards
     *  to {@link SettingGroupRenderer} for string edits and other captures. */
    public static boolean onKeyPressed(int key) {
        return SettingGroupRenderer.onKeyPressed(key);
    }

    /** Called from NewGuiScreen on char typed. */
    public static boolean onCharTyped(char chr) {
        return SettingGroupRenderer.onCharTyped(chr);
    }

    /** True if any module button is awaiting input (module keybind or setting-level input). */
    public static boolean isListening() {
        return listeningForKey != null || SettingGroupRenderer.isListening();
    }

    /**
     * Returns a colored potion ItemStack to use as the visual for a StatusEffect.
     * Kept here as a convenience alias for callers that imported this before the
     * refactor (e.g. {@code NewAmplifierMapScreen}).
     */
    public static net.minecraft.item.ItemStack statusEffectIcon(net.minecraft.entity.effect.StatusEffect effect) {
        return SettingGroupRenderer.statusEffectIcon(effect);
    }

    // ---- CLICK ----

    public boolean mouseClicked(int x, int y, int width, int mouseX, int mouseY, int button) {
        int rowH = getRowHeight();

        // Module title row
        if (mouseY >= y && mouseY < y + rowH) {
            if (button == 0) { module.toggle(); module.sendToggledMsg(); return true; }
            if (button == 1 && hasAnyVisibleSetting()) { expanded = !expanded; return true; }
        }

        if (expanded && expandAnimation > 0.5f && hasAnyVisibleSetting()) {
            int settingsY = y + rowH + nativePixelGap();
            // Delegate to SettingGroupRenderer for all setting-row clicks
            if (SettingGroupRenderer.mouseClickedGroups(module.settings, x, settingsY, width,
                    mouseX, mouseY, button,
                    () -> GuiThemes.get().moduleScreen(module))) {
                return true;
            }

            // Module keybind row at the bottom
            int settingsHeight = SettingGroupRenderer.getGroupsHeight(module.settings);
            int keybindY = settingsY + settingsHeight;
            if (mouseY >= keybindY && mouseY < keybindY + rowH && button == 0) {
                // Route through Meteor's official binding flow so canBindTo,
                // modifier handling, and ModuleBindChangedEvent all fire. We
                // keep our own listening flag just for the "Key ..." label.
                listeningForKey = this;
                Modules.get().setModuleToBind(module);
                return true;
            }
        }

        return false;
    }

    /** Cleared by NewGuiBindCapture when ModuleBindChangedEvent fires. */
    public static void clearKeyListeningIfMatches(Module module) {
        if (listeningForKey != null && listeningForKey.module == module) {
            listeningForKey = null;
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        SettingGroupRenderer.mouseReleasedGroups();
    }

    // ---- Geometry ----

    public Module getModule() { return module; }

    public int getHeight() {
        int rowH = getRowHeight();
        if (expandAnimation < 0.01f) return rowH;
        int full = rowH + getSettingsHeight() + nativePixelGap();
        return (int) (rowH + (full - rowH) * expandAnimation);
    }

    /** Total height of the expanded settings area (groups + module-keybind row). */
    private int getSettingsHeight() {
        return SettingGroupRenderer.getGroupsHeight(module.settings) + getRowHeight();
    }

    public boolean isExpanded() { return expanded; }
}
