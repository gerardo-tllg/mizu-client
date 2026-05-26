package io.netty.handler.codec.socksx.v5;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import java.util.List;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5InitialResponseDecoder.class */
public class Socks5InitialResponseDecoder extends ReplayingDecoder<State> {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5InitialResponseDecoder$State.class */
    public enum State {
        INIT,
        SUCCESS,
        FAILURE
    }

    public Socks5InitialResponseDecoder() {
        super(State.INIT);
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: io.netty.handler.codec.DecoderException */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0093 A[Catch: Exception -> 0x00af, TryCatch #0 {Exception -> 0x00af, blocks: (B:2:0x0000, B:3:0x000e, B:4:0x0028, B:6:0x0039, B:7:0x0067, B:8:0x0068, B:9:0x0088, B:11:0x0093, B:12:0x00a3), top: B:17:0x0000 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void decode(io.netty.channel.ChannelHandlerContext r6, io.netty.buffer.ByteBuf r7, java.util.List<java.lang.Object> r8) throws java.lang.Exception {
        /*
            r5 = this;
            int[] r0 = io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder.AnonymousClass1.$SwitchMap$io$netty$handler$codec$socksx$v5$Socks5InitialResponseDecoder$State     // Catch: java.lang.Exception -> Laf
            r1 = r5
            java.lang.Object r1 = r1.state()     // Catch: java.lang.Exception -> Laf
            io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder$State r1 = (io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder.State) r1     // Catch: java.lang.Exception -> Laf
            int r1 = r1.ordinal()     // Catch: java.lang.Exception -> Laf
            r0 = r0[r1]     // Catch: java.lang.Exception -> Laf
            switch(r0) {
                case 1: goto L28;
                case 2: goto L88;
                case 3: goto La3;
                default: goto Lac;
            }     // Catch: java.lang.Exception -> Laf
        L28:
            r0 = r7
            byte r0 = r0.readByte()     // Catch: java.lang.Exception -> Laf
            r9 = r0
            r0 = r9
            io.netty.handler.codec.socksx.SocksVersion r1 = io.netty.handler.codec.socksx.SocksVersion.SOCKS5     // Catch: java.lang.Exception -> Laf
            byte r1 = r1.byteValue()     // Catch: java.lang.Exception -> Laf
            if (r0 == r1) goto L68
            io.netty.handler.codec.DecoderException r0 = new io.netty.handler.codec.DecoderException     // Catch: java.lang.Exception -> Laf
            r1 = r0
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> Laf
            r3 = r2
            r3.<init>()     // Catch: java.lang.Exception -> Laf
            java.lang.String r3 = "unsupported version: "
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> Laf
            r3 = r9
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> Laf
            java.lang.String r3 = " (expected: "
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> Laf
            io.netty.handler.codec.socksx.SocksVersion r3 = io.netty.handler.codec.socksx.SocksVersion.SOCKS5     // Catch: java.lang.Exception -> Laf
            byte r3 = r3.byteValue()     // Catch: java.lang.Exception -> Laf
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> Laf
            r3 = 41
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> Laf
            java.lang.String r2 = r2.toString()     // Catch: java.lang.Exception -> Laf
            r1.<init>(r2)     // Catch: java.lang.Exception -> Laf
            throw r0     // Catch: java.lang.Exception -> Laf
        L68:
            r0 = r7
            byte r0 = r0.readByte()     // Catch: java.lang.Exception -> Laf
            io.netty.handler.codec.socksx.v5.Socks5AuthMethod r0 = io.netty.handler.codec.socksx.v5.Socks5AuthMethod.valueOf(r0)     // Catch: java.lang.Exception -> Laf
            r10 = r0
            r0 = r8
            io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse r1 = new io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse     // Catch: java.lang.Exception -> Laf
            r2 = r1
            r3 = r10
            r2.<init>(r3)     // Catch: java.lang.Exception -> Laf
            boolean r0 = r0.add(r1)     // Catch: java.lang.Exception -> Laf
            r0 = r5
            io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder$State r1 = io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder.State.SUCCESS     // Catch: java.lang.Exception -> Laf
            r0.checkpoint(r1)     // Catch: java.lang.Exception -> Laf
        L88:
            r0 = r5
            int r0 = r0.actualReadableBytes()     // Catch: java.lang.Exception -> Laf
            r9 = r0
            r0 = r9
            if (r0 <= 0) goto Lac
            r0 = r8
            r1 = r7
            r2 = r9
            io.netty.buffer.ByteBuf r1 = r1.readRetainedSlice(r2)     // Catch: java.lang.Exception -> Laf
            boolean r0 = r0.add(r1)     // Catch: java.lang.Exception -> Laf
            goto Lac
        La3:
            r0 = r7
            r1 = r5
            int r1 = r1.actualReadableBytes()     // Catch: java.lang.Exception -> Laf
            io.netty.buffer.ByteBuf r0 = r0.skipBytes(r1)     // Catch: java.lang.Exception -> Laf
        Lac:
            goto Lb8
        Laf:
            r9 = move-exception
            r0 = r5
            r1 = r8
            r2 = r9
            r0.fail(r1, r2)
        Lb8:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder.decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List):void");
    }

    private void fail(List<Object> out, Exception cause) {
        if (!(cause instanceof DecoderException)) {
            cause = new DecoderException(cause);
        }
        checkpoint(State.FAILURE);
        Socks5Message m = new DefaultSocks5InitialResponse(Socks5AuthMethod.UNACCEPTED);
        m.setDecoderResult(DecoderResult.failure(cause));
        out.add(m);
    }
}
