package meteordevelopment.meteorclient.gui.newgui.components;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.screens.NewAmplifierMapScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewBlockDataScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewBlockEditScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewCollectionListScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewColorListScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewEntityTypeScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewSingleValueScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewStringListScreen;
import meteordevelopment.meteorclient.gui.newgui.screens.NewVectorEditScreen;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.gui.utils.IScreenFactory;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.FontFamily;
import meteordevelopment.meteorclient.renderer.text.FontInfo;
import meteordevelopment.meteorclient.settings.ActionSetting;
import meteordevelopment.meteorclient.settings.BlockDataSetting;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BlockPosSetting;
import meteordevelopment.meteorclient.settings.BlockSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorListSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.FontFaceSetting;
import meteordevelopment.meteorclient.settings.GenericSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.ParticleTypeListSetting;
import meteordevelopment.meteorclient.settings.ProvidedStringSetting;
import meteordevelopment.meteorclient.settings.ScreenHandlerListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.SoundEventListSetting;
import meteordevelopment.meteorclient.settings.StatusEffectAmplifierMapSetting;
import meteordevelopment.meteorclient.settings.StatusEffectListSetting;
import meteordevelopment.meteorclient.settings.StorageBlockListSetting;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.settings.Vector3dSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_1291;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_1887;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2378;
import net.minecraft.class_2394;
import net.minecraft.class_2396;
import net.minecraft.class_2591;
import net.minecraft.class_2596;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_3917;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_5321;
import net.minecraft.class_634;
import net.minecraft.class_638;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import net.minecraft.class_9334;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/components/SettingGroupRenderer.class */
public final class SettingGroupRenderer {
    static final int ACCENT_X = 3;
    static StringSetting editingString = null;
    static String editingBuffer = "";
    static boolean draggingSlider = false;
    static Setting<?> draggingSetting = null;
    static ColorSetting draggingColorSetting = null;
    static int draggingColorComponent = -1;
    static final Map<ColorSetting, PickerState> pickerStates = new HashMap();
    private static DragCtx dragCtx;
    static final int SV_ROWS = 6;
    static final int PICKER_ROWS = 9;

    private SettingGroupRenderer() {
    }

    public static int renderGroups(class_332 context, Iterable<SettingGroup> groups, int x, int y, int width, int mouseX, int mouseY) {
        tickDrag(mouseX, mouseY);
        FontManager fm = FontManager.get();
        int currentY = y;
        for (SettingGroup group : groups) {
            currentY = renderSettingGroup(context, fm, group, x, currentY, width, mouseX, mouseY);
        }
        return currentY;
    }

