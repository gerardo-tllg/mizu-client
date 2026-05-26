package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_10093;
import net.minecraft.class_22;
import net.minecraft.class_9209;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/MapTextureManagerAccessor.class */
@Mixin({class_10093.class})
public interface MapTextureManagerAccessor {
    @Invoker("method_62625")
    class_10093.class_331 invokeGetMapTexture(class_9209 class_9209Var, class_22 class_22Var);
}
