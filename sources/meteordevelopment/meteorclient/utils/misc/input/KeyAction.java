package meteordevelopment.meteorclient.utils.misc.input;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/input/KeyAction.class */
public enum KeyAction {
    Press,
    Repeat,
    Release;

    public static KeyAction get(int action) {
        switch (action) {
            case 0:
                return Release;
            case 1:
                return Press;
            default:
                return Repeat;
        }
    }
}
