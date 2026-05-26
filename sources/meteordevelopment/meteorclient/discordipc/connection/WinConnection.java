package meteordevelopment.meteorclient.discordipc.connection;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.discordipc.Opcode;
import meteordevelopment.meteorclient.discordipc.Packet;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/discordipc/connection/WinConnection.class */
public class WinConnection extends Connection {
    private final RandomAccessFile raf;
    private final Consumer<Packet> callback;

    public WinConnection(String name, Consumer<Packet> callback) throws IOException {
        this.raf = new RandomAccessFile(name, "rw");
        this.callback = callback;
        Thread thread = new Thread(this::run);
        thread.setName("Discord IPC - Read thread");
        thread.start();
    }

    @Override // meteordevelopment.meteorclient.discordipc.connection.Connection
    protected void write(ByteBuffer buffer) {
        try {
            this.raf.write(buffer.array());
        } catch (IOException var3) {
            var3.printStackTrace();
        }
    }

    private void run() {
        ByteBuffer intB = ByteBuffer.allocate(4);
        while (true) {
            try {
                readFully(intB);
                Opcode opcode = Opcode.valueOf(Integer.reverseBytes(intB.getInt(0)));
                readFully(intB);
                int length = Integer.reverseBytes(intB.getInt(0));
                ByteBuffer dataB = ByteBuffer.allocate(length);
                readFully(dataB);
                String data = Charset.defaultCharset().decode(dataB.rewind()).toString();
                this.callback.accept(new Packet(opcode, JsonParser.parseString(data).getAsJsonObject()));
            } catch (Exception e) {
                return;
            }
        }
    }

    private void readFully(ByteBuffer buffer) throws IOException {
        buffer.rewind();
        while (this.raf.length() < buffer.remaining()) {
            Thread.onSpinWait();
            try {
                Thread.sleep(100L);
            } catch (InterruptedException var3) {
                var3.printStackTrace();
            }
        }
        while (buffer.hasRemaining()) {
            this.raf.getChannel().read(buffer);
        }
    }

    @Override // meteordevelopment.meteorclient.discordipc.connection.Connection
    public void close() {
        try {
            this.raf.close();
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }
}
