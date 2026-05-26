package meteordevelopment.meteorclient.renderer;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/DrawMode.class */
public enum DrawMode {
    Lines(2),
    Triangles(3);

    public final int indicesCount;

    DrawMode(int indicesCount) {
        this.indicesCount = indicesCount;
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public int getGL() throws MatchException {
        switch (this) {
            case Lines:
                return 1;
            case Triangles:
                return 4;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }
}
