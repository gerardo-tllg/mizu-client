package io.netty.handler.codec.socksx.v5;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.internal.ObjectUtil;
import java.util.List;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5CommandRequestDecoder.class */
public class Socks5CommandRequestDecoder extends ReplayingDecoder<State> {
    private final Socks5AddressDecoder addressDecoder;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5CommandRequestDecoder$State.class */
    public enum State {
        INIT,
        SUCCESS,
        FAILURE
    }

    public Socks5CommandRequestDecoder() {
        this(Socks5AddressDecoder.DEFAULT);
    }

    public Socks5CommandRequestDecoder(Socks5AddressDecoder addressDecoder) {
        super(State.INIT);
        this.addressDecoder = (Socks5AddressDecoder) ObjectUtil.checkNotNull(addressDecoder, "addressDecoder");
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: io.netty.handler.codec.DecoderException */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:11:0x00bc A[Catch: Exception -> 0x00d8, TryCatch #0 {Exception -> 0x00d8, blocks: (B:2:0x0000, B:3:0x000e, B:4:0x0028, B:6:0x0039, B:7:0x0067, B:8:0x0068, B:9:0x00b1, B:11:0x00bc, B:12:0x00cc), top: B:17:0x0000 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void decode(io.netty.channel.ChannelHandlerContext r9, io.netty.buffer.ByteBuf r10, java.util.List<java.lang.Object> r11) throws java.lang.Exception {
        /*
            Method dump skipped, instruction units count: 226
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder.decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List):void");
    }

    private void fail(List<Object> out, Exception cause) {
        if (!(cause instanceof DecoderException)) {
            cause = new DecoderException(cause);
        }
        checkpoint(State.FAILURE);
        Socks5Message m = new DefaultSocks5CommandRequest(Socks5CommandType.CONNECT, Socks5AddressType.IPv4, "0.0.0.0", 1);
        m.setDecoderResult(DecoderResult.failure(cause));
        out.add(m);
    }
}
