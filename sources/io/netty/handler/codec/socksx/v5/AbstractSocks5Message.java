package io.netty.handler.codec.socksx.v5;

import io.netty.handler.codec.socksx.AbstractSocksMessage;
import io.netty.handler.codec.socksx.SocksVersion;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/AbstractSocks5Message.class */
public abstract class AbstractSocks5Message extends AbstractSocksMessage implements Socks5Message {
    @Override // io.netty.handler.codec.socksx.SocksMessage
    public final SocksVersion version() {
        return SocksVersion.SOCKS5;
    }
}
