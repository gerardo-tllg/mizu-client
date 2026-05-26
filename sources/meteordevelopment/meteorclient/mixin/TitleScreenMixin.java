package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.gui.titlemenu.CustomTitleScreen;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.player.TitleScreenCredits;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/TitleScreenMixin.class */
@Mixin({class_442.class})
public abstract class TitleScreenMixin extends class_437 {
    public TitleScreenMixin(class_2561 title) {
        super(title);
    }

    @Inject(method = {"init"}, at = {@At("RETURN")})
    private void onInit(CallbackInfo ci) {
        class_310.method_1551().method_1507(new CustomTitleScreen());
    }

    @Inject(method = {"render"}, at = {@At("TAIL")})
    private void onRender(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Config.get().titleScreenCredits.get().booleanValue()) {
            TitleScreenCredits.render(context);
        }
    }

    @Inject(method = {"mouseClicked"}, at = {@At("HEAD")}, cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
        if (!Config.get().titleScreenCredits.get().booleanValue() || button != 0 || !TitleScreenCredits.onClicked(mouseX, mouseY)) {
            return;
        }
        info.setReturnValue(true);
    }
}
