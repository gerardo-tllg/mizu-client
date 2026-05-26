package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socks/SocksInitRequestDecoder.class */
public class SocksInitRequestDecoder extends ReplayingDecoder<State> {

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socks/SocksInitRequestDecoder$State.class */
    public enum State {
        CHECK_PROTOCOL_VERSION,
        READ_AUTH_SCHEMES
    }

    public SocksInitRequestDecoder() {
        super(State.CHECK_PROTOCOL_VERSION);
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        List<SocksAuthScheme> authSchemes;
        switch ((State) state()) {
            case CHECK_PROTOCOL_VERSION:
                if (byteBuf.readByte() != SocksProtocolVersion.SOCKS5.byteValue()) {
                    out.add(SocksCommonUtils.UNKNOWN_SOCKS_REQUEST);
                    ctx.pipeline().remove(this);
                    return;
                } else {
                    checkpoint(State.READ_AUTH_SCHEMES);
                    break;
                }
            case READ_AUTH_SCHEMES:
                byte authSchemeNum = byteBuf.readByte();
                if (authSchemeNum > 0) {
                    authSchemes = new ArrayList<>(authSchemeNum);
                    for (int i = 0; i < authSchemeNum; i++) {
                        authSchemes.add(SocksAuthScheme.valueOf(byteBuf.readByte()));
                    }
                } else {
                    authSchemes = Collections.emptyList();
                }
                out.add(new SocksInitRequest(authSchemes));
                ctx.pipeline().remove(this);
                return;
            default:
                throw new Error();
        }
    }
}
