package meteordevelopment.meteorclient.gui.newgui.screens;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.components.CapturingGuiTheme;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.BlockDataSetting;
import meteordevelopment.meteorclient.settings.IBlockData;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.class_1799;
import net.minecraft.class_2248;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewBlockDataScreen.class */
public class NewBlockDataScreen<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBlockData<T>> extends class_437 {
    private final BlockDataSetting<T> setting;
    private String filterText;
    private boolean filterFocused;
    private int scroll;
    private int panelX;
    private int panelWidth;
    private int panelY;
    private int panelHeight;
    private int searchY;
    private static final int PADDING = 6;
    private static final int SEARCH_HEIGHT_MULT = 2;
    private static final int ICON_SIZE = 16;
    private static final int REMOVE_BTN_WIDTH = 18;

    public NewBlockDataScreen(BlockDataSetting<T> setting) {
        super(class_2561.method_43470("Configure Blocks — " + setting.name));
        this.filterText = "";
        this.filterFocused = true;
        this.scroll = 0;
        this.setting = setting;
    }

    protected void method_25426() {
        super.method_25426();
        int screenW = this.field_22789;
        int screenH = this.field_22790;
        int margin = Math.max(20, screenW / 6);
        this.panelWidth = screenW - (margin * 2);
        this.panelX = margin;
        int searchH = FontManager.get().getRowHeight() * 2;
        this.searchY = 20;
        this.panelY = this.searchY + searchH + 8;
        this.panelHeight = (screenH - this.panelY) - 20;
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        String str;
        FontManager fm = FontManager.get();
        super.method_25394(context, mouseX, mouseY, delta);
        int lineColor = fm.primaryAlpha(Opcode.GETFIELD);
        int fillColor = fm.secondaryAlpha(200);
        int headerH = fm.getHeaderHeight();
        int rowH = fm.getRowHeight();
        fm.drawText(context, this.field_22785.getString(), this.panelX, 4, fm.getTextColor());
        int searchH = rowH * 2;
        context.method_25294(this.panelX, this.searchY, this.panelX + this.panelWidth, this.searchY + searchH, this.filterFocused ? fm.secondaryAlpha(220) : fm.secondaryAlpha(Opcode.FCMPG));
        RenderUtils.drawThinOutline(context, this.panelX, this.searchY, this.panelWidth, searchH, lineColor);
        int labelW = fm.getTextWidth("Search: ");
        fm.drawText(context, "Search: ", this.panelX + 6, this.searchY + ((searchH - fm.getTextHeight()) / 2), GuiColors.TEXT_SETTING_VALUE);
        fm.drawText(context, this.filterText, this.panelX + 6 + labelW, this.searchY + ((searchH - fm.getTextHeight()) / 2), fm.getTextColor());
        if (this.filterFocused && ((System.currentTimeMillis() / 500) & 1) == 0) {
            int caretX = this.panelX + 6 + labelW + fm.getTextWidth(this.filterText);
            context.method_25294(caretX, this.searchY + 3, caretX + 1, (this.searchY + searchH) - 3, fm.getTextColor());
        }
        fm.drawText(context, "[Clear]", ((this.panelX + this.panelWidth) - fm.getTextWidth("[Clear]")) - 6, this.searchY + ((searchH - fm.getTextHeight()) / 2), GuiColors.TEXT_SETTING_VALUE);
        context.method_25294(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + this.panelHeight, fm.primaryAlpha(Opcode.ISHL));
        RenderUtils.drawThinOutline(context, this.panelX, this.panelY, this.panelWidth, this.panelHeight, lineColor);
        int overrideCount = countOverrides();
        if (overrideCount == 0) {
            str = "Blocks (no overrides yet)";
        } else {
            str = "Blocks (" + overrideCount + " override" + (overrideCount == 1 ? "" : "s") + ")";
        }
        String heading = str;
        context.method_25294(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + headerH, fillColor);
        RenderUtils.drawThinHLine(context, this.panelX, this.panelY + headerH, this.panelWidth, lineColor);
        fm.drawText(context, heading, this.panelX + 6, this.panelY + ((headerH - fm.getTextHeight()) / 2), fm.getTextColor());
        List<class_2248> blocks = buildFilteredList();
        int effectiveRowH = Math.max(rowH, 18);
        int rowY = ((this.panelY + headerH) + 1) - this.scroll;
        int rowEnd = this.panelY + this.panelHeight;
        context.method_44379(this.panelX + 1, this.panelY + headerH + 1, (this.panelX + this.panelWidth) - 1, rowEnd - 1);
        if (blocks.isEmpty()) {
            String msg = this.filterText.isEmpty() ? "(no blocks)" : "(no matches)";
            int tx = this.panelX + ((this.panelWidth - fm.getTextWidth(msg)) / 2);
            int ty = this.panelY + headerH + (((this.panelHeight - headerH) - fm.getTextHeight()) / 2);
            fm.drawText(context, msg, tx, ty, GuiColors.TEXT_DISABLED);
        }
        for (class_2248 block : blocks) {
            if (rowY + effectiveRowH < this.panelY + headerH + 1) {
                rowY += effectiveRowH;
            } else {
                if (rowY > rowEnd) {
                    break;
                }
                ICopyable iCopyable = (ICopyable) ((Map) this.setting.get()).get(block);
                boolean hasOverride = iCopyable != null && ((IChangeable) iCopyable).isChanged();
                boolean rowHovered = mouseX >= this.panelX + 1 && mouseX < (this.panelX + this.panelWidth) - 1 && mouseY >= rowY && mouseY < rowY + effectiveRowH && mouseY >= (this.panelY + headerH) + 1 && mouseY <= rowEnd - 1;
                if (rowHovered) {
                    context.method_25294(this.panelX + 1, rowY, (this.panelX + this.panelWidth) - 1, rowY + effectiveRowH, fm.secondaryAlpha(Opcode.ISHL));
                }
                int textColor = (rowHovered || hasOverride) ? fm.getTextColor() : fm.getTextSecondary();
                int tcolor = textColor;
                class_1799 icon = block.method_8389().method_7854();
                int iconY = rowY + ((effectiveRowH - 16) / 2);
                context.method_51427(icon, this.panelX + 6, iconY);
                String label = Names.get(block) + (hasOverride ? "  *" : "");
                int nameX = this.panelX + 6 + 16 + 4;
                fm.drawText(context, label, nameX, rowY + ((effectiveRowH - fm.getTextHeight()) / 2), tcolor);
                if (hasOverride) {
                    int xBtnX = ((this.panelX + this.panelWidth) - 18) - 2;
                    boolean xBtnHovered = mouseX >= xBtnX && mouseX < xBtnX + 18 && mouseY >= rowY && mouseY < rowY + effectiveRowH;
                    int xColor = xBtnHovered ? -40864 : GuiColors.TEXT_DISABLED;
                    int xw = fm.getTextWidth("reset");
                    fm.drawText(context, "reset", xBtnX + ((18 - xw) / 2), rowY + ((effectiveRowH - fm.getTextHeight()) / 2), xColor);
                }
                rowY += effectiveRowH;
            }
        }
        context.method_44380();
        int totalRowsH = blocks.size() * effectiveRowH;
        int visibleRowsH = (this.panelHeight - headerH) - 2;
        if (totalRowsH > visibleRowsH) {
            int sx = (this.panelX + this.panelWidth) - 3;
            int scrollH = Math.max(10, (visibleRowsH * visibleRowsH) / totalRowsH);
            int scrollY = this.panelY + headerH + 2 + ((this.scroll * (visibleRowsH - scrollH)) / Math.max(1, totalRowsH - visibleRowsH));
            context.method_25294(sx, scrollY, sx + 2, scrollY + scrollH, fm.secondaryAlpha(220));
        }
    }

