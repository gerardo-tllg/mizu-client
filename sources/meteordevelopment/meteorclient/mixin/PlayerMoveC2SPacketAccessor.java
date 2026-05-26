package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_2828;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/PlayerMoveC2SPacketAccessor.class */
@Mixin({class_2828.class})
public interface PlayerMoveC2SPacketAccessor {
    @Accessor("field_12886")
    @Mutable
    void setY(double d);

    @Accessor("field_29179")
    @Mutable
    void setOnGround(boolean z);
}
