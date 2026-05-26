package javassist.bytecode.annotation;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/bytecode/annotation/NoSuchClassError.class */
public class NoSuchClassError extends Error {
    private static final long serialVersionUID = 1;
    private String className;

    public NoSuchClassError(String className, Error cause) {
        super(cause.toString(), cause);
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }
}
