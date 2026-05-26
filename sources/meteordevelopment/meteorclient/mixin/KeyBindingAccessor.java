package meteordevelopment.meteorclient.mixin;

import java.util.Map;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/KeyBindingAccessor.class */
@Mixin({class_304.class})
public interface KeyBindingAccessor {
    @Accessor("boundKey")
    class_3675.class_306 getKey();

    @Accessor("timesPressed")
    int meteor$getTimesPressed();

    @Accessor("timesPressed")
    void meteor$setTimesPressed(int i);

    @Invoker("reset")
    void invokeReset();

    @Accessor("CATEGORY_ORDER_MAP")
    static Map<String, Integer> getCategoryOrderMap() {
        return null;
    }

    @Accessor("KEYS_BY_ID")
    static Map<String, class_304> getKeysById() {
        return null;
    }
}
