package meteordevelopment.meteorclient.mixininterface;

import net.minecraft.class_2338;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixininterface/IBox.class */
public interface IBox {
    void meteor$expand(double d);

    void meteor$set(double d, double d2, double d3, double d4, double d5, double d6);

    default void meteor$set(class_2338 pos) {
        meteor$set(pos.method_10263(), pos.method_10264(), pos.method_10260(), pos.method_10263() + 1, pos.method_10264() + 1, pos.method_10260() + 1);
    }
}
