package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.ObjectUtil;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socks/SocksInitResponse.class */
public final class SocksInitResponse extends SocksResponse {
    private final SocksAuthScheme authScheme;

    public SocksInitResponse(SocksAuthScheme authScheme) {
        super(SocksResponseType.INIT);
        this.authScheme = (SocksAuthScheme) ObjectUtil.checkNotNull(authScheme, "authScheme");
    }

    public SocksAuthScheme authScheme() {
        return this.authScheme;
    }

    @Override // io.netty.handler.codec.socks.SocksMessage
    public void encodeAsByteBuf(ByteBuf byteBuf) {
        byteBuf.writeByte(protocolVersion().byteValue());
        byteBuf.writeByte(this.authScheme.byteValue());
    }
}
