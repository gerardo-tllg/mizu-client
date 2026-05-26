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
    @Accessor("field_1726")
    @Mutable
    void setSession(class_320 class_320Var);

    @Accessor("field_1739")
    Proxy getProxy();

    @Accessor("field_33697")
    class_6360 getResourceReloadLogger();

    @Accessor("field_1771")
    int getAttackCooldown();

    @Accessor("field_1771")
    void setAttackCooldown(int i);

    @Invoker("method_1536")
    boolean leftClick();

    @Accessor("field_39068")
    @Mutable
    void setProfileKeys(class_7853 class_7853Var);

    @Accessor("field_39420")
    YggdrasilAuthenticationService getAuthenticationService();

    @Accessor("field_26902")
    @Mutable
    void setUserApiService(UserApiService userApiService);

    @Accessor("field_1723")
    @Mutable
    void setSessionService(MinecraftSessionService minecraftSessionService);

    @Accessor("field_39420")
    @Mutable
    void setAuthenticationService(YggdrasilAuthenticationService yggdrasilAuthenticationService);

    @Accessor("field_1707")
    @Mutable
    void setSkinProvider(class_1071 class_1071Var);

    @Accessor("field_26842")
    @Mutable
    void setSocialInteractionsManager(class_5520 class_5520Var);

    @Accessor("field_39492")
    @Mutable
    void setAbuseReportContext(class_7574 class_7574Var);

    @Accessor("field_45899")
    @Mutable
    void setGameProfileFuture(CompletableFuture<ProfileResult> completableFuture);

    @Accessor("field_1738")
    static int getFps() {
        return 0;
    }
}
