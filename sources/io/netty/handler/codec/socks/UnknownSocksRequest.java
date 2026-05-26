package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socks/UnknownSocksRequest.class */
public final class UnknownSocksRequest extends SocksRequest {
    public UnknownSocksRequest() {
        super(SocksRequestType.UNKNOWN);
    }

    @Override // io.netty.handler.codec.socks.SocksMessage
    public void encodeAsByteBuf(ByteBuf byteBuf) {
    }
}
