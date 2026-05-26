package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.mixininterface.IClientWorld;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_638;
import net.minecraft.class_7202;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ClientWorldMixin.class */
@Mixin({class_638.class})
public class ClientWorldMixin implements IClientWorld {

    @Shadow
    @Final
    private class_7202 field_37951;

    @Override // meteordevelopment.meteorclient.mixininterface.IClientWorld
    public int meteor$getSequence() {
        return this.field_37951.getSequence();
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IClientWorld
    public int meteor$getAndIncrementSequence() {
        return this.field_37951.invokeIncrementSequence().getSequence();
    }

    @ModifyReturnValue(method = {"getSkyColor"}, at = {@At("RETURN")})
    private int modifySkyColor(int original) {
        Color color;
        Ambience ambience = (Ambience) Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.customSkyColor.get().booleanValue() && (color = ambience.skyColor()) != null) {
            return color.getPacked();
        }
        return original;
    }

    @ModifyReturnValue(method = {"getCloudsColor"}, at = {@At("RETURN")})
    private int modifyCloudsColor(int original) {
        Ambience ambience = (Ambience) Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.customCloudColor.get().booleanValue()) {
            return ambience.cloudColor.get().getPacked();
        }
        return original;
    }
}
