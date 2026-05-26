package meteordevelopment.meteorclient.gui.themes.mizu.widgets.pressable;

import meteordevelopment.meteorclient.gui.themes.mizu.MizuWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/mizu/widgets/pressable/WMizuFavorite.class */
public class WMizuFavorite extends WFavorite implements MizuWidget {
    public WMizuFavorite(boolean checked) {
        super(checked);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite
    protected Color getColor() {
        return theme().favoriteColor.get();
    }
}


