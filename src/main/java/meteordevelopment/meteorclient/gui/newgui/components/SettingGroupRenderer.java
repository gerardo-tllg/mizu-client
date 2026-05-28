/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.components;

import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.screens.NewAmplifierMapScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewCollectionListScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewColorListScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewEntityTypeScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewSingleValueScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewStringListScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewVectorEditScreen;
import meteordevelopment.meteorclient.gui.newgui.util.ColorUtils;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.FontFamily;
import meteordevelopment.meteorclient.renderer.text.FontInfo;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Renders and handles input for an arbitrary {@link SettingGroup} or list of them.
 * Extracted from {@link ModuleButton} so that any screen (HUD editor, BlockData
 * sub-screen, Generic object editor) can reuse the same setting renderer.
 *
 * <p>All editing state (inline-editing strings, listening for keybinds, color
 * picker state, slider drag state) is global and static — only one interaction
 * is active at a time across the entire GUI.</p>
 *
 * <p>Usage from a screen:
 * <pre>
 * // In render:
 * SettingGroupRenderer.renderGroups(context, groups, x, y, width, mouseX, mouseY);
 *
 * // In mouseClicked:
 * SettingGroupRenderer.mouseClickedGroups(groups, x, y, width, mouseX, mouseY, button, fallback);
 *
 * // In screen close / page change:
 * SettingGroupRenderer.commitStringEdit();
 * </pre>
 */
public final class SettingGroupRenderer {
    static final int ACCENT_X = 3;

    // ---- Global input state ----
    // These are static because only one setting interaction is active at any time.
    static StringSetting editingString = null;
    static String editingBuffer = "";
    static boolean draggingSlider = false;
    static Setting<?> draggingSetting = null;
    static ColorSetting draggingColorSetting = null;
    static int draggingColorComponent = -1;
    static final Map<ColorSetting, PickerState> pickerStates = new HashMap<>();

    private SettingGroupRenderer() {}

    // ---- Public render / click API ----

    /** Render all visible settings in the given groups starting at (x, y).
     *  Returns the final Y after the last row. */
    public static int renderGroups(DrawContext context, Iterable<SettingGroup> groups,
                                   int x, int y, int width, int mouseX, int mouseY) {
        // Follow the cursor while a slider/picker is being dragged.
        tickDrag(mouseX, mouseY);
        FontManager fm = FontManager.get();
        int currentY = y;
        for (SettingGroup group : groups) {
            currentY = renderSettingGroup(context, fm, group, x, currentY, width, mouseX, mouseY);
        }
        return currentY;
    }

