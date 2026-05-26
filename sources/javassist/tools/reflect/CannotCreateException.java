package javassist.tools.reflect;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/tools/reflect/CannotCreateException.class */
public class CannotCreateException extends Exception {
    private static final long serialVersionUID = 1;

    public CannotCreateException(String s) {
        super(s);
    }

    public CannotCreateException(Exception e) {
        super("by " + e.toString());
    }
}
