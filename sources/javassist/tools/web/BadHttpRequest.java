package javassist.tools.web;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/tools/web/BadHttpRequest.class */
public class BadHttpRequest extends Exception {
    private static final long serialVersionUID = 1;
    private Exception e;

    public BadHttpRequest() {
        this.e = null;
    }

    public BadHttpRequest(Exception _e) {
        this.e = _e;
    }

    @Override // java.lang.Throwable
    public String toString() {
        if (this.e == null) {
            return super.toString();
        }
        return this.e.toString();
    }
}
