package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.class_2561;
import net.minecraft.class_2572;
import net.minecraft.class_2588;
import net.minecraft.class_5250;
import net.minecraft.class_7743;
import net.minecraft.class_8828;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/AbstractSignEditScreenMixin.class */
@Mixin({class_7743.class})
public abstract class AbstractSignEditScreenMixin {
    @ModifyExpressionValue(method = {"<init>(Lnet/minecraft/block/entity/SignBlockEntity;ZZLnet/minecraft/text/Text;)V"}, at = {@At(value = "INVOKE", target = "Ljava/util/stream/IntStream;mapToObj(Ljava/util/function/IntFunction;)Ljava/util/stream/Stream;")})
    private Stream<class_2561> modifyTranslatableText(Stream<class_2561> original) {
        return original.map(this::modifyText);
    }

    @Unique
    private class_2561 modifyText(class_2561 message) {
        class_5250 modified = class_5250.method_43477(message.method_10851());
        class_2572 class_2572VarMethod_10851 = message.method_10851();
        if (class_2572VarMethod_10851 instanceof class_2572) {
            class_2572 content = class_2572VarMethod_10851;
            String key = content.method_10901();
            if (key.contains(MeteorClient.MOD_ID)) {
                modified = class_5250.method_43477(new class_8828.class_2585(key));
            }
        }
        class_2588 class_2588VarMethod_10851 = message.method_10851();
        if (class_2588VarMethod_10851 instanceof class_2588) {
            class_2588 content2 = class_2588VarMethod_10851;
            String key2 = content2.method_11022();
            if (key2.contains(MeteorClient.MOD_ID)) {
                modified = class_5250.method_43477(new class_8828.class_2585(key2));
            }
        }
        modified.method_10862(message.method_10866());
        for (class_2561 sibling : message.method_10855()) {
            modified.method_10852(modifyText(sibling));
        }
        return modified;
    }
}
