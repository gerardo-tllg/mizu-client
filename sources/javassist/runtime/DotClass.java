package javassist.runtime;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/runtime/DotClass.class */
public class DotClass {
    public static NoClassDefFoundError fail(ClassNotFoundException e) {
        return new NoClassDefFoundError(e.getMessage());
    }
}
