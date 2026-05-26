package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_1657;
import net.minecraft.class_4050;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/PlayerEntityAccessor.class */
@Mixin({class_1657.class})
public interface PlayerEntityAccessor {
    @Invoker("method_52558")
    boolean meteor$canChangeIntoPose(class_4050 class_4050Var);
}
