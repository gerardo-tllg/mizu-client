package meteordevelopment.meteorclient.gui.newgui.screens;

import java.util.ArrayList;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.settings.StringListSetting;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewStringListScreen.class */
public class NewStringListScreen extends class_437 {
    private final StringListSetting setting;
    private final ArrayList<String> entries;
    private int editingIndex;
    private int scroll;
    private int margin;
    private int listX;
    private int listY;
    private int listWidth;
    private int listHeight;
    private static final int PADDING = 6;

    public NewStringListScreen(StringListSetting setting) {
        super(class_2561.method_43470("Edit " + setting.name));
        this.editingIndex = -1;
        this.scroll = 0;
        this.setting = setting;
        this.entries = new ArrayList<>(setting.get());
    }

    protected void method_25426() {
        super.method_25426();
        this.margin = Math.max(40, this.field_22789 / 6);
        this.listX = this.margin;
        this.listY = 30;
        this.listWidth = this.field_22789 - (this.margin * 2);
        this.listHeight = this.field_22790 - 60;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        FontManager fm = FontManager.get();
        super.method_25394(context, mouseX, mouseY, delta);
        int lineColor = fm.primaryAlpha(Opcode.GETFIELD);
        int rowH = fm.getRowHeight();
        int headerH = fm.getHeaderHeight();
        fm.drawText(context, this.field_22785.getString(), this.listX, 8, fm.getTextColor());
        context.method_25294(this.listX, this.listY, this.listX + this.listWidth, this.listY + this.listHeight, fm.primaryAlpha(Opcode.ISHL));
        RenderUtils.drawThinOutline(context, this.listX, this.listY, this.listWidth, this.listHeight, lineColor);
        context.method_25294(this.listX, this.listY, this.listX + this.listWidth, this.listY + headerH, fm.secondaryAlpha(200));
        RenderUtils.drawThinHLine(context, this.listX, this.listY + headerH, this.listWidth, lineColor);
        String heading = this.setting.name + " (" + this.entries.size() + ")";
        fm.drawText(context, heading, this.listX + 6, this.listY + ((headerH - fm.getTextHeight()) / 2), fm.getTextColor());
        fm.drawText(context, "[+ Add]", ((this.listX + this.listWidth) - fm.getTextWidth("[+ Add]")) - 6, this.listY + ((headerH - fm.getTextHeight()) / 2), GuiColors.TEXT_SETTING_VALUE);
        int rowY = ((this.listY + headerH) + 1) - this.scroll;
        int rowEnd = this.listY + this.listHeight;
        context.method_44379(this.listX + 1, this.listY + headerH + 1, (this.listX + this.listWidth) - 1, rowEnd - 1);
        int i = 0;
        while (i < this.entries.size()) {
            if (rowY + rowH >= this.listY + headerH + 1) {
                if (rowY > rowEnd) {
                    break;
                }
                boolean hovered = hit(mouseX, mouseY, this.listX + 1, rowY, this.listWidth - 2, rowH);
                boolean editing = this.editingIndex == i;
                if (hovered || editing) {
                    context.method_25294(this.listX + 1, rowY, (this.listX + this.listWidth) - 1, rowY + rowH, fm.secondaryAlpha(Opcode.ISHL));
                }
                int tcolor = editing ? fm.getTextColor() : fm.getTextSecondary();
                String text = this.entries.get(i);
                fm.drawText(context, text, this.listX + 6, rowY + ((rowH - fm.getTextHeight()) / 2), tcolor);
                if (editing && ((System.currentTimeMillis() / 500) & 1) == 0) {
                    int caretX = this.listX + 6 + fm.getTextWidth(text);
                    context.method_25294(caretX, rowY + 2, caretX + 1, (rowY + rowH) - 2, fm.getTextColor());
                }
                fm.drawText(context, "[x]", ((this.listX + this.listWidth) - fm.getTextWidth("[x]")) - 6, rowY + ((rowH - fm.getTextHeight()) / 2), GuiColors.TEXT_DISABLED);
            }
            rowY += rowH;
            i++;
        }
        context.method_44380();
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int headerH = fm.getHeaderHeight();
        int addX = ((this.listX + this.listWidth) - fm.getTextWidth("[+ Add]")) - 6;
        if (mouseY >= this.listY && mouseY < this.listY + headerH && mouseX >= addX && mouseX <= (this.listX + this.listWidth) - 2) {
            commitEdit();
            this.entries.add("");
            this.editingIndex = this.entries.size() - 1;
            this.setting.set(new ArrayList(this.entries));
            return true;
        }
        int rowY = ((this.listY + headerH) + 1) - this.scroll;
        for (int i = 0; i < this.entries.size(); i++) {
            if (mouseY >= rowY && mouseY < rowY + rowH) {
                int delX = ((this.listX + this.listWidth) - fm.getTextWidth("[x]")) - 6;
                if (mouseX >= delX) {
                    commitEdit();
                    this.entries.remove(i);
                    this.setting.set(new ArrayList(this.entries));
                    if (this.editingIndex != i) {
                        if (this.editingIndex > i) {
                            this.editingIndex--;
                            return true;
                        }
                        return true;
                    }
                    this.editingIndex = -1;
                    return true;
                }
                commitEdit();
                this.editingIndex = i;
                return true;
            }
            rowY += rowH;
        }
        commitEdit();
        this.editingIndex = -1;
        return super.method_25402(mouseX, mouseY, button);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (hit((int) mouseX, (int) mouseY, this.listX, this.listY, this.listWidth, this.listHeight)) {
            int rowH = FontManager.get().getRowHeight();
            int headerH = FontManager.get().getHeaderHeight();
            int max = Math.max(0, (this.entries.size() * rowH) - (this.listHeight - headerH));
            this.scroll -= (int) (verticalAmount * 14.0d);
            this.scroll = Math.max(0, Math.min(this.scroll, max));
            return true;
        }
        return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (this.editingIndex >= 0) {
            if (keyCode == 257 || keyCode == 335) {
                commitEdit();
                this.editingIndex = -1;
                return true;
            }
            if (keyCode == 256) {
                this.editingIndex = -1;
                return true;
            }
            if (keyCode == 259) {
                String cur = this.entries.get(this.editingIndex);
                if (!cur.isEmpty()) {
                    this.entries.set(this.editingIndex, cur.substring(0, cur.length() - 1));
                    return true;
                }
                return true;
            }
            return true;
        }
        if (keyCode == 256) {
            commitEdit();
            method_25419();
            return true;
        }
        return super.method_25404(keyCode, scanCode, modifiers);
    }

    public boolean method_25400(char chr, int modifiers) {
        if (this.editingIndex >= 0 && chr >= ' ' && chr != 127) {
            String cur = this.entries.get(this.editingIndex);
            String next = cur + chr;
            CharFilter f = this.setting.filter;
            if (f == null || f.filter(next, chr)) {
                this.entries.set(this.editingIndex, next);
                return true;
            }
            return true;
        }
        return super.method_25400(chr, modifiers);
    }

    public void method_25419() {
        commitEdit();
        super.method_25419();
    }

    private void commitEdit() {
        this.setting.set(new ArrayList(this.entries));
    }

    public boolean method_25421() {
        return false;
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= ((double) x) && mx < ((double) (x + w)) && my >= ((double) y) && my < ((double) (y + h));
    }
}