    private int countOverrides() {
        int n = 0;
        for (Map.Entry<class_2248, T> e : ((Map) this.setting.get()).entrySet()) {
            if (e.getValue() != null && ((IChangeable) ((ICopyable) e.getValue())).isChanged()) {
                n++;
            }
        }
        return n;
    }

    private List<class_2248> buildFilteredList() {
        List<class_2248> changed = new ArrayList<>();
        List<class_2248> rest = new ArrayList<>();
        for (class_2248 b : class_7923.field_41175) {
            String name = Names.get(b);
            if (matchesFilter(name, this.filterText)) {
                ICopyable iCopyable = (ICopyable) ((Map) this.setting.get()).get(b);
                if (iCopyable == null || !((IChangeable) iCopyable).isChanged()) {
                    rest.add(b);
                } else {
                    changed.add(b);
                }
            }
        }
        changed.sort(Comparator.comparing(Names::get));
        rest.sort(Comparator.comparing(Names::get));
        changed.addAll(rest);
        return changed;
    }

    private static boolean matchesFilter(String name, String filter) {
        if (filter.isEmpty()) {
            return true;
        }
        String a = name.toLowerCase();
        String b = filter.toLowerCase();
        return a.contains(b);
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int headerH = fm.getHeaderHeight();
        int searchH = rowH * 2;
        if (mouseX >= this.panelX && mouseX < this.panelX + this.panelWidth && mouseY >= this.searchY && mouseY < this.searchY + searchH) {
            this.filterFocused = true;
            int clearX = ((this.panelX + this.panelWidth) - fm.getTextWidth("[Clear]")) - 6;
            if (mouseX >= clearX) {
                this.filterText = "";
                this.scroll = 0;
                return true;
            }
            return true;
        }
        this.filterFocused = false;
        if (mouseX < this.panelX || mouseX >= this.panelX + this.panelWidth || mouseY < this.panelY || mouseY >= this.panelY + this.panelHeight) {
            return super.method_25402(mouseX, mouseY, button);
        }
        int effectiveRowH = Math.max(rowH, 18);
        int rowY = ((this.panelY + headerH) + 1) - this.scroll;
        int rowEnd = this.panelY + this.panelHeight;
        List<class_2248> blocks = buildFilteredList();
        for (class_2248 block : blocks) {
            if (mouseY >= rowY && mouseY < rowY + effectiveRowH && mouseY >= this.panelY + headerH + 1 && mouseY <= rowEnd - 1) {
                ICopyable iCopyable = (ICopyable) ((Map) this.setting.get()).get(block);
                boolean hasOverride = iCopyable != null && ((IChangeable) iCopyable).isChanged();
                if (hasOverride && button == 0) {
                    int xBtnX = ((this.panelX + this.panelWidth) - 18) - 2;
                    if (mouseX >= xBtnX && mouseX < xBtnX + 18) {
                        ((Map) this.setting.get()).remove(block);
                        this.setting.onChanged();
                        return true;
                    }
                }
                if (button == 0) {
                    openBlockEdit(block, iCopyable);
                    return true;
                }
                if (button == 1 && hasOverride) {
                    ((Map) this.setting.get()).remove(block);
                    this.setting.onChanged();
                    return true;
                }
                return true;
            }
            rowY += effectiveRowH;
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    /* JADX WARN: Incorrect types in method signature: (Lnet/minecraft/class_2248;TT;)V */
    private void openBlockEdit(class_2248 block, ICopyable existing) {
        class_310 mc = class_310.method_1551();
        ICopyable iCopyableCopy = existing;
        if (iCopyableCopy == null) {
            iCopyableCopy = ((ICopyable) this.setting.defaultData.get()).copy();
        }
        ((Map) this.setting.get()).put(block, iCopyableCopy);
        CapturingGuiTheme capture = new CapturingGuiTheme();
        class_437 widgetScreen = null;
        try {
            widgetScreen = ((IBlockData) iCopyableCopy).createScreen(capture, block, this.setting);
            if (widgetScreen instanceof WidgetScreen) {
                WidgetScreen ws = (WidgetScreen) widgetScreen;
                ws.initWidgets();
            }
        } catch (Throwable th) {
        }
        if (capture.captured != null) {
            String title = "Configure " + Names.get(block);
            mc.method_1507(new NewBlockEditScreen(title, capture.captured, new NewBlockDataScreen(this.setting)));
        } else if (widgetScreen != null) {
            mc.method_1507(widgetScreen);
        } else {
            mc.method_1507(((IBlockData) iCopyableCopy).createScreen(GuiThemes.get(), block, this.setting));
        }
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        FontManager fm = FontManager.get();
        int effectiveRowH = Math.max(fm.getRowHeight(), 18);
        int totalRowsH = buildFilteredList().size() * effectiveRowH;
        int visibleRowsH = (this.panelHeight - fm.getHeaderHeight()) - 2;
        int maxScroll = Math.max(0, totalRowsH - visibleRowsH);
        this.scroll = Math.max(0, Math.min(maxScroll, this.scroll - ((int) ((verticalAmount * ((double) effectiveRowH)) * 2.0d))));
        return true;
    }

    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (this.filterFocused) {
            if (keyCode == 259) {
                if (!this.filterText.isEmpty()) {
                    this.filterText = this.filterText.substring(0, this.filterText.length() - 1);
                    this.scroll = 0;
                    return true;
                }
                return true;
            }
            if (keyCode == 256) {
                this.filterFocused = false;
                return true;
            }
        } else if (keyCode == 256) {
            method_25419();
            return true;
        }
        return super.method_25404(keyCode, scanCode, modifiers);
    }

    public boolean method_25400(char chr, int modifiers) {
        if (this.filterFocused && chr >= ' ' && chr != 127) {
            this.filterText += chr;
            this.scroll = 0;
            return true;
        }
        return super.method_25400(chr, modifiers);
    }
}
