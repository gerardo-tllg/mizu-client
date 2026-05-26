package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_2338;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ClientPlayerInteractionManagerAccessor.class */
@Mixin({class_636.class})
public interface ClientPlayerInteractionManagerAccessor {
    @Accessor("field_3715")
    float getBreakingProgress();

    @Accessor("field_3715")
    void setCurrentBreakingProgress(float f);

    @Accessor("field_3714")
    class_2338 getCurrentBreakingBlockPos();
}
