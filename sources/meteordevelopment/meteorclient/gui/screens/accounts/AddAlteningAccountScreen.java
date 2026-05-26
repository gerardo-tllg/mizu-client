package meteordevelopment.meteorclient.gui.screens.accounts;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.accounts.types.TheAlteningAccount;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/accounts/AddAlteningAccountScreen.class */
public class AddAlteningAccountScreen extends AddAccountScreen {
    public AddAlteningAccountScreen(GuiTheme theme, AccountsScreen parent) {
        super(theme, "Add The Altening Account", parent);
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        WTable t = (WTable) add(this.theme.table()).widget();
        t.add(this.theme.label("Token: "));
        WTextBox token = (WTextBox) t.add(this.theme.textBox("")).minWidth(400.0d).expandX().widget();
        token.setFocused(true);
        t.row();
        this.add = (WButton) t.add(this.theme.button("Add")).expandX().widget();
        this.add.action = () -> {
            if (!token.get().isEmpty()) {
                AccountsScreen.addAccount(this, this.parent, new TheAlteningAccount(token.get()));
            }
        };
        this.enterAction = this.add.action;
    }
}
