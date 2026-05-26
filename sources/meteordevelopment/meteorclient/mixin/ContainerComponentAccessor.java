package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_1799;
import net.minecraft.class_2371;
import net.minecraft.class_9288;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ContainerComponentAccessor.class */
@Mixin({class_9288.class})
public interface ContainerComponentAccessor {
    @Accessor
    class_2371<class_1799> getStacks();
}
