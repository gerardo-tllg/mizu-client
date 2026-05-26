package io.netty.handler.codec.socksx.v5;

import io.netty.util.internal.ObjectUtil;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5AddressType.class */
public class Socks5AddressType implements Comparable<Socks5AddressType> {
    public static final Socks5AddressType IPv4 = new Socks5AddressType(1, "IPv4");
    public static final Socks5AddressType DOMAIN = new Socks5AddressType(3, "DOMAIN");
    public static final Socks5AddressType IPv6 = new Socks5AddressType(4, "IPv6");
    private final byte byteValue;
    private final String name;
    private String text;

    public static Socks5AddressType valueOf(byte b) {
        switch (b) {
            case 1:
                return IPv4;
            case 2:
            default:
                return new Socks5AddressType(b);
            case 3:
                return DOMAIN;
            case 4:
                return IPv6;
        }
    }

    public Socks5AddressType(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks5AddressType(int byteValue, String name) {
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
        return (obj instanceof Socks5AddressType) && this.byteValue == ((Socks5AddressType) obj).byteValue;
    }

    @Override // java.lang.Comparable
    public int compareTo(Socks5AddressType o) {
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
