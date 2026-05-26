package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WAccount;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/meteor/widgets/WMeteorAccount.class */
public class WMeteorAccount extends WAccount implements MeteorWidget {
    public WMeteorAccount(WidgetScreen screen, Account<?> account) {
        super(screen, account);
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WAccount
    protected Color loggedInColor() {
        return theme().loggedInColor.get();
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WAccount
    protected Color accountTypeColor() {
        return theme().textSecondaryColor.get();
    }
}
