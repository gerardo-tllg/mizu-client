package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_1665;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ProjectileInGroundAccessor.class */
@Mixin({class_1665.class})
public interface ProjectileInGroundAccessor {
    @Invoker("method_65059")
    boolean invokeIsInGround();
}
