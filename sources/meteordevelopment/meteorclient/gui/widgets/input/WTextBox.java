package meteordevelopment.meteorclient.gui.widgets.input;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiKeyEvents;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import org.apache.commons.lang3.SystemUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/input/WTextBox.class */
public abstract class WTextBox extends WWidget {
    private static final Renderer DEFAULT_RENDERER = (renderer, x, y, text, color) -> {
        renderer.text(text, x, y, color, false);
    };
    public Runnable action;
    public Runnable actionOnUnfocused;
    protected String text;
    protected String placeholder;
    protected CharFilter filter;
    protected final Renderer renderer;
    protected boolean focused;
    protected DoubleList textWidths;
    protected int cursor;
    protected double textStart;
    protected boolean selecting;
    protected int selectionStart;
    protected int selectionEnd;
    private int preSelectionCursor;
    private List<String> completions;
    private int completionsStart;
    private WContainer completionsW;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/input/WTextBox$ICompletionItem.class */
    public interface ICompletionItem {
        boolean isSelected();

        void setSelected(boolean z);

        String getCompletion();
    }

    protected abstract WContainer createCompletionsRootWidget();

    protected abstract <T extends WWidget & ICompletionItem> T createCompletionsValueWidth(String str, boolean z);

    public WTextBox(String text, CharFilter filter, Class<? extends Renderer> renderer) {
        this(text, null, filter, renderer);
    }

