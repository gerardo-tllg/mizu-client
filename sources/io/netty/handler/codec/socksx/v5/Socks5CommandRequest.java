package io.netty.handler.codec.socksx.v5;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5CommandRequest.class */
public interface Socks5CommandRequest extends Socks5Message {
    Socks5CommandType type();

    Socks5AddressType dstAddrType();

    String dstAddr();

    int dstPort();
}
