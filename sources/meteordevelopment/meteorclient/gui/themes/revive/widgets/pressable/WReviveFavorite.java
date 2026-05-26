package meteordevelopment.meteorclient.gui.themes.revive.widgets.pressable;

import meteordevelopment.meteorclient.gui.themes.revive.ReviveWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/revive/widgets/pressable/WReviveFavorite.class */
public class WReviveFavorite extends WFavorite implements ReviveWidget {
    public WReviveFavorite(boolean checked) {
        super(checked);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite
    protected Color getColor() {
        return theme().favoriteColor.get();
    }
}
