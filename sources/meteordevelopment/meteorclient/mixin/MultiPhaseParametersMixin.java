package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IMultiPhaseParameters;
import net.minecraft.class_1921;
import net.minecraft.class_4668;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/MultiPhaseParametersMixin.class */
@Mixin({class_1921.class_4688.class})
public abstract class MultiPhaseParametersMixin implements IMultiPhaseParameters {

    @Shadow
    @Final
    class_4668.class_4678 field_57931;

    @Override // meteordevelopment.meteorclient.mixininterface.IMultiPhaseParameters
    public class_4668.class_4678 meteor$getTarget() {
        return this.field_57931;
    }
}
