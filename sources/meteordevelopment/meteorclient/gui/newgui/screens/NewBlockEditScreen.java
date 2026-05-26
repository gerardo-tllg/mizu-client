package meteordevelopment.meteorclient.gui.newgui.screens;

import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.components.SettingGroupRenderer;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.Settings;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewBlockEditScreen.class */
public class NewBlockEditScreen extends class_437 {
    private final Settings settings;
    private final class_437 returnTo;
    private int contentX;
    private int contentY;
    private int contentWidth;
    private int contentHeight;
    private int scroll;
    private static final int PADDING = 8;
    private static final int TITLE_HEIGHT = 20;
    private static final int BACK_BUTTON_WIDTH = 60;

    public NewBlockEditScreen(String title, Settings settings, class_437 returnTo) {
        super(class_2561.method_43470(title));
        this.scroll = 0;
        this.settings = settings;
        this.returnTo = returnTo;
    }

    public boolean method_25421() {
        return false;
    }

    protected void method_25426() {
        super.method_25426();
        int margin = Math.max(20, this.field_22789 / 6);
        this.contentX = margin;
        this.contentWidth = this.field_22789 - (margin * 2);
        this.contentY = 28;
        this.contentHeight = (this.field_22790 - this.contentY) - 8;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        FontManager fm = FontManager.get();
        super.method_25394(context, mouseX, mouseY, delta);
        int lineColor = fm.primaryAlpha(Opcode.GETFIELD);
        fm.secondaryAlpha(200);
        int rowH = fm.getRowHeight();
        int backX = this.contentX;
        boolean backHovered = mouseX >= backX && mouseX < backX + 60 && mouseY >= 4 && mouseY < (4 + rowH) + 2;
        int backFill = backHovered ? fm.secondaryAlpha(220) : fm.secondaryAlpha(Opcode.IF_ICMPNE);
        context.method_25294(backX, 4, backX + 60, 4 + rowH + 2, backFill);
        RenderUtils.drawThinOutline(context, backX, 4, 60, rowH + 2, lineColor);
        fm.drawText(context, "← Back", backX + ((60 - fm.getTextWidth("← Back")) / 2), 4 + (((rowH + 2) - fm.getTextHeight()) / 2), fm.getTextColor());
        fm.drawText(context, this.field_22785.getString(), backX + 60 + 8, 4 + (((rowH + 2) - fm.getTextHeight()) / 2), fm.getTextColor());
        context.method_25294(this.contentX, this.contentY, this.contentX + this.contentWidth, this.contentY + this.contentHeight, fm.primaryAlpha(Opcode.ISHL));
        RenderUtils.drawThinOutline(context, this.contentX, this.contentY, this.contentWidth, this.contentHeight, lineColor);
        int innerX = this.contentX + 8;
        int innerY = this.contentY + 8;
        int innerWidth = this.contentWidth - 16;
        int innerHeight = this.contentHeight - 16;
        int totalH = SettingGroupRenderer.getGroupsHeight(this.settings);
        int drawY = innerY - this.scroll;
        context.method_44379(innerX, innerY, innerX + innerWidth, innerY + innerHeight);
        SettingGroupRenderer.renderGroups(context, this.settings, innerX, drawY, innerWidth, mouseX, mouseY);
        context.method_44380();
        if (totalH > innerHeight) {
            int sx = (this.contentX + this.contentWidth) - 3;
            int scrollH = Math.max(10, (innerHeight * innerHeight) / totalH);
            int scrollY = innerY + ((this.scroll * (innerHeight - scrollH)) / Math.max(1, totalH - innerHeight));
            context.method_25294(sx, scrollY, sx + 2, scrollY + scrollH, fm.secondaryAlpha(220));
        }
        if (totalH == 0) {
            fm.drawText(context, "(no settings)", this.contentX + ((this.contentWidth - fm.getTextWidth("(no settings)")) / 2), this.contentY + ((this.contentHeight - fm.getTextHeight()) / 2), GuiColors.TEXT_DISABLED);
        }
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int backX = this.contentX;
        if (mouseX >= backX && mouseX < backX + 60 && mouseY >= 4 && mouseY < 4 + rowH + 2) {
            class_310.method_1551().method_1507(this.returnTo);
            return true;
        }
        SettingGroupRenderer.commitStringEdit();
        int innerX = this.contentX + 8;
        int innerY = this.contentY + 8;
        int innerWidth = this.contentWidth - 16;
        int innerHeight = this.contentHeight - 16;
        if (mouseX >= innerX && mouseX < innerX + innerWidth && mouseY >= innerY && mouseY < innerY + innerHeight) {
            int drawY = innerY - this.scroll;
            boolean handled = SettingGroupRenderer.mouseClickedGroups(this.settings, innerX, drawY, innerWidth, (int) mouseX, (int) mouseY, button, () -> {
                return null;
            });
            if (handled) {
                return true;
            }
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    public boolean method_25406(double mouseX, double mouseY, int button) {
        SettingGroupRenderer.mouseReleasedGroups();
        return super.method_25406(mouseX, mouseY, button);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        FontManager fm = FontManager.get();
        int innerHeight = this.contentHeight - 16;
        int totalH = SettingGroupRenderer.getGroupsHeight(this.settings);
        int maxScroll = Math.max(0, totalH - innerHeight);
        this.scroll = Math.max(0, Math.min(maxScroll, this.scroll - ((int) ((verticalAmount * ((double) fm.getRowHeight())) * 2.0d))));
        return true;
    }

    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (SettingGroupRenderer.onKeyPressed(keyCode)) {
            return true;
        }
        if (keyCode == 256) {
            class_310.method_1551().method_1507(this.returnTo);
            return true;
        }
        return super.method_25404(keyCode, scanCode, modifiers);
    }

    public boolean method_25400(char chr, int modifiers) {
        if (SettingGroupRenderer.onCharTyped(chr)) {
            return true;
        }
        return super.method_25400(chr, modifiers);
    }

    public void method_25419() {
        SettingGroupRenderer.commitStringEdit();
        class_310.method_1551().method_1507(this.returnTo);
    }
}
