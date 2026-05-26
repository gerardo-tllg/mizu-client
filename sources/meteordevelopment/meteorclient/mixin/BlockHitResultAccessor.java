package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_2350;
import net.minecraft.class_3965;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/BlockHitResultAccessor.class */
@Mixin({class_3965.class})
public interface BlockHitResultAccessor {
    @Accessor("side")
    @Mutable
    void setSide(class_2350 class_2350Var);
}
