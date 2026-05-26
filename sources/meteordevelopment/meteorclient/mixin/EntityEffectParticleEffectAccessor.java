package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_9381;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/EntityEffectParticleEffectAccessor.class */
@Mixin({class_9381.class})
public interface EntityEffectParticleEffectAccessor {
    @Accessor("field_49910")
    int getColor();
}
