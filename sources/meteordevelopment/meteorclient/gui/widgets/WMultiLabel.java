package meteordevelopment.meteorclient.gui.widgets;

import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/WMultiLabel.class */
public abstract class WMultiLabel extends WLabel {
    protected List<String> lines;
    protected double maxWidth;

    public WMultiLabel(String text, boolean title, double maxWidth) {
        super(text, title);
        this.lines = new ArrayList(2);
        this.maxWidth = maxWidth;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WLabel, meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        this.lines.clear();
        String[] textLines = this.text.split("\n");
        StringBuilder sb = new StringBuilder();
        double spaceWidth = this.theme.textWidth(" ", 1, this.title);
        double maxWidth = this.theme.scale(this.maxWidth);
        double lineWidth = 0.0d;
        double maxLineWidth = 0.0d;
        int iInLine = 0;
        for (String line : textLines) {
            for (String word : line.split(" ")) {
                double wordWidth = this.theme.textWidth(word, word.length(), this.title);
                double toAdd = wordWidth;
                if (iInLine > 0) {
                    toAdd += spaceWidth;
                }
                if (lineWidth + toAdd > maxWidth) {
                    this.lines.add(sb.toString());
                    sb.setLength(0);
                    sb.append(word);
                    lineWidth = wordWidth;
                    iInLine = 1;
                } else {
                    if (iInLine > 0) {
                        sb.append(' ');
                        lineWidth += spaceWidth;
                    }
                    sb.append(word);
                    lineWidth += wordWidth;
                    iInLine++;
                }
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
            }
            this.lines.add(sb.toString());
            sb.setLength(0);
            lineWidth = 0.0d;
            iInLine = 0;
        }
        if (!sb.isEmpty()) {
            this.lines.add(sb.toString());
        }
        this.width = maxLineWidth;
        this.height = this.theme.textHeight(this.title) * ((double) this.lines.size());
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WLabel
    public void set(String text) {
        if (!text.equals(this.text)) {
            invalidate();
        }
        this.text = text;
    }
}
