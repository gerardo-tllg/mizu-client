package io.netty.handler.codec.socks;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socks/SocksAuthStatus.class */
public enum SocksAuthStatus {
    SUCCESS((byte) 0),
    FAILURE((byte) -1);

    private final byte b;

    SocksAuthStatus(byte b) {
        this.b = b;
    }

    @Deprecated
    public static SocksAuthStatus fromByte(byte b) {
        return valueOf(b);
    }

    public static SocksAuthStatus valueOf(byte b) {
        for (SocksAuthStatus code : values()) {
            if (code.b == b) {
                return code;
            }
        }
        return FAILURE;
    }

    public byte byteValue() {
        return this.b;
    }
}
