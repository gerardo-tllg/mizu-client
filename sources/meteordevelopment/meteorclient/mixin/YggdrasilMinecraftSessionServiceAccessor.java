package meteordevelopment.meteorclient.mixin;

import com.mojang.authlib.Environment;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import java.net.Proxy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/YggdrasilMinecraftSessionServiceAccessor.class */
@Mixin({YggdrasilMinecraftSessionService.class})
public interface YggdrasilMinecraftSessionServiceAccessor {
    @Invoker("<init>")
    static YggdrasilMinecraftSessionService createYggdrasilMinecraftSessionService(ServicesKeySet servicesKeySet, Proxy proxy, Environment env) {
        throw new UnsupportedOperationException();
    }
}
