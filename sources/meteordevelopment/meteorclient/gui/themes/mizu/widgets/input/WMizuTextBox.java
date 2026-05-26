package meteordevelopment.meteorclient.gui.themes.mizu.widgets.input;

import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.mizu.MizuGuiTheme;
import meteordevelopment.meteorclient.gui.themes.mizu.MizuWidget;
import meteordevelopment.meteorclient.gui.themes.mizu.widgets.WMizuLabel;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/input/WMizuTextBox.class */
public class WMizuTextBox extends WTextBox implements MizuWidget {
    private boolean cursorVisible;
    private double cursorTimer;
    private double animProgress;

    public WMizuTextBox(String text, String placeholder, CharFilter filter, Class<? extends WTextBox.Renderer> renderer) {
        super(text, placeholder, filter, renderer);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.input.WTextBox
    protected WContainer createCompletionsRootWidget() {
        return new WVerticalList() { // from class: meteordevelopment.meteorclient.gui.themes.mizu.widgets.input.WMizuTextBox.1
            @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
            protected void onRender(GuiRenderer renderer1, double mouseX, double mouseY, double delta) {
                MizuGuiTheme theme1 = WMizuTextBox.this.theme();
                double s = theme1.scale(1.0d);
                Color c = theme1.outlineColor.get();
                Color col = theme1.backgroundColor.get();
                int preA = col.a;
                col.a += col.a / 2;
                col.validate();
                renderer1.quad(this, col);
                col.a = preA;
                renderer1.quad(this.x, (this.y + this.height) - s, this.width, s, c);
                renderer1.quad(this.x, this.y, s, this.height - s, c);
                renderer1.quad((this.x + this.width) - s, this.y, s, this.height - s, c);
            }
        };
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.input.WTextBox
    protected <T extends WWidget & WTextBox.ICompletionItem> T createCompletionsValueWidth(String completion, boolean selected) {
        return new CompletionItem(completion, false, selected);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/input/WMizuTextBox$CompletionItem.class */
    private static class CompletionItem extends WMizuLabel implements WTextBox.ICompletionItem {
        private static final Color SELECTED_COLOR = new Color(0, Opcode.IXOR, 255, 25);
        private boolean selected;

        public CompletionItem(String text, boolean title, boolean selected) {
            super(text, title);
            this.selected = selected;
        }

        @Override // meteordevelopment.meteorclient.gui.themes.mizu.widgets.WMizuLabel, meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            super.onRender(renderer, mouseX, mouseY, delta);
            if (this.selected) {
                renderer.quad(this, SELECTED_COLOR);
            }
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.input.WTextBox.ICompletionItem
        public boolean isSelected() {
            return this.selected;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.input.WTextBox.ICompletionItem
        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.input.WTextBox.ICompletionItem
        public String getCompletion() {
            return this.text;
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.input.WTextBox
    protected void onCursorChanged() {
        this.cursorVisible = true;
        this.cursorTimer = 0.0d;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (this.cursorTimer >= 1.0d) {
            this.cursorVisible = !this.cursorVisible;
            this.cursorTimer = 0.0d;
        } else {
            this.cursorTimer += delta * 1.75d;
        }
        renderBackground(renderer, this, false, false);
        MizuGuiTheme theme = theme();
        double pad = pad();
        double overflowWidth = getOverflowWidthForRender();
        renderer.scissorStart(this.x + pad, this.y + pad, this.width - (pad * 2.0d), this.height - (pad * 2.0d));
        if (!this.text.isEmpty()) {
            this.renderer.render(renderer, (this.x + pad) - overflowWidth, this.y + pad, this.text, theme.textColor.get());
        } else if (this.placeholder != null) {
            this.renderer.render(renderer, (this.x + pad) - overflowWidth, this.y + pad, this.placeholder, theme.placeholderColor.get());
        }
        if (this.focused && (this.cursor != this.selectionStart || this.cursor != this.selectionEnd)) {
            double selStart = ((this.x + pad) + getTextWidth(this.selectionStart)) - overflowWidth;
            double selEnd = ((this.x + pad) + getTextWidth(this.selectionEnd)) - overflowWidth;
            renderer.quad(selStart, this.y + pad, selEnd - selStart, theme.textHeight(), theme.textHighlightColor.get());
        }
        this.animProgress += delta * 10.0d * ((double) ((this.focused && this.cursorVisible) ? 1 : -1));
        this.animProgress = class_3532.method_15350(this.animProgress, 0.0d, 1.0d);
        if ((this.focused && this.cursorVisible) || this.animProgress > 0.0d) {
            renderer.setAlpha(this.animProgress);
            renderer.quad(((this.x + pad) + getTextWidth(this.cursor)) - overflowWidth, this.y + pad, theme.scale(1.0d), theme.textHeight(), theme.accentColor.get());
            renderer.setAlpha(1.0d);
        }
        renderer.scissorEnd();
    }
}


