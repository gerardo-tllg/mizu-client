package javassist.tools.rmi;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/tools/rmi/Sample.class */
public class Sample {
    private ObjectImporter importer;
    private int objectId;

    public Object forward(Object[] args, int identifier) {
        return this.importer.call(this.objectId, identifier, args);
    }

    public static Object forwardStatic(Object[] args, int identifier) throws RemoteException {
        throw new RemoteException("cannot call a static method.");
    }
}
