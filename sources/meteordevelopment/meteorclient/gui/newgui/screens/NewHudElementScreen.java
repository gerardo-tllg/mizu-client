package meteordevelopment.meteorclient.gui.newgui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.components.SettingGroupRenderer;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudBox;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.XAnchor;
import meteordevelopment.meteorclient.systems.hud.YAnchor;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_10366;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.joml.Matrix4f;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewHudElementScreen.class */
public class NewHudElementScreen extends class_437 {
    private final HudElement element;
    private final class_437 returnTo;
    private final Settings anchorSettings;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int listX;
    private int listY;
    private int listW;
    private int listH;
    private int barY;
    private int barHeight;
    private int scroll;
    private static final int PANEL_WIDTH = 200;

    public NewHudElementScreen(HudElement element, class_437 returnTo) {
        super(class_2561.method_43470(element.info.title));
        this.scroll = 0;
        this.element = element;
        this.returnTo = returnTo;
        this.anchorSettings = new Settings();
        SettingGroup sg = this.anchorSettings.createGroup("Anchors");
        sg.add(new BoolSetting.Builder().name("auto-anchors").description("Automatically assigns anchors based on the position.").defaultValue(true).onModuleActivated(s -> {
            s.set(Boolean.valueOf(element.autoAnchors));
        }).onChanged(v -> {
            if (v.booleanValue()) {
                element.box.updateAnchors();
            }
            element.autoAnchors = v.booleanValue();
        }).build());
        EnumSetting.Builder builderOnModuleActivated = new EnumSetting.Builder().name("x-anchor").description("Horizontal anchor.").defaultValue(XAnchor.Left).visible(() -> {
            return !element.autoAnchors;
        }).onModuleActivated(s2 -> {
            s2.set(element.box.xAnchor);
        });
        HudBox hudBox = element.box;
        Objects.requireNonNull(hudBox);
        sg.add(builderOnModuleActivated.onChanged(hudBox::setXAnchor).build());
        EnumSetting.Builder builderOnModuleActivated2 = new EnumSetting.Builder().name("y-anchor").description("Vertical anchor.").defaultValue(YAnchor.Top).visible(() -> {
            return !element.autoAnchors;
        }).onModuleActivated(s3 -> {
            s3.set(element.box.yAnchor);
        });
        HudBox hudBox2 = element.box;
        Objects.requireNonNull(hudBox2);
        sg.add(builderOnModuleActivated2.onChanged(hudBox2::setYAnchor).build());
        this.anchorSettings.onActivated();
        element.settings.onActivated();
    }

    public boolean method_25421() {
        return false;
    }

    protected void method_25426() {
        super.method_25426();
        layoutPanel();
    }

