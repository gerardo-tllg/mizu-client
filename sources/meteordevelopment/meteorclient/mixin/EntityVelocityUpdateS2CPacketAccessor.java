package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_2743;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/EntityVelocityUpdateS2CPacketAccessor.class */
@Mixin({class_2743.class})
public interface EntityVelocityUpdateS2CPacketAccessor {
    @Accessor("velocityX")
    @Mutable
    void setX(int i);

    @Accessor("velocityY")
    @Mutable
    void setY(int i);

    @Accessor("velocityZ")
    @Mutable
    void setZ(int i);
}
