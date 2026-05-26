package meteordevelopment.meteorclient.systems.accounts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.accounts.types.CrackedAccount;
import meteordevelopment.meteorclient.systems.accounts.types.MicrosoftAccount;
import meteordevelopment.meteorclient.systems.accounts.types.TheAlteningAccount;
import meteordevelopment.meteorclient.utils.misc.NbtException;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.class_2487;
import org.jetbrains.annotations.NotNull;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/Accounts.class */
public class Accounts extends System<Accounts> implements Iterable<Account<?>> {
    private List<Account<?>> accounts;

    public Accounts() {
        super("accounts");
        this.accounts = new ArrayList();
    }

    public static Accounts get() {
        return (Accounts) Systems.get(Accounts.class);
    }

    public void add(Account<?> account) {
        this.accounts.add(account);
        save();
    }

    public boolean exists(Account<?> account) {
        return this.accounts.contains(account);
    }

    public void remove(Account<?> account) {
        if (this.accounts.remove(account)) {
            save();
        }
    }

    public int size() {
        return this.accounts.size();
    }

    @Override // java.lang.Iterable
    @NotNull
    public Iterator<Account<?>> iterator() {
        return this.accounts.iterator();
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10566("accounts", NbtUtils.listToTag(this.accounts));
        return tag;
    }

    @Override // meteordevelopment.meteorclient.systems.System, meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public Accounts fromTag(class_2487 tag) {
        MeteorExecutor.execute(() -> {
            this.accounts = NbtUtils.listFromTag(tag.method_68569("accounts"), tag1 -> {
                class_2487 t = (class_2487) tag1;
                if (!t.method_10545("type")) {
                    return null;
                }
                AccountType type = AccountType.valueOf(t.method_68564("type", ""));
                try {
                    switch (type) {
                        case Cracked:
                            return new CrackedAccount(null).fromTag(t);
                        case Microsoft:
                            return new MicrosoftAccount(null).fromTag(t);
                        case TheAltening:
                            return new TheAlteningAccount(null).fromTag(t);
                        default:
                            throw new MatchException((String) null, (Throwable) null);
                    }
                } catch (NbtException e) {
                    return null;
                }
            });
        });
        return this;
    }
}
