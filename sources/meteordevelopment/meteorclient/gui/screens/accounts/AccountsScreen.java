package meteordevelopment.meteorclient.gui.screens.accounts;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WAccount;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.Accounts;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/accounts/AccountsScreen.class */
public class AccountsScreen extends WindowScreen {
    public AccountsScreen(GuiTheme theme) {
        super(theme, "Accounts");
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        for (Account<?> account : Accounts.get()) {
            WAccount wAccount = (WAccount) add(this.theme.account(this, account)).expandX().widget();
            wAccount.refreshScreenAction = this::reload;
        }
        WHorizontalList l = (WHorizontalList) add(this.theme.horizontalList()).expandX().widget();
        addButton(l, "Cracked", () -> {
            MeteorClient.mc.method_1507(new AddCrackedAccountScreen(this.theme, this));
        });
        addButton(l, "Altening", () -> {
            MeteorClient.mc.method_1507(new AddAlteningAccountScreen(this.theme, this));
        });
        addButton(l, "Microsoft", () -> {
            MeteorClient.mc.method_1507(new AddMicrosoftAccountScreen(this.theme, this));
        });
    }

    private void addButton(WContainer c, String text, Runnable action) {
        WButton button = (WButton) c.add(this.theme.button(text)).expandX().widget();
        button.action = action;
    }

    public static void addAccount(@Nullable AddAccountScreen screen, AccountsScreen parent, Account<?> account) {
        if (screen != null) {
            screen.locked = true;
        }
        MeteorExecutor.execute(() -> {
            if (account.fetchInfo()) {
                account.getCache().loadHead();
                Accounts.get().add(account);
                if (account.login()) {
                    Accounts.get().save();
                }
                if (screen != null) {
                    screen.locked = false;
                    screen.method_25419();
                }
                parent.reload();
                return;
            }
            if (screen != null) {
                screen.locked = false;
            }
        });
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public boolean toClipboard() {
        return NbtUtils.toClipboard(Accounts.get());
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public boolean fromClipboard() {
        return NbtUtils.fromClipboard(Accounts.get());
    }
}
