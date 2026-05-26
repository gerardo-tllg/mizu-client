package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import net.minecraft.class_5294;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/DimensionEffectsMixin.class */
@Mixin({class_5294.class})
public abstract class DimensionEffectsMixin {
    @ModifyReturnValue(method = {"getSkyType"}, at = {@At("RETURN")})
    private class_5294.class_5401 modifySkyType(class_5294.class_5401 original) {
        Ambience ambience = (Ambience) Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.endSky.get().booleanValue()) {
            return class_5294.class_5401.field_25641;
        }
        return original;
    }
}
