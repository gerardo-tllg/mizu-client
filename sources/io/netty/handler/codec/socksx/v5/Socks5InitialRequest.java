package io.netty.handler.codec.socksx.v5;

import java.util.List;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5InitialRequest.class */
public interface Socks5InitialRequest extends Socks5Message {
    List<Socks5AuthMethod> authMethods();
}
