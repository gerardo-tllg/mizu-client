package meteordevelopment.meteorclient.utils.misc;

import meteordevelopment.meteorclient.utils.misc.ICopyable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/ICopyable.class */
public interface ICopyable<T extends ICopyable<T>> {
    T set(T t);

    T copy();
}
