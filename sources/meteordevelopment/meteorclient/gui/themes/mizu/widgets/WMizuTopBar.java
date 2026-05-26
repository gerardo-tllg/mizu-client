package meteordevelopment.meteorclient.gui.themes.mizu.widgets;

import meteordevelopment.meteorclient.gui.themes.mizu.MizuWidget;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/WMizuTopBar.class */
public class WMizuTopBar extends WTopBar implements MizuWidget {
    @Override // meteordevelopment.meteorclient.gui.widgets.WTopBar
    protected Color getButtonColor(boolean pressed, boolean hovered) {
        return theme().backgroundColor.get(pressed, hovered);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WTopBar
    protected Color getNameColor() {
        return theme().accentColor.get();
    }
}


