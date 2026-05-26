package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.PlaySoundEvent;
import net.minecraft.class_1113;
import net.minecraft.class_1140;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/SoundSystemMixin.class */
@Mixin({class_1140.class})
public abstract class SoundSystemMixin {
    @Inject(method = {"play(Lnet/minecraft/client/sound/SoundInstance;)V"}, at = {@At("HEAD")}, cancellable = true)
    private void onPlay(class_1113 soundInstance, CallbackInfo info) {
        PlaySoundEvent event = (PlaySoundEvent) MeteorClient.EVENT_BUS.post(PlaySoundEvent.get(soundInstance));
        if (event.isCancelled()) {
            info.cancel();
        }
    }
}
