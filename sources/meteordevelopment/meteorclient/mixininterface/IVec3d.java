package meteordevelopment.meteorclient.mixininterface;

import net.minecraft.class_2382;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixininterface/IVec3d.class */
public interface IVec3d {
    void meteor$set(double d, double d2, double d3);

    void meteor$setXZ(double d, double d2);

    void meteor$setY(double d);

    default void meteor$set(class_2382 vec) {
        meteor$set(vec.method_10263(), vec.method_10264(), vec.method_10260());
    }

    default void meteor$set(Vector3d vec) {
        meteor$set(vec.x, vec.y, vec.z);
    }
}
