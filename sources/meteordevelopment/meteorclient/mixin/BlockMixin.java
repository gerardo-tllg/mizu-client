package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import net.minecraft.class_1935;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_4970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/BlockMixin.class */
@Mixin({class_2248.class})
public abstract class BlockMixin extends class_4970 implements class_1935 {
    public BlockMixin(class_4970.class_2251 settings) {
        super(settings);
    }

    @ModifyReturnValue(method = {"shouldDrawSide"}, at = {@At("RETURN")})
    private static boolean onShouldDrawSide(boolean original, class_2680 state, class_2680 otherState, class_2350 side) {
        Xray xray = (Xray) Modules.get().get(Xray.class);
        if (xray.isActive()) {
        }
        return original;
    }

    @ModifyReturnValue(method = {"getSlipperiness"}, at = {@At("RETURN")})
    public float getSlipperiness(float original) {
        if (Modules.get() == null) {
            return original;
        }
        class_2248 block = (class_2248) this;
        if (block == class_2246.field_10030 && ((NoSlow) Modules.get().get(NoSlow.class)).slimeBlock()) {
            return 0.6f;
        }
        return original;
    }
}
