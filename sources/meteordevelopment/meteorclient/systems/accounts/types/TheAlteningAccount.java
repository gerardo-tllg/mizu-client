package meteordevelopment.meteorclient.systems.accounts.types;

import com.mojang.authlib.Environment;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import de.florianmichael.waybackauthlib.InvalidCredentialsException;
import de.florianmichael.waybackauthlib.WaybackAuthLib;
import java.util.Optional;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.YggdrasilMinecraftSessionServiceAccessor;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.AccountType;
import meteordevelopment.meteorclient.systems.accounts.TokenAccount;
import meteordevelopment.meteorclient.utils.misc.NbtException;
import net.minecraft.class_2487;
import net.minecraft.class_320;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/types/TheAlteningAccount.class */
public class TheAlteningAccount extends Account<TheAlteningAccount> implements TokenAccount {
    private static final Environment ENVIRONMENT = new Environment("http://sessionserver.thealtening.com", "http://authserver.thealtening.com", "The Altening");
    private static final YggdrasilAuthenticationService SERVICE = new YggdrasilAuthenticationService(MeteorClient.mc.getProxy(), ENVIRONMENT);
    private String token;

    @Nullable
    private WaybackAuthLib auth;

    public TheAlteningAccount(String token) {
        super(AccountType.TheAltening, token);
        this.token = token;
    }

    @Override // meteordevelopment.meteorclient.systems.accounts.Account
    public boolean fetchInfo() {
        this.auth = getAuth();
        try {
            this.auth.logIn();
            this.cache.username = this.auth.getCurrentProfile().getName();
            this.cache.uuid = this.auth.getCurrentProfile().getId().toString();
            this.cache.loadHead();
            return true;
        } catch (InvalidCredentialsException e) {
            MeteorClient.LOG.error("Invalid TheAltening credentials.");
            return false;
        } catch (Exception e2) {
            MeteorClient.LOG.error("Failed to fetch info for TheAltening account!");
            return false;
        }
    }

    @Override // meteordevelopment.meteorclient.systems.accounts.Account
    public boolean login() {
        if (this.auth == null) {
            return false;
        }
        applyLoginEnvironment(SERVICE, YggdrasilMinecraftSessionServiceAccessor.createYggdrasilMinecraftSessionService(SERVICE.getServicesKeySet(), SERVICE.getProxy(), ENVIRONMENT));
        try {
            setSession(new class_320(this.auth.getCurrentProfile().getName(), this.auth.getCurrentProfile().getId(), this.auth.getAccessToken(), Optional.empty(), Optional.empty(), class_320.class_321.field_1988));
            return true;
        } catch (Exception e) {
            MeteorClient.LOG.error("Failed to login with TheAltening.");
            return false;
        }
    }

    private WaybackAuthLib getAuth() {
        WaybackAuthLib auth = new WaybackAuthLib(ENVIRONMENT.servicesHost());
        auth.setUsername(this.name);
        auth.setPassword("Meteor on Crack!");
        return auth;
    }

    @Override // meteordevelopment.meteorclient.systems.accounts.TokenAccount
    public String getToken() {
        return this.token;
    }

    @Override // meteordevelopment.meteorclient.systems.accounts.Account, meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10582("type", this.type.name());
        tag.method_10582("name", this.name);
        tag.method_10582("token", this.token);
        tag.method_10566("cache", this.cache.toTag());
        return tag;
    }

    @Override // meteordevelopment.meteorclient.systems.accounts.Account, meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public TheAlteningAccount fromTag(class_2487 tag) {
        if (tag.method_10558("name").isEmpty() || tag.method_10562("cache").isEmpty() || tag.method_10558("token").isEmpty()) {
            throw new NbtException();
        }
        this.name = (String) tag.method_10558("name").get();
        this.token = (String) tag.method_10558("token").get();
        this.cache.fromTag((class_2487) tag.method_10562("cache").get());
        return this;
    }
}