    public static int getGroupsHeight(Iterable<SettingGroup> groups) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int total = 0;
        for (SettingGroup group : groups) {
            total += rowH;
            if (group.sectionExpanded) {
                for (Setting<?> setting : group) {
                    if (setting.isVisible()) {
                        total += getSettingRows(setting) * rowH;
                    }
                }
            }
        }
        return total;
    }

    public static boolean mouseClickedGroups(Iterable<SettingGroup> groups, int x, int y, int width, int mouseX, int mouseY, int button, Supplier<class_437> fallback) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int settingY = y;
        for (SettingGroup group : groups) {
            if (mouseY >= settingY && mouseY < settingY + rowH && (button == 0 || button == 1)) {
                group.sectionExpanded = !group.sectionExpanded;
                return true;
            }
            settingY += rowH;
            if (group.sectionExpanded) {
                for (Setting<?> setting : group) {
                    if (setting.isVisible()) {
                        int consumed = handleSettingClick(setting, x, settingY, width, mouseX, mouseY, button, fallback);
                        if (consumed < 0) {
                            return true;
                        }
                        settingY += consumed;
                    }
                }
            }
        }
        return false;
    }

    public static void mouseReleasedGroups() {
        draggingSlider = false;
        draggingSetting = null;
        draggingColorSetting = null;
        draggingColorComponent = -1;
    }

    public static boolean onKeyPressed(int key) {
        if (editingString != null) {
            if (key == 257 || key == 335) {
                commitStringEdit();
                return true;
            }
            if (key == 256) {
                editingString = null;
                editingBuffer = "";
                return true;
            }
            if (key != 259 || editingBuffer.isEmpty()) {
                return true;
            }
            editingBuffer = editingBuffer.substring(0, editingBuffer.length() - 1);
            return true;
        }
        return false;
    }

    public static boolean onCharTyped(char chr) {
        if (editingString == null) {
            return false;
        }
        if (chr >= ' ' && chr != 127) {
            String next = editingBuffer + chr;
            if (editingString.filter == null || editingString.filter.filter(next, chr)) {
                editingBuffer = next;
                return true;
            }
            return true;
        }
        return true;
    }

    public static boolean isListening() {
        return NewGuiBindCapture.get().isListeningForSetting() || editingString != null;
    }

    public static void commitStringEdit() {
        if (editingString != null) {
            editingString.set(editingBuffer);
            editingString = null;
            editingBuffer = "";
        }
    }

    public static void tickDrag(int mouseX, int mouseY) {
        DragCtx ctx;
        if (draggingSlider) {
            Setting<?> setting = draggingSetting;
            if (setting instanceof IntSetting) {
                IntSetting is = (IntSetting) setting;
                DragCtx ctx2 = dragCtx;
                if (ctx2 != null) {
                    updateIntSlider(is, ctx2.x, ctx2.width, mouseX);
                    return;
                }
                return;
            }
        }
        if (draggingSlider) {
            Setting<?> setting2 = draggingSetting;
            if (setting2 instanceof DoubleSetting) {
                DoubleSetting ds = (DoubleSetting) setting2;
                DragCtx ctx3 = dragCtx;
                if (ctx3 != null) {
                    updateDoubleSlider(ds, ctx3.x, ctx3.width, mouseX);
                    return;
                }
                return;
            }
        }
        if (draggingColorSetting == null || draggingColorComponent < 0 || (ctx = dragCtx) == null) {
            return;
        }
        updateColorSlider(draggingColorSetting, draggingColorComponent, ctx.x + 3 + 1, (ctx.width - 3) - 2, mouseX, mouseY, ctx.svTop, ctx.svHeight);
    }

    public static void tickDrag(int mouseX) {
        tickDrag(mouseX, 0);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/components/SettingGroupRenderer$DragCtx.class */
    private static class DragCtx {
        int x;
        int width;
        int svTop;
        int svHeight;

        DragCtx(int x, int w) {
            this.x = x;
            this.width = w;
        }

        DragCtx(int x, int w, int svTop, int svHeight) {
            this.x = x;
            this.width = w;
            this.svTop = svTop;
            this.svHeight = svHeight;
        }
    }

    static int textY(int y, int rowH) {
        return y + ((rowH - FontManager.get().getTextHeight()) / 2);
    }

    static void fillNative(class_332 context, int x1, int y1, int x2, int y2, int color) {
        float scale = (float) class_310.method_1551().method_22683().method_4495();
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_22905(1.0f / scale, 1.0f / scale, 1.0f);
        context.method_25294((int) (x1 * scale), (int) (y1 * scale), (int) (x2 * scale), (int) (y2 * scale), color);
        matrices.method_22909();
    }

    private static int renderSettingGroup(class_332 context, FontManager fm, SettingGroup group, int x, int y, int width, int mouseX, int mouseY) {
        int rowH = fm.getRowHeight();
        fillNative(context, x + 3 + 1, y, (x + width) - 1, y + rowH, fm.secondaryAlpha(Opcode.GETFIELD));
        int clipLeft = x + 3 + 1;
        int clipRight = (x + width) - 1;
        context.method_44379(clipLeft, y, clipRight, y + rowH);
        boolean hovered = mouseY >= y && mouseY < y + rowH && mouseX >= x && mouseX < x + width;
        fm.drawTextMarquee(context, group, group.name, clipLeft + 3, textY(y, rowH), clipRight - 12, fm.getTextColor(), hovered);
        String toggle = group.sectionExpanded ? "-" : "+";
        fm.drawText(context, toggle, (clipRight - fm.getTextWidth(toggle)) - 2, textY(y, rowH), fm.getTextColor());
        context.method_44380();
        int y2 = y + rowH;
        if (group.sectionExpanded) {
            for (Setting<?> setting : group) {
                if (setting.isVisible()) {
                    y2 = renderSetting(context, fm, setting, x, y2, width, mouseX, mouseY);
                }
            }
        }
        return y2;
    }

    private static int renderSetting(class_332 context, FontManager fm, Setting<?> setting, int x, int y, int width, int mouseX, int mouseY) {
        String str;
        int rowH = fm.getRowHeight();
        boolean hovered = mouseY >= y && mouseY < y + rowH && mouseX >= x && mouseX < x + width;
        if (setting instanceof BoolSetting) {
            BoolSetting bs = (BoolSetting) setting;
            renderBoolSetting(context, fm, bs, x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof EnumSetting) {
            EnumSetting<?> es = (EnumSetting) setting;
            renderEnumSetting(context, fm, es, x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof IntSetting) {
            IntSetting is = (IntSetting) setting;
            renderIntSetting(context, fm, is, x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof DoubleSetting) {
            DoubleSetting ds = (DoubleSetting) setting;
            renderDoubleSetting(context, fm, ds, x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof ColorSetting) {
            ColorSetting cs = (ColorSetting) setting;
            renderColorSetting(context, fm, cs, x, y, width);
            return y + (rowH * (1 + getPickerRows(cs)));
        }
        if (setting instanceof KeybindSetting) {
            KeybindSetting ks = (KeybindSetting) setting;
            renderKeybindSetting(context, fm, ks, x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof ProvidedStringSetting) {
            ProvidedStringSetting pss = (ProvidedStringSetting) setting;
            renderProvidedStringSetting(context, fm, pss, x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof FontFaceSetting) {
            FontFaceSetting fs = (FontFaceSetting) setting;
            renderFontFaceSetting(context, fm, fs, x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof BlockPosSetting) {
            BlockPosSetting bps = (BlockPosSetting) setting;
            renderBlockPosSetting(context, fm, bps, x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof Vector3dSetting) {
            Vector3dSetting vs = (Vector3dSetting) setting;
            renderVector3dSetting(context, fm, vs, x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof StringListSetting) {
            StringListSetting sls = (StringListSetting) setting;
            renderJumpRow(context, fm, setting, sls.get().size() + " entries", x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof ModuleListSetting) {
            ModuleListSetting mls = (ModuleListSetting) setting;
            renderJumpRow(context, fm, setting, mls.get().size() + " modules", x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof EntityTypeListSetting) {
            EntityTypeListSetting etls = (EntityTypeListSetting) setting;
            renderJumpRow(context, fm, setting, etls.get().size() + " entities", x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof EnchantmentListSetting) {
            EnchantmentListSetting els = (EnchantmentListSetting) setting;
            renderJumpRow(context, fm, setting, els.get().size() + " enchants", x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof BlockListSetting) {
            BlockListSetting bls = (BlockListSetting) setting;
            renderJumpRow(context, fm, setting, bls.get().size() + " blocks", x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof ItemListSetting) {
            ItemListSetting ils = (ItemListSetting) setting;
            renderJumpRow(context, fm, setting, ils.get().size() + " items", x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof StatusEffectListSetting) {
            StatusEffectListSetting sels = (StatusEffectListSetting) setting;
            renderJumpRow(context, fm, setting, sels.get().size() + " effects", x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof SoundEventListSetting) {
            SoundEventListSetting sevls = (SoundEventListSetting) setting;
            renderJumpRow(context, fm, setting, sevls.get().size() + " sounds", x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof ParticleTypeListSetting) {
            ParticleTypeListSetting ptls = (ParticleTypeListSetting) setting;
            renderJumpRow(context, fm, setting, ptls.get().size() + " particles", x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof PacketListSetting) {
            PacketListSetting pls = (PacketListSetting) setting;
            renderJumpRow(context, fm, setting, pls.get().size() + " packets", x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof ScreenHandlerListSetting) {
            ScreenHandlerListSetting shls = (ScreenHandlerListSetting) setting;
            renderJumpRow(context, fm, setting, shls.get().size() + " handlers", x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof StorageBlockListSetting) {
            StorageBlockListSetting sbls = (StorageBlockListSetting) setting;
            renderJumpRow(context, fm, setting, sbls.get().size() + " storages", x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof BlockSetting) {
            BlockSetting bs2 = (BlockSetting) setting;
            String name = bs2.get() == null ? "(none)" : Names.get(bs2.get());
            renderJumpRow(context, fm, setting, name, x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof ItemSetting) {
            ItemSetting is2 = (ItemSetting) setting;
            String name2 = is2.get() == null ? "(none)" : Names.get(is2.get());
            renderJumpRow(context, fm, setting, name2, x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof ColorListSetting) {
            ColorListSetting cls = (ColorListSetting) setting;
            renderJumpRow(context, fm, setting, cls.get().size() + " colors", x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof StatusEffectAmplifierMapSetting) {
            StatusEffectAmplifierMapSetting seams = (StatusEffectAmplifierMapSetting) setting;
            renderJumpRow(context, fm, setting, seams.get().size() + " effects", x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof BlockDataSetting) {
            BlockDataSetting<?> bds = (BlockDataSetting) setting;
            int overrideCount = 0;
            for (Map.Entry entry : bds.get().entrySet()) {
                Object v = entry.getValue();
                if (v != null && ((IChangeable) v).isChanged()) {
                    overrideCount++;
                }
            }
            if (overrideCount == 0) {
                str = "no overrides";
            } else {
                str = overrideCount + " override" + (overrideCount == 1 ? "" : "s");
            }
            String summary = str;
            renderJumpRow(context, fm, setting, summary, x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof GenericSetting) {
            renderJumpRow(context, fm, setting, "edit", x, y, width, hovered);
            return y + rowH;
        }
        if (setting instanceof StringSetting) {
            StringSetting ss = (StringSetting) setting;
            renderStringSetting(context, fm, ss, x, y, width);
            return y + rowH;
        }
        if (setting instanceof ActionSetting) {
            ActionSetting as = (ActionSetting) setting;
            renderJumpRow(context, fm, setting, as.buttonLabel, x, y, width, hovered);
            return y + rowH;
        }
        renderStubSetting(context, fm, setting, x, y, width);
        return y + rowH;
    }

    static int getSettingRows(Setting<?> setting) {
        if (!(setting instanceof ColorSetting)) {
            return 1;
        }
        ColorSetting cs = (ColorSetting) setting;
        return 1 + getPickerRows(cs);
    }

    private static void renderBoolSetting(class_332 context, FontManager fm, BoolSetting setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        if (setting.get().booleanValue()) {
            fillNative(context, x + 3 + 1, y, (x + width) - 1, y + rowH, fm.secondaryAlpha(Opcode.GETFIELD));
        }
        int clipLeft = x + 3 + 1;
        int clipRight = (x + width) - 1;
        context.method_44379(clipLeft, y, clipRight, y + rowH);
        int textColor = setting.get().booleanValue() ? fm.getTextColor() : fm.getTextSecondary();
        fm.drawTextMarquee(context, setting, setting.name, clipLeft + 3, textY(y, rowH), clipRight - 12, textColor, hovered);
        String toggle = setting.get().booleanValue() ? "+" : "-";
        int toggleColor = setting.get().booleanValue() ? fm.getTextColor() : GuiColors.TEXT_DISABLED;
        fm.drawText(context, toggle, (clipRight - fm.getTextWidth(toggle)) - 2, textY(y, rowH), toggleColor);
        context.method_44380();
    }

    private static void renderEnumSetting(class_332 context, FontManager fm, EnumSetting<?> setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        fillNative(context, x + 3 + 1, y, (x + width) - 1, y + rowH, fm.secondaryAlpha(Opcode.GETFIELD));
        int clipLeft = x + 3 + 1;
        int clipRight = (x + width) - 1;
        context.method_44379(clipLeft, y, clipRight, y + rowH);
        fm.drawTextMarquee(context, setting, setting.name + ": " + ((Enum) setting.get()).toString(), clipLeft + 3, textY(y, rowH), clipRight - 2, fm.getTextColor(), hovered);
        context.method_44380();
    }

    private static void renderIntSetting(class_332 context, FontManager fm, IntSetting setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        int sliderStart = x + 3 + 1;
        if (!setting.noSlider) {
            double range = setting.sliderMax - setting.sliderMin;
            float progress = range > 0.0d ? (float) (((double) (setting.get().intValue() - setting.sliderMin)) / range) : 0.0f;
            int fillWidth = Math.max(1, (int) (((width - 3) - 2) * Math.max(0.0f, Math.min(1.0f, progress))));
            fillNative(context, sliderStart, y, sliderStart + fillWidth, y + rowH, fm.secondaryAlpha(Opcode.ISHL));
        }
        int clipLeft = x + 3 + 1;
        int clipRight = (x + width) - 1;
        context.method_44379(clipLeft, y, clipRight, y + rowH);
        fm.drawTextMarquee(context, setting, setting.name + ": " + String.valueOf(setting.get()), clipLeft + 3, textY(y, rowH), clipRight - 2, GuiColors.TEXT_SETTING_VALUE, hovered);
        context.method_44380();
    }

    private static void renderDoubleSetting(class_332 context, FontManager fm, DoubleSetting setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        int sliderStart = x + 3 + 1;
        if (!setting.noSlider) {
            double range = setting.sliderMax - setting.sliderMin;
            float progress = range > 0.0d ? (float) ((setting.get().doubleValue() - setting.sliderMin) / range) : 0.0f;
            int fillWidth = Math.max(1, (int) (((width - 3) - 2) * Math.max(0.0f, Math.min(1.0f, progress))));
            fillNative(context, sliderStart, y, sliderStart + fillWidth, y + rowH, fm.secondaryAlpha(Opcode.ISHL));
        }
        int clipLeft = x + 3 + 1;
        int clipRight = (x + width) - 1;
        context.method_44379(clipLeft, y, clipRight, y + rowH);
        int dp = Math.max(1, setting.decimalPlaces);
        String valueStr = String.format("%." + dp + "f", setting.get());
        fm.drawTextMarquee(context, setting, setting.name + ": " + valueStr, clipLeft + 3, textY(y, rowH), clipRight - 2, GuiColors.TEXT_SETTING_VALUE, hovered);
        context.method_44380();
    }

    private static void renderColorSetting(class_332 context, FontManager fm, ColorSetting setting, int x, int y, int width) {
        int rowH = fm.getRowHeight();
        SettingColor c = setting.get();
        fillNative(context, x + 3 + 1, y, (x + width) - 1, y + rowH, fm.secondaryAlpha(Opcode.GETFIELD));
        int clipLeft = x + 3 + 1;
        int clipRight = (x + width) - 1;
        int swatchSize = Math.min(Math.max(5, rowH - 5), 7);
        int swatchRight = clipRight - 3;
        int swatchLeft = swatchRight - swatchSize;
        int swatchTop = y + ((rowH - swatchSize) / 2);
        int swatchBottom = swatchTop + swatchSize;
        drawCheckerboard(context, swatchLeft, swatchTop, swatchSize, swatchSize);
        int argb = ((c.a & 255) << 24) | ((c.r & 255) << 16) | ((c.g & 255) << 8) | (c.b & 255);
        fillNative(context, swatchLeft, swatchTop, swatchRight, swatchBottom, argb);
        RenderUtils.drawThinOutline(context, swatchLeft, swatchTop, swatchSize, swatchSize, GuiColors.BORDER_LIGHT);
        context.method_44379(clipLeft, y, swatchLeft - 2, y + rowH);
        fm.drawText(context, setting.name, clipLeft + 3, textY(y, rowH), fm.getTextColor());
        context.method_44380();
        if (isPickerOpen(setting)) {
            renderColorPicker(context, fm, setting, x, y + rowH, width);
        }
    }

    private static void renderColorPicker(class_332 context, FontManager fm, ColorSetting setting, int x, int y, int width) {
        PickerState ps = ensurePicker(setting);
        int rowH = fm.getRowHeight();
        int sliderStart = x + 3 + 1;
        int sliderWidth = (width - 3) - 2;
        int svHeight = rowH * 6;
        drawSVGradient(context, ps.hue, sliderStart, y, sliderWidth, svHeight);
        RenderUtils.drawThinOutline(context, sliderStart, y, sliderWidth, svHeight, GuiColors.BORDER);
        int svX = sliderStart + ((int) (ps.saturation * (sliderWidth - 1)));
        int svY = y + ((int) ((1.0f - ps.brightness) * (svHeight - 1)));
        drawCrosshair(context, svX, svY);
        int hueRow = y + svHeight;
        drawHueStrip(context, sliderStart, hueRow, sliderWidth, rowH);
        RenderUtils.drawThinOutline(context, sliderStart, hueRow, sliderWidth, rowH, GuiColors.BORDER);
        int huePos = sliderStart + ((int) (ps.hue * (sliderWidth - 1)));
        drawBarMarker(context, huePos, hueRow, rowH);
        int aRow = hueRow + rowH;
        SettingColor c = setting.get();
        drawCheckerboard(context, sliderStart, aRow, sliderWidth, rowH);
        drawAlphaGradient(context, c.r & 255, c.g & 255, c.b & 255, sliderStart, aRow, sliderWidth, rowH);
        RenderUtils.drawThinOutline(context, sliderStart, aRow, sliderWidth, rowH, GuiColors.BORDER);
        int aPos = sliderStart + ((int) (((c.a & 255) / 255.0f) * (sliderWidth - 1)));
        drawBarMarker(context, aPos, aRow, rowH);
        String alphaLabel = "A " + (c.a & 255);
        fm.drawText(context, alphaLabel, sliderStart + 3, textY(aRow, rowH), -1);
        int btnRow = aRow + rowH;
        int halfW = sliderWidth / 2;
        drawPickerButton(context, fm, "Copy", sliderStart, btnRow, halfW - 1, rowH);
        drawPickerButton(context, fm, "Paste", sliderStart + halfW + 1, btnRow, (sliderWidth - halfW) - 1, rowH);
    }

    private static void drawHueStrip(class_332 context, int x, int y, int w, int h) {
        for (int i = 0; i < w; i++) {
            float hue = i / Math.max(1, w - 1);
            int rgb = Color.HSBtoRGB(hue, 1.0f, 1.0f);
            int argb = (-16777216) | (rgb & 16777215);
            fillNative(context, x + i, y, x + i + 1, y + h, argb);
        }
    }

    private static void drawSVGradient(class_332 context, float hue, int x, int y, int w, int h) {
        int pure = Color.HSBtoRGB(hue, 1.0f, 1.0f);
        int hr = (pure >> 16) & 255;
        int hg = (pure >> 8) & 255;
        int hb = pure & 255;
        for (int i = 0; i < w; i++) {
            float sat = i / Math.max(1, w - 1);
            int r = (int) (((1.0f - sat) * 255.0f) + (sat * hr));
            int g = (int) (((1.0f - sat) * 255.0f) + (sat * hg));
            int b = (int) (((1.0f - sat) * 255.0f) + (sat * hb));
            int baseColor = (-16777216) | (r << 16) | (g << 8) | b;
            fillNative(context, x + i, y, x + i + 1, y + h, baseColor);
        }
        for (int j = 0; j < h; j++) {
            float t = j / Math.max(1, h - 1);
            int alpha = (int) (t * 255.0f);
            int overlay = alpha << 24;
            fillNative(context, x, y + j, x + w, y + j + 1, overlay);
        }
    }

    private static void drawAlphaGradient(class_332 context, int r, int g, int b, int x, int y, int w, int h) {
        for (int i = 0; i < w; i++) {
            float t = i / Math.max(1, w - 1);
            int alpha = (int) (t * 255.0f);
            int color = (alpha << 24) | (r << 16) | (g << 8) | b;
            fillNative(context, x + i, y, x + i + 1, y + h, color);
        }
    }

    private static void drawCheckerboard(class_332 context, int x, int y, int w, int h) {
        fillNative(context, x, y, x + w, y + h, -3355444);
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < w) {
                int i3 = 0;
                while (true) {
                    int j = i3;
                    if (j < h) {
                        if ((((i2 / 3) + (j / 3)) & 1) != 0) {
                            int x2 = Math.min(x + i2 + 3, x + w);
                            int y2 = Math.min(y + j + 3, y + h);
                            fillNative(context, x + i2, y + j, x2, y2, -8355712);
                        }
                        i3 = j + 3;
                    }
                }
                i = i2 + 3;
            } else {
                return;
            }
        }
    }

    private static void drawCrosshair(class_332 context, int cx, int cy) {
        fillNative(context, cx - 1, cy - 1, cx + 2, cy + 2, -16777216);
        fillNative(context, cx, cy, cx + 1, cy + 1, -1);
    }

    private static void drawBarMarker(class_332 context, int cx, int y, int h) {
        fillNative(context, cx - 1, y - 1, cx + 2, y + h + 1, -16777216);
        fillNative(context, cx, y, cx + 1, y + h, -1);
    }

    private static void drawPickerButton(class_332 context, FontManager fm, String label, int x, int y, int w, int h) {
        fillNative(context, x, y, x + w, y + h, fm.secondaryAlpha(200));
        RenderUtils.drawThinOutline(context, x, y, w, h, GuiColors.BORDER);
        int textW = fm.getTextWidth(label);
        fm.drawText(context, label, x + ((w - textW) / 2), textY(y, h), fm.getTextColor());
    }

    private static void renderBlockPosSetting(class_332 context, FontManager fm, BlockPosSetting setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        fillNative(context, x + 3 + 1, y, (x + width) - 1, y + rowH, fm.secondaryAlpha(Opcode.GETFIELD));
        int clipLeft = x + 3 + 1;
        int clipRight = (x + width) - 1;
        context.method_44379(clipLeft, y, clipRight, y + rowH);
        class_2338 p = setting.get();
        String text = setting.name + ": " + p.method_10263() + "," + p.method_10264() + "," + p.method_10260();
        fm.drawTextMarquee(context, setting, text, clipLeft + 3, textY(y, rowH), clipRight - 2, fm.getTextColor(), hovered);
        context.method_44380();
    }

    private static void renderVector3dSetting(class_332 context, FontManager fm, Vector3dSetting setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        fillNative(context, x + 3 + 1, y, (x + width) - 1, y + rowH, fm.secondaryAlpha(Opcode.GETFIELD));
        int clipLeft = x + 3 + 1;
        int clipRight = (x + width) - 1;
        context.method_44379(clipLeft, y, clipRight, y + rowH);
        Vector3d v = setting.get();
        int dp = Math.max(1, setting.decimalPlaces);
        String fmt = "%." + dp + "f";
        String text = setting.name + ": " + String.format(fmt, Double.valueOf(v.x)) + "," + String.format(fmt, Double.valueOf(v.y)) + "," + String.format(fmt, Double.valueOf(v.z));
        fm.drawTextMarquee(context, setting, text, clipLeft + 3, textY(y, rowH), clipRight - 2, fm.getTextColor(), hovered);
        context.method_44380();
    }

    private static void renderJumpRow(class_332 context, FontManager fm, Setting<?> setting, String summary, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        fillNative(context, x + 3 + 1, y, (x + width) - 1, y + rowH, fm.secondaryAlpha(Opcode.GETFIELD));
        int clipLeft = x + 3 + 1;
        int clipRight = (x + width) - 1;
        context.method_44379(clipLeft, y, clipRight, y + rowH);
        fm.drawText(context, setting.name, clipLeft + 3, textY(y, rowH), fm.getTextColor());
        String hint = summary + "  ›";
        int hintColor = hovered ? fm.getTextColor() : GuiColors.TEXT_SETTING_VALUE;
        fm.drawText(context, hint, (clipRight - fm.getTextWidth(hint)) - 2, textY(y, rowH), hintColor);
        context.method_44380();
    }

    private static void renderStubSetting(class_332 context, FontManager fm, Setting<?> setting, int x, int y, int width) {
        int rowH = fm.getRowHeight();
        fillNative(context, x + 3 + 1, y, (x + width) - 1, y + rowH, fm.primaryAlpha(60));
        int clipLeft = x + 3 + 1;
        int clipRight = (x + width) - 1;
        context.method_44379(clipLeft, y, clipRight, y + rowH);
        fm.drawText(context, setting.name, clipLeft + 3, textY(y, rowH), GuiColors.TEXT_STUB);
        fm.drawText(context, "edit →", (clipRight - fm.getTextWidth("edit →")) - 2, textY(y, rowH), GuiColors.TEXT_STUB);
        context.method_44380();
    }

    private static void renderStringSetting(class_332 context, FontManager fm, StringSetting setting, int x, int y, int width) {
        int rowH = fm.getRowHeight();
        boolean editing = editingString == setting;
        if (editing) {
            fillNative(context, x + 3 + 1, y, (x + width) - 1, y + rowH, fm.secondaryAlpha(200));
        }
        int clipLeft = x + 3 + 1;
        int clipRight = (x + width) - 1;
        context.method_44379(clipLeft, y, clipRight, y + rowH);
        String label = setting.name + ": ";
        int labelW = fm.getTextWidth(label);
        fm.drawText(context, label, clipLeft + 3, textY(y, rowH), GuiColors.TEXT_SETTING_VALUE);
        String text = editing ? editingBuffer : setting.get();
        fm.drawText(context, text, clipLeft + 3 + labelW, textY(y, rowH), fm.getTextColor());
        if (editing && ((System.currentTimeMillis() / 500) & 1) == 0) {
            int caretX = clipLeft + 3 + labelW + fm.getTextWidth(text);
            fillNative(context, caretX, y + 2, caretX + 1, (y + rowH) - 2, fm.getTextColor());
        }
        context.method_44380();
    }

    private static void renderProvidedStringSetting(class_332 context, FontManager fm, ProvidedStringSetting setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        fillNative(context, x + 3 + 1, y, (x + width) - 1, y + rowH, fm.secondaryAlpha(Opcode.GETFIELD));
        int clipLeft = x + 3 + 1;
        int clipRight = (x + width) - 1;
        context.method_44379(clipLeft, y, clipRight, y + rowH);
        fm.drawTextMarquee(context, setting, setting.name + ": " + setting.get(), clipLeft + 3, textY(y, rowH), clipRight - 2, fm.getTextColor(), hovered);
        context.method_44380();
    }

    private static void renderFontFaceSetting(class_332 context, FontManager fm, FontFaceSetting setting, int x, int y, int width, boolean hovered) {
        int rowH = fm.getRowHeight();
        fillNative(context, x + 3 + 1, y, (x + width) - 1, y + rowH, fm.secondaryAlpha(Opcode.GETFIELD));
        int clipLeft = x + 3 + 1;
        int clipRight = (x + width) - 1;
        context.method_44379(clipLeft, y, clipRight, y + rowH);
        String shown = setting.get() != null ? setting.get().toString() : "(none)";
        fm.drawTextMarquee(context, setting, setting.name + ": " + shown, clipLeft + 3, textY(y, rowH), clipRight - 2, fm.getTextColor(), hovered);
        context.method_44380();
    }

    private static void renderKeybindSetting(class_332 context, FontManager fm, KeybindSetting setting, int x, int y, int width, boolean hovered) {
        String text;
        int color;
        int rowH = fm.getRowHeight();
        int clipLeft = x + 3 + 1;
        int clipRight = (x + width) - 1;
        context.method_44379(clipLeft, y, clipRight, y + rowH);
        if (NewGuiBindCapture.get().getListeningSetting() == setting) {
            text = setting.name + ": ...";
            color = fm.getTextColor();
        } else {
            Keybind kb = setting.get();
            text = setting.name + ": " + (kb.isSet() ? kb.toString() : "NONE");
            color = GuiColors.TEXT_SETTING_VALUE;
        }
        fm.drawTextMarquee(context, setting, text, clipLeft + 3, textY(y, rowH), clipRight - 2, color, hovered);
        context.method_44380();
    }

    private static int handleSettingClick(Setting<?> setting, int x, int y, int width, int mouseX, int mouseY, int button, Supplier<class_437> fallback) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        if (setting instanceof BoolSetting) {
            BoolSetting bs = (BoolSetting) setting;
            if (mouseY < y || mouseY >= y + rowH || button != 0) {
                return rowH;
            }
            bs.set(Boolean.valueOf(!bs.get().booleanValue()));
            return -1;
        }
        if (setting instanceof EnumSetting) {
            EnumSetting<?> es = (EnumSetting) setting;
            if (mouseY < y || mouseY >= y + rowH || button != 0) {
                return rowH;
            }
            cycleEnum(es);
            return -1;
        }
        if (setting instanceof IntSetting) {
            IntSetting is = (IntSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0 && !is.noSlider) {
                draggingSlider = true;
                draggingSetting = is;
                dragCtx = new DragCtx(x, width);
                updateIntSlider(is, x, width, mouseX);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof DoubleSetting) {
            DoubleSetting ds = (DoubleSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0 && !ds.noSlider) {
                draggingSlider = true;
                draggingSetting = ds;
                dragCtx = new DragCtx(x, width);
                updateDoubleSlider(ds, x, width, mouseX);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof ColorSetting) {
            ColorSetting cs = (ColorSetting) setting;
            if (mouseY >= y && mouseY < y + rowH) {
                if (button == 1) {
                    togglePicker(cs);
                    return -1;
                }
                if (button != 0) {
                    return -1;
                }
                togglePicker(cs);
                return -1;
            }
            if (isPickerOpen(cs)) {
                int baseY = y + rowH;
                int ss = x + 3 + 1;
                int sw = (width - 3) - 2;
                int svHeight = rowH * 6;
                int hueRow = baseY + svHeight;
                int alphaRow = hueRow + rowH;
                int btnRow = alphaRow + rowH;
                if (button == 0 && mouseY >= baseY && mouseY < baseY + svHeight) {
                    draggingColorSetting = cs;
                    draggingColorComponent = 0;
                    dragCtx = new DragCtx(x, width, baseY, svHeight);
                    updateColorSlider(cs, 0, ss, sw, mouseX, mouseY, baseY, svHeight);
                    return -1;
                }
                if (button == 0 && mouseY >= hueRow && mouseY < hueRow + rowH) {
                    draggingColorSetting = cs;
                    draggingColorComponent = 1;
                    dragCtx = new DragCtx(x, width, baseY, svHeight);
                    updateColorSlider(cs, 1, ss, sw, mouseX, mouseY, baseY, svHeight);
                    return -1;
                }
                if (button == 0 && mouseY >= alphaRow && mouseY < alphaRow + rowH) {
                    draggingColorSetting = cs;
                    draggingColorComponent = 2;
                    dragCtx = new DragCtx(x, width, baseY, svHeight);
                    updateColorSlider(cs, 2, ss, sw, mouseX, mouseY, baseY, svHeight);
                    return -1;
                }
                if (button == 0 && mouseY >= btnRow && mouseY < btnRow + rowH) {
                    int mid = ss + (sw / 2);
                    if (mouseX >= mid) {
                        pasteColorFromClipboard(cs);
                        return -1;
                    }
                    copyColorToClipboard(cs);
                    return -1;
                }
                return rowH * (1 + getPickerRows(cs));
            }
            return rowH;
        }
        if (setting instanceof KeybindSetting) {
            KeybindSetting ks = (KeybindSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                NewGuiBindCapture.get().startListeningForSetting(ks);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof ProvidedStringSetting) {
            ProvidedStringSetting pss = (ProvidedStringSetting) setting;
            if (mouseY >= y && mouseY < y + rowH) {
                if (button == 0) {
                    cycleProvidedString(pss, pss.supplier.get(), 1);
                    return -1;
                }
                if (button == 1) {
                    cycleProvidedString(pss, pss.supplier.get(), -1);
                    return -1;
                }
            }
            return rowH;
        }
        if (setting instanceof FontFaceSetting) {
            FontFaceSetting fs = (FontFaceSetting) setting;
            if (mouseY >= y && mouseY < y + rowH) {
                List<FontFace> faces = buildFontFaces();
                if (button == 0) {
                    cycleFontFace(fs, faces, 1);
                    return -1;
                }
                if (button == 1) {
                    cycleFontFace(fs, faces, -1);
                    return -1;
                }
            }
            return rowH;
        }
        if (setting instanceof StringSetting) {
            StringSetting ss2 = (StringSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                commitStringEdit();
                editingString = ss2;
                editingBuffer = ss2.get();
                return -1;
            }
            return rowH;
        }
        if (setting instanceof BlockPosSetting) {
            BlockPosSetting bps = (BlockPosSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                class_310.method_1551().method_1507(new NewVectorEditScreen(bps));
                return -1;
            }
            return rowH;
        }
        if (setting instanceof Vector3dSetting) {
            Vector3dSetting v3s = (Vector3dSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                class_310.method_1551().method_1507(new NewVectorEditScreen(v3s));
                return -1;
            }
            return rowH;
        }
        if (setting instanceof StringListSetting) {
            StringListSetting sls = (StringListSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                class_310.method_1551().method_1507(new NewStringListScreen(sls));
                return -1;
            }
            return rowH;
        }
        if (setting instanceof ModuleListSetting) {
            ModuleListSetting mls = (ModuleListSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                NewCollectionListScreen<Module> screen = new NewCollectionListScreen<>("Select Modules — " + mls.name, mls, mls.get(), Modules.get().getAll(), m -> {
                    return m.title;
                });
                class_310.method_1551().method_1507(screen);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof EntityTypeListSetting) {
            EntityTypeListSetting etls = (EntityTypeListSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                class_310.method_1551().method_1507(new NewEntityTypeScreen(etls));
                return -1;
            }
            return rowH;
        }
        if (setting instanceof EnchantmentListSetting) {
            EnchantmentListSetting els = (EnchantmentListSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<class_5321<class_1887>> all = new ArrayList<>();
                class_310 mc = class_310.method_1551();
                class_638 world = mc.field_1687;
                class_634 nh = mc.method_1562();
                class_2378<class_1887> reg = null;
                if (world != null) {
                    reg = (class_2378) world.method_30349().method_46759(class_7924.field_41265).orElse(null);
                }
                if (reg == null && nh != null) {
                    reg = (class_2378) nh.method_29091().method_46759(class_7924.field_41265).orElse(null);
                }
                if (reg != null) {
                    Stream streamMethod_46754 = reg.method_46754();
                    Objects.requireNonNull(all);
                    streamMethod_46754.forEach((v1) -> {
                        r1.add(v1);
                    });
                } else {
                    all.addAll(els.get());
                }
                NewCollectionListScreen<class_5321<class_1887>> screen2 = new NewCollectionListScreen<>("Select Enchantments — " + els.name, els, els.get(), all, Names::get);
                class_310.method_1551().method_1507(screen2);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof BlockListSetting) {
            BlockListSetting bls = (BlockListSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<class_2248> all2 = new ArrayList<>();
                for (class_2248 b : class_7923.field_41175) {
                    all2.add(b);
                }
                NewCollectionListScreen<class_2248> screen3 = new NewCollectionListScreen<>("Select Blocks — " + bls.name, bls, bls.get(), all2, Names::get, b2 -> {
                    return b2.method_8389().method_7854();
                }, null);
                class_310.method_1551().method_1507(screen3);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof ItemListSetting) {
            ItemListSetting ils = (ItemListSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<class_1792> all3 = new ArrayList<>();
                for (class_1792 it : class_7923.field_41178) {
                    all3.add(it);
                }
                NewCollectionListScreen<class_1792> screen4 = new NewCollectionListScreen<>("Select Items — " + ils.name, ils, ils.get(), all3, Names::get, (v0) -> {
                    return v0.method_7854();
                }, null);
                class_310.method_1551().method_1507(screen4);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof StatusEffectListSetting) {
            StatusEffectListSetting sels = (StatusEffectListSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<class_1291> all4 = new ArrayList<>();
                for (class_1291 e : class_7923.field_41174) {
                    all4.add(e);
                }
                NewCollectionListScreen<class_1291> screen5 = new NewCollectionListScreen<>("Select Effects — " + sels.name, sels, sels.get(), all4, Names::get, SettingGroupRenderer::statusEffectIcon, null);
                class_310.method_1551().method_1507(screen5);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof SoundEventListSetting) {
            SoundEventListSetting sevls = (SoundEventListSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<class_3414> all5 = new ArrayList<>();
                for (class_3414 e2 : class_7923.field_41172) {
                    all5.add(e2);
                }
                NewCollectionListScreen<class_3414> screen6 = new NewCollectionListScreen<>("Select Sounds — " + sevls.name, sevls, sevls.get(), all5, e3 -> {
                    return e3.comp_3319().method_12832();
                }, null, null);
                class_310.method_1551().method_1507(screen6);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof ParticleTypeListSetting) {
            ParticleTypeListSetting ptls = (ParticleTypeListSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<class_2396<?>> all6 = new ArrayList<>();
                for (class_2396<?> p : class_7923.field_41180) {
                    all6.add(p);
                }
                NewCollectionListScreen<class_2396<?>> screen7 = new NewCollectionListScreen<>("Select Particles — " + ptls.name, ptls, ptls.get(), all6, Names::get, null, p2 -> {
                    return !(p2 instanceof class_2394);
                });
                class_310.method_1551().method_1507(screen7);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof PacketListSetting) {
            PacketListSetting pls = (PacketListSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<Class<? extends class_2596<?>>> all7 = new ArrayList<>();
                for (Class<? extends class_2596<?>> c : PacketUtils.PACKETS) {
                    all7.add(c);
                }
                NewCollectionListScreen<Class<? extends class_2596<?>>> screen8 = new NewCollectionListScreen<>("Select Packets — " + pls.name, pls, pls.get(), all7, PacketUtils::getName, null, c2 -> {
                    return (pls.filter == null || pls.filter.test(c2)) ? false : true;
                });
                class_310.method_1551().method_1507(screen8);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof ScreenHandlerListSetting) {
            ScreenHandlerListSetting shls = (ScreenHandlerListSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<class_3917<?>> all8 = new ArrayList<>();
                for (class_3917<?> t : class_7923.field_41187) {
                    all8.add(t);
                }
                NewCollectionListScreen<class_3917<?>> screen9 = new NewCollectionListScreen<>("Select Screen Handlers — " + shls.name, shls, shls.get(), all8, t2 -> {
                    class_2960 id = class_7923.field_41187.method_10221(t2);
                    return id == null ? "(null)" : id.toString();
                }, null, null);
                class_310.method_1551().method_1507(screen9);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof StorageBlockListSetting) {
            StorageBlockListSetting sbls = (StorageBlockListSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<class_2591<?>> all9 = new ArrayList<>();
                for (class_2591<?> t3 : StorageBlockListSetting.REGISTRY) {
                    all9.add(t3);
                }
                NewCollectionListScreen<class_2591<?>> screen10 = new NewCollectionListScreen<>("Select Storage Blocks — " + sbls.name, sbls, sbls.get(), all9, SettingGroupRenderer::storageBlockName, t4 -> {
                    class_1792 item = storageBlockItem(t4);
                    if (item == null) {
                        return null;
                    }
                    return item.method_7854();
                }, null);
                class_310.method_1551().method_1507(screen10);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof BlockSetting) {
            BlockSetting bs2 = (BlockSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<class_2248> all10 = new ArrayList<>();
                for (class_2248 b3 : class_7923.field_41175) {
                    all10.add(b3);
                }
                NewSingleValueScreen<class_2248> screen11 = new NewSingleValueScreen<>("Select Block — " + bs2.name, bs2, all10, Names::get, b4 -> {
                    return b4.method_8389().method_7854();
                }, null, null);
                class_310.method_1551().method_1507(screen11);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof ItemSetting) {
            ItemSetting is2 = (ItemSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                List<class_1792> all11 = new ArrayList<>();
                for (class_1792 it2 : class_7923.field_41178) {
                    all11.add(it2);
                }
                NewSingleValueScreen<class_1792> screen12 = new NewSingleValueScreen<>("Select Item — " + is2.name, is2, all11, Names::get, (v0) -> {
                    return v0.method_7854();
                }, null, null);
                class_310.method_1551().method_1507(screen12);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof ColorListSetting) {
            ColorListSetting cls = (ColorListSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                class_310.method_1551().method_1507(new NewColorListScreen(cls));
                return -1;
            }
            return rowH;
        }
        if (setting instanceof StatusEffectAmplifierMapSetting) {
            StatusEffectAmplifierMapSetting seams = (StatusEffectAmplifierMapSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                class_310.method_1551().method_1507(new NewAmplifierMapScreen(seams));
                return -1;
            }
            return rowH;
        }
        if (setting instanceof BlockDataSetting) {
            BlockDataSetting<?> bds = (BlockDataSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                openBlockDataPicker(bds);
                return -1;
            }
            return rowH;
        }
        if (setting instanceof GenericSetting) {
            GenericSetting<?> gs = (GenericSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                Object factory = gs.get();
                if (factory == null) {
                    return -1;
                }
                CapturingGuiTheme capture = new CapturingGuiTheme();
                class_437 widgetScreen = null;
                try {
                    widgetScreen = ((IScreenFactory) factory).createScreen(capture);
                    if (widgetScreen instanceof WidgetScreen) {
                        WidgetScreen ws = (WidgetScreen) widgetScreen;
                        ws.initWidgets();
                    }
                } catch (Throwable th) {
                }
                if (capture.captured != null) {
                    class_310.method_1551().method_1507(new NewBlockEditScreen("Configure " + setting.name, capture.captured, class_310.method_1551().field_1755));
                    return -1;
                }
                if (widgetScreen != null) {
                    class_310.method_1551().method_1507(widgetScreen);
                    return -1;
                }
                class_310.method_1551().method_1507(((IScreenFactory) factory).createScreen(GuiThemes.get()));
                return -1;
            }
            return rowH;
        }
        if (setting instanceof ActionSetting) {
            ActionSetting as = (ActionSetting) setting;
            if (mouseY >= y && mouseY < y + rowH && button == 0) {
                if (as.action != null) {
                    as.action.run();
                    return -1;
                }
                return -1;
            }
            return rowH;
        }
        if (mouseY >= y && mouseY < y + rowH && button == 0 && fallback != null) {
            class_437 s = fallback.get();
            if (s != null) {
                class_310.method_1551().method_1507(s);
                return -1;
            }
            return -1;
        }
        return rowH;
    }

    private static void openBlockDataPicker(BlockDataSetting<?> setting) {
        class_310.method_1551().method_1507(new NewBlockDataScreen(setting));
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static void cycleEnum(EnumSetting setting) {
        Object[] constants = setting.get().getClass().getEnumConstants();
        if (constants == null || constants.length == 0) {
            return;
        }
        int idx = ((Enum) setting.get()).ordinal();
        setting.set((Enum) constants[(idx + 1) % constants.length]);
    }

    private static void cycleProvidedString(ProvidedStringSetting setting, String[] opts, int direction) {
        if (opts == null || opts.length == 0) {
            return;
        }
        String current = setting.get();
        int idx = -1;
        int i = 0;
        while (true) {
            if (i >= opts.length) {
                break;
            }
            if (opts[i].equals(current)) {
                idx = i;
                break;
            }
            i++;
        }
        int next = idx < 0 ? 0 : (((idx + direction) % opts.length) + opts.length) % opts.length;
        setting.set(opts[next]);
    }

    private static void cycleFontFace(FontFaceSetting setting, List<FontFace> faces, int direction) {
        if (faces.isEmpty()) {
            return;
        }
        int current = -1;
        FontFace cur = setting.get();
        if (cur != null) {
            int i = 0;
            while (true) {
                if (i >= faces.size()) {
                    break;
                }
                if (faces.get(i).toString().equals(cur.toString())) {
                    current = i;
                    break;
                }
                i++;
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
                    if (face != null) {
                        faces.add(face);
                    }
                } catch (Throwable th) {
                }
            }
        }
        return faces;
    }

    private static void updateIntSlider(IntSetting setting, int x, int width, int mouseX) {
        int ss = x + 3 + 1;
        int sw = (width - 3) - 2;
        double progress = Math.max(0.0d, Math.min(1.0d, ((double) (mouseX - ss)) / ((double) sw)));
        int minV = Math.max(setting.min, setting.sliderMin);
        int maxV = Math.min(setting.max, setting.sliderMax);
        int value = Math.max(setting.min, Math.min(setting.max, (int) Math.round(((double) minV) + (((double) (maxV - minV)) * progress))));
        if (!setting.get().equals(Integer.valueOf(value))) {
            setting.set(Integer.valueOf(value));
        }
    }

    private static void updateDoubleSlider(DoubleSetting setting, int x, int width, int mouseX) {
        int ss = x + 3 + 1;
        int sw = (width - 3) - 2;
        double progress = Math.max(0.0d, Math.min(1.0d, ((double) (mouseX - ss)) / ((double) sw)));
        double minV = Math.max(setting.min, setting.sliderMin);
        double maxV = Math.min(setting.max, setting.sliderMax);
        double value = Math.max(setting.min, Math.min(setting.max, minV + ((maxV - minV) * progress)));
        if (!setting.get().equals(Double.valueOf(value))) {
            setting.set(Double.valueOf(value));
        }
    }

    private static void updateColorSlider(ColorSetting setting, int component, int ss, int sw, int mouseX, int mouseY, int svTop, int svHeight) {
        PickerState ps = ensurePicker(setting);
        float progressX = (float) Math.max(0.0d, Math.min(1.0d, ((double) (mouseX - ss)) / ((double) sw)));
        switch (component) {
            case 0:
                ps.saturation = progressX;
                float progressY = svHeight <= 0 ? 0.0f : (float) Math.max(0.0d, Math.min(1.0d, ((double) (mouseY - svTop)) / ((double) svHeight)));
                ps.brightness = 1.0f - progressY;
                break;
            case 1:
                ps.hue = progressX;
                break;
            case 2:
                setting.get().a = (int) (progressX * 255.0f);
                setting.onChanged();
                return;
            default:
                return;
        }
        int rgb = Color.HSBtoRGB(ps.hue, ps.saturation, ps.brightness);
        SettingColor c = setting.get();
        c.r = (rgb >> 16) & 255;
        c.g = (rgb >> 8) & 255;
        c.b = rgb & 255;
        setting.onChanged();
    }

    private static void copyColorToClipboard(ColorSetting setting) {
        SettingColor c = setting.get();
        String hex = String.format("#%02X%02X%02X%02X", Integer.valueOf(c.r & 255), Integer.valueOf(c.g & 255), Integer.valueOf(c.b & 255), Integer.valueOf(c.a & 255));
        class_310.method_1551().field_1774.method_1455(hex);
    }

    private static void pasteColorFromClipboard(ColorSetting setting) {
        String raw = class_310.method_1551().field_1774.method_1460();
        if (raw == null) {
            return;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return;
        }
        SettingColor c = setting.get();
        try {
            if (s.startsWith("#")) {
                s = s.substring(1);
            }
            if (s.length() == 6) {
                int val = Integer.parseUnsignedInt(s, 16);
                c.r = (val >> 16) & 255;
                c.g = (val >> 8) & 255;
                c.b = val & 255;
            } else if (s.length() == 8) {
                long val2 = Long.parseUnsignedLong(s, 16);
                c.r = (int) ((val2 >> 24) & 255);
                c.g = (int) ((val2 >> 16) & 255);
                c.b = (int) ((val2 >> 8) & 255);
                c.a = (int) (val2 & 255);
            } else if (s.contains(",")) {
                String[] parts = s.split(",");
                if (parts.length < 3) {
                    return;
                }
                c.r = clamp255(Integer.parseInt(parts[0].trim()));
                c.g = clamp255(Integer.parseInt(parts[1].trim()));
                c.b = clamp255(Integer.parseInt(parts[2].trim()));
                if (parts.length >= 4) {
                    c.a = clamp255(Integer.parseInt(parts[3].trim()));
                }
            } else {
                return;
            }
            setting.onChanged();
            PickerState ps = pickerStates.get(setting);
            if (ps != null) {
                float[] hsb = Color.RGBtoHSB(c.r & 255, c.g & 255, c.b & 255, (float[]) null);
                ps.hue = hsb[0];
                ps.saturation = hsb[1];
                ps.brightness = hsb[2];
            }
        } catch (Throwable th) {
        }
    }

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/components/SettingGroupRenderer$PickerState.class */
    private static class PickerState {
        boolean open;
        float hue;
        float saturation;
        float brightness;

        private PickerState() {
        }
    }

    private static PickerState ensurePicker(ColorSetting setting) {
        PickerState ps = pickerStates.get(setting);
        if (ps == null) {
            ps = new PickerState();
            SettingColor c = setting.get();
            float[] hsb = Color.RGBtoHSB(c.r, c.g, c.b, (float[]) null);
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
        return isPickerOpen(setting) ? 9 : 0;
    }

    public static class_1799 statusEffectIcon(class_1291 effect) {
        class_1799 potion = class_1802.field_8574.method_7854();
        class_1844 contents = (class_1844) potion.method_58694(class_9334.field_49651);
        if (contents == null) {
            return potion;
        }
        potion.method_57379(class_9334.field_49651, new class_1844(contents.comp_2378(), Optional.of(Integer.valueOf(effect.method_5556())), contents.comp_2380(), Optional.empty()));
        return potion;
    }

    public static String storageBlockName(class_2591<?> type) {
        class_2960 id = class_7923.field_41181.method_10221(type);
        if (id == null) {
            return "(unknown)";
        }
        String path = id.method_12832();
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            if (!parts[i].isEmpty()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
            }
        }
        return sb.toString();
    }

    public static class_1792 storageBlockItem(class_2591<?> type) {
        class_1792 item;
        class_2960 id = class_7923.field_41181.method_10221(type);
        if (id != null && (item = (class_1792) class_7923.field_41178.method_63535(id)) != class_1802.field_8162) {
            return item;
        }
        return class_1802.field_8077;
    }
}
