package meteordevelopment.meteorclient.gui.newgui.screens;

import java.util.ArrayList;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.ColorListSetting;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewColorListScreen.class */
public class NewColorListScreen extends class_437 {
    private final ColorListSetting setting;
    private final ArrayList<SettingColor> entries;
    private int editingIndex;
    private String editBuffer;
    private int scroll;
    private int listX;
    private int listY;
    private int listWidth;
    private int listHeight;
    private static final int PADDING = 6;
    private static final int SWATCH_SIZE = 14;
    private static final int ROW_HEIGHT_MIN = 18;

    public NewColorListScreen(ColorListSetting setting) {
        super(class_2561.method_43470("Edit " + setting.name));
        this.editingIndex = -1;
        this.editBuffer = "";
        this.scroll = 0;
        this.setting = setting;
        this.entries = new ArrayList<>();
        for (SettingColor c : setting.get()) {
            this.entries.add(new SettingColor(c));
        }
    }

    protected void method_25426() {
        super.method_25426();
        int margin = Math.max(40, this.field_22789 / 6);
        this.listX = margin;
        this.listY = 30;
        this.listWidth = this.field_22789 - (margin * 2);
        this.listHeight = this.field_22790 - 60;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        FontManager fm = FontManager.get();
        super.method_25394(context, mouseX, mouseY, delta);
        int lineColor = fm.primaryAlpha(Opcode.GETFIELD);
        int headerH = fm.getHeaderHeight();
        int rowH = Math.max(fm.getRowHeight(), 18);
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
                SettingColor c = this.entries.get(i);
                boolean editing = this.editingIndex == i;
                boolean hovered = hit(mouseX, mouseY, this.listX + 1, rowY, this.listWidth - 2, rowH);
                if (hovered || editing) {
                    context.method_25294(this.listX + 1, rowY, (this.listX + this.listWidth) - 1, rowY + rowH, fm.secondaryAlpha(Opcode.ISHL));
                }
                int swatchX = this.listX + 6;
                int swatchY = rowY + ((rowH - 14) / 2);
                int argb = ((c.a & 255) << 24) | ((c.r & 255) << 16) | ((c.g & 255) << 8) | (c.b & 255);
                context.method_25294(swatchX, swatchY, swatchX + 14, swatchY + 14, argb);
                RenderUtils.drawThinOutline(context, swatchX, swatchY, 14, 14, lineColor);
                int hexX = swatchX + 14 + 6;
                String hex = editing ? this.editBuffer : toHex(c);
                int hexTextColor = editing ? fm.getTextColor() : fm.getTextSecondary();
                fm.drawText(context, hex, hexX, rowY + ((rowH - fm.getTextHeight()) / 2), hexTextColor);
                if (editing && ((System.currentTimeMillis() / 500) & 1) == 0) {
                    int caretX = hexX + fm.getTextWidth(hex);
                    context.method_25294(caretX, rowY + 3, caretX + 1, (rowY + rowH) - 3, fm.getTextColor());
                }
                String rainbowLabel = c.rainbow ? "[R]" : "[r]";
                int rainbowColor = c.rainbow ? fm.getTextColor() : GuiColors.TEXT_DISABLED;
                int rainbowX = ((((this.listX + this.listWidth) - fm.getTextWidth("[x]")) - 6) - 6) - fm.getTextWidth(rainbowLabel);
                fm.drawText(context, rainbowLabel, rainbowX, rowY + ((rowH - fm.getTextHeight()) / 2), rainbowColor);
                fm.drawText(context, "[x]", ((this.listX + this.listWidth) - fm.getTextWidth("[x]")) - 6, rowY + ((rowH - fm.getTextHeight()) / 2), GuiColors.TEXT_DISABLED);
            }
            rowY += rowH;
            i++;
        }
        context.method_44380();
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = Math.max(fm.getRowHeight(), 18);
        int headerH = fm.getHeaderHeight();
        int addX = ((this.listX + this.listWidth) - fm.getTextWidth("[+ Add]")) - 6;
        if (mouseY >= this.listY && mouseY < this.listY + headerH && mouseX >= addX && mouseX <= (this.listX + this.listWidth) - 2) {
            commitEdit();
            this.entries.add(new SettingColor(255, 255, 255, 255));
            this.setting.get().clear();
            this.setting.get().addAll(this.entries);
            this.setting.onChanged();
            return true;
        }
        int rowY = ((this.listY + headerH) + 1) - this.scroll;
        for (int i = 0; i < this.entries.size(); i++) {
            if (mouseY >= rowY && mouseY < rowY + rowH) {
                int delX = ((this.listX + this.listWidth) - fm.getTextWidth("[x]")) - 6;
                if (mouseX >= delX) {
                    commitEdit();
                    this.entries.remove(i);
                    this.setting.get().clear();
                    this.setting.get().addAll(this.entries);
                    this.setting.onChanged();
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
                String rainbowLabel = this.entries.get(i).rainbow ? "[R]" : "[r]";
                int rainbowX = ((((this.listX + this.listWidth) - fm.getTextWidth("[x]")) - 6) - 6) - fm.getTextWidth(rainbowLabel);
                int rainbowEndX = (((this.listX + this.listWidth) - fm.getTextWidth("[x]")) - 6) - 6;
                if (mouseX >= rainbowX && mouseX < rainbowEndX) {
                    commitEdit();
                    this.entries.get(i).rainbow = !this.entries.get(i).rainbow;
                    this.setting.get().clear();
                    this.setting.get().addAll(this.entries);
                    this.setting.onChanged();
                    return true;
                }
                commitEdit();
                this.editingIndex = i;
                this.editBuffer = toHex(this.entries.get(i));
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
            FontManager fm = FontManager.get();
            int rowH = Math.max(fm.getRowHeight(), 18);
            int headerH = fm.getHeaderHeight();
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
                this.editBuffer = "";
                return true;
            }
            if (keyCode != 259 || this.editBuffer.isEmpty()) {
                return true;
            }
            this.editBuffer = this.editBuffer.substring(0, this.editBuffer.length() - 1);
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
            boolean ok = (chr >= '0' && chr <= '9') || (chr >= 'a' && chr <= 'f') || ((chr >= 'A' && chr <= 'F') || chr == '#');
            if (!ok || this.editBuffer.length() >= 9) {
                return true;
            }
            this.editBuffer += chr;
            return true;
        }
        return super.method_25400(chr, modifiers);
    }

    public void method_25419() {
        commitEdit();
        super.method_25419();
    }

    public boolean method_25421() {
        return false;
    }

    private void commitEdit() {
        if (this.editingIndex < 0) {
            return;
        }
        SettingColor parsed = parseHex(this.editBuffer);
        if (parsed != null) {
            boolean wasRainbow = this.entries.get(this.editingIndex).rainbow;
            parsed.rainbow = wasRainbow;
            this.entries.set(this.editingIndex, parsed);
            this.setting.get().clear();
            this.setting.get().addAll(this.entries);
            this.setting.onChanged();
        }
        this.editBuffer = "";
    }

    private static String toHex(SettingColor c) {
        return String.format("#%02X%02X%02X%02X", Integer.valueOf(c.r & 255), Integer.valueOf(c.g & 255), Integer.valueOf(c.b & 255), Integer.valueOf(c.a & 255));
    }

    private static SettingColor parseHex(String s) {
        int r;
        int g;
        int b;
        String t = s.startsWith("#") ? s.substring(1) : s;
        try {
            int a = 255;
            if (t.length() == 3) {
                r = Integer.parseInt(t.charAt(0) + t.charAt(0), 16);
                g = Integer.parseInt(t.charAt(1) + t.charAt(1), 16);
                b = Integer.parseInt(t.charAt(2) + t.charAt(2), 16);
            } else if (t.length() == 4) {
                r = Integer.parseInt(t.charAt(0) + t.charAt(0), 16);
                g = Integer.parseInt(t.charAt(1) + t.charAt(1), 16);
                b = Integer.parseInt(t.charAt(2) + t.charAt(2), 16);
                a = Integer.parseInt(t.charAt(3) + t.charAt(3), 16);
            } else if (t.length() == 6) {
                r = Integer.parseInt(t.substring(0, 2), 16);
                g = Integer.parseInt(t.substring(2, 4), 16);
                b = Integer.parseInt(t.substring(4, 6), 16);
            } else if (t.length() == 8) {
                r = Integer.parseInt(t.substring(0, 2), 16);
                g = Integer.parseInt(t.substring(2, 4), 16);
                b = Integer.parseInt(t.substring(4, 6), 16);
                a = Integer.parseInt(t.substring(6, 8), 16);
            } else {
                return null;
            }
            return new SettingColor(r, g, b, a);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= ((double) x) && mx < ((double) (x + w)) && my >= ((double) y) && my < ((double) (y + h));
    }
}
