package meteordevelopment.meteorclient.gui.newgui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_10366;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.joml.Matrix4f;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewHudElementPresetsScreen.class */
public class NewHudElementPresetsScreen extends class_437 {
    private final HudElementInfo<?> info;
    private final int spawnX;
    private final int spawnY;
    private final class_437 backTo;
    private final class_437 afterAddTo;
    private String searchQuery;
    private int scroll;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private static final int PANEL_WIDTH = 170;
    private static final int PADDING = 2;
    private static final int SEARCH_ROW_GAP = 2;

    public NewHudElementPresetsScreen(HudElementInfo<?> info, int spawnX, int spawnY, class_437 backTo, class_437 afterAddTo) {
        super(class_2561.method_43470("Presets — " + info.title));
        this.searchQuery = "";
        this.scroll = 0;
        this.info = info;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.backTo = backTo;
        this.afterAddTo = afterAddTo;
    }

    public boolean method_25421() {
        return false;
    }

    protected void method_25426() {
        super.method_25426();
        positionPanel();
    }

    private void positionPanel() {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        double sf = class_310.method_1551().method_22683().method_4495();
        this.panelWidth = 170;
        int desiredH = rowH + rowH + 2 + (filteredPresets().size() * rowH) + 4;
        this.panelHeight = Math.min(desiredH, this.field_22790 - 10);
        int targetX = (int) (((double) this.spawnX) / sf);
        int targetY = (int) (((double) this.spawnY) / sf);
        this.panelX = Math.max(4, Math.min((this.field_22789 - this.panelWidth) - 4, targetX));
        this.panelY = Math.max(4, Math.min((this.field_22790 - this.panelHeight) - 4, targetY));
    }

