package meteordevelopment.meteorclient.gui.newgui.screens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewCollectionListScreen.class */
public class NewCollectionListScreen<T> extends class_437 {
    private final Setting<?> setting;
    private final Collection<T> collection;
    private final Iterable<T> registry;
    private final Function<T, String> nameFn;
    private final Function<T, class_1799> iconFn;
    private final Predicate<T> skipFn;
    private String filterText;
    private boolean filterFocused;
    private int availableScroll;
    private int addedScroll;
    private int panelWidth;
    private int panelY;
    private int panelHeight;
    private int searchY;
    private int availableX;
    private int addedX;
    private static final int PADDING = 6;
    private static final int SEARCH_HEIGHT_MULT = 2;
    private static final int ICON_SIZE = 16;

    public NewCollectionListScreen(String title, Setting<?> setting, Collection<T> collection, Iterable<T> registry, Function<T, String> nameFn) {
        this(title, setting, collection, registry, nameFn, null, null);
    }

    public NewCollectionListScreen(String title, Setting<?> setting, Collection<T> collection, Iterable<T> registry, Function<T, String> nameFn, Function<T, class_1799> iconFn, Predicate<T> skipFn) {
        super(class_2561.method_43470(title));
        this.filterText = "";
        this.filterFocused = true;
        this.availableScroll = 0;
        this.addedScroll = 0;
        this.setting = setting;
        this.collection = collection;
        this.registry = registry;
        this.nameFn = nameFn;
        this.iconFn = iconFn;
        this.skipFn = skipFn;
    }

    protected void method_25426() {
        super.method_25426();
        int screenW = this.field_22789;
        int screenH = this.field_22790;
        int margin = Math.max(20, screenW / 10);
        this.panelWidth = ((screenW - (margin * 2)) - 8) / 2;
        int searchH = FontManager.get().getRowHeight() * 2;
        this.searchY = 20;
        this.panelY = this.searchY + searchH + 8;
        this.panelHeight = (screenH - this.panelY) - 20;
        this.availableX = margin;
        this.addedX = margin + this.panelWidth + 8;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        FontManager fm = FontManager.get();
        super.method_25394(context, mouseX, mouseY, delta);
        int lineColor = fm.primaryAlpha(Opcode.GETFIELD);
        int fillColor = fm.secondaryAlpha(200);
        int headerHeight = fm.getHeaderHeight();
        int rowH = fm.getRowHeight();
        fm.drawText(context, this.field_22785.getString(), this.availableX, 4, fm.getTextColor());
        int searchH = rowH * 2;
        int searchW = (this.panelWidth * 2) + 8;
        hit(mouseX, mouseY, this.availableX, this.searchY, searchW, searchH);
        context.method_25294(this.availableX, this.searchY, this.availableX + searchW, this.searchY + searchH, this.filterFocused ? fm.secondaryAlpha(220) : fm.secondaryAlpha(Opcode.FCMPG));
        RenderUtils.drawThinOutline(context, this.availableX, this.searchY, searchW, searchH, lineColor);
        int labelW = fm.getTextWidth("Search: ");
        fm.drawText(context, "Search: ", this.availableX + 6, this.searchY + ((searchH - fm.getTextHeight()) / 2), GuiColors.TEXT_SETTING_VALUE);
        fm.drawText(context, this.filterText, this.availableX + 6 + labelW, this.searchY + ((searchH - fm.getTextHeight()) / 2), fm.getTextColor());
        if (this.filterFocused && ((System.currentTimeMillis() / 500) & 1) == 0) {
            int caretX = this.availableX + 6 + labelW + fm.getTextWidth(this.filterText);
            context.method_25294(caretX, this.searchY + 3, caretX + 1, (this.searchY + searchH) - 3, fm.getTextColor());
        }
        fm.drawText(context, "[Clear]", ((this.availableX + searchW) - fm.getTextWidth("[Clear]")) - 6, this.searchY + ((searchH - fm.getTextHeight()) / 2), GuiColors.TEXT_SETTING_VALUE);
        List<Ranked<T>> availableList = new ArrayList<>();
        List<Ranked<T>> addedList = new ArrayList<>();
        for (T value : this.registry) {
            if (this.skipFn == null || !this.skipFn.test(value)) {
                String name = this.nameFn.apply(value);
                int rank = rankMatch(name, this.filterText);
                if (rank != Integer.MIN_VALUE) {
                    if (this.collection.contains(value)) {
                        addedList.add(new Ranked<>(value, name, rank));
                    } else {
                        availableList.add(new Ranked<>(value, name, rank));
                    }
                }
            }
        }
        if (this.filterText.isEmpty()) {
            availableList.sort(Comparator.comparing(r -> {
                return r.name;
            }));
            addedList.sort(Comparator.comparing(r2 -> {
                return r2.name;
            }));
        } else {
            availableList.sort((a, b) -> {
                return b.rank - a.rank;
            });
            addedList.sort((a2, b2) -> {
                return b2.rank - a2.rank;
            });
        }
        renderPanel(context, fm, "Available (" + availableList.size() + ")", this.availableX, this.panelY, this.panelWidth, this.panelHeight, availableList, this.availableScroll, mouseX, mouseY, fillColor, lineColor, headerHeight, rowH, true);
        renderPanel(context, fm, "Added (" + addedList.size() + ")", this.addedX, this.panelY, this.panelWidth, this.panelHeight, addedList, this.addedScroll, mouseX, mouseY, fillColor, lineColor, headerHeight, rowH, false);
    }

