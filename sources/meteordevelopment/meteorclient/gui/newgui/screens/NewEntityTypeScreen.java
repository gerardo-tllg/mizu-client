package meteordevelopment.meteorclient.gui.newgui.screens;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.class_1299;
import net.minecraft.class_1311;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewEntityTypeScreen.class */
public class NewEntityTypeScreen extends class_437 {
    private static final String S_ANIMALS = "Animals";
    private static final String S_WATER = "Water Animals";
    private static final String S_MONSTERS = "Monsters";
    private static final String S_AMBIENT = "Ambient";
    private static final String S_MISC = "Misc";
    private static final String[] SECTION_ORDER = {S_ANIMALS, S_WATER, S_MONSTERS, S_AMBIENT, S_MISC};
    private final EntityTypeListSetting setting;
    private final Map<String, List<class_1299<?>>> sections;
    private final Map<String, Boolean> expanded;
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
    private static final int CHECKBOX_SIZE = 10;

    public NewEntityTypeScreen(EntityTypeListSetting setting) {
        super(class_2561.method_43470("Select Entities — " + setting.name));
        this.sections = new LinkedHashMap();
        this.expanded = new LinkedHashMap();
        this.filterText = "";
        this.filterFocused = true;
        this.scroll = 0;
        this.setting = setting;
        for (String str : SECTION_ORDER) {
            this.expanded.put(str, false);
        }
        rebuildSections();
        int total = 0;
        for (List<class_1299<?>> l : this.sections.values()) {
            total += l.size();
        }
        if (total <= 30) {
            for (String s : SECTION_ORDER) {
                if (!this.sections.get(s).isEmpty()) {
                    this.expanded.put(s, true);
                }
            }
        }
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
        int rowH = fm.getRowHeight();
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
        context.method_25294(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + headerH, fm.secondaryAlpha(200));
        RenderUtils.drawThinHLine(context, this.panelX, this.panelY + headerH, this.panelWidth, lineColor);
        int addedCount = this.setting.get().size();
        fm.drawText(context, "Entities (" + addedCount + " added)", this.panelX + 6, this.panelY + ((headerH - fm.getTextHeight()) / 2), fm.getTextColor());
        int curY = ((this.panelY + headerH) + 1) - this.scroll;
        int rowEnd = this.panelY + this.panelHeight;
        context.method_44379(this.panelX + 1, this.panelY + headerH + 1, (this.panelX + this.panelWidth) - 1, rowEnd - 1);
        for (String secName : SECTION_ORDER) {
            List<class_1299<?>> entries = this.sections.get(secName);
            if (!entries.isEmpty()) {
                boolean isExpanded = this.expanded.getOrDefault(secName, false).booleanValue();
                if (curY + rowH >= this.panelY + headerH + 1 && curY <= rowEnd) {
                    context.method_25294(this.panelX + 1, curY, (this.panelX + this.panelWidth) - 1, curY + rowH, fm.secondaryAlpha(hit((double) mouseX, (double) mouseY, this.panelX + 1, curY, this.panelWidth - 2, rowH) ? Opcode.GETFIELD : Opcode.F2L));
                    String indicator = isExpanded ? "-" : "+";
                    fm.drawText(context, indicator, this.panelX + 6, curY + ((rowH - fm.getTextHeight()) / 2), fm.getTextColor());
                    fm.drawText(context, secName + " (" + entries.size() + ")", this.panelX + 6 + fm.getTextWidth("+") + 6, curY + ((rowH - fm.getTextHeight()) / 2), fm.getTextColor());
                    int addedInSec = 0;
                    Iterator<class_1299<?>> it = entries.iterator();
                    while (it.hasNext()) {
                        if (this.setting.get().contains(it.next())) {
                            addedInSec++;
                        }
                    }
                    boolean allAdded = addedInSec == entries.size();
                    boolean someAdded = addedInSec > 0;
                    drawCheckbox(context, fm, ((this.panelX + this.panelWidth) - 10) - 6, curY + ((rowH - 10) / 2), allAdded, someAdded && !allAdded, lineColor);
                }
                curY += rowH;
                if (isExpanded) {
                    for (class_1299<?> e : entries) {
                        if (curY + rowH < this.panelY + headerH + 1) {
                            curY += rowH;
                        } else {
                            if (curY > rowEnd) {
                                break;
                            }
                            boolean hovered = hit(mouseX, mouseY, this.panelX + 1, curY, this.panelWidth - 2, rowH);
                            boolean inColl = this.setting.get().contains(e);
                            if (hovered || inColl) {
                                context.method_25294(this.panelX + 1, curY, (this.panelX + this.panelWidth) - 1, curY + rowH, fm.secondaryAlpha(hovered ? Opcode.IXOR : 70));
                            }
                            int tcolor = (hovered || inColl) ? fm.getTextColor() : fm.getTextSecondary();
                            fm.drawText(context, Names.get(e), this.panelX + 6 + 14, curY + ((rowH - fm.getTextHeight()) / 2), tcolor);
                            drawCheckbox(context, fm, ((this.panelX + this.panelWidth) - 10) - 6, curY + ((rowH - 10) / 2), inColl, false, fm.primaryAlpha(Opcode.GETFIELD));
                            curY += rowH;
                        }
                    }
                }
            }
        }
        context.method_44380();
    }

