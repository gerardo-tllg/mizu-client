package io.netty.handler.codec.socksx.v4;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.CharsetUtil;
import java.util.List;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v4/Socks4ServerDecoder.class */
public class Socks4ServerDecoder extends ReplayingDecoder<State> {
    private static final int MAX_FIELD_LENGTH = 255;
    private Socks4CommandType type;
    private String dstAddr;
    private int dstPort;
    private String userId;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v4/Socks4ServerDecoder$State.class */
    public enum State {
        START,
        READ_USERID,
        READ_DOMAIN,
        SUCCESS,
        FAILURE
    }

    public Socks4ServerDecoder() {
        super(State.START);
        setSingleDecode(true);
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: io.netty.handler.codec.DecoderException */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:12:0x009f A[Catch: Exception -> 0x0101, TryCatch #0 {Exception -> 0x0101, blocks: (B:2:0x0000, B:3:0x000e, B:4:0x0030, B:6:0x0041, B:7:0x005c, B:8:0x005d, B:9:0x0082, B:10:0x0093, B:12:0x009f, B:14:0x00ab, B:15:0x00b5, B:16:0x00da, B:18:0x00e5, B:19:0x00f5), top: B:24:0x0000 }] */
    /* JADX WARN: Removed duplicated region for block: B:18:0x00e5 A[Catch: Exception -> 0x0101, TryCatch #0 {Exception -> 0x0101, blocks: (B:2:0x0000, B:3:0x000e, B:4:0x0030, B:6:0x0041, B:7:0x005c, B:8:0x005d, B:9:0x0082, B:10:0x0093, B:12:0x009f, B:14:0x00ab, B:15:0x00b5, B:16:0x00da, B:18:0x00e5, B:19:0x00f5), top: B:24:0x0000 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void decode(io.netty.channel.ChannelHandlerContext r9, io.netty.buffer.ByteBuf r10, java.util.List<java.lang.Object> r11) throws java.lang.Exception {
        /*
            Method dump skipped, instruction units count: 267
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: io.netty.handler.codec.socksx.v4.Socks4ServerDecoder.decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List):void");
    }

    private void fail(List<Object> out, Exception cause) {
        if (!(cause instanceof DecoderException)) {
            cause = new DecoderException(cause);
        }
        Socks4CommandRequest m = new DefaultSocks4CommandRequest(this.type != null ? this.type : Socks4CommandType.CONNECT, this.dstAddr != null ? this.dstAddr : "", this.dstPort != 0 ? this.dstPort : 65535, this.userId != null ? this.userId : "");
        m.setDecoderResult(DecoderResult.failure(cause));
        out.add(m);
        checkpoint(State.FAILURE);
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: io.netty.handler.codec.DecoderException */
    private static String readString(String fieldName, ByteBuf in) throws DecoderException {
        int length = in.bytesBefore(256, (byte) 0);
        if (length < 0) {
            throw new DecoderException("field '" + fieldName + "' longer than " + MAX_FIELD_LENGTH + " chars");
        }
        String value = in.readSlice(length).toString(CharsetUtil.US_ASCII);
        in.skipBytes(1);
        return value;
    }
}
