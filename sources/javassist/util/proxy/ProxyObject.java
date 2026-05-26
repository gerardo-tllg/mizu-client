package javassist.util.proxy;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/util/proxy/ProxyObject.class */
public interface ProxyObject extends Proxy {
    @Override // javassist.util.proxy.Proxy
    void setHandler(MethodHandler methodHandler);

    MethodHandler getHandler();
}
