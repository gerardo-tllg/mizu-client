package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_1293;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/StatusEffectInstanceAccessor.class */
@Mixin({class_1293.class})
public interface StatusEffectInstanceAccessor {
    @Accessor("field_5895")
    void setDuration(int i);

    @Accessor("field_5893")
    void setAmplifier(int i);
}
