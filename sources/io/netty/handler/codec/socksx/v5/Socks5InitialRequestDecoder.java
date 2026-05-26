package io.netty.handler.codec.socksx.v5;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import java.util.List;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5InitialRequestDecoder.class */
public class Socks5InitialRequestDecoder extends ReplayingDecoder<State> {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5InitialRequestDecoder$State.class */
    public enum State {
        INIT,
        SUCCESS,
        FAILURE
    }

    public Socks5InitialRequestDecoder() {
        super(State.INIT);
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: io.netty.handler.codec.DecoderException */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:15:0x00b3 A[Catch: Exception -> 0x00cf, TryCatch #0 {Exception -> 0x00cf, blocks: (B:2:0x0000, B:3:0x000e, B:4:0x0028, B:6:0x0039, B:7:0x0067, B:8:0x0068, B:11:0x007f, B:12:0x0091, B:13:0x00a8, B:15:0x00b3, B:16:0x00c3), top: B:21:0x0000 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void decode(io.netty.channel.ChannelHandlerContext r6, io.netty.buffer.ByteBuf r7, java.util.List<java.lang.Object> r8) throws java.lang.Exception {
        /*
            Method dump skipped, instruction units count: 217
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder.decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List):void");
    }

    private void fail(List<Object> out, Exception cause) {
        if (!(cause instanceof DecoderException)) {
            cause = new DecoderException(cause);
        }
        checkpoint(State.FAILURE);
        Socks5Message m = new DefaultSocks5InitialRequest(Socks5AuthMethod.NO_AUTH);
        m.setDecoderResult(DecoderResult.failure(cause));
        out.add(m);
    }
}
