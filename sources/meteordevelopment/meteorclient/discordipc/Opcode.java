package meteordevelopment.meteorclient.discordipc;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/discordipc/Opcode.class */
public enum Opcode {
    Handshake,
    Frame,
    Close,
    Ping,
    Pong;

    private static final Opcode[] VALUES = values();

    public static Opcode valueOf(int i) {
        return VALUES[i];
    }

    private static Opcode[] $values() {
        return new Opcode[]{Handshake, Frame, Close, Ping, Pong};
    }
}
