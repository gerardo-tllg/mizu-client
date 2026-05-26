package meteordevelopment.meteorclient.gui.themes.revive.widgets;

import meteordevelopment.meteorclient.gui.themes.revive.ReviveWidget;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/revive/widgets/WReviveTopBar.class */
public class WReviveTopBar extends WTopBar implements ReviveWidget {
    @Override // meteordevelopment.meteorclient.gui.widgets.WTopBar
    protected Color getButtonColor(boolean pressed, boolean hovered) {
        return theme().backgroundColor.get(pressed, hovered);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WTopBar
    protected Color getNameColor() {
        return theme().accentColor.get();
    }
}
