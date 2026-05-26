package io.netty.handler.codec.socksx.v4;

import io.netty.util.internal.ObjectUtil;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v4/Socks4CommandType.class */
public class Socks4CommandType implements Comparable<Socks4CommandType> {
    public static final Socks4CommandType CONNECT = new Socks4CommandType(1, "CONNECT");
    public static final Socks4CommandType BIND = new Socks4CommandType(2, "BIND");
    private final byte byteValue;
    private final String name;
    private String text;

    public static Socks4CommandType valueOf(byte b) {
        switch (b) {
            case 1:
                return CONNECT;
            case 2:
                return BIND;
            default:
                return new Socks4CommandType(b);
        }
    }

    public Socks4CommandType(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks4CommandType(int byteValue, String name) {
        this.name = (String) ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    public byte byteValue() {
        return this.byteValue;
    }

    public int hashCode() {
        return this.byteValue;
    }

    public boolean equals(Object obj) {
        return (obj instanceof Socks4CommandType) && this.byteValue == ((Socks4CommandType) obj).byteValue;
    }

    @Override // java.lang.Comparable
    public int compareTo(Socks4CommandType o) {
        return this.byteValue - o.byteValue;
    }

    public String toString() {
        String text = this.text;
        if (text == null) {
            String str = this.name + '(' + (this.byteValue & 255) + ')';
            text = str;
            this.text = str;
        }
        return text;
    }
}
