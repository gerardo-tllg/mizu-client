package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/meteor/widgets/WMeteorTopBar.class */
public class WMeteorTopBar extends WTopBar implements MeteorWidget {
    @Override // meteordevelopment.meteorclient.gui.widgets.WTopBar
    protected Color getButtonColor(boolean pressed, boolean hovered) {
        return theme().backgroundColor.get(pressed, hovered);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WTopBar
    protected Color getNameColor() {
        return theme().textColor.get();
    }
}
