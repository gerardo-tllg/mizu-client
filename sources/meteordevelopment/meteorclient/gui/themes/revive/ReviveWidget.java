package meteordevelopment.meteorclient.gui.themes.revive;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.BaseWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/revive/ReviveWidget.class */
public interface ReviveWidget extends BaseWidget {
    default ReviveGuiTheme theme() {
        return (ReviveGuiTheme) getTheme();
    }

    default void renderBackground(GuiRenderer renderer, WWidget widget, boolean pressed, boolean mouseOver) {
        ReviveGuiTheme theme = theme();
        double s = theme.scale(1.0d);
        renderer.quad(widget.x + s, widget.y + s, widget.width - (s * 2.0d), widget.height - (s * 2.0d), theme.backgroundColor.get(pressed, mouseOver));
        Color outline = theme.outlineColor.get(pressed, mouseOver);
        renderer.quad(widget.x, widget.y, widget.width, s, outline);
        renderer.quad(widget.x, (widget.y + widget.height) - s, widget.width, s, outline);
        renderer.quad(widget.x, widget.y + s, s, widget.height - (s * 2.0d), outline);
        renderer.quad((widget.x + widget.width) - s, widget.y + s, s, widget.height - (s * 2.0d), outline);
    }
}
