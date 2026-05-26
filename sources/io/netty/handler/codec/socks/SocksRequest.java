package io.netty.handler.codec.socks;

import io.netty.util.internal.ObjectUtil;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socks/SocksRequest.class */
public abstract class SocksRequest extends SocksMessage {
    private final SocksRequestType requestType;

    protected SocksRequest(SocksRequestType requestType) {
        super(SocksMessageType.REQUEST);
        this.requestType = (SocksRequestType) ObjectUtil.checkNotNull(requestType, "requestType");
    }

    public SocksRequestType requestType() {
        return this.requestType;
    }
}
