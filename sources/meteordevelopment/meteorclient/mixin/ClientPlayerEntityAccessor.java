package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ClientPlayerEntityAccessor.class */
@Mixin({class_746.class})
public interface ClientPlayerEntityAccessor {
    @Accessor("field_3922")
    void setMountJumpStrength(float f);

    @Accessor("field_3923")
    void setTicksSinceLastPositionPacketSent(int i);

    @Invoker("method_46743")
    boolean invokeCanSprint();
}
