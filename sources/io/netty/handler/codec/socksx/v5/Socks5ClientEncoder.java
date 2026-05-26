package io.netty.handler.codec.socksx.v5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import java.util.List;
import java.util.RandomAccess;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5ClientEncoder.class */
@ChannelHandler.Sharable
public class Socks5ClientEncoder extends MessageToByteEncoder<Socks5Message> {
    public static final Socks5ClientEncoder DEFAULT = new Socks5ClientEncoder();
    private final Socks5AddressEncoder addressEncoder;

    protected Socks5ClientEncoder() {
        this(Socks5AddressEncoder.DEFAULT);
    }

    public Socks5ClientEncoder(Socks5AddressEncoder addressEncoder) {
        this.addressEncoder = (Socks5AddressEncoder) ObjectUtil.checkNotNull(addressEncoder, "addressEncoder");
    }

    protected final Socks5AddressEncoder addressEncoder() {
        return this.addressEncoder;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX INFO: Thrown type has an unknown type hierarchy: io.netty.handler.codec.EncoderException */
    public void encode(ChannelHandlerContext ctx, Socks5Message msg, ByteBuf out) throws Exception {
        if (msg instanceof Socks5InitialRequest) {
            encodeAuthMethodRequest((Socks5InitialRequest) msg, out);
        } else if (msg instanceof Socks5PasswordAuthRequest) {
            encodePasswordAuthRequest((Socks5PasswordAuthRequest) msg, out);
        } else {
            if (msg instanceof Socks5CommandRequest) {
                encodeCommandRequest((Socks5CommandRequest) msg, out);
                return;
            }
            throw new EncoderException("unsupported message type: " + StringUtil.simpleClassName(msg));
        }
    }

    private static void encodeAuthMethodRequest(Socks5InitialRequest msg, ByteBuf out) {
        out.writeByte(msg.version().byteValue());
        List<Socks5AuthMethod> authMethods = msg.authMethods();
        int numAuthMethods = authMethods.size();
        out.writeByte(numAuthMethods);
        if (authMethods instanceof RandomAccess) {
            for (int i = 0; i < numAuthMethods; i++) {
                out.writeByte(authMethods.get(i).byteValue());
            }
            return;
        }
        for (Socks5AuthMethod a : authMethods) {
            out.writeByte(a.byteValue());
        }
    }

    private static void encodePasswordAuthRequest(Socks5PasswordAuthRequest msg, ByteBuf out) {
        out.writeByte(1);
        String username = msg.username();
        out.writeByte(username.length());
        ByteBufUtil.writeAscii(out, username);
        String password = msg.password();
        out.writeByte(password.length());
        ByteBufUtil.writeAscii(out, password);
    }

    private void encodeCommandRequest(Socks5CommandRequest msg, ByteBuf out) throws Exception {
        out.writeByte(msg.version().byteValue());
        out.writeByte(msg.type().byteValue());
        out.writeByte(0);
        Socks5AddressType dstAddrType = msg.dstAddrType();
        out.writeByte(dstAddrType.byteValue());
        this.addressEncoder.encodeAddress(dstAddrType, msg.dstAddr(), out);
        ByteBufUtil.writeShortBE(out, msg.dstPort());
    }
}
