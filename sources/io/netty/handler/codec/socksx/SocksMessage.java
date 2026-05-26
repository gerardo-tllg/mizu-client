package io.netty.handler.codec.socksx;

import io.netty.handler.codec.DecoderResultProvider;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/SocksMessage.class */
public interface SocksMessage extends DecoderResultProvider {
    SocksVersion version();
}
