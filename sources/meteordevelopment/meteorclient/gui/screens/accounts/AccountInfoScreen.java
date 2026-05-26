package meteordevelopment.meteorclient.gui.screens.accounts;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.TokenAccount;
import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/accounts/AccountInfoScreen.class */
public class AccountInfoScreen extends WindowScreen {
    private final Account<?> account;

    public AccountInfoScreen(GuiTheme theme, Account<?> account) {
        super(theme, account.getUsername() + " details");
        this.account = account;
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        TokenAccount e = (TokenAccount) this.account;
        WHorizontalList l = (WHorizontalList) add(this.theme.horizontalList()).expandX().widget();
        WButton copy = this.theme.button("Copy");
        copy.action = () -> {
            MeteorClient.mc.field_1774.method_1455(e.getToken());
        };
        l.add(this.theme.label("TheAltening token:"));
        l.add(this.theme.label(e.getToken()).color(Color.GRAY)).pad(5.0d);
        l.add(copy);
    }
}
