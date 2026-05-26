package io.netty.handler.codec.socksx.v4;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v4/Socks4CommandRequest.class */
public interface Socks4CommandRequest extends Socks4Message {
    Socks4CommandType type();

    String userId();

    String dstAddr();

    int dstPort();
}
