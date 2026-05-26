package io.netty.handler.codec.socksx.v5;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5PasswordAuthRequest.class */
public interface Socks5PasswordAuthRequest extends Socks5Message {
    String username();

    String password();
}