    public WTextBox(String text, String placeholder, CharFilter filter, Class<? extends Renderer> renderer) {
        this.textWidths = new DoubleArrayList();
        this.text = text;
        this.placeholder = placeholder;
        this.filter = filter;
        try {
            this.renderer = renderer != null ? renderer.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]) : DEFAULT_RENDERER;
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        double pad = pad();
        double s = this.theme.textHeight();
        this.width = pad + s + pad;
        this.height = pad + s + pad;
        calculateTextWidths();
        if (this.completionsW != null) {
            this.completionsW.calculateSize();
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void calculateWidgetPositions() {
        super.calculateWidgetPositions();
        if (this.completionsW != null) {
            this.completionsW.x = this.x;
            this.completionsW.y = this.y + this.height;
            this.completionsW.calculateWidgetPositions();
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void move(double deltaX, double deltaY) {
        super.move(deltaX, deltaY);
        if (this.completionsW != null) {
            this.completionsW.move(deltaX, deltaY);
        }
    }

    protected double maxTextWidth() {
        return this.width - (pad() * 2.0d);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
        if (this.mouseOver && !used) {
            if (button == 1) {
                if (!this.text.isEmpty()) {
                    this.text = "";
                    this.cursor = 0;
                    this.selectionStart = 0;
                    this.selectionEnd = 0;
                    runAction();
                }
            } else if (button == 0) {
                this.selecting = true;
                double overflowWidth = getOverflowWidthForRender();
                double relativeMouseX = (mouseX - this.x) + overflowWidth;
                double pad = pad();
                double smallestDifference = Double.MAX_VALUE;
                this.cursor = this.text.length();
                for (int i = 0; i < this.textWidths.size(); i++) {
                    double difference = Math.abs((this.textWidths.getDouble(i) + pad) - relativeMouseX);
                    if (difference < smallestDifference) {
                        smallestDifference = difference;
                        this.cursor = i;
                    }
                }
                this.preSelectionCursor = this.cursor;
                resetSelection();
                cursorChanged();
            }
            setFocused(true);
            return true;
        }
        if (this.focused) {
            setFocused(false);
            return false;
        }
        return false;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
        if (this.selecting) {
            double overflowWidth = getOverflowWidthForRender();
            double relativeMouseX = (mouseX - this.x) + overflowWidth;
            double pad = pad();
            double smallestDifference = Double.MAX_VALUE;
            for (int i = 0; i < this.textWidths.size(); i++) {
                double difference = Math.abs((this.textWidths.getDouble(i) + pad) - relativeMouseX);
                if (difference < smallestDifference) {
                    smallestDifference = difference;
                    if (i < this.preSelectionCursor) {
                        this.selectionStart = i;
                        this.cursor = i;
                    } else if (i > this.preSelectionCursor) {
                        this.selectionEnd = i;
                        this.cursor = i;
                    } else {
                        this.cursor = this.preSelectionCursor;
                        resetSelection();
                    }
                }
            }
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        this.selecting = false;
        if (this.selectionStart < this.preSelectionCursor && this.preSelectionCursor == this.selectionEnd) {
            this.cursor = this.selectionStart;
            return false;
        }
        if (this.selectionEnd > this.preSelectionCursor && this.preSelectionCursor == this.selectionStart) {
            this.cursor = this.selectionEnd;
            return false;
        }
        return false;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onKeyPressed(int key, int mods) {
        if (!this.focused) {
            return false;
        }
        boolean control = class_310.field_1703 ? mods == 8 : mods == 2;
        if (control && key == 67) {
            if (this.cursor != this.selectionStart || this.cursor != this.selectionEnd) {
                MeteorClient.mc.field_1774.method_1455(this.text.substring(this.selectionStart, this.selectionEnd));
                return true;
            }
            return true;
        }
        if (control && key == 88) {
            if (this.cursor != this.selectionStart || this.cursor != this.selectionEnd) {
                MeteorClient.mc.field_1774.method_1455(this.text.substring(this.selectionStart, this.selectionEnd));
                clearSelection();
                return true;
            }
            return true;
        }
        if (control && key == 65) {
            this.cursor = this.text.length();
            this.selectionStart = 0;
            this.selectionEnd = this.cursor;
        } else {
            if (mods == ((class_310.field_1703 ? 8 : 2) | 1) && key == 65) {
                resetSelection();
            } else {
                if (key == 257 || key == 335) {
                    setFocused(false);
                    if (this.actionOnUnfocused != null) {
                        this.actionOnUnfocused.run();
                        return true;
                    }
                    return true;
                }
                if (key == 258 && this.completionsW != null) {
                    String completion = ((ICompletionItem) this.completionsW.cells.get(getSelectedCompletion()).widget()).getCompletion();
                    StringBuilder sb = new StringBuilder(this.text.length() + completion.length() + 1);
                    String a = this.text.substring(0, this.cursor);
                    sb.append(a);
                    int i = 0;
                    while (true) {
                        if (i >= completion.length() - 1) {
                            break;
                        }
                        if (!a.endsWith(completion.substring(0, (completion.length() - i) - 1))) {
                            i++;
                        } else {
                            completion = completion.substring((completion.length() - i) - 1);
                            break;
                        }
                    }
                    sb.append(completion);
                    if (completion.endsWith("(")) {
                        sb.append(')');
                    }
                    sb.append((CharSequence) this.text, this.cursor, this.text.length());
                    this.text = sb.toString();
                    this.cursor += completion.length();
                    resetSelection();
                    runAction();
                    return true;
                }
            }
        }
        return onKeyRepeated(key, mods);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onKeyRepeated(int key, int mods) {
        int count;
        int iCountToNextSpace;
        if (!this.focused) {
            return false;
        }
        boolean control = class_310.field_1703 ? mods == 8 : mods == 2;
        boolean shift = mods == 1;
        boolean controlShift = mods == ((SystemUtils.IS_OS_WINDOWS ? 4 : class_310.field_1703 ? 8 : 2) | 1);
        boolean altShift = mods == ((SystemUtils.IS_OS_WINDOWS ? 2 : 4) | 1);
        if (control && key == 86) {
            clearSelection();
            String preText = this.text;
            String clipboard = MeteorClient.mc.field_1774.method_1460();
            int addedChars = 0;
            StringBuilder sb = new StringBuilder(this.text.length() + clipboard.length());
            sb.append(this.text);
            for (int i = 0; i < clipboard.length(); i++) {
                char c = clipboard.charAt(i);
                if (this.filter.filter(sb.toString(), c)) {
                    sb.insert(this.cursor + addedChars, c);
                    addedChars++;
                }
            }
            this.text = sb.toString();
            this.cursor += addedChars;
            resetSelection();
            if (!this.text.equals(preText)) {
                runAction();
                return true;
            }
            return true;
        }
        if (key == 259) {
            if (this.cursor > 0 && this.cursor == this.selectionStart && this.cursor == this.selectionEnd) {
                String preText2 = this.text;
                if (mods == (SystemUtils.IS_OS_WINDOWS ? 4 : class_310.field_1703 ? 8 : 2)) {
                    iCountToNextSpace = this.cursor;
                } else if (mods == (SystemUtils.IS_OS_WINDOWS ? 2 : 4)) {
                    iCountToNextSpace = countToNextSpace(true);
                } else {
                    iCountToNextSpace = 1;
                }
                int count2 = iCountToNextSpace;
                this.text = this.text.substring(0, this.cursor - count2) + this.text.substring(this.cursor);
                this.cursor -= count2;
                resetSelection();
                if (!this.text.equals(preText2)) {
                    runAction();
                    return true;
                }
                return true;
            }
            if (this.cursor != this.selectionStart || this.cursor != this.selectionEnd) {
                clearSelection();
                return true;
            }
            return true;
        }
        if (key == 261) {
            if (this.cursor == this.selectionStart && this.cursor == this.selectionEnd) {
                if (this.cursor < this.text.length()) {
                    String preText3 = this.text;
                    if (mods == (SystemUtils.IS_OS_WINDOWS ? 4 : class_310.field_1703 ? 8 : 2)) {
                        count = this.text.length() - this.cursor;
                    } else if (mods == (SystemUtils.IS_OS_WINDOWS ? 2 : 4)) {
                        count = countToNextSpace(false);
                    } else {
                        count = 1;
                    }
                    this.text = this.text.substring(0, this.cursor) + this.text.substring(this.cursor + count);
                    if (!this.text.equals(preText3)) {
                        runAction();
                        return true;
                    }
                    return true;
                }
                return true;
            }
            clearSelection();
            return true;
        }
        if (key == 263) {
            if (this.cursor > 0) {
                if (mods == (SystemUtils.IS_OS_WINDOWS ? 2 : 4)) {
                    this.cursor -= countToNextSpace(true);
                    resetSelection();
                } else if (mods == (SystemUtils.IS_OS_WINDOWS ? 4 : class_310.field_1703 ? 8 : 2)) {
                    this.cursor = 0;
                    resetSelection();
                } else if (altShift) {
                    if (this.cursor == this.selectionEnd && this.cursor != this.selectionStart) {
                        this.cursor -= countToNextSpace(true);
                        this.selectionEnd = this.cursor;
                    } else {
                        this.cursor -= countToNextSpace(true);
                        this.selectionStart = this.cursor;
                    }
                } else if (controlShift) {
                    if (this.cursor == this.selectionEnd && this.cursor != this.selectionStart) {
                        this.selectionEnd = this.selectionStart;
                    }
                    this.selectionStart = 0;
                    this.cursor = 0;
                } else if (shift) {
                    if (this.cursor == this.selectionEnd && this.cursor != this.selectionStart) {
                        this.selectionEnd = this.cursor - 1;
                    } else {
                        this.selectionStart = this.cursor - 1;
                    }
                    this.cursor--;
                } else {
                    if (this.cursor == this.selectionEnd && this.cursor != this.selectionStart) {
                        this.cursor = this.selectionStart;
                    } else {
                        this.cursor--;
                    }
                    resetSelection();
                }
                cursorChanged();
                return true;
            }
            if (this.selectionStart != this.selectionEnd && this.selectionStart == 0 && mods == 0) {
                this.cursor = 0;
                resetSelection();
                cursorChanged();
                return true;
            }
            return true;
        }
        if (key == 262) {
            if (this.cursor < this.text.length()) {
                if (mods == (SystemUtils.IS_OS_WINDOWS ? 2 : 4)) {
                    this.cursor += countToNextSpace(false);
                    resetSelection();
                } else if (mods == (SystemUtils.IS_OS_WINDOWS ? 4 : class_310.field_1703 ? 8 : 2)) {
                    this.cursor = this.text.length();
                    resetSelection();
                } else if (altShift) {
                    if (this.cursor == this.selectionStart && this.cursor != this.selectionEnd) {
                        this.cursor += countToNextSpace(false);
                        this.selectionStart = this.cursor;
                    } else {
                        this.cursor += countToNextSpace(false);
                        this.selectionEnd = this.cursor;
                    }
                } else if (controlShift) {
                    if (this.cursor == this.selectionStart && this.cursor != this.selectionEnd) {
                        this.selectionStart = this.selectionEnd;
                    }
                    this.cursor = this.text.length();
                    this.selectionEnd = this.cursor;
                } else if (shift) {
                    if (this.cursor == this.selectionStart && this.cursor != this.selectionEnd) {
                        this.selectionStart = this.cursor + 1;
                    } else {
                        this.selectionEnd = this.cursor + 1;
                    }
                    this.cursor++;
                } else {
                    if (this.cursor == this.selectionStart && this.cursor != this.selectionEnd) {
                        this.cursor = this.selectionEnd;
                    } else {
                        this.cursor++;
                    }
                    resetSelection();
                }
                cursorChanged();
                return true;
            }
            if (this.selectionStart != this.selectionEnd && this.selectionEnd == this.text.length() && mods == 0) {
                this.cursor = this.text.length();
                resetSelection();
                cursorChanged();
                return true;
            }
            return true;
        }
        if (key == 264 && this.completionsW != null) {
            int currentI = getSelectedCompletion();
            if (currentI == Math.min(5, this.completions.size() - 1)) {
                if (this.completionsStart + 6 < this.completions.size()) {
                    this.completionsStart++;
                    createCompletions(this.completionsStart + currentI);
                    return true;
                }
                return true;
            }
            ((ICompletionItem) this.completionsW.cells.get(currentI).widget()).setSelected(false);
            ((ICompletionItem) this.completionsW.cells.get(currentI + 1).widget()).setSelected(true);
            return true;
        }
        if (key == 265 && this.completionsW != null) {
            int currentI2 = getSelectedCompletion();
            if (currentI2 == 0) {
                if (this.completionsStart > 0) {
                    this.completionsStart--;
                    createCompletions(this.completionsStart + currentI2);
                    return true;
                }
                return true;
            }
            ((ICompletionItem) this.completionsW.cells.get(currentI2).widget()).setSelected(false);
            ((ICompletionItem) this.completionsW.cells.get(currentI2 - 1).widget()).setSelected(true);
            return true;
        }
        return false;
    }

    private int getSelectedCompletion() {
        for (int i = 0; i < this.completionsW.cells.size(); i++) {
            ICompletionItem item = (ICompletionItem) this.completionsW.cells.get(i).widget();
            if (item.isSelected()) {
                return i;
            }
        }
        return -1;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean onCharTyped(char c) {
        if (this.focused && this.filter.filter(this.text, c)) {
            clearSelection();
            this.text = this.text.substring(0, this.cursor) + c + this.text.substring(this.cursor);
            this.cursor++;
            resetSelection();
            runAction();
            return true;
        }
        return false;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (isFocused()) {
            GuiKeyEvents.canUseKeys = false;
        }
        if (this.completionsW != null && this.focused) {
            renderer.absolutePost(() -> {
                renderer.beginRender();
                this.completionsW.render(renderer, mouseX, mouseY, delta);
                renderer.endRender();
            });
        }
        return super.render(renderer, mouseX, mouseY, delta);
    }

    private void clearSelection() {
        if (this.selectionStart == this.selectionEnd) {
            return;
        }
        String preText = this.text;
        this.text = this.text.substring(0, this.selectionStart) + this.text.substring(this.selectionEnd);
        this.cursor = this.selectionStart;
        this.selectionEnd = this.cursor;
        if (!this.text.equals(preText)) {
            runAction();
        }
    }

    private void resetSelection() {
        this.selectionStart = this.cursor;
        this.selectionEnd = this.cursor;
    }

    /* JADX WARN: Removed duplicated region for block: B:10:0x0022  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private int countToNextSpace(boolean r4) {
        /*
            r3 = this;
            r0 = 0
            r5 = r0
            r0 = 0
            r6 = r0
            r0 = r3
            int r0 = r0.cursor
            r7 = r0
        La:
            r0 = r4
            if (r0 == 0) goto L16
            r0 = r7
            if (r0 < 0) goto L7f
            goto L22
        L16:
            r0 = r7
            r1 = r3
            java.lang.String r1 = r1.text
            int r1 = r1.length()
            if (r0 >= r1) goto L7f
        L22:
            r0 = r7
            r8 = r0
            r0 = r4
            if (r0 == 0) goto L2d
            int r8 = r8 + (-1)
        L2d:
            r0 = r8
            r1 = r3
            java.lang.String r1 = r1.text
            int r1 = r1.length()
            if (r0 < r1) goto L3c
            goto L6e
        L3c:
            r0 = r8
            if (r0 >= 0) goto L44
            goto L7f
        L44:
            r0 = r6
            if (r0 == 0) goto L5a
            r0 = r3
            java.lang.String r0 = r0.text
            r1 = r8
            char r0 = r0.charAt(r1)
            boolean r0 = java.lang.Character.isWhitespace(r0)
            if (r0 == 0) goto L5a
            goto L7f
        L5a:
            r0 = r3
            java.lang.String r0 = r0.text
            r1 = r8
            char r0 = r0.charAt(r1)
            boolean r0 = java.lang.Character.isWhitespace(r0)
            if (r0 != 0) goto L6b
            r0 = 1
            r6 = r0
        L6b:
            int r5 = r5 + 1
        L6e:
            r0 = r7
            r1 = r4
            if (r1 == 0) goto L78
            r1 = -1
            goto L79
        L78:
            r1 = 1
        L79:
            int r0 = r0 + r1
            r7 = r0
            goto La
        L7f:
            r0 = r5
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: meteordevelopment.meteorclient.gui.widgets.input.WTextBox.countToNextSpace(boolean):int");
    }

    private void calculateTextWidths() {
        this.textWidths.clear();
        for (int i = 0; i <= this.text.length(); i++) {
            this.textWidths.add(this.theme.textWidth(this.text, i, false));
        }
    }

    private void runAction() {
        calculateTextWidths();
        cursorChanged();
        if (this.action != null) {
            this.action.run();
        }
    }

    private double textWidth() {
        if (this.textWidths.isEmpty()) {
            return 0.0d;
        }
        return this.textWidths.getDouble(this.textWidths.size() - 1);
    }

    private void cursorChanged() {
        double cursor = getCursorTextWidth(-2);
        if (cursor < this.textStart) {
            this.textStart -= this.textStart - cursor;
        }
        double cursor2 = getCursorTextWidth(2);
        if (cursor2 > this.textStart + maxTextWidth()) {
            this.textStart += cursor2 - (this.textStart + maxTextWidth());
        }
        this.textStart = class_3532.method_15350(this.textStart, 0.0d, Math.max(textWidth() - maxTextWidth(), 0.0d));
        onCursorChanged();
        this.completions = this.renderer.getCompletions(this.text, this.cursor);
        this.completionsStart = 0;
        this.completionsW = null;
        if (this.completions == null || this.completions.isEmpty()) {
            return;
        }
        createCompletions(0);
    }

    protected void onCursorChanged() {
    }

    private void createCompletions(int selected) {
        this.completionsW = createCompletionsRootWidget();
        this.completionsW.theme = this.theme;
        int max = Math.min(this.completions.size(), this.completionsStart + 6);
        int i = this.completionsStart;
        while (i < max) {
            WWidget widget = createCompletionsValueWidth(this.completions.get(i), i == selected);
            widget.theme = this.theme;
            Cell<?> cell = this.completionsW.add(widget).expandX().padHorizontal(4.0d);
            if (i == max - 1) {
                cell.padBottom(4.0d);
            }
            i++;
        }
        this.completionsW.calculateSize();
        this.completionsW.x = Math.min(Math.max(((this.x - (pad() * 2.0d)) + getTextWidth(this.cursor)) - getOverflowWidthForRender(), this.x), (this.x + this.width) - this.completionsW.width);
        this.completionsW.y = this.y + this.height;
        this.completionsW.calculateWidgetPositions();
    }

    protected double getTextWidth(int pos) {
        if (this.textWidths.isEmpty()) {
            return 0.0d;
        }
        if (pos < 0) {
            pos = 0;
        } else if (pos >= this.textWidths.size()) {
            pos = this.textWidths.size() - 1;
        }
        return this.textWidths.getDouble(pos);
    }

    protected double getCursorTextWidth(int offset) {
        return getTextWidth(this.cursor + offset);
    }

    protected double getOverflowWidthForRender() {
        return this.textStart;
    }

    public String get() {
        return this.text;
    }

    public void set(String text) {
        this.text = text;
        this.cursor = class_3532.method_15340(this.cursor, 0, text.length());
        this.selectionStart = this.cursor;
        this.selectionEnd = this.cursor;
        calculateTextWidths();
        cursorChanged();
    }

    public boolean isFocused() {
        return this.focused;
    }

    public void setFocused(boolean focused) {
        if (this.focused && !focused && this.actionOnUnfocused != null) {
            this.actionOnUnfocused.run();
        }
        boolean wasJustFocused = focused && !this.focused;
        this.focused = focused;
        resetSelection();
        if (wasJustFocused) {
            onCursorChanged();
        }
    }

    public void setCursorMax() {
        this.cursor = this.text.length();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/input/WTextBox$Renderer.class */
    public interface Renderer {
        void render(GuiRenderer guiRenderer, double d, double d2, String str, Color color);

        default List<String> getCompletions(String text, int position) {
            return null;
        }
    }
}
