package meteordevelopment.meteorclient.gui.widgets.containers;

import meteordevelopment.meteorclient.gui.utils.Cell;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/containers/WHorizontalList.class */
public class WHorizontalList extends WContainer {
    public double spacing = 3.0d;
    protected double calculatedWidth;
    protected int fillXCount;

    protected double spacing() {
        return this.theme.scale(this.spacing);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        this.width = 0.0d;
        this.height = 0.0d;
        this.fillXCount = 0;
        for (int i = 0; i < this.cells.size(); i++) {
            Cell<?> cell = this.cells.get(i);
            if (i > 0) {
                this.width += spacing();
            }
            this.width += cell.padLeft() + cell.widget().width + cell.padRight();
            this.height = Math.max(this.height, cell.padTop() + cell.widget().height + cell.padBottom());
            if (cell.expandCellX) {
                this.fillXCount++;
            }
        }
        this.calculatedWidth = this.width;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateWidgetPositions() {
        double x = this.x;
        double fillXWidth = (this.width - this.calculatedWidth) / ((double) this.fillXCount);
        for (int i = 0; i < this.cells.size(); i++) {
            Cell<?> cell = this.cells.get(i);
            if (i > 0) {
                x += spacing();
            }
            double x2 = x + cell.padLeft();
            cell.x = x2;
            cell.y = this.y + cell.padTop();
            cell.width = cell.widget().width;
            cell.height = (this.height - cell.padTop()) - cell.padTop();
            if (cell.expandCellX) {
                cell.width += fillXWidth;
            }
            cell.alignWidget();
            x = x2 + cell.width + cell.padRight();
        }
    }
}
