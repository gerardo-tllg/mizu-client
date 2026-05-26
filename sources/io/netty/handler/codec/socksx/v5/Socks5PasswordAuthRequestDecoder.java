package io.netty.handler.codec.socksx.v5;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import java.util.List;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5PasswordAuthRequestDecoder.class */
public class Socks5PasswordAuthRequestDecoder extends ReplayingDecoder<State> {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5PasswordAuthRequestDecoder$State.class */
    public enum State {
        INIT,
        SUCCESS,
        FAILURE
    }

    public Socks5PasswordAuthRequestDecoder() {
        super(State.INIT);
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: io.netty.handler.codec.DecoderException */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:11:0x00c1 A[Catch: Exception -> 0x00dd, TryCatch #0 {Exception -> 0x00dd, blocks: (B:2:0x0000, B:3:0x000e, B:4:0x0028, B:6:0x003c, B:7:0x005c, B:8:0x005d, B:9:0x00b6, B:11:0x00c1, B:12:0x00d1), top: B:17:0x0000 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void decode(io.netty.channel.ChannelHandlerContext r10, io.netty.buffer.ByteBuf r11, java.util.List<java.lang.Object> r12) throws java.lang.Exception {
        /*
            Method dump skipped, instruction units count: 231
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder.decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List):void");
    }

    private void fail(List<Object> out, Exception cause) {
        if (!(cause instanceof DecoderException)) {
            cause = new DecoderException(cause);
        }
        checkpoint(State.FAILURE);
        Socks5Message m = new DefaultSocks5PasswordAuthRequest("", "");
        m.setDecoderResult(DecoderResult.failure(cause));
        out.add(m);
    }
}