    private void layoutPanel() {
        FontManager fm = FontManager.get();
        int headerH = fm.getHeaderHeight();
        int rowH = fm.getRowHeight();
        this.barHeight = rowH + 2;
        this.panelWidth = Math.min(200, this.field_22789 - 20);
        int totalContent = SettingGroupRenderer.getGroupsHeight(this.anchorSettings) + SettingGroupRenderer.getGroupsHeight(this.element.settings);
        int desiredListH = Math.max(rowH, totalContent);
        int desiredH = headerH + desiredListH + this.barHeight;
        int maxH = this.field_22790 - 20;
        this.panelHeight = Math.min(desiredH, maxH);
        this.panelX = (this.field_22789 - this.panelWidth) / 2;
        this.panelY = (this.field_22790 - this.panelHeight) / 2;
        this.listX = this.panelX;
        this.listY = this.panelY + headerH;
        this.listW = this.panelWidth;
        this.listH = (this.panelHeight - headerH) - this.barHeight;
        this.barY = (this.panelY + this.panelHeight) - this.barHeight;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        int iSecondaryAlpha;
        Matrix4f savedProj = new Matrix4f(RenderSystem.getProjectionMatrix());
        Utils.unscaledProjection();
        HudEditorScreen.renderElements(context);
        RenderSystem.setProjectionMatrix(savedProj, class_10366.field_54954);
        layoutPanel();
        FontManager fm = FontManager.get();
        int headerH = fm.getHeaderHeight();
        int rowH = fm.getRowHeight();
        int outlineColor = fm.primaryAlpha(Opcode.TABLESWITCH);
        int headerColor = fm.primaryAlpha(220);
        boolean backHovered = mouseX >= 10 && mouseX < 10 + 60 && mouseY >= 4 && mouseY < (4 + rowH) + 2;
        int backFill = backHovered ? fm.secondaryAlpha(220) : fm.secondaryAlpha(Opcode.IF_ICMPNE);
        context.method_25294(10, 4, 10 + 60, 4 + rowH + 2, backFill);
        RenderUtils.drawThinOutline(context, 10, 4, 60, rowH + 2, outlineColor);
        fm.drawText(context, "← Back", 10 + ((60 - fm.getTextWidth("← Back")) / 2), 4 + (((rowH + 2) - fm.getTextHeight()) / 2), fm.getTextColor());
        RenderUtils.drawThickOutline(context, this.panelX, this.panelY, this.panelWidth, this.panelHeight, 3, outlineColor);
        RenderUtils.fillNative(context, this.panelX, this.panelY, this.panelWidth, headerH, headerColor);
        context.method_44379(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + headerH);
        fm.drawText(context, this.element.info.title, this.panelX + 4, this.panelY + ((headerH - fm.getTextHeight()) / 2), fm.getTextColor());
        context.method_44380();
        context.method_44379(this.listX, this.listY, this.listX + this.listW, this.listY + this.listH);
        int drawY = this.listY - this.scroll;
        int drawY2 = SettingGroupRenderer.renderGroups(context, this.anchorSettings, this.listX, drawY, this.listW, mouseX, mouseY);
        if (this.element.settings.sizeGroups() > 0) {
            SettingGroupRenderer.renderGroups(context, this.element.settings, this.listX, drawY2, this.listW, mouseX, mouseY);
        }
        context.method_44380();
        int totalH = SettingGroupRenderer.getGroupsHeight(this.anchorSettings) + SettingGroupRenderer.getGroupsHeight(this.element.settings);
        int maxScroll = Math.max(0, totalH - this.listH);
        if (maxScroll > 0) {
            int trackH = Math.max(1, this.listH - 2);
            int thumbH = Math.max(8, (int) ((((long) this.listH) * ((long) trackH)) / ((long) Math.max(1, totalH))));
            int thumbY = this.listY + 1 + ((int) ((((long) (trackH - thumbH)) * ((long) this.scroll)) / ((long) maxScroll)));
            int sbX = (this.panelX + this.panelWidth) - 2;
            context.method_25294(sbX, thumbY, sbX + 1, thumbY + thumbH, fm.primaryAlpha(220));
        }
        if (totalH == 0) {
            fm.drawText(context, "(no settings)", this.listX + ((this.listW - fm.getTextWidth("(no settings)")) / 2), this.listY + ((this.listH - fm.getTextHeight()) / 2), GuiColors.TEXT_DISABLED);
        }
        int halfW = this.panelWidth / 2;
        int activeX = this.panelX;
        int removeX = this.panelX + halfW;
        int removeW = this.panelWidth - halfW;
        boolean activeHover = mouseX >= activeX && mouseX < activeX + halfW && mouseY >= this.barY && mouseY < this.barY + this.barHeight;
        boolean removeHover = mouseX >= removeX && mouseX < removeX + removeW && mouseY >= this.barY && mouseY < this.barY + this.barHeight;
        if (this.element.isActive()) {
            iSecondaryAlpha = activeHover ? -12859808 : -13656499;
        } else {
            iSecondaryAlpha = activeHover ? fm.secondaryAlpha(240) : fm.secondaryAlpha(Opcode.GETFIELD);
        }
        int activeFill = iSecondaryAlpha;
        RenderUtils.fillNative(context, activeX, this.barY, halfW, this.barHeight, activeFill);
        int removeFill = removeHover ? -3657166 : -6411734;
        RenderUtils.fillNative(context, removeX, this.barY, removeW, this.barHeight, removeFill);
        RenderUtils.fillNative(context, removeX, this.barY, 1, this.barHeight, outlineColor);
        RenderUtils.fillNative(context, this.panelX, this.barY, this.panelWidth, 1, outlineColor);
        String activeLabel = this.element.isActive() ? "Active: yes" : "Active: no";
        fm.drawText(context, activeLabel, activeX + ((halfW - fm.getTextWidth(activeLabel)) / 2), this.barY + ((this.barHeight - fm.getTextHeight()) / 2), fm.getTextColor());
        fm.drawText(context, "Remove", removeX + ((removeW - fm.getTextWidth("Remove")) / 2), this.barY + ((this.barHeight - fm.getTextHeight()) / 2), fm.getTextColor());
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        if (mouseX >= 10 && mouseX < 10 + 60 && mouseY >= 4 && mouseY < 4 + rowH + 2) {
            class_310.method_1551().method_1507(this.returnTo);
            return true;
        }
        int halfW = this.panelWidth / 2;
        int activeX = this.panelX;
        int removeX = this.panelX + halfW;
        int removeW = this.panelWidth - halfW;
        if (mouseX >= activeX && mouseX < activeX + halfW && mouseY >= this.barY && mouseY < this.barY + this.barHeight) {
            this.element.toggle();
            Hud.get().save();
            return true;
        }
        if (mouseX >= removeX && mouseX < removeX + removeW && mouseY >= this.barY && mouseY < this.barY + this.barHeight) {
            this.element.remove();
            Hud.get().save();
            class_310.method_1551().method_1507(this.returnTo);
            return true;
        }
        SettingGroupRenderer.commitStringEdit();
        if (mouseX >= this.listX && mouseX < this.listX + this.listW && mouseY >= this.listY && mouseY < this.listY + this.listH) {
            int drawY = this.listY - this.scroll;
            if (SettingGroupRenderer.mouseClickedGroups(this.anchorSettings, this.listX, drawY, this.listW, (int) mouseX, (int) mouseY, button, () -> {
                return null;
            })) {
                return true;
            }
            if (SettingGroupRenderer.mouseClickedGroups(this.element.settings, this.listX, drawY + SettingGroupRenderer.getGroupsHeight(this.anchorSettings), this.listW, (int) mouseX, (int) mouseY, button, () -> {
                return null;
            })) {
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
        if (mouseX < this.listX || mouseX >= this.listX + this.listW || mouseY < this.listY || mouseY >= this.listY + this.listH) {
            return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        FontManager fm = FontManager.get();
        int totalH = SettingGroupRenderer.getGroupsHeight(this.anchorSettings) + SettingGroupRenderer.getGroupsHeight(this.element.settings);
        int maxScroll = Math.max(0, totalH - this.listH);
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
        Hud.get().save();
        class_310.method_1551().method_1507(this.returnTo);
    }
}
