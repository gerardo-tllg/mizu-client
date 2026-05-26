package meteordevelopment.meteorclient.mixin;

import java.util.Set;
import net.minecraft.class_10182;
import net.minecraft.class_2708;
import net.minecraft.class_2709;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/PlayerPositionLookS2CPacketAccessor.class */
@Mixin({class_2708.class})
public interface PlayerPositionLookS2CPacketAccessor {
    @Accessor("change")
    class_10182 getChange();

    @Accessor("relatives")
    Set<class_2709> getRelatives();

    @Accessor("change")
    @Mutable
    void setChange(class_10182 class_10182Var);
}
