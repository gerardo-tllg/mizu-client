package javassist.tools.rmi;

import java.io.Serializable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/tools/rmi/RemoteRef.class */
public class RemoteRef implements Serializable {
    private static final long serialVersionUID = 1;
    public int oid;
    public String classname;

    public RemoteRef(int i) {
        this.oid = i;
        this.classname = null;
    }

    public RemoteRef(int i, String name) {
        this.oid = i;
        this.classname = name;
    }
}
