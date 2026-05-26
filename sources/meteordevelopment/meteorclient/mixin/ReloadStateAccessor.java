package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_6360;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ReloadStateAccessor.class */
@Mixin({class_6360.class_6363.class})
public interface ReloadStateAccessor {
    @Accessor("finished")
    boolean isFinished();
}
