package meteordevelopment.meteorclient.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.class_3191;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/WorldRendererAccessor.class */
@Mixin({class_761.class})
public interface WorldRendererAccessor {
    @Accessor("blockBreakingInfos")
    Int2ObjectMap<class_3191> getBlockBreakingInfos();
}
