package io.netty.handler.codec.socksx.v4;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import java.util.List;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v4/Socks4ClientDecoder.class */
public class Socks4ClientDecoder extends ReplayingDecoder<State> {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v4/Socks4ClientDecoder$State.class */
    public enum State {
        START,
        SUCCESS,
        FAILURE
    }

    public Socks4ClientDecoder() {
        super(State.START);
        setSingleDecode(true);
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: io.netty.handler.codec.DecoderException */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0092 A[Catch: Exception -> 0x00ae, TryCatch #0 {Exception -> 0x00ae, blocks: (B:2:0x0000, B:3:0x000e, B:4:0x0028, B:6:0x0033, B:7:0x0053, B:8:0x0054, B:9:0x0087, B:11:0x0092, B:12:0x00a2), top: B:17:0x0000 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void decode(io.netty.channel.ChannelHandlerContext r8, io.netty.buffer.ByteBuf r9, java.util.List<java.lang.Object> r10) throws java.lang.Exception {
        /*
            r7 = this;
            int[] r0 = io.netty.handler.codec.socksx.v4.Socks4ClientDecoder.AnonymousClass1.$SwitchMap$io$netty$handler$codec$socksx$v4$Socks4ClientDecoder$State     // Catch: java.lang.Exception -> Lae
            r1 = r7
            java.lang.Object r1 = r1.state()     // Catch: java.lang.Exception -> Lae
            io.netty.handler.codec.socksx.v4.Socks4ClientDecoder$State r1 = (io.netty.handler.codec.socksx.v4.Socks4ClientDecoder.State) r1     // Catch: java.lang.Exception -> Lae
            int r1 = r1.ordinal()     // Catch: java.lang.Exception -> Lae
            r0 = r0[r1]     // Catch: java.lang.Exception -> Lae
            switch(r0) {
                case 1: goto L28;
                case 2: goto L87;
                case 3: goto La2;
                default: goto Lab;
            }     // Catch: java.lang.Exception -> Lae
        L28:
            r0 = r9
            short r0 = r0.readUnsignedByte()     // Catch: java.lang.Exception -> Lae
            r11 = r0
            r0 = r11
            if (r0 == 0) goto L54
            io.netty.handler.codec.DecoderException r0 = new io.netty.handler.codec.DecoderException     // Catch: java.lang.Exception -> Lae
            r1 = r0
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> Lae
            r3 = r2
            r3.<init>()     // Catch: java.lang.Exception -> Lae
            java.lang.String r3 = "unsupported reply version: "
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> Lae
            r3 = r11
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> Lae
            java.lang.String r3 = " (expected: 0)"
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch: java.lang.Exception -> Lae
            java.lang.String r2 = r2.toString()     // Catch: java.lang.Exception -> Lae
            r1.<init>(r2)     // Catch: java.lang.Exception -> Lae
            throw r0     // Catch: java.lang.Exception -> Lae
        L54:
            r0 = r9
            byte r0 = r0.readByte()     // Catch: java.lang.Exception -> Lae
            io.netty.handler.codec.socksx.v4.Socks4CommandStatus r0 = io.netty.handler.codec.socksx.v4.Socks4CommandStatus.valueOf(r0)     // Catch: java.lang.Exception -> Lae
            r12 = r0
            r0 = r9
            int r0 = io.netty.buffer.ByteBufUtil.readUnsignedShortBE(r0)     // Catch: java.lang.Exception -> Lae
            r13 = r0
            r0 = r9
            int r0 = io.netty.buffer.ByteBufUtil.readIntBE(r0)     // Catch: java.lang.Exception -> Lae
            java.lang.String r0 = io.netty.util.NetUtil.intToIpAddress(r0)     // Catch: java.lang.Exception -> Lae
            r14 = r0
            r0 = r10
            io.netty.handler.codec.socksx.v4.DefaultSocks4CommandResponse r1 = new io.netty.handler.codec.socksx.v4.DefaultSocks4CommandResponse     // Catch: java.lang.Exception -> Lae
            r2 = r1
            r3 = r12
            r4 = r14
            r5 = r13
            r2.<init>(r3, r4, r5)     // Catch: java.lang.Exception -> Lae
            boolean r0 = r0.add(r1)     // Catch: java.lang.Exception -> Lae
            r0 = r7
            io.netty.handler.codec.socksx.v4.Socks4ClientDecoder$State r1 = io.netty.handler.codec.socksx.v4.Socks4ClientDecoder.State.SUCCESS     // Catch: java.lang.Exception -> Lae
            r0.checkpoint(r1)     // Catch: java.lang.Exception -> Lae
        L87:
            r0 = r7
            int r0 = r0.actualReadableBytes()     // Catch: java.lang.Exception -> Lae
            r11 = r0
            r0 = r11
            if (r0 <= 0) goto Lab
            r0 = r10
            r1 = r9
            r2 = r11
            io.netty.buffer.ByteBuf r1 = r1.readRetainedSlice(r2)     // Catch: java.lang.Exception -> Lae
            boolean r0 = r0.add(r1)     // Catch: java.lang.Exception -> Lae
            goto Lab
        La2:
            r0 = r9
            r1 = r7
            int r1 = r1.actualReadableBytes()     // Catch: java.lang.Exception -> Lae
            io.netty.buffer.ByteBuf r0 = r0.skipBytes(r1)     // Catch: java.lang.Exception -> Lae
        Lab:
            goto Lb7
        Lae:
            r11 = move-exception
            r0 = r7
            r1 = r10
            r2 = r11
            r0.fail(r1, r2)
        Lb7:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: io.netty.handler.codec.socksx.v4.Socks4ClientDecoder.decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List):void");
    }

    private void fail(List<Object> out, Exception cause) {
        if (!(cause instanceof DecoderException)) {
            cause = new DecoderException(cause);
        }
        Socks4CommandResponse m = new DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED);
        m.setDecoderResult(DecoderResult.failure(cause));
        out.add(m);
        checkpoint(State.FAILURE);
    }
}
