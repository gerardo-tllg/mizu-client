package meteordevelopment.meteorclient.gui.screens.accounts;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/accounts/AddAccountScreen.class */
public abstract class AddAccountScreen extends WindowScreen {
    public final AccountsScreen parent;
    public WButton add;
    private int timer;

    protected AddAccountScreen(GuiTheme theme, String title, AccountsScreen parent) {
        super(theme, title);
        this.parent = parent;
    }

    public void method_25393() {
        if (this.locked) {
            if (this.timer > 2) {
                this.add.set(getNext(this.add));
                this.timer = 0;
                return;
            } else {
                this.timer++;
                return;
            }
        }
        if (!this.add.getText().equals("Add")) {
            this.add.set("Add");
        }
    }

    private String getNext(WButton add) {
        switch (add.getText()) {
            case "Add":
            case "oo0":
                return "ooo";
            case "ooo":
                return "0oo";
            case "0oo":
                return "o0o";
            case "o0o":
                return "oo0";
            default:
                return "Add";
        }
    }
}
