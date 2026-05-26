package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.ChunkOcclusionEvent;
import net.minecraft.class_2338;
import net.minecraft.class_852;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ChunkOcclusionDataBuilderMixin.class */
@Mixin({class_852.class})
public abstract class ChunkOcclusionDataBuilderMixin {
    @Inject(method = {"markClosed"}, at = {@At("HEAD")}, cancellable = true)
    private void onMarkClosed(class_2338 pos, CallbackInfo info) {
        ChunkOcclusionEvent event = (ChunkOcclusionEvent) MeteorClient.EVENT_BUS.post(ChunkOcclusionEvent.get());
        if (event.isCancelled()) {
            info.cancel();
        }
    }
}
