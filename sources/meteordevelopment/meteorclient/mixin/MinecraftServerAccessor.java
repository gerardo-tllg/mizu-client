package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_32;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/MinecraftServerAccessor.class */
@Mixin({MinecraftServer.class})
public interface MinecraftServerAccessor {
    @Accessor("field_23784")
    class_32.class_5143 getSession();
}
