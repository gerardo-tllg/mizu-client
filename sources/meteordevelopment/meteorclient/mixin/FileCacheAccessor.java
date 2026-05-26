package meteordevelopment.meteorclient.mixin;

import java.nio.file.Path;
import net.minecraft.class_1071;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/FileCacheAccessor.class */
@Mixin({class_1071.class_8687.class})
public interface FileCacheAccessor {
    @Accessor
    Path getDirectory();
}
