package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IMultiPhase;
import net.minecraft.class_1921;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/MultiPhaseMixin.class */
@Mixin({class_1921.class_4687.class})
public abstract class MultiPhaseMixin implements IMultiPhase {

    @Shadow
    @Final
    private class_1921.class_4688 field_21403;

    @Override // meteordevelopment.meteorclient.mixininterface.IMultiPhase
    public class_1921.class_4688 meteor$getParameters() {
        return this.field_21403;
    }
}
