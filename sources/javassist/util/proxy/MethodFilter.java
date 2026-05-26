package javassist.util.proxy;

import java.lang.reflect.Method;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/util/proxy/MethodFilter.class */
public interface MethodFilter {
    boolean isHandled(Method method);
}
