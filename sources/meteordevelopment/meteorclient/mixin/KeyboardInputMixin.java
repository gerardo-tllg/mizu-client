package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Sneak;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import net.minecraft.class_10185;
import net.minecraft.class_743;
import net.minecraft.class_744;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/KeyboardInputMixin.class */
@Mixin({class_743.class})
public abstract class KeyboardInputMixin extends class_744 {
    @Inject(method = {"tick"}, at = {@At("TAIL")})
    private void isPressed(CallbackInfo ci) {
        if (((Sneak) Modules.get().get(Sneak.class)).doVanilla() || ((Freecam) Modules.get().get(Freecam.class)).staySneaking()) {
            this.field_54155 = new class_10185(this.field_54155.comp_3159(), this.field_54155.comp_3160(), this.field_54155.comp_3161(), this.field_54155.comp_3162(), this.field_54155.comp_3163(), true, this.field_54155.comp_3165());
        }
    }
}
