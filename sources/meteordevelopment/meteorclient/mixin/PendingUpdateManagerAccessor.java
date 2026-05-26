package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_7202;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/PendingUpdateManagerAccessor.class */
@Mixin({class_7202.class})
public interface PendingUpdateManagerAccessor {
    @Accessor("sequence")
    int getSequence();

    @Invoker("incrementSequence")
    class_7202 invokeIncrementSequence();
}
