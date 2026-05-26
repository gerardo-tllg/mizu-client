package meteordevelopment.meteorclient.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.class_640;
import net.minecraft.class_8685;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/PlayerListEntryMixin.class */
@Mixin({class_640.class})
public abstract class PlayerListEntryMixin {
    @Shadow
    public abstract GameProfile method_2966();

    @Inject(method = {"getSkinTextures"}, at = {@At("HEAD")}, cancellable = true)
    private void onGetTexture(CallbackInfoReturnable<class_8685> info) {
    }
}
