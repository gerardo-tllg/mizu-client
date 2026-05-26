package meteordevelopment.meteorclient.mixininterface;

import net.minecraft.class_2561;
import net.minecraft.class_2661;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixininterface/DisconnectS2CPacketAccessor.class */
@Mixin({class_2661.class})
public interface DisconnectS2CPacketAccessor {
    @Accessor("reason")
    @Mutable
    void setReason(class_2561 class_2561Var);
}
