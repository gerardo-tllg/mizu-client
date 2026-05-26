package meteordevelopment.meteorclient.gui.screens.accounts;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.accounts.Accounts;
import meteordevelopment.meteorclient.systems.accounts.types.CrackedAccount;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/accounts/AddCrackedAccountScreen.class */
public class AddCrackedAccountScreen extends AddAccountScreen {
    public AddCrackedAccountScreen(GuiTheme theme, AccountsScreen parent) {
        super(theme, "Add Cracked Account", parent);
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        WTable t = (WTable) add(this.theme.table()).widget();
        t.add(this.theme.label("Name: "));
        WTextBox name = (WTextBox) t.add(this.theme.textBox("", "MizuPlayer", (text, c) -> {
            return c != ' ';
        })).minWidth(400.0d).expandX().widget();
        name.setFocused(true);
        t.row();
        this.add = (WButton) t.add(this.theme.button("Add")).expandX().widget();
        this.add.action = () -> {
            if (!name.get().isEmpty() && name.get().length() < 17) {
                CrackedAccount account = new CrackedAccount(name.get());
                if (!Accounts.get().exists(account)) {
                    AccountsScreen.addAccount(this, this.parent, account);
                }
            }
        };
        this.enterAction = this.add.action;
    }
}