    private void drawCheckbox(class_332 context, FontManager fm, int x, int y, boolean checked, boolean partial, int lineColor) {
        context.method_25294(x, y, x + 10, y + 10, fm.primaryAlpha(200));
        RenderUtils.drawThinOutline(context, x, y, 10, 10, lineColor);
        if (checked) {
            context.method_25294(x + 2, y + 2, (x + 10) - 2, (y + 10) - 2, fm.getTextColor());
        } else if (partial) {
            context.method_25294(x + 3, y + 4, (x + 10) - 3, (y + 10) - 4, fm.getTextSecondary());
        }
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int headerH = fm.getHeaderHeight();
        int searchH = rowH * 2;
        if (hit((int) mouseX, (int) mouseY, this.panelX, this.searchY, this.panelWidth, searchH)) {
            this.filterFocused = true;
            int clearX = ((this.panelX + this.panelWidth) - fm.getTextWidth("[Clear]")) - 6;
            if (mouseX >= clearX) {
                this.filterText = "";
                rebuildSections();
                this.scroll = 0;
                return true;
            }
            return true;
        }
        this.filterFocused = false;
        if (!hit((int) mouseX, (int) mouseY, this.panelX, this.panelY, this.panelWidth, this.panelHeight)) {
            return super.method_25402(mouseX, mouseY, button);
        }
        int curY = ((this.panelY + headerH) + 1) - this.scroll;
        int rowEnd = this.panelY + this.panelHeight;
        int checkboxX = ((this.panelX + this.panelWidth) - 10) - 6;
        for (String secName : SECTION_ORDER) {
            List<class_1299<?>> entries = this.sections.get(secName);
            if (!entries.isEmpty()) {
                if (mouseY >= curY && mouseY < curY + rowH && curY >= this.panelY + headerH + 1 && curY + rowH <= rowEnd) {
                    if (mouseX >= checkboxX - 2 && mouseX <= checkboxX + 10 + 2) {
                        boolean anyAdded = false;
                        Iterator<class_1299<?>> it = entries.iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                break;
                            }
                            if (this.setting.get().contains(it.next())) {
                                anyAdded = true;
                                break;
                            }
                        }
                        if (anyAdded) {
                            this.setting.get().removeAll(entries);
                        } else {
                            this.setting.get().addAll(entries);
                        }
                        this.setting.onChanged();
                        return true;
                    }
                    this.expanded.put(secName, Boolean.valueOf(!this.expanded.get(secName).booleanValue()));
                    return true;
                }
                curY += rowH;
                if (this.expanded.getOrDefault(secName, false).booleanValue()) {
                    for (class_1299<?> e : entries) {
                        if (mouseY >= curY && mouseY < curY + rowH && curY >= this.panelY + headerH + 1 && curY + rowH <= rowEnd) {
                            if (this.setting.get().contains(e)) {
                                this.setting.get().remove(e);
                            } else {
                                this.setting.get().add(e);
                            }
                            this.setting.onChanged();
                            return true;
                        }
                        curY += rowH;
                    }
                } else {
                    continue;
                }
            }
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (hit((int) mouseX, (int) mouseY, this.panelX, this.panelY, this.panelWidth, this.panelHeight)) {
            FontManager fm = FontManager.get();
            int rowH = fm.getRowHeight();
            int headerH = fm.getHeaderHeight();
            int total = 0;
            for (String secName : SECTION_ORDER) {
                List<class_1299<?>> entries = this.sections.get(secName);
                if (!entries.isEmpty()) {
                    total++;
                    if (this.expanded.getOrDefault(secName, false).booleanValue()) {
                        total += entries.size();
                    }
                }
            }
            int max = Math.max(0, (total * rowH) - (this.panelHeight - headerH));
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
                    rebuildSections();
                    this.scroll = 0;
                    return true;
                }
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
            rebuildSections();
            this.scroll = 0;
            return true;
        }
        return super.method_25400(chr, modifiers);
    }

    public boolean method_25421() {
        return false;
    }

    private void rebuildSections() {
        for (String s : SECTION_ORDER) {
            this.sections.put(s, new ArrayList());
        }
        List<class_1299<?>> flat = new ArrayList<>();
        for (class_1299<?> e : class_7923.field_41177) {
            if (this.setting.filter == null || this.setting.filter.test(e)) {
                String name = Names.get(e);
                if (!this.filterText.isEmpty()) {
                    int words = Utils.searchInWords(name, this.filterText);
                    int diff = Utils.searchLevenshteinDefault(name, this.filterText, false);
                    if (words != 0 || diff <= name.length() / 2) {
                    }
                }
                flat.add(e);
            }
        }
        flat.sort(Comparator.comparing(Names::get, String.CASE_INSENSITIVE_ORDER));
        for (class_1299<?> e2 : flat) {
            String sec = sectionFor(e2);
            this.sections.get(sec).add(e2);
        }
    }

    /* JADX INFO: renamed from: meteordevelopment.meteorclient.gui.newgui.screens.NewEntityTypeScreen$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewEntityTypeScreen$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$net$minecraft$entity$SpawnGroup = new int[class_1311.values().length];

        static {
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_6294.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_24460.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_6300.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_30092.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_34447.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_6302.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_6303.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    private static String sectionFor(class_1299<?> e) {
        class_1311 g = e.method_5891();
        switch (AnonymousClass1.$SwitchMap$net$minecraft$entity$SpawnGroup[g.ordinal()]) {
            case 1:
                return S_ANIMALS;
            case 2:
            case 3:
            case 4:
            case 5:
                return S_WATER;
            case 6:
                return S_MONSTERS;
            case 7:
                return S_AMBIENT;
            default:
                return S_MISC;
        }
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= ((double) x) && mx < ((double) (x + w)) && my >= ((double) y) && my < ((double) (y + h));
    }
}
