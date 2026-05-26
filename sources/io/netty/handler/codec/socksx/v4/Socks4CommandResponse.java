package io.netty.handler.codec.socksx.v4;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v4/Socks4CommandResponse.class */
public interface Socks4CommandResponse extends Socks4Message {
    Socks4CommandStatus status();

    String dstAddr();

    int dstPort();
}
