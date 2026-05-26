package meteordevelopment.meteorclient.gui.themes.meteor.widgets.pressable;

import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/meteor/widgets/pressable/WMeteorFavorite.class */
public class WMeteorFavorite extends WFavorite implements MeteorWidget {
    public WMeteorFavorite(boolean checked) {
        super(checked);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite
    protected Color getColor() {
        return theme().favoriteColor.get();
    }
}
