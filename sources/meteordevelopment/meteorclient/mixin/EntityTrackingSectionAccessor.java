package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_3509;
import net.minecraft.class_5572;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/EntityTrackingSectionAccessor.class */
@Mixin({class_5572.class})
public interface EntityTrackingSectionAccessor {
    @Accessor("field_27248")
    <T> class_3509<T> getCollection();
}
