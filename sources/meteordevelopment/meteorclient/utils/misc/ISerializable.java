package meteordevelopment.meteorclient.utils.misc;

import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/ISerializable.class */
public interface ISerializable<T> {
    class_2487 toTag();

    T fromTag(class_2487 class_2487Var);
}
