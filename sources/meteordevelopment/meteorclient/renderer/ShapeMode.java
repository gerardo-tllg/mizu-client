package meteordevelopment.meteorclient.renderer;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/renderer/ShapeMode.class */
public enum ShapeMode {
    Lines,
    Sides,
    Both;

    public boolean lines() {
        return this == Lines || this == Both;
    }

    public boolean sides() {
        return this == Sides || this == Both;
    }
}
