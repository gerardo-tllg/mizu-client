package meteordevelopment.meteorclient.systems.accounts.types;

import java.util.Optional;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.AccountType;
import net.minecraft.class_320;
import net.minecraft.class_4844;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/types/CrackedAccount.class */
public class CrackedAccount extends Account<CrackedAccount> {
    public CrackedAccount(String name) {
        super(AccountType.Cracked, name);
    }

    @Override // meteordevelopment.meteorclient.systems.accounts.Account
    public boolean fetchInfo() {
        this.cache.username = this.name;
        return true;
    }

    @Override // meteordevelopment.meteorclient.systems.accounts.Account
    public boolean login() {
        super.login();
        this.cache.loadHead();
        setSession(new class_320(this.name, class_4844.method_43344(this.name), "", Optional.empty(), Optional.empty(), class_320.class_321.field_1988));
        return true;
    }

    public boolean equals(Object o) {
        if (o instanceof CrackedAccount) {
            return ((CrackedAccount) o).getUsername().equals(getUsername());
        }
        return false;
    }
}
