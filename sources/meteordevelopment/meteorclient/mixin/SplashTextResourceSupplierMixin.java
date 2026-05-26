package meteordevelopment.meteorclient.mixin;

import java.util.List;
import java.util.Random;
import meteordevelopment.meteorclient.systems.config.Config;
import net.minecraft.class_4008;
import net.minecraft.class_8519;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/SplashTextResourceSupplierMixin.class */
@Mixin({class_4008.class})
public abstract class SplashTextResourceSupplierMixin {

    @Unique
    private static final Random random = new Random();

    @Unique
    private boolean override = true;

    @Unique
    private final List<String> meteorSplashes = getMeteorSplashes();

    @Inject(method = {"get"}, at = {@At("HEAD")}, cancellable = true)
    private void onApply(CallbackInfoReturnable<class_8519> cir) {
        if (Config.get() == null || !Config.get().titleScreenSplashes.get().booleanValue()) {
            return;
        }
        if (this.override) {
            cir.setReturnValue(new class_8519(this.meteorSplashes.get(random.nextInt(this.meteorSplashes.size()))));
        }
        this.override = !this.override;
    }

    @Unique
    private static List<String> getMeteorSplashes() {
        return List.of("Meteor on Crack!", "Star Meteor Client on GitHub!", "Based utility mod.", "§6Crownizzle §fbased god", "§4meteorclient.com", "§4Meteor on Crack!", "§6Meteor on Crack!");
    }
}