    private void renderPanel(class_332 context, FontManager fm, String heading, int px, int py, int pw, int ph, List<Ranked<T>> items, int scroll, int mouseX, int mouseY, int fillColor, int lineColor, int headerH, int rowH, boolean isAvailable) {
        class_1799 icon;
        String str;
        context.method_25294(px, py, px + pw, py + ph, fm.primaryAlpha(Opcode.ISHL));
        RenderUtils.drawThinOutline(context, px, py, pw, ph, lineColor);
        context.method_25294(px, py, px + pw, py + headerH, fillColor);
        RenderUtils.drawThinHLine(context, px, py + headerH, pw, lineColor);
        fm.drawText(context, heading, px + 6, py + ((headerH - fm.getTextHeight()) / 2), fm.getTextColor());
        int effectiveRowH = this.iconFn != null ? Math.max(rowH, 18) : rowH;
        int rowY = ((py + headerH) + 1) - scroll;
        int rowEnd = py + ph;
        context.method_44379(px + 1, py + headerH + 1, (px + pw) - 1, rowEnd - 1);
        if (items.isEmpty()) {
            if (isAvailable) {
                str = this.filterText.isEmpty() ? "(no items to add)" : "(no matches)";
            } else {
                str = this.filterText.isEmpty() ? "(nothing added)" : "(no matches)";
            }
            String msg = str;
            int tx = px + ((pw - fm.getTextWidth(msg)) / 2);
            int ty = py + headerH + (((ph - headerH) - fm.getTextHeight()) / 2);
            fm.drawText(context, msg, tx, ty, GuiColors.TEXT_DISABLED);
        }
        for (Ranked<T> r : items) {
            if (rowY + effectiveRowH < py + headerH + 1) {
                rowY += effectiveRowH;
            } else {
                if (rowY > rowEnd) {
                    break;
                }
                boolean rowHovered = hit(mouseX, mouseY, px + 1, rowY, pw - 2, effectiveRowH) && mouseY >= (py + headerH) + 1 && mouseY <= rowEnd - 1;
                if (rowHovered) {
                    context.method_25294(px + 1, rowY, (px + pw) - 1, rowY + effectiveRowH, fm.secondaryAlpha(Opcode.ISHL));
                }
                int tcolor = rowHovered ? fm.getTextColor() : fm.getTextSecondary();
                int textX = px + 6;
                if (this.iconFn != null && (icon = this.iconFn.apply(r.value)) != null && !icon.method_7960()) {
                    int iconY = rowY + ((effectiveRowH - 16) / 2);
                    context.method_51427(icon, textX, iconY);
                    textX += 20;
                }
                String label = this.iconFn != null ? r.name : (isAvailable ? "+ " : "- ") + r.name;
                fm.drawText(context, label, textX, rowY + ((effectiveRowH - fm.getTextHeight()) / 2), tcolor);
                rowY += effectiveRowH;
            }
        }
        context.method_44380();
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        FontManager fm = FontManager.get();
        int rowH = fm.getRowHeight();
        int headerH = fm.getHeaderHeight();
        int searchH = rowH * 2;
        int searchW = (this.panelWidth * 2) + 8;
        if (hit((int) mouseX, (int) mouseY, this.availableX, this.searchY, searchW, searchH)) {
            this.filterFocused = true;
            int clearX = ((this.availableX + searchW) - fm.getTextWidth("[Clear]")) - 6;
            if (mouseX >= clearX) {
                this.filterText = "";
                this.availableScroll = 0;
                this.addedScroll = 0;
                return true;
            }
            return true;
        }
        this.filterFocused = false;
        boolean inAvailable = hit((int) mouseX, (int) mouseY, this.availableX, this.panelY, this.panelWidth, this.panelHeight);
        boolean inAdded = hit((int) mouseX, (int) mouseY, this.addedX, this.panelY, this.panelWidth, this.panelHeight);
        if (!inAvailable && !inAdded) {
            return super.method_25402(mouseX, mouseY, button);
        }
        int i = inAvailable ? this.availableX : this.addedX;
        int scroll = inAvailable ? this.availableScroll : this.addedScroll;
        int effectiveRowH = this.iconFn != null ? Math.max(rowH, 18) : rowH;
        int rowY = ((this.panelY + headerH) + 1) - scroll;
        int rowEnd = this.panelY + this.panelHeight;
        List<Ranked<T>> items = new ArrayList<>();
        for (T value : this.registry) {
            if (this.skipFn == null || !this.skipFn.test(value)) {
                String name = this.nameFn.apply(value);
                int rank = rankMatch(name, this.filterText);
                if (rank != Integer.MIN_VALUE) {
                    boolean inColl = this.collection.contains(value);
                    if (inAvailable && !inColl) {
                        items.add(new Ranked<>(value, name, rank));
                    } else if (inAdded && inColl) {
                        items.add(new Ranked<>(value, name, rank));
                    }
                }
            }
        }
        if (this.filterText.isEmpty()) {
            items.sort(Comparator.comparing(r -> {
                return r.name;
            }));
        } else {
            items.sort((a, b) -> {
                return b.rank - a.rank;
            });
        }
        for (Ranked<T> r2 : items) {
            if (mouseY >= rowY && mouseY < rowY + effectiveRowH && mouseY >= this.panelY + headerH + 1 && mouseY <= rowEnd - 1) {
                if (inAvailable) {
                    this.collection.add(r2.value);
                } else {
                    this.collection.remove(r2.value);
                }
                this.setting.onChanged();
                return true;
            }
            rowY += effectiveRowH;
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        FontManager fm = FontManager.get();
        int headerH = fm.getHeaderHeight();
        int rowH = fm.getRowHeight();
        int effectiveRowH = this.iconFn != null ? Math.max(rowH, 18) : rowH;
        int avail = 0;
        int added = 0;
        for (T v : this.registry) {
            if (this.skipFn == null || !this.skipFn.test(v)) {
                String name = this.nameFn.apply(v);
                if (rankMatch(name, this.filterText) != Integer.MIN_VALUE) {
                    if (this.collection.contains(v)) {
                        added++;
                    } else {
                        avail++;
                    }
                }
            }
        }
        int contentAvail = Math.max(0, (avail * effectiveRowH) - (this.panelHeight - headerH));
        int contentAdded = Math.max(0, (added * effectiveRowH) - (this.panelHeight - headerH));
        if (hit((int) mouseX, (int) mouseY, this.availableX, this.panelY, this.panelWidth, this.panelHeight)) {
            this.availableScroll -= (int) (verticalAmount * 14.0d);
            this.availableScroll = Math.max(0, Math.min(this.availableScroll, contentAvail));
            return true;
        }
        if (hit((int) mouseX, (int) mouseY, this.addedX, this.panelY, this.panelWidth, this.panelHeight)) {
            this.addedScroll -= (int) (verticalAmount * 14.0d);
            this.addedScroll = Math.max(0, Math.min(this.addedScroll, contentAdded));
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
                this.availableScroll = 0;
                this.addedScroll = 0;
                return true;
            }
            if (keyCode == 256) {
                this.filterFocused = false;
                return true;
            }
            if (keyCode == 257) {
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
            this.availableScroll = 0;
            this.addedScroll = 0;
            return true;
        }
        return super.method_25400(chr, modifiers);
    }

    public boolean method_25421() {
        return false;
    }

    private static boolean hit(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
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

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/newgui/screens/NewCollectionListScreen$Ranked.class */
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
