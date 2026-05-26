package meteordevelopment.meteorclient.discordipc.connection;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.discordipc.Opcode;
import meteordevelopment.meteorclient.discordipc.Packet;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/discordipc/connection/Connection.class */
public abstract class Connection {
    private static final String[] UNIX_TEMP_PATHS = {"XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP"};

    protected abstract void write(ByteBuffer byteBuffer);

    public abstract void close();

    public static Connection open(Consumer<Packet> callback) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            for (int i = 0; i < 10; i++) {
                try {
                    return new WinConnection("\\\\?\\pipe\\discord-ipc-" + i, callback);
                } catch (IOException e) {
                }
            }
            return null;
        }
        String name = null;
        for (String tempPath : UNIX_TEMP_PATHS) {
            name = System.getenv(tempPath);
            if (name != null) {
                break;
            }
        }
        if (name == null) {
            name = "/tmp";
        }
        String name2 = name + "/discord-ipc-";
        for (int i2 = 0; i2 < 10; i2++) {
            try {
                return new UnixConnection(name2 + i2, callback);
            } catch (IOException e2) {
            }
        }
        return null;
    }

    public void write(Opcode opcode, JsonObject o) {
        o.addProperty("nonce", UUID.randomUUID().toString());
        byte[] d = o.toString().getBytes();
        ByteBuffer packet = ByteBuffer.allocate(d.length + 8);
        packet.putInt(Integer.reverseBytes(opcode.ordinal()));
        packet.putInt(Integer.reverseBytes(d.length));
        packet.put(d);
        packet.rewind();
        write(packet);
    }
}
