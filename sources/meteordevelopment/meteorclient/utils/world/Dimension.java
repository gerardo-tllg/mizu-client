package meteordevelopment.meteorclient.utils.world;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/world/Dimension.class */
public enum Dimension {
    Overworld,
    Nether,
    End;

    public Dimension opposite() {
        switch (this) {
            case Overworld:
                return Nether;
            case Nether:
                return Overworld;
            default:
                return this;
        }
    }
}
