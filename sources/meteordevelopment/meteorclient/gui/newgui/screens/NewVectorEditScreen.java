package meteordevelopment.meteorclient.gui.newgui.screens;

import javassist.bytecode.Opcode;
import javassist.compiler.TokenId;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.BlockPosSetting;
import meteordevelopment.meteorclient.settings.Vector3dSetting;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_746;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewVectorEditScreen.class */
public class NewVectorEditScreen extends class_437 {
    private final BlockPosSetting blockPosSetting;
    private final Vector3dSetting vectorSetting;
    private String[] fields;
    private int focusIndex;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private static final int PADDING = 8;
    private static final int FIELD_HEIGHT = 22;

    public NewVectorEditScreen(BlockPosSetting setting) {
        super(class_2561.method_43470("Edit " + setting.name));
        this.fields = new String[3];
        this.focusIndex = 0;
        this.blockPosSetting = setting;
        this.vectorSetting = null;
        class_2338 p = setting.get();
        this.fields[0] = String.valueOf(p.method_10263());
        this.fields[1] = String.valueOf(p.method_10264());
        this.fields[2] = String.valueOf(p.method_10260());
    }

    public NewVectorEditScreen(Vector3dSetting setting) {
        super(class_2561.method_43470("Edit " + setting.name));
        this.fields = new String[3];
        this.focusIndex = 0;
        this.blockPosSetting = null;
        this.vectorSetting = setting;
        Vector3d v = setting.get();
        int dp = Math.max(1, setting.decimalPlaces);
        String fmt = "%." + dp + "f";
        this.fields[0] = String.format(fmt, Double.valueOf(v.x));
        this.fields[1] = String.format(fmt, Double.valueOf(v.y));
        this.fields[2] = String.format(fmt, Double.valueOf(v.z));
    }

    protected void method_25426() {
        super.method_25426();
        this.panelWidth = Math.min(TokenId.EXOR_E, this.field_22789 - 40);
        this.panelHeight = Opcode.F2L;
        this.panelX = (this.field_22789 - this.panelWidth) / 2;
        this.panelY = (this.field_22790 - this.panelHeight) / 2;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        FontManager fm = FontManager.get();
        super.method_25394(context, mouseX, mouseY, delta);
        int lineColor = fm.primaryAlpha(Opcode.GETFIELD);
        int headerH = fm.getHeaderHeight();
        context.method_25294(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + this.panelHeight, fm.primaryAlpha(Opcode.IF_ICMPNE));
        RenderUtils.drawThinOutline(context, this.panelX, this.panelY, this.panelWidth, this.panelHeight, lineColor);
        context.method_25294(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + headerH, fm.secondaryAlpha(200));
        RenderUtils.drawThinHLine(context, this.panelX, this.panelY + headerH, this.panelWidth, lineColor);
        fm.drawText(context, this.field_22785.getString(), this.panelX + 8, this.panelY + ((headerH - fm.getTextHeight()) / 2), fm.getTextColor());
        String[] labels = {"X:", "Y:", "Z:"};
        int y = this.panelY + headerH + 8;
        int fx = this.panelX + 8;
        int fw = this.panelWidth - 16;
        int labelW = fm.getTextWidth("Z:") + 6;
        int i = 0;
        while (i < 3) {
            fm.drawText(context, labels[i], fx, y + ((22 - fm.getTextHeight()) / 2), fm.getTextColor());
            int inputX = fx + labelW;
            int inputW = fw - labelW;
            boolean focused = this.focusIndex == i;
            context.method_25294(inputX, y, inputX + inputW, y + 22, focused ? fm.secondaryAlpha(220) : fm.secondaryAlpha(Opcode.FCMPG));
            RenderUtils.drawThinOutline(context, inputX, y, inputW, 22, lineColor);
            int textY = y + ((22 - fm.getTextHeight()) / 2);
            fm.drawText(context, this.fields[i], inputX + 4, textY, fm.getTextColor());
            if (focused && ((System.currentTimeMillis() / 500) & 1) == 0) {
                int caretX = inputX + 4 + fm.getTextWidth(this.fields[i]);
                context.method_25294(caretX, y + 3, caretX + 1, (y + 22) - 3, fm.getTextColor());
            }
            y += 30;
            i++;
        }
        int btnY = y + 4;
        int fromMeW = fm.getTextWidth("[From Me]") + 12;
        int cancelW = fm.getTextWidth("[Cancel]") + 12;
        int applyW = fm.getTextWidth("[Apply]") + 12;
        int applyX = (fx + fw) - applyW;
        int cancelX = (applyX - cancelW) - 8;
        drawButton(context, fm, "[From Me]", fx, btnY, fromMeW, 22, lineColor);
        drawButton(context, fm, "[Cancel]", cancelX, btnY, cancelW, 22, lineColor);
        drawButton(context, fm, "[Apply]", applyX, btnY, applyW, 22, lineColor);
    }

