package meteordevelopment.meteorclient.gui.newgui.screens;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.gui.newgui.FontManager;
import meteordevelopment.meteorclient.gui.newgui.GuiColors;
import meteordevelopment.meteorclient.gui.newgui.util.RenderUtils;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewSingleValueScreen.class */
public class NewSingleValueScreen<T> extends class_437 {
    private final Setting<T> setting;
    private final Iterable<T> registry;
    private final Function<T, String> nameFn;
    private final Function<T, class_1799> iconFn;
    private final Predicate<T> skipFn;
    private final Consumer<T> onPick;
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

    public NewSingleValueScreen(String title, Setting<T> setting, Iterable<T> registry, Function<T, String> nameFn, Function<T, class_1799> iconFn, Predicate<T> skipFn, Consumer<T> onPick) {
        super(class_2561.method_43470(title));
        this.filterText = "";
        this.filterFocused = true;
        this.scroll = 0;
        this.setting = setting;
        this.registry = registry;
        this.nameFn = nameFn;
        this.iconFn = iconFn;
        this.skipFn = skipFn;
        this.onPick = onPick;
    }

    protected void method_25426() {
        super.method_25426();
        int marginX = Math.max(40, this.field_22789 / 4);
        this.panelX = marginX;
        this.panelWidth = this.field_22789 - (marginX * 2);
        int searchH = FontManager.get().getRowHeight() * 2;
        this.searchY = 20;
        this.panelY = this.searchY + searchH + 8;
        this.panelHeight = (this.field_22790 - this.panelY) - 20;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        class_1799 icon;
        FontManager fm = FontManager.get();
        super.method_25394(context, mouseX, mouseY, delta);
        int lineColor = fm.primaryAlpha(Opcode.GETFIELD);
        int headerH = fm.getHeaderHeight();
        int rowH = fm.getRowHeight();
        int effectiveRowH = this.iconFn != null ? Math.max(rowH, 18) : rowH;
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
        List<Ranked<T>> items = buildFilteredList();
        context.method_25294(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + headerH, fm.secondaryAlpha(200));
        RenderUtils.drawThinHLine(context, this.panelX, this.panelY + headerH, this.panelWidth, lineColor);
        String heading = "Choose (" + items.size() + ")";
        fm.drawText(context, heading, this.panelX + 6, this.panelY + ((headerH - fm.getTextHeight()) / 2), fm.getTextColor());
        int rowY = ((this.panelY + headerH) + 1) - this.scroll;
        int rowEnd = this.panelY + this.panelHeight;
        context.method_44379(this.panelX + 1, this.panelY + headerH + 1, (this.panelX + this.panelWidth) - 1, rowEnd - 1);
        T current = this.setting.get();
        for (Ranked<T> r : items) {
            if (rowY + effectiveRowH < this.panelY + headerH + 1) {
                rowY += effectiveRowH;
            } else {
                if (rowY > rowEnd) {
                    break;
                }
                boolean hovered = hit((double) mouseX, (double) mouseY, this.panelX + 1, rowY, this.panelWidth - 2, effectiveRowH) && mouseY >= (this.panelY + headerH) + 1 && mouseY <= rowEnd - 1;
                boolean isCurrent = current != null && current.equals(r.value);
                if (hovered || isCurrent) {
                    context.method_25294(this.panelX + 1, rowY, (this.panelX + this.panelWidth) - 1, rowY + effectiveRowH, fm.secondaryAlpha(hovered ? Opcode.FCMPG : 90));
                }
                int tcolor = (hovered || isCurrent) ? fm.getTextColor() : fm.getTextSecondary();
                int textX = this.panelX + 6;
                if (this.iconFn != null && (icon = this.iconFn.apply(r.value)) != null && !icon.method_7960()) {
                    int iconY = rowY + ((effectiveRowH - 16) / 2);
                    context.method_51427(icon, textX, iconY);
                    textX += 20;
                }
                fm.drawText(context, r.name, textX, rowY + ((effectiveRowH - fm.getTextHeight()) / 2), tcolor);
                rowY += effectiveRowH;
            }
        }
        context.method_44380();
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int headerH = fm.getHeaderHeight();
        int effectiveRowH = this.iconFn != null ? Math.max(rowH, 18) : rowH;
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
        for (Ranked<T> r : buildFilteredList()) {
            if (mouseY >= rowY && mouseY < rowY + effectiveRowH && mouseY >= this.panelY + headerH + 1 && mouseY <= rowEnd - 1) {
                this.setting.set(r.value);
                if (this.onPick != null) {
                    this.onPick.accept(r.value);
                }
                method_25419();
                return true;
            }
            rowY += effectiveRowH;
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (hit((int) mouseX, (int) mouseY, this.panelX, this.panelY, this.panelWidth, this.panelHeight)) {
            FontManager fm = FontManager.get();
            int rowH = fm.getRowHeight();
            int effectiveRowH = this.iconFn != null ? Math.max(rowH, 18) : rowH;
            int headerH = fm.getHeaderHeight();
            int count = 0;
            for (T v : this.registry) {
                if (this.skipFn == null || !this.skipFn.test(v)) {
                    if (rankMatch(this.nameFn.apply(v), this.filterText) != Integer.MIN_VALUE) {
                        count++;
                    }
                }
            }
            int max = Math.max(0, (count * effectiveRowH) - (this.panelHeight - headerH));
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

    private List<Ranked<T>> buildFilteredList() {
        List<Ranked<T>> list = new ArrayList<>();
        for (T value : this.registry) {
            if (this.skipFn == null || !this.skipFn.test(value)) {
                String name = this.nameFn.apply(value);
                int rank = rankMatch(name, this.filterText);
                if (rank != Integer.MIN_VALUE) {
                    list.add(new Ranked<>(value, name, rank));
                }
            }
        }
        if (this.filterText.isEmpty()) {
            list.sort(Comparator.comparing(r -> {
                return r.name;
            }));
        } else {
            list.sort((a, b) -> {
                return b.rank - a.rank;
            });
        }
        return list;
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= ((double) x) && mx < ((double) (x + w)) && my >= ((double) y) && my < ((double) (y + h));
    }

    private static int rankMatch(String name, String filter) {
        if (filter.isEmpty()) {
            return 0;
        }
        int words = Utils.searchInWords(name, filter);
        int diff = Utils.searchLevenshteinDefault(name, filter, false);
        if (words > 0 || diff <= name.length() / 2) {
            return -diff;
        }
        return Integer.MIN_VALUE;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewSingleValueScreen$Ranked.class */
    private static class Ranked<T> {
        final T value;
        final String name;
        final int rank;

        Ranked(T value, String name, int rank) {
            this.value = value;
            this.name = name;
            this.rank = rank;
        }
    }
}
