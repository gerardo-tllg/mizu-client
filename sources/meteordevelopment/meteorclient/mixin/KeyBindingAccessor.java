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
    @Accessor("field_1655")
    class_3675.class_306 getKey();

    @Accessor("field_1661")
    int meteor$getTimesPressed();

    @Accessor("field_1661")
    void meteor$setTimesPressed(int i);

    @Invoker("method_1425")
    void invokeReset();

    @Accessor("field_1656")
    static Map<String, Integer> getCategoryOrderMap() {
        return null;
    }

    @Accessor("field_1657")
    static Map<String, class_304> getKeysById() {
        return null;
    }
}