    private void drawButton(class_332 context, FontManager fm, String label, int x, int y, int w, int h, int lineColor) {
        context.method_25294(x, y, x + w, y + h, fm.secondaryAlpha(200));
        RenderUtils.drawThinOutline(context, x, y, w, h, lineColor);
        int tx = x + ((w - fm.getTextWidth(label)) / 2);
        int ty = y + ((h - fm.getTextHeight()) / 2);
        fm.drawText(context, label, tx, ty, fm.getTextColor());
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int headerH = fm.getHeaderHeight();
        int y0 = this.panelY + headerH + 8;
        int fx = this.panelX + 8;
        int fw = this.panelWidth - 16;
        int labelW = fm.getTextWidth("Z:") + 6;
        int inputX = fx + labelW;
        int inputW = fw - labelW;
        for (int i = 0; i < 3; i++) {
            int y = y0 + (i * 30);
            if (mouseX >= inputX && mouseX < inputX + inputW && mouseY >= y && mouseY < y + 22) {
                this.focusIndex = i;
                return true;
            }
        }
        int btnY = y0 + 90 + 4;
        int fromMeW = fm.getTextWidth("[From Me]") + 12;
        int cancelW = fm.getTextWidth("[Cancel]") + 12;
        int applyW = fm.getTextWidth("[Apply]") + 12;
        int applyX = (fx + fw) - applyW;
        int cancelX = (applyX - cancelW) - 8;
        if (inRect(mouseX, mouseY, fx, btnY, fromMeW, 22)) {
            fillFromPlayer();
            return true;
        }
        if (inRect(mouseX, mouseY, cancelX, btnY, cancelW, 22)) {
            method_25419();
            return true;
        }
        if (inRect(mouseX, mouseY, applyX, btnY, applyW, 22)) {
            if (commit()) {
                method_25419();
                return true;
            }
            return true;
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            method_25419();
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            if (commit()) {
                method_25419();
                return true;
            }
            return true;
        }
        if (keyCode == 258) {
            this.focusIndex = (this.focusIndex + (hasShift(modifiers) ? 2 : 1)) % 3;
            return true;
        }
        if (keyCode == 259) {
            if (!this.fields[this.focusIndex].isEmpty()) {
                this.fields[this.focusIndex] = this.fields[this.focusIndex].substring(0, this.fields[this.focusIndex].length() - 1);
                return true;
            }
            return true;
        }
        return super.method_25404(keyCode, scanCode, modifiers);
    }

    public boolean method_25400(char chr, int modifiers) {
        if (chr >= ' ' && chr != 127) {
            boolean acceptsFloat = this.vectorSetting != null;
            boolean ok = (chr >= '0' && chr <= '9') || chr == '-' || (acceptsFloat && (chr == '.' || chr == 'e' || chr == 'E' || chr == '+'));
            if (ok && this.fields[this.focusIndex].length() < 16) {
                String[] strArr = this.fields;
                int i = this.focusIndex;
                strArr[i] = strArr[i] + chr;
                return true;
            }
            return true;
        }
        return super.method_25400(chr, modifiers);
    }

    public boolean method_25421() {
        return false;
    }

    private boolean commit() {
        try {
            if (this.blockPosSetting != null) {
                int x = Integer.parseInt(this.fields[0].trim());
                int y = Integer.parseInt(this.fields[1].trim());
                int z = Integer.parseInt(this.fields[2].trim());
                this.blockPosSetting.set(new class_2338(x, y, z));
                return true;
            }
            if (this.vectorSetting != null) {
                double x2 = Double.parseDouble(this.fields[0].trim());
                double y2 = Double.parseDouble(this.fields[1].trim());
                double z2 = Double.parseDouble(this.fields[2].trim());
                this.vectorSetting.set(new Vector3d(Math.max(this.vectorSetting.min, Math.min(this.vectorSetting.max, x2)), Math.max(this.vectorSetting.min, Math.min(this.vectorSetting.max, y2)), Math.max(this.vectorSetting.min, Math.min(this.vectorSetting.max, z2))));
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void fillFromPlayer() {
        class_746 class_746Var = class_310.method_1551().field_1724;
        if (class_746Var == null) {
            return;
        }
        if (this.blockPosSetting != null) {
            class_2338 pos = class_746Var.method_24515();
            this.fields[0] = String.valueOf(pos.method_10263());
            this.fields[1] = String.valueOf(pos.method_10264());
            this.fields[2] = String.valueOf(pos.method_10260());
            return;
        }
        if (this.vectorSetting != null) {
            int dp = Math.max(1, this.vectorSetting.decimalPlaces);
            String fmt = "%." + dp + "f";
            this.fields[0] = String.format(fmt, Double.valueOf(class_746Var.method_23317()));
            this.fields[1] = String.format(fmt, Double.valueOf(class_746Var.method_23318()));
            this.fields[2] = String.format(fmt, Double.valueOf(class_746Var.method_23321()));
        }
    }

    private static boolean inRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= ((double) x) && mx < ((double) (x + w)) && my >= ((double) y) && my < ((double) (y + h));
    }

    private static boolean hasShift(int modifiers) {
        return (modifiers & 1) != 0;
    }
}
