package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_638;
import net.minecraft.class_7202;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ClientWorldAccessor.class */
@Mixin({class_638.class})
public interface ClientWorldAccessor {
    @Accessor("pendingUpdateManager")
    class_7202 getPendingUpdateManager();
}
