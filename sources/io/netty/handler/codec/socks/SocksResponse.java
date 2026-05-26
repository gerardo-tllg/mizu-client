package io.netty.handler.codec.socks;

import io.netty.util.internal.ObjectUtil;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socks/SocksResponse.class */
public abstract class SocksResponse extends SocksMessage {
    private final SocksResponseType responseType;

    protected SocksResponse(SocksResponseType responseType) {
        super(SocksMessageType.RESPONSE);
        this.responseType = (SocksResponseType) ObjectUtil.checkNotNull(responseType, "responseType");
    }

    public SocksResponseType responseType() {
        return this.responseType;
    }
}