    private List<HudElementInfo<?>.Preset> filteredPresets() {
        List<HudElementInfo<?>.Preset> out = new ArrayList<>();
        for (HudElementInfo<?>.Preset p : this.info.presets) {
            if (Utils.searchTextDefault(p.title, this.searchQuery, false)) {
                out.add(p);
            }
        }
        return out;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        Matrix4f savedProj = new Matrix4f(RenderSystem.getProjectionMatrix());
        Utils.unscaledProjection();
        HudEditorScreen.renderElements(context);
        RenderSystem.setProjectionMatrix(savedProj, class_10366.field_54954);
        positionPanel();
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int outlineColor = fm.primaryAlpha(170);
        int headerColor = fm.primaryAlpha(220);
        RenderUtils.drawThickOutline(context, this.panelX, this.panelY, this.panelWidth, this.panelHeight, 3, outlineColor);
        RenderUtils.fillNative(context, this.panelX, this.panelY, this.panelWidth, rowH, headerColor);
        context.method_44379(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + rowH);
        String title = "Presets: " + this.info.title;
        fm.drawText(context, title, this.panelX + 4, this.panelY + ((rowH - fm.getTextHeight()) / 2), fm.getTextColor());
        int closeX = ((this.panelX + this.panelWidth) - fm.getTextWidth("✕")) - 4;
        fm.drawText(context, "✕", closeX, this.panelY + ((rowH - fm.getTextHeight()) / 2), fm.getTextColor());
        context.method_44380();
        int searchY = this.panelY + rowH + 2;
        int searchX = this.panelX + 2;
        int searchW = this.panelWidth - 4;
        RenderUtils.fillNative(context, searchX, searchY, searchW, rowH, fm.secondaryAlpha(200));
        String label = this.searchQuery.isEmpty() ? "Search…" : this.searchQuery;
        int tc = this.searchQuery.isEmpty() ? GuiColors.TEXT_DISABLED : fm.getTextColor();
        fm.drawText(context, label, searchX + 3, searchY + ((rowH - fm.getTextHeight()) / 2), tc);
        if (!this.searchQuery.isEmpty() && ((System.currentTimeMillis() / 500) & 1) == 0) {
            int caretX = searchX + 3 + fm.getTextWidth(this.searchQuery);
            context.method_25294(caretX, searchY + 2, caretX + 1, (searchY + rowH) - 2, fm.getTextColor());
        }
        int listY = searchY + rowH + 1;
        int listBottom = (this.panelY + this.panelHeight) - 2;
        int listH = Math.max(rowH, listBottom - listY);
        int listX = this.panelX + 2;
        int listW = this.panelWidth - 4;
        context.method_44379(listX, listY, listX + listW, listY + listH);
        int drawY = listY - this.scroll;
        int totalH = 0;
        List<HudElementInfo<?>.Preset> list = filteredPresets();
        for (HudElementInfo<?>.Preset preset : list) {
            int btnX = (listX + listW) - rowH;
            boolean hovered = mouseX >= listX && mouseX < listX + listW && mouseY >= drawY && mouseY < drawY + rowH;
            if (hovered) {
                RenderUtils.fillNative(context, listX, drawY, listW, rowH, fm.secondaryAlpha(Opcode.F2L));
            }
            context.method_44379(listX, drawY, btnX - 2, drawY + rowH);
            fm.drawText(context, preset.title, listX + 3, drawY + ((rowH - fm.getTextHeight()) / 2), fm.getTextColor());
            context.method_44380();
            boolean btnHovered = mouseX >= btnX && mouseX < btnX + rowH && mouseY >= drawY && mouseY < drawY + rowH;
            int btnFill = btnHovered ? -12859808 : -13656499;
            RenderUtils.fillNative(context, btnX, drawY, rowH, rowH, btnFill);
            fm.drawText(context, "+", btnX + ((rowH - fm.getTextWidth("+")) / 2), drawY + ((rowH - fm.getTextHeight()) / 2), fm.getTextColor());
            drawY += rowH;
            totalH += rowH;
        }
        context.method_44380();
        int maxScroll = Math.max(0, totalH - listH);
        if (maxScroll > 0) {
            int trackH = Math.max(1, listH - 2);
            int thumbH = Math.max(8, (int) ((((long) listH) * ((long) trackH)) / ((long) Math.max(1, totalH))));
            int thumbY = listY + 1 + ((int) ((((long) (trackH - thumbH)) * ((long) this.scroll)) / ((long) maxScroll)));
            int sbX = (this.panelX + this.panelWidth) - 2;
            context.method_25294(sbX, thumbY, sbX + 1, thumbY + thumbH, fm.primaryAlpha(220));
        }
        if (list.isEmpty()) {
            String msg = this.searchQuery.isEmpty() ? "(no presets)" : "No matches.";
            fm.drawText(context, msg, listX + ((listW - fm.getTextWidth(msg)) / 2), listY + ((listH - fm.getTextHeight()) / 2), GuiColors.TEXT_DISABLED);
        }
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        if (mouseX < this.panelX || mouseX >= this.panelX + this.panelWidth || mouseY < this.panelY || mouseY >= this.panelY + this.panelHeight) {
            class_310.method_1551().method_1507(this.backTo);
            return true;
        }
        int closeX = ((this.panelX + this.panelWidth) - fm.getTextWidth("✕")) - 4;
        if (mouseX >= closeX - 2 && mouseY >= this.panelY && mouseY < this.panelY + rowH) {
            class_310.method_1551().method_1507(this.backTo);
            return true;
        }
        int searchY = this.panelY + rowH + 2;
        int listY = searchY + rowH + 1;
        int listBottom = (this.panelY + this.panelHeight) - 2;
        int listH = Math.max(rowH, listBottom - listY);
        int listX = this.panelX + 2;
        int listW = this.panelWidth - 4;
        if (mouseX < listX || mouseX >= listX + listW || mouseY < listY || mouseY >= listY + listH) {
            return super.method_25402(mouseX, mouseY, button);
        }
        int drawY = listY - this.scroll;
        for (HudElementInfo<?>.Preset preset : filteredPresets()) {
            if (mouseY >= drawY && mouseY < drawY + rowH) {
                Hud.get().add(preset, this.spawnX, this.spawnY);
                Hud.get().save();
                class_310.method_1551().method_1507(this.afterAddTo);
                return true;
            }
            drawY += rowH;
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX < this.panelX || mouseX >= this.panelX + this.panelWidth || mouseY < this.panelY || mouseY >= this.panelY + this.panelHeight) {
            return false;
        }
        FontManager fm = FontManager.get();
        this.scroll = Math.max(0, this.scroll - ((int) ((verticalAmount * ((double) fm.getRowHeight())) * 2.0d)));
        return true;
    }

    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            class_310.method_1551().method_1507(this.backTo);
            return true;
        }
        if (keyCode == 259 && !this.searchQuery.isEmpty()) {
            this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
            this.scroll = 0;
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            List<HudElementInfo<?>.Preset> list = filteredPresets();
            if (!list.isEmpty()) {
                Hud.get().add(list.get(0), this.spawnX, this.spawnY);
                Hud.get().save();
                class_310.method_1551().method_1507(this.afterAddTo);
                return true;
            }
        }
        return super.method_25404(keyCode, scanCode, modifiers);
    }

    public boolean method_25400(char chr, int modifiers) {
        if (chr >= ' ' && chr != 127) {
            this.searchQuery += chr;
            this.scroll = 0;
            return true;
        }
        return super.method_25400(chr, modifiers);
    }

    public void method_25419() {
        class_310.method_1551().method_1507(this.backTo);
    }
}
