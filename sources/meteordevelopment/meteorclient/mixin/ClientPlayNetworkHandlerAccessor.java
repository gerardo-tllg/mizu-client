package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_5455;
import net.minecraft.class_634;
import net.minecraft.class_7610;
import net.minecraft.class_7637;
import net.minecraft.class_7699;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ClientPlayNetworkHandlerAccessor.class */
@Mixin({class_634.class})
public interface ClientPlayNetworkHandlerAccessor {
    @Accessor("field_19144")
    int getChunkLoadDistance();

    @Accessor("field_39808")
    class_7610.class_7612 getMessagePacker();

    @Accessor("field_39858")
    class_7637 getLastSeenMessagesCollector();

    @Accessor("field_25063")
    class_5455.class_6890 getCombinedDynamicRegistries();

    @Accessor("field_45600")
    class_7699 getEnabledFeatures();
}
