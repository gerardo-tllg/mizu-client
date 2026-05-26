package meteordevelopment.meteorclient.gui.newgui.screens;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewAddHudElementScreen.class */
public class NewAddHudElementScreen extends class_437 {
    private final int spawnX;
    private final int spawnY;
    private final class_437 returnTo;
    private String searchQuery;
    private int scroll;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private static final int PANEL_WIDTH = 170;
    private static final int PADDING = 2;
    private static final int SEARCH_ROW_GAP = 2;

    public NewAddHudElementScreen(int spawnX, int spawnY, class_437 returnTo) {
        super(class_2561.method_43470("Add HUD element"));
        this.searchQuery = "";
        this.scroll = 0;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.returnTo = returnTo;
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
        int totalRows = countTotalRows();
        int desiredH = rowH + rowH + 2 + (totalRows * rowH) + 4;
        int maxH = this.field_22790 - 10;
        this.panelHeight = Math.min(desiredH, maxH);
        int targetX = (int) (((double) this.spawnX) / sf);
        int targetY = (int) (((double) this.spawnY) / sf);
        this.panelX = Math.max(4, Math.min((this.field_22789 - this.panelWidth) - 4, targetX));
        this.panelY = Math.max(4, Math.min((this.field_22790 - this.panelHeight) - 4, targetY));
    }

    private int countTotalRows() {
        int total = 0;
        for (Map.Entry<HudGroup, List<Item>> e : buildGrouped().entrySet()) {
            total = total + 1 + e.getValue().size();
        }
        return total;
    }

    private Map<HudGroup, List<Item>> buildGrouped() {
        Map<HudGroup, List<Item>> grouped = new LinkedHashMap<>();
        for (HudElementInfo<?> info : Hud.get().infos.values()) {
            if (info.hasPresets() && !this.searchQuery.isEmpty()) {
                for (HudElementInfo<?>.Preset preset : info.presets) {
                    String title = info.title + "  -  " + preset.title;
                    if (Utils.searchTextDefault(title, this.searchQuery, false)) {
                        grouped.computeIfAbsent(info.group, g -> {
                            return new ArrayList();
                        }).add(new Item(title, preset));
                    }
                }
            } else if (Utils.searchTextDefault(info.title, this.searchQuery, false)) {
                grouped.computeIfAbsent(info.group, g2 -> {
                    return new ArrayList();
                }).add(new Item(info.title, info));
            }
        }
        return grouped;
    }

    /* JADX WARN: Removed duplicated region for block: B:26:0x02af  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void method_25394(net.minecraft.class_332 r9, int r10, int r11, float r12) {
        /*
            Method dump skipped, instruction units count: 1182
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: meteordevelopment.meteorclient.gui.newgui.screens.NewAddHudElementScreen.method_25394(net.minecraft.class_332, int, int, float):void");
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        if (mouseX < this.panelX || mouseX >= this.panelX + this.panelWidth || mouseY < this.panelY || mouseY >= this.panelY + this.panelHeight) {
            class_310.method_1551().method_1507(this.returnTo);
            return true;
        }
        int closeX = ((this.panelX + this.panelWidth) - fm.getTextWidth("✕")) - 4;
        if (mouseX >= closeX - 2 && mouseY >= this.panelY && mouseY < this.panelY + rowH) {
            class_310.method_1551().method_1507(this.returnTo);
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
        for (Map.Entry<HudGroup, List<Item>> entry : buildGrouped().entrySet()) {
            drawY += rowH;
            for (Item item : entry.getValue()) {
                if (mouseY >= drawY && mouseY < drawY + rowH) {
                    runObject(item.object());
                    return true;
                }
                drawY += rowH;
            }
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    private void runObject(Object object) {
        if (object == null) {
            return;
        }
        if (object instanceof HudElementInfo.Preset) {
            Hud.get().add((HudElementInfo<?>.Preset) object, this.spawnX, this.spawnY);
            Hud.get().save();
            class_310.method_1551().method_1507(this.returnTo);
            return;
        }
        HudElementInfo<?> info = (HudElementInfo) object;
        if (info.hasPresets()) {
            class_310.method_1551().method_1507(new NewHudElementPresetsScreen(info, this.spawnX, this.spawnY, this, this.returnTo));
            return;
        }
        Hud.get().add(info, this.spawnX, this.spawnY);
        Hud.get().save();
        class_310.method_1551().method_1507(this.returnTo);
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
            class_310.method_1551().method_1507(this.returnTo);
            return true;
        }
        if (keyCode == 259 && !this.searchQuery.isEmpty()) {
            this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
            this.scroll = 0;
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            Map<HudGroup, List<Item>> grouped = buildGrouped();
            for (List<Item> items : grouped.values()) {
                if (!items.isEmpty()) {
                    runObject(items.get(0).object());
                    return true;
                }
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
        class_310.method_1551().method_1507(this.returnTo);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewAddHudElementScreen$Item.class */
    private record Item(String title, Object object) {
    }
}
