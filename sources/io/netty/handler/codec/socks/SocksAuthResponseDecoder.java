package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import java.util.List;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socks/SocksAuthResponseDecoder.class */
public class SocksAuthResponseDecoder extends ReplayingDecoder<State> {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socks/SocksAuthResponseDecoder$State.class */
    public enum State {
        CHECK_PROTOCOL_VERSION,
        READ_AUTH_RESPONSE
    }

    public SocksAuthResponseDecoder() {
        super(State.CHECK_PROTOCOL_VERSION);
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) throws Exception {
        switch ((State) state()) {
            case CHECK_PROTOCOL_VERSION:
                if (byteBuf.readByte() != SocksSubnegotiationVersion.AUTH_PASSWORD.byteValue()) {
                    out.add(SocksCommonUtils.UNKNOWN_SOCKS_RESPONSE);
                    channelHandlerContext.pipeline().remove(this);
                    return;
                } else {
                    checkpoint(State.READ_AUTH_RESPONSE);
                    break;
                }
            case READ_AUTH_RESPONSE:
                SocksAuthStatus authStatus = SocksAuthStatus.valueOf(byteBuf.readByte());
                out.add(new SocksAuthResponse(authStatus));
                channelHandlerContext.pipeline().remove(this);
                return;
            default:
                throw new Error();
        }
    }
}
