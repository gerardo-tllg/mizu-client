package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_1764;
import net.minecraft.class_9278;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/CrossbowItemAccessor.class */
@Mixin({class_1764.class})
public interface CrossbowItemAccessor {
    @Invoker("method_20309")
    static float getSpeed(class_9278 itemStack) {
        return 0.0f;
    }
}
