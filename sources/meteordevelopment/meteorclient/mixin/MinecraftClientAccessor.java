package meteordevelopment.meteorclient.mixin;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.net.Proxy;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_1071;
import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_5520;
import net.minecraft.class_6360;
import net.minecraft.class_7574;
import net.minecraft.class_7853;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/MinecraftClientAccessor.class */
@Mixin({class_310.class})
public interface MinecraftClientAccessor {
    @Accessor("session")
    @Mutable
    void setSession(class_320 class_320Var);

    @Accessor("networkProxy")
    Proxy getProxy();

    @Accessor("resourceReloadLogger")
    class_6360 getResourceReloadLogger();

    @Accessor("attackCooldown")
    int getAttackCooldown();

    @Accessor("attackCooldown")
    void setAttackCooldown(int i);

    @Invoker("doAttack")
    boolean leftClick();

    @Accessor("profileKeys")
    @Mutable
    void setProfileKeys(class_7853 class_7853Var);

    @Accessor("authenticationService")
    YggdrasilAuthenticationService getAuthenticationService();

    @Accessor
    @Mutable
    void setUserApiService(UserApiService userApiService);

    @Accessor("sessionService")
    @Mutable
    void setSessionService(MinecraftSessionService minecraftSessionService);

    @Accessor("authenticationService")
    @Mutable
    void setAuthenticationService(YggdrasilAuthenticationService yggdrasilAuthenticationService);

    @Accessor("skinProvider")
    @Mutable
    void setSkinProvider(class_1071 class_1071Var);

    @Accessor("socialInteractionsManager")
    @Mutable
    void setSocialInteractionsManager(class_5520 class_5520Var);

    @Accessor("abuseReportContext")
    @Mutable
    void setAbuseReportContext(class_7574 class_7574Var);

    @Accessor("gameProfileFuture")
    @Mutable
    void setGameProfileFuture(CompletableFuture<ProfileResult> completableFuture);

    @Accessor("currentFps")
    static int getFps() {
        return 0;
    }
}
