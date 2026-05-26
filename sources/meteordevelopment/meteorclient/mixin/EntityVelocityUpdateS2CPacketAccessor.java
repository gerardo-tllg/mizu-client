package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_2743;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/EntityVelocityUpdateS2CPacketAccessor.class */
@Mixin({class_2743.class})
public interface EntityVelocityUpdateS2CPacketAccessor {
    @Accessor("field_12563")
    @Mutable
    void setX(int i);

    @Accessor("field_12562")
    @Mutable
    void setY(int i);

    @Accessor("field_12561")
    @Mutable
    void setZ(int i);
}
