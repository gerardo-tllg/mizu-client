package meteordevelopment.meteorclient.gui.themes.meteor;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.BaseWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/meteor/MeteorWidget.class */
public interface MeteorWidget extends BaseWidget {
    default MeteorGuiTheme theme() {
        return (MeteorGuiTheme) getTheme();
    }

    default void renderBackground(GuiRenderer renderer, WWidget widget, boolean pressed, boolean mouseOver) {
        MeteorGuiTheme theme = theme();
        double s = theme.scale(2.0d);
        renderer.quad(widget.x + s, widget.y + s, widget.width - (s * 2.0d), widget.height - (s * 2.0d), theme.backgroundColor.get(pressed, mouseOver));
        Color outlineColor = theme.outlineColor.get(pressed, mouseOver);
        renderer.quad(widget.x, widget.y, widget.width, s, outlineColor);
        renderer.quad(widget.x, (widget.y + widget.height) - s, widget.width, s, outlineColor);
        renderer.quad(widget.x, widget.y + s, s, widget.height - (s * 2.0d), outlineColor);
        renderer.quad((widget.x + widget.width) - s, widget.y + s, s, widget.height - (s * 2.0d), outlineColor);
    }
}
