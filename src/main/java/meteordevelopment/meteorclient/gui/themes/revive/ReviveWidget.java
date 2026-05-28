package meteordevelopment.meteorclient.gui.themes.revive;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.BaseWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.render.color.Color;

public interface ReviveWidget extends BaseWidget {

    default ReviveGuiTheme theme() {
        return (ReviveGuiTheme) getTheme();
    }

    /**
     * Renders the standard Revive background: filled inner rect + thin outline border.
     * Uses a 1px border (scale(1)) for a sleeker look than the default 2px Meteor border.
     */
    default void renderBackground(GuiRenderer renderer, WWidget widget, boolean pressed, boolean mouseOver) {
        ReviveGuiTheme theme = theme();
        double s = theme.scale(1);

        // Inner fill
        renderer.quad(
            widget.x + s, widget.y + s,
            widget.width - s * 2, widget.height - s * 2,
            theme.backgroundColor.get(pressed, mouseOver)
        );

        // Outline — top, bottom, left, right
        Color outline = theme.outlineColor.get(pressed, mouseOver);
        renderer.quad(widget.x, widget.y,                             widget.width, s,                    outline);
        renderer.quad(widget.x, widget.y + widget.height - s,         widget.width, s,                    outline);
        renderer.quad(widget.x, widget.y + s,                         s, widget.height - s * 2,           outline);
        renderer.quad(widget.x + widget.width - s, widget.y + s,      s, widget.height - s * 2,           outline);
    }
}
