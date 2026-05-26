package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import java.util.List;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socks/SocksInitResponseDecoder.class */
public class SocksInitResponseDecoder extends ReplayingDecoder<State> {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socks/SocksInitResponseDecoder$State.class */
    public enum State {
        CHECK_PROTOCOL_VERSION,
        READ_PREFERRED_AUTH_TYPE
    }

    public SocksInitResponseDecoder() {
        super(State.CHECK_PROTOCOL_VERSION);
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        switch ((State) state()) {
            case CHECK_PROTOCOL_VERSION:
                if (byteBuf.readByte() != SocksProtocolVersion.SOCKS5.byteValue()) {
                    out.add(SocksCommonUtils.UNKNOWN_SOCKS_RESPONSE);
                    ctx.pipeline().remove(this);
                    return;
                } else {
                    checkpoint(State.READ_PREFERRED_AUTH_TYPE);
                    break;
                }
            case READ_PREFERRED_AUTH_TYPE:
                SocksAuthScheme authScheme = SocksAuthScheme.valueOf(byteBuf.readByte());
                out.add(new SocksInitResponse(authScheme));
                ctx.pipeline().remove(this);
                return;
            default:
                throw new Error();
        }
    }
}
