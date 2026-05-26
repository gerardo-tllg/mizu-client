package meteordevelopment.meteorclient.gui.widgets.containers;

import meteordevelopment.meteorclient.gui.utils.Cell;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/containers/WVerticalList.class */
public class WVerticalList extends WContainer {
    public double spacing = 3.0d;
    protected double widthRemove;

    protected double spacing() {
        return this.theme.scale(this.spacing);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateSize() {
        this.width = 0.0d;
        this.height = 0.0d;
        for (int i = 0; i < this.cells.size(); i++) {
            Cell<?> cell = this.cells.get(i);
            if (i > 0) {
                this.height += spacing();
            }
            this.width = Math.max(this.width, cell.padLeft() + cell.widget().width + cell.padRight());
            this.height += cell.padTop() + cell.widget().height + cell.padBottom();
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
    protected void onCalculateWidgetPositions() {
        double y = this.y;
        for (int i = 0; i < this.cells.size(); i++) {
            Cell<?> cell = this.cells.get(i);
            if (i > 0) {
                y += spacing();
            }
            double y2 = y + cell.padTop();
            cell.x = this.x + cell.padLeft();
            cell.y = y2;
            cell.width = ((this.width - this.widthRemove) - cell.padLeft()) - cell.padRight();
            cell.height = cell.widget().height;
            cell.alignWidget();
            y = y2 + cell.height + cell.padBottom();
        }
    }
}
