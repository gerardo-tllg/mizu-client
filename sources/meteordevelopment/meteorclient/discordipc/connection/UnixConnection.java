package meteordevelopment.meteorclient.discordipc.connection;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.discordipc.Opcode;
import meteordevelopment.meteorclient.discordipc.Packet;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/discordipc/connection/UnixConnection.class */
public class UnixConnection extends Connection {
    private final Selector s = Selector.open();
    private final SocketChannel sc;
    private final Consumer<Packet> callback;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/discordipc/connection/UnixConnection$State.class */
    private enum State {
        Opcode,
        Length,
        Data
    }

    public UnixConnection(String name, Consumer<Packet> callback) throws IOException {
        this.sc = SocketChannel.open(UnixDomainSocketAddress.of(name));
        this.callback = callback;
        this.sc.configureBlocking(false);
        this.sc.register(this.s, 1);
        Thread thread = new Thread(this::run);
        thread.setName("Discord IPC - Read thread");
        thread.start();
    }

    private void run() {
        State state = State.Opcode;
        ByteBuffer intB = ByteBuffer.allocate(4);
        ByteBuffer dataB = null;
        Opcode opcode = null;
        while (true) {
            try {
                this.s.select();
                switch (state) {
                    case Opcode:
                        this.sc.read(intB);
                        if (!intB.hasRemaining()) {
                            opcode = Opcode.valueOf(Integer.reverseBytes(intB.getInt(0)));
                            state = State.Length;
                            intB.rewind();
                        }
                        break;
                    case Length:
                        this.sc.read(intB);
                        if (!intB.hasRemaining()) {
                            dataB = ByteBuffer.allocate(Integer.reverseBytes(intB.getInt(0)));
                            state = State.Data;
                            intB.rewind();
                        }
                        break;
                    case Data:
                        this.sc.read(dataB);
                        if (!dataB.hasRemaining()) {
                            String data = Charset.defaultCharset().decode(dataB.rewind()).toString();
                            this.callback.accept(new Packet(opcode, JsonParser.parseString(data).getAsJsonObject()));
                            dataB = null;
                            state = State.Opcode;
                        }
                        break;
                }
            } catch (Exception e) {
                return;
            }
        }
    }

    @Override // meteordevelopment.meteorclient.discordipc.connection.Connection
    protected void write(ByteBuffer buffer) {
        try {
            this.sc.write(buffer);
        } catch (IOException var3) {
            var3.printStackTrace();
        }
    }

    @Override // meteordevelopment.meteorclient.discordipc.connection.Connection
    public void close() {
        try {
            this.s.close();
            this.sc.close();
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }
}
