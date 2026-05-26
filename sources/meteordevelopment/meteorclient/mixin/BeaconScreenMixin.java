package meteordevelopment.meteorclient.mixin;

import java.util.List;
import java.util.Objects;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.BetterBeacons;
import net.minecraft.class_1291;
import net.minecraft.class_1661;
import net.minecraft.class_1704;
import net.minecraft.class_2561;
import net.minecraft.class_2580;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_465;
import net.minecraft.class_466;
import net.minecraft.class_6880;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/BeaconScreenMixin.class */
@Mixin({class_466.class})
public abstract class BeaconScreenMixin extends class_465<class_1704> {
    @Shadow
    protected abstract <T extends class_339> void method_37076(T t);

    public BeaconScreenMixin(class_1704 handler, class_1661 inventory, class_2561 title) {
        super(handler, inventory, title);
    }

    @Inject(method = {"init"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/BeaconScreen;addButton(Lnet/minecraft/client/gui/widget/ClickableWidget;)V", ordinal = 1, shift = At.Shift.AFTER)}, cancellable = true)
    private void changeButtons(CallbackInfo ci) {
        if (((BetterBeacons) Modules.get().get(BetterBeacons.class)).isActive()) {
            List<class_6880<class_1291>> effects = class_2580.field_11801.stream().flatMap((v0) -> {
                return v0.stream();
            }).toList();
            class_466 class_466Var = class_310.method_1551().field_1755;
            if (class_466Var instanceof class_466) {
                class_466 beaconScreen = class_466Var;
                for (int x = 0; x < 3; x++) {
                    for (int y = 0; y < 2; y++) {
                        class_6880<class_1291> effect = effects.get((x * 2) + y);
                        int xMin = this.field_2776 + (x * 25);
                        int yMin = this.field_2800 + (y * 25);
                        Objects.requireNonNull(beaconScreen);
                        method_37076(new class_466.class_469(beaconScreen, xMin + 27, yMin + 32, effect, true, -1));
                        Objects.requireNonNull(beaconScreen);
                        class_466.class_469 secondaryWidget = new class_466.class_469(beaconScreen, xMin + Opcode.I2L, yMin + 32, effect, false, 3);
                        if (method_17577().method_17373() != 4) {
                            secondaryWidget.field_22763 = false;
                        }
                        method_37076(secondaryWidget);
                    }
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = {"drawBackground"}, at = {@At("TAIL")})
    private void onDrawBackground(class_332 context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (((BetterBeacons) Modules.get().get(BetterBeacons.class)).isActive()) {
            context.method_25294(this.field_2776 + 10, this.field_2800 + 7, this.field_2776 + 220, this.field_2800 + 98, -14606047);
        }
    }
}
