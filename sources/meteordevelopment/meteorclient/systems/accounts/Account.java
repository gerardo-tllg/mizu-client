package meteordevelopment.meteorclient.systems.accounts;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.MinecraftClientAccessor;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.NbtException;
import net.minecraft.class_1071;
import net.minecraft.class_156;
import net.minecraft.class_2487;
import net.minecraft.class_320;
import net.minecraft.class_5520;
import net.minecraft.class_7500;
import net.minecraft.class_7569;
import net.minecraft.class_7574;
import net.minecraft.class_7853;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/Account.class */
public abstract class Account<T extends Account<?>> implements ISerializable<T> {
    protected AccountType type;
    protected String name;
    protected final AccountCache cache = new AccountCache();

    public abstract boolean fetchInfo();

    protected Account(AccountType type, String name) {
        this.type = type;
        this.name = name;
    }

    public boolean login() {
        YggdrasilAuthenticationService authenticationService = new YggdrasilAuthenticationService(MeteorClient.mc.getProxy());
        applyLoginEnvironment(authenticationService, authenticationService.createMinecraftSessionService());
        return true;
    }

    public String getUsername() {
        return this.cache.username.isEmpty() ? this.name : this.cache.username;
    }

    public AccountType getType() {
        return this.type;
    }

    public AccountCache getCache() {
        return this.cache;
    }

    public static void setSession(class_320 session) {
        MinecraftClientAccessor mca = MeteorClient.mc;
        mca.setSession(session);
        UserApiService apiService = mca.getAuthenticationService().createUserApiService(session.method_1674());
        mca.setUserApiService(apiService);
        mca.setSocialInteractionsManager(new class_5520(MeteorClient.mc, apiService));
        mca.setProfileKeys(class_7853.method_46532(apiService, session, MeteorClient.mc.field_1697.toPath()));
        mca.setAbuseReportContext(class_7574.method_44599(class_7569.method_44586(), apiService));
        mca.setGameProfileFuture(CompletableFuture.supplyAsync(() -> {
            return MeteorClient.mc.method_1495().fetchProfile(MeteorClient.mc.method_1548().method_44717(), true);
        }, class_156.method_27958()));
    }

    public static void applyLoginEnvironment(YggdrasilAuthenticationService authService, MinecraftSessionService sessService) {
        MinecraftClientAccessor mca = MeteorClient.mc;
        mca.setAuthenticationService(authService);
        class_7500.method_44172(authService.getServicesKeySet(), ServicesKeyType.PROFILE_KEY);
        mca.setSessionService(sessService);
        Path skinCachePath = MeteorClient.mc.method_1582().getSkinCache().getDirectory();
        mca.setSkinProvider(new class_1071(skinCachePath, sessService, MeteorClient.mc));
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10582("type", this.type.name());
        tag.method_10582("name", this.name);
        tag.method_10566("cache", this.cache.toTag());
        return tag;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public T fromTag(class_2487 tag) {
        if (tag.method_10558("name").isEmpty() || tag.method_10562("cache").isEmpty()) {
            throw new NbtException();
        }
        this.name = (String) tag.method_10558("name").get();
        this.cache.fromTag((class_2487) tag.method_10562("cache").get());
        return this;
    }
}
