package meteordevelopment.meteorclient.gui.themes.revive.widgets.pressable;

import meteordevelopment.meteorclient.gui.themes.revive.ReviveWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WReviveFavorite extends WFavorite implements ReviveWidget {
    public WReviveFavorite(boolean checked) { super(checked); }

    @Override
    protected Color getColor() { return theme().favoriteColor.get(); }
}
