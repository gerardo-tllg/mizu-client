package meteordevelopment.meteorclient.mixin;

import net.minecraft.class_5225;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/TextHandlerAccessor.class */
@Mixin({class_5225.class})
public interface TextHandlerAccessor {
    @Accessor("widthRetriever")
    class_5225.class_5231 getWidthRetriever();
}