    /** Total pixel height the groups will occupy when fully rendered. */
    public static int getGroupsHeight(Iterable<SettingGroup> groups) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int total = 0;
        for (SettingGroup group : groups) {
            total += rowH; // header
            if (group.sectionExpanded) {
                for (Setting<?> setting : group) {
                    if (!setting.isVisible()) continue;
                    total += getSettingRows(setting) * rowH;
                }
            }
        }
        return total;
    }

    /** Handle a mouse click against all visible settings in the groups.
     *  The {@code fallback} supplier is invoked when a setting falls back to
     *  Meteor's original editing screen (BlockData, Generic, etc.).
     *  Returns true if the click was consumed. */
    public static boolean mouseClickedGroups(Iterable<SettingGroup> groups, int x, int y, int width,
                                             int mouseX, int mouseY, int button,
                                             Supplier<Screen> fallback) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int settingY = y;
        for (SettingGroup group : groups) {
            if (mouseY >= settingY && mouseY < settingY + rowH) {
                if (button == 0 || button == 1) {
                    group.sectionExpanded = !group.sectionExpanded;
                    return true;
                }
            }
            settingY += rowH;

            if (group.sectionExpanded) {
                for (Setting<?> setting : group) {
                    if (!setting.isVisible()) continue;
                    int consumed = handleSettingClick(setting, x, settingY, width, mouseX, mouseY, button, fallback);
                    if (consumed < 0) return true;
                    settingY += consumed;
                }
            }
        }
        return false;
    }

    /** Reset any in-progress drag state. Call from the owning screen's mouseReleased. */
    public static void mouseReleasedGroups() {
        draggingSlider = false;
        draggingSetting = null;
        draggingColorSetting = null;
        draggingColorComponent = -1;
    }

    /** Handle a key press while something in SettingGroupRenderer is listening.
     *  Returns true if the key was consumed. */
    public static boolean onKeyPressed(int key) {
        // String-edit mode
        if (editingString != null) {
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                commitStringEdit();
                return true;
            }
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                editingString = null;
                editingBuffer = "";
                return true;
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (!editingBuffer.isEmpty()) editingBuffer = editingBuffer.substring(0, editingBuffer.length() - 1);
                return true;
            }
            return true;
        }

        // Per-setting KeybindSetting capture is handled on the event bus by
        // NewGuiBindCapture so that canBindTo/modifiers are respected and the
        // key event is cancelled; nothing to do here.

        return false;
    }

    /** Handle a char while inline-editing a string setting. */
    public static boolean onCharTyped(char chr) {
        if (editingString == null) return false;
        if (chr >= 32 && chr != 127) {
            String next = editingBuffer + chr;
            if (editingString.filter == null || editingString.filter.filter(next, chr)) {
                editingBuffer = next;
            }
        }
        return true;
    }

    /** True if any form of input capture is active (string edit, keybind listen, etc.). */
    public static boolean isListening() {
        return NewGuiBindCapture.get().isListeningForSetting() || editingString != null;
    }

    /** Commit any pending inline-string edit to its setting. Safe when idle. */
    public static void commitStringEdit() {
        if (editingString != null) {
            editingString.set(editingBuffer);
            editingString = null;
            editingBuffer = "";
        }
    }

    /** Update drag state — call from the owning screen's mouseDragged or per-frame. */
    public static void tickDrag(int mouseX, int mouseY) {
        if (draggingSlider && draggingSetting instanceof IntSetting is) {
            DragCtx ctx = dragCtx;
            if (ctx != null) updateIntSlider(is, ctx.x, ctx.width, mouseX);
        } else if (draggingSlider && draggingSetting instanceof DoubleSetting ds) {
            DragCtx ctx = dragCtx;
            if (ctx != null) updateDoubleSlider(ds, ctx.x, ctx.width, mouseX);
        } else if (draggingColorSetting != null && draggingColorComponent >= 0) {
            DragCtx ctx = dragCtx;
            if (ctx != null) updateColorSlider(draggingColorSetting, draggingColorComponent,
                ctx.x + ACCENT_X + 1, ctx.width - ACCENT_X - 2,
                mouseX, mouseY, ctx.svTop, ctx.svHeight);
        }
    }

    /** Legacy 1-arg overload: drag without mouseY info (used by pre-2D-picker callers). */
    public static void tickDrag(int mouseX) { tickDrag(mouseX, 0); }

    /** Capture of where a drag was initiated so tickDrag() can continue the motion. */
    private static DragCtx dragCtx;
    private static class DragCtx {
        int x; int width;
        int svTop, svHeight;
        DragCtx(int x, int w) { this.x = x; this.width = w; }
        DragCtx(int x, int w, int svTop, int svHeight) {
            this.x = x; this.width = w; this.svTop = svTop; this.svHeight = svHeight;
        }
    }

    // ---- Helpers (positioning, drawing) ----

    static int textY(int y, int rowH) {
        return y + (rowH - FontManager.get().getTextHeight()) / 2;
    }

    static void fillNative(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        float scale = (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
        var matrices = context.getMatrices();
        matrices.push();
        matrices.scale(1f / scale, 1f / scale, 1f);
        context.fill((int)(x1 * scale), (int)(y1 * scale),
                     (int)(x2 * scale), (int)(y2 * scale), color);
        matrices.pop();
    }

    // ---- Group + Setting dispatch ----

    /** Render a Meteor SettingGroup header and (if expanded) its visible settings. */
    private static int renderSettingGroup(DrawContext context, FontManager fm, SettingGroup group,
                                          int x, int y, int width, int mouseX, int mouseY) {
        int rowH = fm.getRowHeight();

        fillNative(context, x + ACCENT_X + 1, y, x + width - 1, y + rowH, fm.secondaryAlpha(180));
        int clipLeft = x + ACCENT_X + 1;
        int clipRight = x + width - 1;
        context.enableScissor(clipLeft, y, clipRight, y + rowH);
        boolean hovered = mouseY >= y && mouseY < y + rowH && mouseX >= x && mouseX < x + width;
        fm.drawTextMarquee(context, group, group.name, clipLeft + 3, textY(y, rowH), clipRight - 12, fm.getTextColor(), hovered);
        String toggle = group.sectionExpanded ? "-" : "+";
        fm.drawText(context, toggle, clipRight - fm.getTextWidth(toggle) - 2, textY(y, rowH), fm.getTextColor());
        context.disableScissor();
        y += rowH;

        if (group.sectionExpanded) {
            for (Setting<?> setting : group) {
                if (!setting.isVisible()) continue;
                y = renderSetting(context, fm, setting, x, y, width, mouseX, mouseY);
            }
        }
        return y;
    }

    /** Render a single setting and return the Y after it. */
    private static int renderSetting(DrawContext context, FontManager fm, Setting<?> setting,
                                     int x, int y, int width, int mouseX, int mouseY) {
        int rowH = fm.getRowHeight();
        boolean hovered = mouseY >= y && mouseY < y + rowH && mouseX >= x && mouseX < x + width;

        if (setting instanceof BoolSetting bs) {
            renderBoolSetting(context, fm, bs, x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof EnumSetting<?> es) {
            renderEnumSetting(context, fm, es, x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof IntSetting is) {
            renderIntSetting(context, fm, is, x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof DoubleSetting ds) {
            renderDoubleSetting(context, fm, ds, x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof ColorSetting cs) {
            renderColorSetting(context, fm, cs, x, y, width);
            return y + rowH * (1 + getPickerRows(cs));
        } else if (setting instanceof KeybindSetting ks) {
            renderKeybindSetting(context, fm, ks, x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof ProvidedStringSetting pss) {
            renderProvidedStringSetting(context, fm, pss, x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof FontFaceSetting fs) {
            renderFontFaceSetting(context, fm, fs, x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof BlockPosSetting bps) {
            renderBlockPosSetting(context, fm, bps, x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof Vector3dSetting vs) {
            renderVector3dSetting(context, fm, vs, x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof StringListSetting sls) {
            renderJumpRow(context, fm, setting, sls.get().size() + " entries", x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof ModuleListSetting mls) {
            renderJumpRow(context, fm, setting, mls.get().size() + " modules", x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof EntityTypeListSetting etls) {
            renderJumpRow(context, fm, setting, etls.get().size() + " entities", x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof EnchantmentListSetting els) {
            renderJumpRow(context, fm, setting, els.get().size() + " enchants", x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof BlockListSetting bls) {
            renderJumpRow(context, fm, setting, bls.get().size() + " blocks", x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof ItemListSetting ils) {
            renderJumpRow(context, fm, setting, ils.get().size() + " items", x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof StatusEffectListSetting sels) {
            renderJumpRow(context, fm, setting, sels.get().size() + " effects", x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof SoundEventListSetting sevls) {
            renderJumpRow(context, fm, setting, sevls.get().size() + " sounds", x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof ParticleTypeListSetting ptls) {
            renderJumpRow(context, fm, setting, ptls.get().size() + " particles", x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof PacketListSetting pls) {
            renderJumpRow(context, fm, setting, pls.get().size() + " packets", x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof ScreenHandlerListSetting shls) {
            renderJumpRow(context, fm, setting, shls.get().size() + " handlers", x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof StorageBlockListSetting sbls) {
            renderJumpRow(context, fm, setting, sbls.get().size() + " storages", x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof BlockSetting bs2) {
            String name = bs2.get() == null ? "(none)" : Names.get(bs2.get());
            renderJumpRow(context, fm, setting, name, x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof ItemSetting is2) {
            String name = is2.get() == null ? "(none)" : Names.get(is2.get());
            renderJumpRow(context, fm, setting, name, x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof ColorListSetting cls) {
            renderJumpRow(context, fm, setting, cls.get().size() + " colors", x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof StatusEffectAmplifierMapSetting seams) {
            renderJumpRow(context, fm, setting, seams.get().size() + " effects", x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof BlockDataSetting<?> bds) {
            // Count only blocks whose T is actually changed from defaults.
            int overrideCount = 0;
            for (var entry : bds.get().entrySet()) {
                var v = entry.getValue();
                if (v != null && v.isChanged()) overrideCount++;
            }
            String summary = overrideCount == 0 ? "no overrides"
                : overrideCount + " override" + (overrideCount == 1 ? "" : "s");
            renderJumpRow(context, fm, setting, summary, x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof GenericSetting) {
            renderJumpRow(context, fm, setting, "edit", x, y, width, hovered);
            return y + rowH;
        } else if (setting instanceof StringSetting ss) {
            renderStringSetting(context, fm, ss, x, y, width);
            return y + rowH;
        } else if (setting instanceof ActionSetting as) {
            renderJumpRow(context, fm, setting, as.buttonLabel, x, y, width, hovered);
            return y + rowH;
        } else {
            renderStubSetting(context, fm, setting, x, y, width);
            return y + rowH;
        }
    }

    /** Rows a setting occupies (usually 1; ColorSetting has extra rows when its picker is open). */
    static int getSettingRows(Setting<?> setting) {
        if (setting instanceof ColorSetting cs) return 1 + getPickerRows(cs);
        return 1;
    }

    // ---- Per-setting-type renderers ----

    private static void renderBoolSetting(DrawContext context, FontManager fm, BoolSetting setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        if (setting.get()) {
            fillNative(context, x + ACCENT_X + 1, y, x + width - 1, y + rowH, fm.secondaryAlpha(180));
        }
        int clipLeft = x + ACCENT_X + 1;
        int clipRight = x + width - 1;
        context.enableScissor(clipLeft, y, clipRight, y + rowH);
        int textColor = setting.get() ? fm.getTextColor() : fm.getTextSecondary();
        fm.drawTextMarquee(context, setting, setting.name, clipLeft + 3, textY(y, rowH), clipRight - 12, textColor, hovered);
        String toggle = setting.get() ? "+" : "-";
        int toggleColor = setting.get() ? fm.getTextColor() : GuiColors.TEXT_DISABLED;
        fm.drawText(context, toggle, clipRight - fm.getTextWidth(toggle) - 2, textY(y, rowH), toggleColor);
        context.disableScissor();
    }

    private static void renderEnumSetting(DrawContext context, FontManager fm, EnumSetting<?> setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        fillNative(context, x + ACCENT_X + 1, y, x + width - 1, y + rowH, fm.secondaryAlpha(180));
        int clipLeft = x + ACCENT_X + 1;
        int clipRight = x + width - 1;
        context.enableScissor(clipLeft, y, clipRight, y + rowH);
        fm.drawTextMarquee(context, setting, setting.name + ": " + setting.get().toString(), clipLeft + 3, textY(y, rowH), clipRight - 2, fm.getTextColor(), hovered);
        context.disableScissor();
    }

    private static void renderIntSetting(DrawContext context, FontManager fm, IntSetting setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        int sliderStart = x + ACCENT_X + 1;
        if (!setting.noSlider) {
            double range = setting.sliderMax - setting.sliderMin;
            float progress = range > 0 ? (float) ((setting.get() - setting.sliderMin) / range) : 0f;
            progress = Math.max(0, Math.min(1, progress));
            int fillWidth = Math.max(1, (int) ((width - ACCENT_X - 2) * progress));
            fillNative(context, sliderStart, y, sliderStart + fillWidth, y + rowH, fm.secondaryAlpha(120));
        }
        int clipLeft = x + ACCENT_X + 1;
        int clipRight = x + width - 1;
        context.enableScissor(clipLeft, y, clipRight, y + rowH);
        fm.drawTextMarquee(context, setting, setting.name + ": " + setting.get(), clipLeft + 3, textY(y, rowH), clipRight - 2, GuiColors.TEXT_SETTING_VALUE, hovered);
        context.disableScissor();
    }

    private static void renderDoubleSetting(DrawContext context, FontManager fm, DoubleSetting setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        int sliderStart = x + ACCENT_X + 1;
        if (!setting.noSlider) {
            double range = setting.sliderMax - setting.sliderMin;
            float progress = range > 0 ? (float) ((setting.get() - setting.sliderMin) / range) : 0f;
            progress = Math.max(0, Math.min(1, progress));
            int fillWidth = Math.max(1, (int) ((width - ACCENT_X - 2) * progress));
            fillNative(context, sliderStart, y, sliderStart + fillWidth, y + rowH, fm.secondaryAlpha(120));
        }
        int clipLeft = x + ACCENT_X + 1;
        int clipRight = x + width - 1;
        context.enableScissor(clipLeft, y, clipRight, y + rowH);
        int dp = Math.max(1, setting.decimalPlaces);
        String valueStr = String.format("%." + dp + "f", setting.get());
        fm.drawTextMarquee(context, setting, setting.name + ": " + valueStr, clipLeft + 3, textY(y, rowH), clipRight - 2, GuiColors.TEXT_SETTING_VALUE, hovered);
        context.disableScissor();
    }

    // Picker layout constants
    static final int SV_ROWS = 6;       // 2D saturation/brightness gradient height (in row-heights)
    static final int PICKER_ROWS = SV_ROWS + 3; // + hue + alpha + copy/paste

    private static void renderColorSetting(DrawContext context, FontManager fm, ColorSetting setting, int x, int y, int width) {
        int rowH = fm.getRowHeight();
        SettingColor c = setting.get();

        // Match other settings' row background
        fillNative(context, x + ACCENT_X + 1, y, x + width - 1, y + rowH, fm.secondaryAlpha(180));
        int clipLeft = x + ACCENT_X + 1;
        int clipRight = x + width - 1;

        // Small color swatch on the right (with checkerboard for alpha)
        int swatchSize = Math.min(Math.max(5, rowH - 5), 7);
        int swatchRight = clipRight - 3;
        int swatchLeft = swatchRight - swatchSize;
        int swatchTop = y + (rowH - swatchSize) / 2;
        int swatchBottom = swatchTop + swatchSize;
        drawCheckerboard(context, swatchLeft, swatchTop, swatchSize, swatchSize);
        int argb = ((c.a & 0xFF) << 24) | ((c.r & 0xFF) << 16) | ((c.g & 0xFF) << 8) | (c.b & 0xFF);
        fillNative(context, swatchLeft, swatchTop, swatchRight, swatchBottom, argb);
        RenderUtils.drawThinOutline(context, swatchLeft, swatchTop, swatchSize, swatchSize, GuiColors.BORDER_LIGHT);

        // Name text — scissor so it doesn't overlap the swatch
        context.enableScissor(clipLeft, y, swatchLeft - 2, y + rowH);
        fm.drawText(context, setting.name, clipLeft + 3, textY(y, rowH), fm.getTextColor());
        context.disableScissor();

        if (isPickerOpen(setting)) {
            renderColorPicker(context, fm, setting, x, y + rowH, width);
        }
    }

    private static void renderColorPicker(DrawContext context, FontManager fm, ColorSetting setting, int x, int y, int width) {
        PickerState ps = ensurePicker(setting);
        int rowH = fm.getRowHeight();
        int sliderStart = x + ACCENT_X + 1;
        int sliderWidth = width - ACCENT_X - 2;

        // 2D saturation/brightness gradient
        int svTop = y;
        int svHeight = rowH * SV_ROWS;
        drawSVGradient(context, ps.hue, sliderStart, svTop, sliderWidth, svHeight);
        RenderUtils.drawThinOutline(context, sliderStart, svTop, sliderWidth, svHeight, GuiColors.BORDER);
        int svX = sliderStart + (int) (ps.saturation * (sliderWidth - 1));
        int svY = svTop + (int) ((1f - ps.brightness) * (svHeight - 1));
        drawCrosshair(context, svX, svY);

        // Hue strip
        int hueRow = svTop + svHeight;
        drawHueStrip(context, sliderStart, hueRow, sliderWidth, rowH);
        RenderUtils.drawThinOutline(context, sliderStart, hueRow, sliderWidth, rowH, GuiColors.BORDER);
        int huePos = sliderStart + (int) (ps.hue * (sliderWidth - 1));
        drawBarMarker(context, huePos, hueRow, rowH);

        // Alpha strip (checkerboard + color→alpha gradient)
        int aRow = hueRow + rowH;
        SettingColor c = setting.get();
        drawCheckerboard(context, sliderStart, aRow, sliderWidth, rowH);
        drawAlphaGradient(context, c.r & 0xFF, c.g & 0xFF, c.b & 0xFF, sliderStart, aRow, sliderWidth, rowH);
        RenderUtils.drawThinOutline(context, sliderStart, aRow, sliderWidth, rowH, GuiColors.BORDER);
        int aPos = sliderStart + (int) (((c.a & 0xFF) / 255f) * (sliderWidth - 1));
        drawBarMarker(context, aPos, aRow, rowH);
        // Live alpha value — shadow for legibility over the gradient
        String alphaLabel = "A " + (c.a & 0xFF);
        fm.drawText(context, alphaLabel, sliderStart + 3, textY(aRow, rowH), 0xFFFFFFFF);

        // Copy | Paste buttons
        int btnRow = aRow + rowH;
        int halfW = sliderWidth / 2;
        drawPickerButton(context, fm, "Copy", sliderStart, btnRow, halfW - 1, rowH);
        drawPickerButton(context, fm, "Paste", sliderStart + halfW + 1, btnRow, sliderWidth - halfW - 1, rowH);
    }

    private static void drawHueStrip(DrawContext context, int x, int y, int w, int h) {
        for (int i = 0; i < w; i++) {
            float hue = (float) i / Math.max(1, w - 1);
            int rgb = Color.HSBtoRGB(hue, 1f, 1f);
            int argb = 0xFF000000 | (rgb & 0xFFFFFF);
            fillNative(context, x + i, y, x + i + 1, y + h, argb);
        }
    }

    private static void drawSVGradient(DrawContext context, float hue, int x, int y, int w, int h) {
        // Base: horizontal ramp from white to pure hue
        int pure = Color.HSBtoRGB(hue, 1f, 1f);
        int hr = (pure >> 16) & 0xFF;
        int hg = (pure >> 8) & 0xFF;
        int hb = pure & 0xFF;
        for (int i = 0; i < w; i++) {
            float sat = (float) i / Math.max(1, w - 1);
            int r = (int) ((1f - sat) * 255 + sat * hr);
            int g = (int) ((1f - sat) * 255 + sat * hg);
            int b = (int) ((1f - sat) * 255 + sat * hb);
            int baseColor = 0xFF000000 | (r << 16) | (g << 8) | b;
            fillNative(context, x + i, y, x + i + 1, y + h, baseColor);
        }
        // Overlay: vertical transparent→black to darken toward bottom
        for (int j = 0; j < h; j++) {
            float t = (float) j / Math.max(1, h - 1);
            int alpha = (int) (t * 255);
            int overlay = (alpha << 24);
            fillNative(context, x, y + j, x + w, y + j + 1, overlay);
        }
    }

    private static void drawAlphaGradient(DrawContext context, int r, int g, int b, int x, int y, int w, int h) {
        for (int i = 0; i < w; i++) {
            float t = (float) i / Math.max(1, w - 1);
            int alpha = (int) (t * 255);
            int color = (alpha << 24) | (r << 16) | (g << 8) | b;
            fillNative(context, x + i, y, x + i + 1, y + h, color);
        }
    }

    private static void drawCheckerboard(DrawContext context, int x, int y, int w, int h) {
        int square = 3;
        int lightGrey = 0xFFCCCCCC;
        int darkGrey = 0xFF808080;
        fillNative(context, x, y, x + w, y + h, lightGrey);
        for (int i = 0; i < w; i += square) {
            for (int j = 0; j < h; j += square) {
                if ((((i / square) + (j / square)) & 1) != 0) {
                    int x2 = Math.min(x + i + square, x + w);
                    int y2 = Math.min(y + j + square, y + h);
                    fillNative(context, x + i, y + j, x2, y2, darkGrey);
                }
            }
        }
    }

    private static void drawCrosshair(DrawContext context, int cx, int cy) {
        // Small white dot with a 1-pixel black outline so it's visible on any background
        fillNative(context, cx - 1, cy - 1, cx + 2, cy + 2, 0xFF000000);
        fillNative(context, cx, cy, cx + 1, cy + 1, 0xFFFFFFFF);
    }

    private static void drawBarMarker(DrawContext context, int cx, int y, int h) {
        fillNative(context, cx - 1, y - 1, cx + 2, y + h + 1, 0xFF000000);
        fillNative(context, cx, y, cx + 1, y + h, 0xFFFFFFFF);
    }

    private static void drawPickerButton(DrawContext context, FontManager fm, String label, int x, int y, int w, int h) {
        fillNative(context, x, y, x + w, y + h, fm.secondaryAlpha(200));
        RenderUtils.drawThinOutline(context, x, y, w, h, GuiColors.BORDER);
        int textW = fm.getTextWidth(label);
        fm.drawText(context, label, x + (w - textW) / 2, textY(y, h), fm.getTextColor());
    }

    private static void renderBlockPosSetting(DrawContext context, FontManager fm, BlockPosSetting setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        fillNative(context, x + ACCENT_X + 1, y, x + width - 1, y + rowH, fm.secondaryAlpha(180));
        int clipLeft = x + ACCENT_X + 1;
        int clipRight = x + width - 1;
        context.enableScissor(clipLeft, y, clipRight, y + rowH);
        var p = setting.get();
        String text = setting.name + ": " + p.getX() + "," + p.getY() + "," + p.getZ();
        fm.drawTextMarquee(context, setting, text, clipLeft + 3, textY(y, rowH), clipRight - 2, fm.getTextColor(), hovered);
        context.disableScissor();
    }

    private static void renderVector3dSetting(DrawContext context, FontManager fm, Vector3dSetting setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        fillNative(context, x + ACCENT_X + 1, y, x + width - 1, y + rowH, fm.secondaryAlpha(180));
        int clipLeft = x + ACCENT_X + 1;
        int clipRight = x + width - 1;
        context.enableScissor(clipLeft, y, clipRight, y + rowH);
        var v = setting.get();
        int dp = Math.max(1, setting.decimalPlaces);
        String fmt = "%." + dp + "f";
        String text = setting.name + ": " + String.format(fmt, v.x) + "," + String.format(fmt, v.y) + "," + String.format(fmt, v.z);
        fm.drawTextMarquee(context, setting, text, clipLeft + 3, textY(y, rowH), clipRight - 2, fm.getTextColor(), hovered);
        context.disableScissor();
    }

    private static void renderJumpRow(DrawContext context, FontManager fm, Setting<?> setting, String summary, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        fillNative(context, x + ACCENT_X + 1, y, x + width - 1, y + rowH, fm.secondaryAlpha(180));
        int clipLeft = x + ACCENT_X + 1;
        int clipRight = x + width - 1;
        context.enableScissor(clipLeft, y, clipRight, y + rowH);
        fm.drawText(context, setting.name, clipLeft + 3, textY(y, rowH), fm.getTextColor());
        String hint = summary + "  \u203A";
        int hintColor = hovered ? fm.getTextColor() : GuiColors.TEXT_SETTING_VALUE;
        fm.drawText(context, hint, clipRight - fm.getTextWidth(hint) - 2, textY(y, rowH), hintColor);
        context.disableScissor();
    }

    private static void renderStubSetting(DrawContext context, FontManager fm, Setting<?> setting, int x, int y, int width) {
        int rowH = fm.getRowHeight();
        fillNative(context, x + ACCENT_X + 1, y, x + width - 1, y + rowH, fm.primaryAlpha(60));
        int clipLeft = x + ACCENT_X + 1;
        int clipRight = x + width - 1;
        context.enableScissor(clipLeft, y, clipRight, y + rowH);
        fm.drawText(context, setting.name, clipLeft + 3, textY(y, rowH), GuiColors.TEXT_STUB);
        String hint = "edit \u2192";
        fm.drawText(context, hint, clipRight - fm.getTextWidth(hint) - 2, textY(y, rowH), GuiColors.TEXT_STUB);
        context.disableScissor();
    }

    private static void renderStringSetting(DrawContext context, FontManager fm, StringSetting setting, int x, int y, int width) {
        int rowH = fm.getRowHeight();
        boolean editing = editingString == setting;
        if (editing) {
            fillNative(context, x + ACCENT_X + 1, y, x + width - 1, y + rowH, fm.secondaryAlpha(200));
        }
        int clipLeft = x + ACCENT_X + 1;
        int clipRight = x + width - 1;
        context.enableScissor(clipLeft, y, clipRight, y + rowH);
        String label = setting.name + ": ";
        int labelW = fm.getTextWidth(label);
        fm.drawText(context, label, clipLeft + 3, textY(y, rowH), GuiColors.TEXT_SETTING_VALUE);
        String text = editing ? editingBuffer : setting.get();
        fm.drawText(context, text, clipLeft + 3 + labelW, textY(y, rowH), fm.getTextColor());
        if (editing && ((System.currentTimeMillis() / 500) & 1) == 0) {
            int caretX = clipLeft + 3 + labelW + fm.getTextWidth(text);
            fillNative(context, caretX, y + 2, caretX + 1, y + rowH - 2, fm.getTextColor());
        }
        context.disableScissor();
    }

    private static void renderProvidedStringSetting(DrawContext context, FontManager fm, ProvidedStringSetting setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        fillNative(context, x + ACCENT_X + 1, y, x + width - 1, y + rowH, fm.secondaryAlpha(180));
        int clipLeft = x + ACCENT_X + 1;
        int clipRight = x + width - 1;
        context.enableScissor(clipLeft, y, clipRight, y + rowH);
        fm.drawTextMarquee(context, setting, setting.name + ": " + setting.get(), clipLeft + 3, textY(y, rowH), clipRight - 2, fm.getTextColor(), hovered);
        context.disableScissor();
    }

    private static void renderFontFaceSetting(DrawContext context, FontManager fm, FontFaceSetting setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        fillNative(context, x + ACCENT_X + 1, y, x + width - 1, y + rowH, fm.secondaryAlpha(180));
        int clipLeft = x + ACCENT_X + 1;
        int clipRight = x + width - 1;
        context.enableScissor(clipLeft, y, clipRight, y + rowH);
        String shown = setting.get() != null ? setting.get().toString() : "(none)";
        fm.drawTextMarquee(context, setting, setting.name + ": " + shown, clipLeft + 3, textY(y, rowH), clipRight - 2, fm.getTextColor(), hovered);
        context.disableScissor();
    }

    private static void renderKeybindSetting(DrawContext context, FontManager fm, KeybindSetting setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        int clipLeft = x + ACCENT_X + 1;
        int clipRight = x + width - 1;
        context.enableScissor(clipLeft, y, clipRight, y + rowH);
        String text;
        int color;
        if (NewGuiBindCapture.get().getListeningSetting() == setting) {
            text = setting.name + ": ...";
            color = fm.getTextColor();
        } else {
            Keybind kb = setting.get();
            text = setting.name + ": " + (kb.isSet() ? kb.toString() : "NONE");
            color = GuiColors.TEXT_SETTING_VALUE;
        }
        fm.drawTextMarquee(context, setting, text, clipLeft + 3, textY(y, rowH), clipRight - 2, color, hovered);
        context.disableScissor();
    }

    // ---- Click handling ----

    /** Returns -1 if consumed, otherwise the row height to advance. */
    private static int handleSettingClick(Setting<?> setting, int x, int y, int width,
                                          int mouseX, int mouseY, int button,
                                          Supplier<Screen> fallback) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();

        if (setting instanceof BoolSetting bs) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) { bs.set(!bs.get()); return -1; }
            return rowH;
        } else if (setting instanceof EnumSetting<?> es) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) { cycleEnum((EnumSetting) es); return -1; }
            return rowH;
        } else if (setting instanceof IntSetting is) {
            if (mouseY >= y && mouseY < y + rowH && button == 0 && !is.noSlider) {
                draggingSlider = true;
                draggingSetting = is;
                dragCtx = new DragCtx(x, width);
                updateIntSlider(is, x, width, mouseX);
                return -1;
            }
            return rowH;
        } else if (setting instanceof DoubleSetting ds) {
            if (mouseY >= y && mouseY < y + rowH && button == 0 && !ds.noSlider) {
                draggingSlider = true;
                draggingSetting = ds;
                dragCtx = new DragCtx(x, width);
                updateDoubleSlider(ds, x, width, mouseX);
                return -1;
            }
            return rowH;
        } else if (setting instanceof ColorSetting cs) {
            // Picker rows take multiple row-heights when open
            if (mouseY >= y && mouseY < y + rowH) {
                if (button == 1) { togglePicker(cs); return -1; }
                if (button == 0) { togglePicker(cs); return -1; }
                return -1;
            }
            // Clicks inside the open picker
            if (isPickerOpen(cs)) {
                int baseY = y + rowH;
                int ss = x + ACCENT_X + 1;
                int sw = width - ACCENT_X - 2;
                int svTop = baseY;
                int svHeight = rowH * SV_ROWS;
                int hueRow = svTop + svHeight;
                int alphaRow = hueRow + rowH;
                int btnRow = alphaRow + rowH;

                if (button == 0 && mouseY >= svTop && mouseY < svTop + svHeight) {
                    draggingColorSetting = cs;
                    draggingColorComponent = 0;
                    dragCtx = new DragCtx(x, width, svTop, svHeight);
                    updateColorSlider(cs, 0, ss, sw, mouseX, mouseY, svTop, svHeight);
                    return -1;
                }
                if (button == 0 && mouseY >= hueRow && mouseY < hueRow + rowH) {
                    draggingColorSetting = cs;
                    draggingColorComponent = 1;
                    dragCtx = new DragCtx(x, width, svTop, svHeight);
                    updateColorSlider(cs, 1, ss, sw, mouseX, mouseY, svTop, svHeight);
                    return -1;
                }
                if (button == 0 && mouseY >= alphaRow && mouseY < alphaRow + rowH) {
                    draggingColorSetting = cs;
                    draggingColorComponent = 2;
                    dragCtx = new DragCtx(x, width, svTop, svHeight);
                    updateColorSlider(cs, 2, ss, sw, mouseX, mouseY, svTop, svHeight);
                    return -1;
                }
                if (button == 0 && mouseY >= btnRow && mouseY < btnRow + rowH) {
                    int mid = ss + sw / 2;
                    if (mouseX < mid) copyColorToClipboard(cs);
                    else pasteColorFromClipboard(cs);
                    return -1;
                }
                return rowH * (1 + getPickerRows(cs));
            }
            return rowH;
        } else if (setting instanceof KeybindSetting ks) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                NewGuiBindCapture.get().startListeningForSetting(ks);
                return -1;
            }
            return rowH;
        } else if (setting instanceof ProvidedStringSetting pss) {
            if (mouseY >= y && mouseY < y + rowH) {
                if (button == 0) { cycleProvidedString(pss, pss.supplier.get(), 1); return -1; }
                if (button == 1) { cycleProvidedString(pss, pss.supplier.get(), -1); return -1; }
            }
            return rowH;
        } else if (setting instanceof FontFaceSetting fs) {
            if (mouseY >= y && mouseY < y + rowH) {
                List<FontFace> faces = buildFontFaces();
                if (button == 0) { cycleFontFace(fs, faces, 1); return -1; }
                if (button == 1) { cycleFontFace(fs, faces, -1); return -1; }
            }
            return rowH;
        } else if (setting instanceof StringSetting ss) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                commitStringEdit();
                editingString = ss;
                editingBuffer = ss.get();
                return -1;
            }
            return rowH;
        } else if (setting instanceof BlockPosSetting bps) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                MinecraftClient.getInstance().setScreen(new NewVectorEditScreen(bps));
                return -1;
            }
            return rowH;
        } else if (setting instanceof Vector3dSetting v3s) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                MinecraftClient.getInstance().setScreen(new NewVectorEditScreen(v3s));
                return -1;
            }
            return rowH;
        } else if (setting instanceof StringListSetting sls) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                MinecraftClient.getInstance().setScreen(new NewStringListScreen(sls));
                return -1;
            }
            return rowH;
        } else if (setting instanceof ModuleListSetting mls) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                NewCollectionListScreen<meteordevelopment.meteorclient.systems.modules.Module> screen = new NewCollectionListScreen<>(
                    "Select Modules \u2014 " + mls.name,
                    mls, mls.get(), Modules.get().getAll(),
                    m -> m.title);
                MinecraftClient.getInstance().setScreen(screen);
                return -1;
            }
            return rowH;
        } else if (setting instanceof EntityTypeListSetting etls) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                MinecraftClient.getInstance().setScreen(new NewEntityTypeScreen(etls));
                return -1;
            }
            return rowH;
        } else if (setting instanceof EnchantmentListSetting els) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<RegistryKey<Enchantment>> all = new ArrayList<>();
                var mc = MinecraftClient.getInstance();
                var world = mc.world;
                var nh = mc.getNetworkHandler();
                net.minecraft.registry.Registry<Enchantment> reg = null;
                if (world != null) {
                    reg = world.getRegistryManager().getOptional(net.minecraft.registry.RegistryKeys.ENCHANTMENT).orElse(null);
                }
                if (reg == null && nh != null) {
                    reg = nh.getRegistryManager().getOptional(net.minecraft.registry.RegistryKeys.ENCHANTMENT).orElse(null);
                }
                if (reg != null) reg.streamKeys().forEach(all::add);
                else all.addAll(els.get());
                NewCollectionListScreen<RegistryKey<Enchantment>> screen = new NewCollectionListScreen<>(
                    "Select Enchantments \u2014 " + els.name, els, els.get(), all, Names::get);
                MinecraftClient.getInstance().setScreen(screen);
                return -1;
            }
            return rowH;
        } else if (setting instanceof BlockListSetting bls) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<Block> all = new ArrayList<>();
                for (Block b : Registries.BLOCK) all.add(b);
                NewCollectionListScreen<Block> screen = new NewCollectionListScreen<>(
                    "Select Blocks \u2014 " + bls.name, bls, bls.get(), all,
                    Names::get, b -> b.asItem().getDefaultStack(), null);
                MinecraftClient.getInstance().setScreen(screen);
                return -1;
            }
            return rowH;
        } else if (setting instanceof ItemListSetting ils) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<Item> all = new ArrayList<>();
                for (Item it : Registries.ITEM) all.add(it);
                NewCollectionListScreen<Item> screen = new NewCollectionListScreen<>(
                    "Select Items \u2014 " + ils.name, ils, ils.get(), all,
                    Names::get, Item::getDefaultStack, null);
                MinecraftClient.getInstance().setScreen(screen);
                return -1;
            }
            return rowH;
        } else if (setting instanceof StatusEffectListSetting sels) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<StatusEffect> all = new ArrayList<>();
                for (StatusEffect e : Registries.STATUS_EFFECT) all.add(e);
                NewCollectionListScreen<StatusEffect> screen = new NewCollectionListScreen<>(
                    "Select Effects \u2014 " + sels.name, sels, sels.get(), all,
                    Names::get, SettingGroupRenderer::statusEffectIcon, null);
                MinecraftClient.getInstance().setScreen(screen);
                return -1;
            }
            return rowH;
        } else if (setting instanceof SoundEventListSetting sevls) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<SoundEvent> all = new ArrayList<>();
                for (SoundEvent e : Registries.SOUND_EVENT) all.add(e);
                NewCollectionListScreen<SoundEvent> screen = new NewCollectionListScreen<>(
                    "Select Sounds \u2014 " + sevls.name, sevls, sevls.get(), all,
                    e -> e.id().getPath(), null, null);
                MinecraftClient.getInstance().setScreen(screen);
                return -1;
            }
            return rowH;
        } else if (setting instanceof ParticleTypeListSetting ptls) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<ParticleType<?>> all = new ArrayList<>();
                for (ParticleType<?> p : Registries.PARTICLE_TYPE) all.add(p);
                NewCollectionListScreen<ParticleType<?>> screen = new NewCollectionListScreen<>(
                    "Select Particles \u2014 " + ptls.name, ptls, ptls.get(), all,
                    Names::get, null, p -> !(p instanceof ParticleEffect));
                MinecraftClient.getInstance().setScreen(screen);
                return -1;
            }
            return rowH;
        } else if (setting instanceof PacketListSetting pls) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<Class<? extends net.minecraft.network.packet.Packet<?>>> all = new ArrayList<>();
                for (Class<? extends net.minecraft.network.packet.Packet<?>> c : meteordevelopment.meteorclient.utils.network.PacketUtils.PACKETS) all.add(c);
                NewCollectionListScreen<Class<? extends net.minecraft.network.packet.Packet<?>>> screen = new NewCollectionListScreen<>(
                    "Select Packets \u2014 " + pls.name, pls, pls.get(), all,
                    meteordevelopment.meteorclient.utils.network.PacketUtils::getName, null,
                    c -> pls.filter != null && !pls.filter.test(c));
                MinecraftClient.getInstance().setScreen(screen);
                return -1;
            }
            return rowH;
        } else if (setting instanceof ScreenHandlerListSetting shls) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<net.minecraft.screen.ScreenHandlerType<?>> all = new ArrayList<>();
                for (net.minecraft.screen.ScreenHandlerType<?> t : Registries.SCREEN_HANDLER) all.add(t);
                NewCollectionListScreen<net.minecraft.screen.ScreenHandlerType<?>> screen = new NewCollectionListScreen<>(
                    "Select Screen Handlers \u2014 " + shls.name, shls, shls.get(), all,
                    t -> {
                        var id = Registries.SCREEN_HANDLER.getId(t);
                        return id == null ? "(null)" : id.toString();
                    }, null, null);
                MinecraftClient.getInstance().setScreen(screen);
                return -1;
            }
            return rowH;
        } else if (setting instanceof StorageBlockListSetting sbls) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<net.minecraft.block.entity.BlockEntityType<?>> all = new ArrayList<>();
                for (net.minecraft.block.entity.BlockEntityType<?> t : StorageBlockListSetting.REGISTRY) all.add(t);
                NewCollectionListScreen<net.minecraft.block.entity.BlockEntityType<?>> screen = new NewCollectionListScreen<>(
                    "Select Storage Blocks \u2014 " + sbls.name, sbls, sbls.get(), all,
                    SettingGroupRenderer::storageBlockName,
                    t -> {
                        Item item = storageBlockItem(t);
                        return item == null ? null : item.getDefaultStack();
                    }, null);
                MinecraftClient.getInstance().setScreen(screen);
                return -1;
            }
            return rowH;
        } else if (setting instanceof BlockSetting bs2) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<Block> all = new ArrayList<>();
                for (Block b : Registries.BLOCK) all.add(b);
                NewSingleValueScreen<Block> screen = new NewSingleValueScreen<>(
                    "Select Block \u2014 " + bs2.name, bs2, all,
                    Names::get, b -> b.asItem().getDefaultStack(), null, null);
                MinecraftClient.getInstance().setScreen(screen);
                return -1;
            }
            return rowH;
        } else if (setting instanceof ItemSetting is2) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<Item> all = new ArrayList<>();
                for (Item it : Registries.ITEM) all.add(it);
                NewSingleValueScreen<Item> screen = new NewSingleValueScreen<>(
                    "Select Item \u2014 " + is2.name, is2, all,
                    Names::get, Item::getDefaultStack, null, null);
                MinecraftClient.getInstance().setScreen(screen);
                return -1;
            }
            return rowH;
        } else if (setting instanceof ColorListSetting cls) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                MinecraftClient.getInstance().setScreen(new NewColorListScreen(cls));
                return -1;
            }
            return rowH;
        } else if (setting instanceof StatusEffectAmplifierMapSetting seams) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                MinecraftClient.getInstance().setScreen(new NewAmplifierMapScreen(seams));
                return -1;
            }
            return rowH;
        } else if (setting instanceof BlockDataSetting<?> bds) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                // Open our new-theme block picker. Delegated to a helper so that
                // the F-bounded generic (T extends IBlockData<T>) doesn't confuse
                // the compiler's type inference on the diamond.
                openBlockDataPicker(bds);
                return -1;
            }
            return rowH;
        } else if (setting instanceof GenericSetting<?> gs) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                // Same capture trick as BlockData: call the T's createScreen with
                // a CapturingGuiTheme, intercept the Settings it builds, render in
                // our theme via NewBlockEditScreen. Falls back to Meteor's widget
                // screen if capture fails.
                var factory = gs.get();
                if (factory == null) return -1;
                var capture = new meteordevelopment.meteorclient.gui.newgui.components.CapturingGuiTheme();
                Screen widgetScreen = null;
                try {
                    widgetScreen = factory.createScreen(capture);
                    if (widgetScreen instanceof meteordevelopment.meteorclient.gui.WidgetScreen ws) {
                        ws.initWidgets();
                    }
                } catch (Throwable ignored) {}

                if (capture.captured != null) {
                    MinecraftClient.getInstance().setScreen(
                        new meteordevelopment.meteorclient.gui.newgui.screens.NewBlockEditScreen(
                            "Configure " + setting.name,
                            capture.captured,
                            MinecraftClient.getInstance().currentScreen));
                } else if (widgetScreen != null) {
                    MinecraftClient.getInstance().setScreen(widgetScreen);
                } else {
                    MinecraftClient.getInstance().setScreen(factory.createScreen(GuiThemes.get()));
                }
                return -1;
            }
            return rowH;
        } else if (setting instanceof ActionSetting as) {
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                if (as.action != null) as.action.run();
                return -1;
            }
            return rowH;
        } else {
            // Any unrecognized type — fall back to whatever screen the caller provides.
            if (mouseY >= y && mouseY < y + rowH && button == 0 && fallback != null) {
                Screen s = fallback.get();
                if (s != null) MinecraftClient.getInstance().setScreen(s);
                return -1;
            }
            return rowH;
        }
    }

    /**
     * Pattern-binding-friendly cast that lets the generic NewBlockDataScreen
     * constructor match {@code BlockDataSetting<T>} from a raw {@code BlockDataSetting<?>}.
     */
    /**
     * Open NewBlockDataScreen for a BlockDataSetting of unknown T. Uses a raw
     * constructor call to sidestep Java's inference failure on F-bounded
     * generics (T extends IBlockData&lt;T&gt; is self-referential and the diamond
     * can't be solved). Runtime behavior is identical — type parameters are
     * erased anyway.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void openBlockDataPicker(BlockDataSetting<?> setting) {
        MinecraftClient.getInstance().setScreen(
            new meteordevelopment.meteorclient.gui.newgui.screens.NewBlockDataScreen(setting));
    }

    // ---- Cycle helpers ----

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void cycleEnum(EnumSetting setting) {
        Object[] constants = setting.get().getClass().getEnumConstants();
        if (constants == null || constants.length == 0) return;
        int idx = ((Enum) setting.get()).ordinal();
        setting.set((Enum) constants[(idx + 1) % constants.length]);
    }

    private static void cycleProvidedString(ProvidedStringSetting setting, String[] opts, int direction) {
        if (opts == null || opts.length == 0) return;
        String current = setting.get();
        int idx = -1;
        for (int i = 0; i < opts.length; i++) { if (opts[i].equals(current)) { idx = i; break; } }
        int next = idx < 0 ? 0 : (((idx + direction) % opts.length) + opts.length) % opts.length;
        setting.set(opts[next]);
    }

    private static void cycleFontFace(FontFaceSetting setting, List<FontFace> faces, int direction) {
        if (faces.isEmpty()) return;
        int current = -1;
        FontFace cur = setting.get();
        if (cur != null) {
            for (int i = 0; i < faces.size(); i++) {
                if (faces.get(i).toString().equals(cur.toString())) { current = i; break; }
            }
        }
        int next = current < 0 ? 0 : (((current + direction) % faces.size()) + faces.size()) % faces.size();
        setting.set(faces.get(next));
    }

    private static List<FontFace> buildFontFaces() {
        List<FontFace> faces = new ArrayList<>();
        for (FontFamily family : Fonts.FONT_FAMILIES) {
            for (FontInfo.Type type : FontInfo.Type.values()) {
                try {
                    FontFace face = family.get(type);
                    if (face != null) faces.add(face);
                } catch (Throwable ignored) {}
            }
        }
        return faces;
    }

    // ---- Slider updates ----

    private static void updateIntSlider(IntSetting setting, int x, int width, int mouseX) {
        int ss = x + ACCENT_X + 1;
        int sw = width - ACCENT_X - 2;
        double progress = Math.max(0, Math.min(1, (mouseX - ss) / (double) sw));
        int minV = Math.max(setting.min, setting.sliderMin);
        int maxV = Math.min(setting.max, setting.sliderMax);
        int value = (int) Math.round(minV + (maxV - minV) * progress);
        value = Math.max(setting.min, Math.min(setting.max, value));
        if (!setting.get().equals(value)) setting.set(value);
    }

    private static void updateDoubleSlider(DoubleSetting setting, int x, int width, int mouseX) {
        int ss = x + ACCENT_X + 1;
        int sw = width - ACCENT_X - 2;
        double progress = Math.max(0, Math.min(1, (mouseX - ss) / (double) sw));
        double minV = Math.max(setting.min, setting.sliderMin);
        double maxV = Math.min(setting.max, setting.sliderMax);
        double value = minV + (maxV - minV) * progress;
        value = Math.max(setting.min, Math.min(setting.max, value));
        if (!setting.get().equals(value)) setting.set(value);
    }

    private static void updateColorSlider(ColorSetting setting, int component, int ss, int sw, int mouseX, int mouseY, int svTop, int svHeight) {
        PickerState ps = ensurePicker(setting);
        float progressX = (float) Math.max(0, Math.min(1, (mouseX - ss) / (double) sw));
        switch (component) {
            case 0 -> {
                // 2D saturation/brightness
                ps.saturation = progressX;
                float progressY = svHeight <= 0 ? 0f
                    : (float) Math.max(0, Math.min(1, (mouseY - svTop) / (double) svHeight));
                ps.brightness = 1f - progressY;
            }
            case 1 -> ps.hue = progressX;
            case 2 -> {
                SettingColor c = setting.get();
                c.a = (int) (progressX * 255);
                setting.onChanged();
                return;
            }
            default -> { return; }
        }
        int rgb = Color.HSBtoRGB(ps.hue, ps.saturation, ps.brightness);
        SettingColor c = setting.get();
        c.r = (rgb >> 16) & 0xFF;
        c.g = (rgb >> 8) & 0xFF;
        c.b = rgb & 0xFF;
        setting.onChanged();
    }

    private static void copyColorToClipboard(ColorSetting setting) {
        SettingColor c = setting.get();
        String hex = String.format("#%02X%02X%02X%02X", c.r & 0xFF, c.g & 0xFF, c.b & 0xFF, c.a & 0xFF);
        MinecraftClient.getInstance().keyboard.setClipboard(hex);
    }

    private static void pasteColorFromClipboard(ColorSetting setting) {
        String raw = MinecraftClient.getInstance().keyboard.getClipboard();
        if (raw == null) return;
        String s = raw.trim();
        if (s.isEmpty()) return;
        SettingColor c = setting.get();
        try {
            if (s.startsWith("#")) s = s.substring(1);
            if (s.length() == 6) {
                int val = Integer.parseUnsignedInt(s, 16);
                c.r = (val >> 16) & 0xFF;
                c.g = (val >> 8) & 0xFF;
                c.b = val & 0xFF;
            } else if (s.length() == 8) {
                long val = Long.parseUnsignedLong(s, 16);
                c.r = (int) ((val >> 24) & 0xFF);
                c.g = (int) ((val >> 16) & 0xFF);
                c.b = (int) ((val >> 8) & 0xFF);
                c.a = (int) (val & 0xFF);
            } else if (s.contains(",")) {
                String[] parts = s.split(",");
                if (parts.length < 3) return;
                c.r = clamp255(Integer.parseInt(parts[0].trim()));
                c.g = clamp255(Integer.parseInt(parts[1].trim()));
                c.b = clamp255(Integer.parseInt(parts[2].trim()));
                if (parts.length >= 4) c.a = clamp255(Integer.parseInt(parts[3].trim()));
            } else {
                return;
            }
            setting.onChanged();
            // Sync HSB state to new RGB so the markers reflect the pasted color
            PickerState ps = pickerStates.get(setting);
            if (ps != null) {
                float[] hsb = Color.RGBtoHSB(c.r & 0xFF, c.g & 0xFF, c.b & 0xFF, null);
                ps.hue = hsb[0];
                ps.saturation = hsb[1];
                ps.brightness = hsb[2];
            }
        } catch (Throwable ignored) {}
    }

    private static int clamp255(int v) { return Math.max(0, Math.min(255, v)); }

    // ---- Color picker helpers ----

    private static class PickerState {
        boolean open;
        float hue, saturation, brightness;
    }

    private static PickerState ensurePicker(ColorSetting setting) {
        PickerState ps = pickerStates.get(setting);
        if (ps == null) {
            ps = new PickerState();
            SettingColor c = setting.get();
            float[] hsb = Color.RGBtoHSB(c.r, c.g, c.b, null);
            ps.hue = hsb[0];
            ps.saturation = hsb[1];
            ps.brightness = hsb[2];
            pickerStates.put(setting, ps);
        }
        return ps;
    }

    private static void togglePicker(ColorSetting setting) {
        PickerState ps = ensurePicker(setting);
        ps.open = !ps.open;
    }

    private static boolean isPickerOpen(ColorSetting setting) {
        PickerState ps = pickerStates.get(setting);
        return ps != null && ps.open;
    }

    static int getPickerRows(ColorSetting setting) {
        return isPickerOpen(setting) ? PICKER_ROWS : 0;
    }

    // ---- Misc exposed helpers (used by screens that build icon suppliers) ----

    /** Returns a colored potion ItemStack to use as the visual for a StatusEffect. */
    public static ItemStack statusEffectIcon(StatusEffect effect) {
        ItemStack potion = Items.POTION.getDefaultStack();
        PotionContentsComponent contents = potion.get(DataComponentTypes.POTION_CONTENTS);
        if (contents == null) return potion;
        potion.set(
            DataComponentTypes.POTION_CONTENTS,
            new PotionContentsComponent(
                contents.potion(),
                Optional.of(effect.getColor()),
                contents.customEffects(),
                Optional.empty()
            )
        );
        return potion;
    }

    /** Readable name for a storage block (BlockEntityType). */
    public static String storageBlockName(net.minecraft.block.entity.BlockEntityType<?> type) {
        var id = Registries.BLOCK_ENTITY_TYPE.getId(type);
        if (id == null) return "(unknown)";
        String path = id.getPath();
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(' ');
            if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        }
        return sb.toString();
    }

    /** Best-effort item for a storage block icon (looks up by block entity ID → matching item). */
    public static Item storageBlockItem(net.minecraft.block.entity.BlockEntityType<?> type) {
        var id = Registries.BLOCK_ENTITY_TYPE.getId(type);
        if (id == null) return Items.BARRIER;
        Item item = Registries.ITEM.get(id);
        return item == Items.AIR ? Items.BARRIER : item;
    }
}
