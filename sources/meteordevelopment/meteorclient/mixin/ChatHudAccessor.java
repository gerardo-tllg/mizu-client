package meteordevelopment.meteorclient.mixin;

import java.util.List;
import net.minecraft.class_303;
import net.minecraft.class_338;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ChatHudAccessor.class */
@Mixin({class_338.class})
public interface ChatHudAccessor {
    @Accessor("field_2064")
    List<class_303.class_7590> getVisibleMessages();

    @Accessor("field_2061")
    List<class_303> getMessages();
}
