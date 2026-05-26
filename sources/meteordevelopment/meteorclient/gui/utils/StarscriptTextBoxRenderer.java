package meteordevelopment.meteorclient.gui.utils;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/utils/StarscriptTextBoxRenderer.class */
public class StarscriptTextBoxRenderer implements WTextBox.Renderer {
    private static final String[] KEYWORDS = {"null", "true", "false", "and", "or"};
    private static final Color RED = new Color(225, 25, 25);
    private String lastText;
    private final List<Section> sections = new ArrayList();

    @Override // meteordevelopment.meteorclient.gui.widgets.input.WTextBox.Renderer
    public void render(GuiRenderer renderer, double x, double y, String text, Color color) {
        if (this.lastText == null || !this.lastText.equals(text)) {
            generate(renderer.theme, text, color);
        }
        for (Section section : this.sections) {
            renderer.text(section.text, x, y, section.color, false);
            x += renderer.theme.textWidth(section.text);
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.input.WTextBox.Renderer
    public List<String> getCompletions(String text, int position) {
        List<String> completions = new ArrayList<>();
        MeteorStarscript.ss.getCompletions(text, position, (completion, function) -> {
            completions.add(function ? completion + "(" : completion);
        });
        completions.sort((v0, v1) -> {
            return v0.compareToIgnoreCase(v1);
        });
        return completions;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:103:0x0461  */
    /* JADX WARN: Removed duplicated region for block: B:116:0x04ed  */
    /* JADX WARN: Removed duplicated region for block: B:124:0x052d  */
    /* JADX WARN: Removed duplicated region for block: B:150:0x0482 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:19:0x00d4 A[LOOP:1: B:19:0x00d4->B:23:0x00f2, LOOP_START, PHI: r14
  0x00d4: PHI (r14v17 'i' int) = (r14v1 'i' int), (r14v18 'i' int) binds: [B:18:0x00b2, B:23:0x00f2] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Removed duplicated region for block: B:26:0x0108  */
    /* JADX WARN: Removed duplicated region for block: B:27:0x0130  */
    /* JADX WARN: Removed duplicated region for block: B:38:0x0163  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x01a0  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void generate(meteordevelopment.meteorclient.gui.GuiTheme r7, java.lang.String r8, meteordevelopment.meteorclient.utils.render.color.Color r9) {
        /*
            Method dump skipped, instruction units count: 1390
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer.generate(meteordevelopment.meteorclient.gui.GuiTheme, java.lang.String, meteordevelopment.meteorclient.utils.render.color.Color):void");
    }

    private boolean isKeyword(String text, int i, String keyword) {
        if (i > 0 && isWordChar(text.charAt(i - 1))) {
            return false;
        }
        if (text.length() <= i + keyword.length() || !isWordChar(text.charAt(i + keyword.length()))) {
            return text.startsWith(keyword, i);
        }
        return false;
    }

    private boolean isWordChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/utils/StarscriptTextBoxRenderer$Section.class */
    private static final class Section extends Record {
        private final String text;
        private final Color color;

        private Section(String text, Color color) {
            this.text = text;
            this.color = color;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, Section.class), Section.class, "text;color", "FIELD:Lmeteordevelopment/meteorclient/gui/utils/StarscriptTextBoxRenderer$Section;->text:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/gui/utils/StarscriptTextBoxRenderer$Section;->color:Lmeteordevelopment/meteorclient/utils/render/color/Color;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, Section.class), Section.class, "text;color", "FIELD:Lmeteordevelopment/meteorclient/gui/utils/StarscriptTextBoxRenderer$Section;->text:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/gui/utils/StarscriptTextBoxRenderer$Section;->color:Lmeteordevelopment/meteorclient/utils/render/color/Color;").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, Section.class, Object.class), Section.class, "text;color", "FIELD:Lmeteordevelopment/meteorclient/gui/utils/StarscriptTextBoxRenderer$Section;->text:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/gui/utils/StarscriptTextBoxRenderer$Section;->color:Lmeteordevelopment/meteorclient/utils/render/color/Color;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public String text() {
            return this.text;
        }

        public Color color() {
            return this.color;
        }
    }
}
