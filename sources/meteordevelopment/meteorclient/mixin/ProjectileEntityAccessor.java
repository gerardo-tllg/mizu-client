package meteordevelopment.meteorclient.mixin;

import java.util.UUID;
import net.minecraft.class_1676;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ProjectileEntityAccessor.class */
@Mixin({class_1676.class})
public interface ProjectileEntityAccessor {
    @Accessor("field_22478")
    UUID getOwnerUuid();
}
