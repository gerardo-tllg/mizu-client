package io.netty.handler.codec.socksx.v5;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import java.util.List;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5PasswordAuthResponseDecoder.class */
public class Socks5PasswordAuthResponseDecoder extends ReplayingDecoder<State> {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5PasswordAuthResponseDecoder$State.class */
    public enum State {
        INIT,
        SUCCESS,
        FAILURE
    }

    public Socks5PasswordAuthResponseDecoder() {
        super(State.INIT);
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: io.netty.handler.codec.DecoderException */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:11:0x007c A[Catch: Exception -> 0x0098, TryCatch #0 {Exception -> 0x0098, blocks: (B:2:0x0000, B:3:0x000e, B:4:0x0028, B:6:0x0034, B:7:0x0054, B:8:0x0055, B:9:0x0071, B:11:0x007c, B:12:0x008c), top: B:17:0x0000 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void decode(io.netty.channel.ChannelHandlerContext r6, io.netty.buffer.ByteBuf r7, java.util.List<java.lang.Object> r8) throws java.lang.Exception {
        /*
            r5 = this;
            int[] r0 = io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponseDecoder.AnonymousClass1.$SwitchMap$io$netty$handler$codec$socksx$v5$Socks5PasswordAuthResponseDecoder$State     // Catch: java.lang.Exception -> L98
            r1 = r5
            java.lang.Object r1 = r1.state()     // Catch: java.lang.Exception -> L98
            io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponseDecoder$State r1 = (io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponseDecoder.State) r1     // Catch: java.lang.Exception -> L98
            int r1 = r1.ordinal()     // Catch: java.lang.Exception -> L98
            r0 = r0[r1]     // Catch: java.lang.Exception -> L98
            switch(r0) {
                case 1: goto L28;
                case 2: goto L71;
                case 3: goto L8c;
                default: goto L95;
            }     // Catch: java.lang.Exception -> L98
        L28:
            r0 = r7
            byte r0 = r0.readByte()     // Catch: java.lang.Exception -> L98
            r9 = r0
            r0 = r9
            r1 = 1
            if (r0 == r1) goto L55
            io.netty.handler.codec.DecoderException r0 = new io.netty.handler.codec.DecoderException     // Catch: java.lang.Exception -> L98
            r1 = r0
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> L98
            r3 = r2
            r3.<init>()     // Catch: java.lang.Exception -> L98
            java.lang.String r3 = "unsupported subnegotiation version: "
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> L98
            r3 = r9
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> L98
            java.lang.String r3 = " (expected: 1)"
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> L98
            java.lang.String r2 = r2.toString()     // Catch: java.lang.Exception -> L98
            r1.<init>(r2)     // Catch: java.lang.Exception -> L98
            throw r0     // Catch: java.lang.Exception -> L98
        L55:
            r0 = r8
            io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthResponse r1 = new io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthResponse     // Catch: java.lang.Exception -> L98
            r2 = r1
            r3 = r7
            byte r3 = r3.readByte()     // Catch: java.lang.Exception -> L98
            io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus r3 = io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus.valueOf(r3)     // Catch: java.lang.Exception -> L98
            r2.<init>(r3)     // Catch: java.lang.Exception -> L98
            boolean r0 = r0.add(r1)     // Catch: java.lang.Exception -> L98
            r0 = r5
            io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponseDecoder$State r1 = io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponseDecoder.State.SUCCESS     // Catch: java.lang.Exception -> L98
            r0.checkpoint(r1)     // Catch: java.lang.Exception -> L98
        L71:
            r0 = r5
            int r0 = r0.actualReadableBytes()     // Catch: java.lang.Exception -> L98
            r9 = r0
            r0 = r9
            if (r0 <= 0) goto L95
            r0 = r8
            r1 = r7
            r2 = r9
            io.netty.buffer.ByteBuf r1 = r1.readRetainedSlice(r2)     // Catch: java.lang.Exception -> L98
            boolean r0 = r0.add(r1)     // Catch: java.lang.Exception -> L98
            goto L95
        L8c:
            r0 = r7
            r1 = r5
            int r1 = r1.actualReadableBytes()     // Catch: java.lang.Exception -> L98
            io.netty.buffer.ByteBuf r0 = r0.skipBytes(r1)     // Catch: java.lang.Exception -> L98
        L95:
            goto La1
        L98:
            r9 = move-exception
            r0 = r5
            r1 = r8
            r2 = r9
            r0.fail(r1, r2)
        La1:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponseDecoder.decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List):void");
    }

    private void fail(List<Object> out, Exception cause) {
        if (!(cause instanceof DecoderException)) {
            cause = new DecoderException(cause);
        }
        checkpoint(State.FAILURE);
        Socks5Message m = new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE);
        m.setDecoderResult(DecoderResult.failure(cause));
        out.add(m);
    }
}
