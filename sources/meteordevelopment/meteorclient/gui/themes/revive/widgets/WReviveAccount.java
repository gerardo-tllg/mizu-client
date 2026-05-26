package meteordevelopment.meteorclient.gui.themes.revive.widgets;

import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.themes.revive.ReviveWidget;
import meteordevelopment.meteorclient.gui.widgets.WAccount;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/themes/revive/widgets/WReviveAccount.class */
public class WReviveAccount extends WAccount implements ReviveWidget {
    public WReviveAccount(WidgetScreen screen, Account<?> account) {
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
