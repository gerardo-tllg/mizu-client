package meteordevelopment.meteorclient.gui.newgui.screens;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.ArrayList;
import java.util.List;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.components.ModuleButton;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.StatusEffectAmplifierMapSetting;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.class_1291;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewAmplifierMapScreen.class */
public class NewAmplifierMapScreen extends class_437 {
    private final StatusEffectAmplifierMapSetting setting;
    private final Reference2IntMap<class_1291> map;
    private String filterText;
    private boolean filterFocused;
    private int scroll;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int searchY;
    private static final int PADDING = 6;
    private static final int SEARCH_HEIGHT_MULT = 2;
    private static final int ICON_SIZE = 16;
    private static final int ROW_H_MIN = 18;
    private static final int MAX_AMP = 255;

    public NewAmplifierMapScreen(StatusEffectAmplifierMapSetting setting) {
        super(class_2561.method_43470("Modify Amplifiers — " + setting.name));
        this.filterText = "";
        this.filterFocused = true;
        this.scroll = 0;
        this.setting = setting;
        this.map = setting.get();
    }

    protected void method_25426() {
        super.method_25426();
        int margin = Math.max(40, this.field_22789 / 4);
        this.panelX = margin;
        this.panelWidth = this.field_22789 - (margin * 2);
        int searchH = FontManager.get().getRowHeight() * 2;
        this.searchY = 20;
        this.panelY = this.searchY + searchH + 8;
        this.panelHeight = (this.field_22790 - this.panelY) - 20;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        FontManager fm = FontManager.get();
        super.method_25394(context, mouseX, mouseY, delta);
        int lineColor = fm.primaryAlpha(Opcode.GETFIELD);
        int rowH = Math.max(fm.getRowHeight(), 18);
        int headerH = fm.getHeaderHeight();
        fm.drawText(context, this.field_22785.getString(), this.panelX, 4, fm.getTextColor());
        int searchH = rowH * 2;
        context.method_25294(this.panelX, this.searchY, this.panelX + this.panelWidth, this.searchY + searchH, this.filterFocused ? fm.secondaryAlpha(220) : fm.secondaryAlpha(Opcode.FCMPG));
        RenderUtils.drawThinOutline(context, this.panelX, this.searchY, this.panelWidth, searchH, lineColor);
        int labelW = fm.getTextWidth("Search: ");
        int searchTextY = this.searchY + ((searchH - fm.getTextHeight()) / 2);
        fm.drawText(context, "Search: ", this.panelX + 6, searchTextY, GuiColors.TEXT_SETTING_VALUE);
        fm.drawText(context, this.filterText, this.panelX + 6 + labelW, searchTextY, fm.getTextColor());
        if (this.filterFocused && ((System.currentTimeMillis() / 500) & 1) == 0) {
            int caretX = this.panelX + 6 + labelW + fm.getTextWidth(this.filterText);
            context.method_25294(caretX, this.searchY + 3, caretX + 1, (this.searchY + searchH) - 3, fm.getTextColor());
        }
        fm.drawText(context, "[Clear]", ((this.panelX + this.panelWidth) - fm.getTextWidth("[Clear]")) - 6, searchTextY, GuiColors.TEXT_SETTING_VALUE);
        context.method_25294(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + this.panelHeight, fm.primaryAlpha(Opcode.ISHL));
        RenderUtils.drawThinOutline(context, this.panelX, this.panelY, this.panelWidth, this.panelHeight, lineColor);
        List<class_1291> items = buildFilteredList();
        context.method_25294(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + headerH, fm.secondaryAlpha(200));
        RenderUtils.drawThinHLine(context, this.panelX, this.panelY + headerH, this.panelWidth, lineColor);
        int activeCount = 0;
        ObjectIterator it = this.map.keySet().iterator();
        while (it.hasNext()) {
            class_1291 e = (class_1291) it.next();
            if (this.map.getInt(e) > 0) {
                activeCount++;
            }
        }
        String heading = "Effects (" + activeCount + " active)";
        fm.drawText(context, heading, this.panelX + 6, this.panelY + ((headerH - fm.getTextHeight()) / 2), fm.getTextColor());
        int rowY = ((this.panelY + headerH) + 1) - this.scroll;
        int rowEnd = this.panelY + this.panelHeight;
        context.method_44379(this.panelX + 1, this.panelY + headerH + 1, (this.panelX + this.panelWidth) - 1, rowEnd - 1);
        for (class_1291 effect : items) {
            if (rowY + rowH < this.panelY + headerH + 1) {
                rowY += rowH;
            } else {
                if (rowY > rowEnd) {
                    break;
                }
                boolean hovered = hit((double) mouseX, (double) mouseY, this.panelX + 1, rowY, this.panelWidth - 2, rowH) && mouseY >= (this.panelY + headerH) + 1 && mouseY <= rowEnd - 1;
                int amp = this.map.getInt(effect);
                boolean active = amp > 0;
                if (hovered || active) {
                    context.method_25294(this.panelX + 1, rowY, (this.panelX + this.panelWidth) - 1, rowY + rowH, fm.secondaryAlpha(hovered ? Opcode.FCMPG : 90));
                }
                int iconX = this.panelX + 6;
                int iconY = rowY + ((rowH - 16) / 2);
                class_1799 icon = ModuleButton.statusEffectIcon(effect);
                if (icon != null && !icon.method_7960()) {
                    context.method_51427(icon, iconX, iconY);
                }
                int textX = iconX + 16 + 4;
                int tcolor = active ? fm.getTextColor() : fm.getTextSecondary();
                fm.drawText(context, Names.get(effect), textX, rowY + ((rowH - fm.getTextHeight()) / 2), tcolor);
                String val = String.valueOf(amp);
                int plusX = ((this.panelX + this.panelWidth) - fm.getTextWidth("[+]")) - 6;
                int valX = (plusX - 4) - fm.getTextWidth(val);
                int minusX = (valX - 4) - fm.getTextWidth("[-]");
                int ampColor = active ? fm.getTextColor() : GuiColors.TEXT_DISABLED;
                fm.drawText(context, "[-]", minusX, rowY + ((rowH - fm.getTextHeight()) / 2), amp > 0 ? fm.getTextColor() : GuiColors.TEXT_DISABLED);
                fm.drawText(context, val, valX, rowY + ((rowH - fm.getTextHeight()) / 2), ampColor);
                fm.drawText(context, "[+]", plusX, rowY + ((rowH - fm.getTextHeight()) / 2), amp < MAX_AMP ? fm.getTextColor() : GuiColors.TEXT_DISABLED);
                rowY += rowH;
            }
        }
        context.method_44380();
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = Math.max(fm.getRowHeight(), 18);
        int headerH = fm.getHeaderHeight();
        int searchH = rowH * 2;
        if (hit((int) mouseX, (int) mouseY, this.panelX, this.searchY, this.panelWidth, searchH)) {
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
        if (!hit((int) mouseX, (int) mouseY, this.panelX, this.panelY, this.panelWidth, this.panelHeight)) {
            return super.method_25402(mouseX, mouseY, button);
        }
        int rowY = ((this.panelY + headerH) + 1) - this.scroll;
        int rowEnd = this.panelY + this.panelHeight;
        for (class_1291 effect : buildFilteredList()) {
            if (mouseY >= rowY && mouseY < rowY + rowH && mouseY >= this.panelY + headerH + 1 && mouseY <= rowEnd - 1) {
                int amp = this.map.getInt(effect);
                String val = String.valueOf(amp);
                int plusX = ((this.panelX + this.panelWidth) - fm.getTextWidth("[+]")) - 6;
                int valX = (plusX - 4) - fm.getTextWidth(val);
                int minusX = (valX - 4) - fm.getTextWidth("[-]");
                if (mouseX >= minusX && mouseX < minusX + fm.getTextWidth("[-]") + 2) {
                    if (amp > 0) {
                        this.map.put(effect, amp - 1);
                        this.setting.onChanged();
                        return true;
                    }
                    return true;
                }
                if (mouseX >= plusX - 2 && mouseX <= (this.panelX + this.panelWidth) - 6) {
                    if (amp < MAX_AMP) {
                        this.map.put(effect, amp + 1);
                        this.setting.onChanged();
                        return true;
                    }
                    return true;
                }
                if (mouseX >= valX - 2 && mouseX < plusX - 2) {
                    if (amp == 0) {
                        this.map.put(effect, 1);
                    } else {
                        this.map.put(effect, 0);
                    }
                    this.setting.onChanged();
                    return true;
                }
                return true;
            }
            rowY += rowH;
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (hit((int) mouseX, (int) mouseY, this.panelX, this.panelY, this.panelWidth, this.panelHeight)) {
            FontManager fm = FontManager.get();
            int rowH = Math.max(fm.getRowHeight(), 18);
            int headerH = fm.getHeaderHeight();
            int count = buildFilteredList().size();
            int max = Math.max(0, (count * rowH) - (this.panelHeight - headerH));
            this.scroll -= (int) (verticalAmount * 14.0d);
            this.scroll = Math.max(0, Math.min(this.scroll, max));
            return true;
        }
        return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (this.filterFocused) {
            if (keyCode == 259) {
                if (!this.filterText.isEmpty()) {
                    this.filterText = this.filterText.substring(0, this.filterText.length() - 1);
                }
                this.scroll = 0;
                return true;
            }
            if (keyCode == 256 || keyCode == 257) {
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

    public boolean method_25421() {
        return false;
    }

    private List<class_1291> buildFilteredList() {
        List<class_1291> list = new ArrayList<>();
        for (class_1291 e : class_7923.field_41174) {
            String name = Names.get(e);
            if (!this.filterText.isEmpty()) {
                int words = Utils.searchInWords(name, this.filterText);
                int diff = Utils.searchLevenshteinDefault(name, this.filterText, false);
                if (words != 0 || diff <= name.length() / 2) {
                }
            }
            list.add(e);
        }
        list.sort((a, b) -> {
            int ampA = this.map.getInt(a);
            int ampB = this.map.getInt(b);
            boolean aActive = ampA > 0;
            boolean bActive = ampB > 0;
            if (aActive != bActive) {
                return aActive ? -1 : 1;
            }
            return Names.get(a).compareToIgnoreCase(Names.get(b));
        });
        return list;
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= ((double) x) && mx < ((double) (x + w)) && my >= ((double) y) && my < ((double) (y + h));
    }
}
