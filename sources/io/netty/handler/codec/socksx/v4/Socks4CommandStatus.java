package io.netty.handler.codec.socksx.v4;

import io.netty.util.internal.ObjectUtil;
import javassist.bytecode.Opcode;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v4/Socks4CommandStatus.class */
public class Socks4CommandStatus implements Comparable<Socks4CommandStatus> {
    public static final Socks4CommandStatus SUCCESS = new Socks4CommandStatus(90, "SUCCESS");
    public static final Socks4CommandStatus REJECTED_OR_FAILED = new Socks4CommandStatus(91, "REJECTED_OR_FAILED");
    public static final Socks4CommandStatus IDENTD_UNREACHABLE = new Socks4CommandStatus(92, "IDENTD_UNREACHABLE");
    public static final Socks4CommandStatus IDENTD_AUTH_FAILURE = new Socks4CommandStatus(93, "IDENTD_AUTH_FAILURE");
    private final byte byteValue;
    private final String name;
    private String text;

    public static Socks4CommandStatus valueOf(byte b) {
        switch (b) {
            case Opcode.DUP_X1 /* 90 */:
                return SUCCESS;
            case Opcode.DUP_X2 /* 91 */:
                return REJECTED_OR_FAILED;
            case Opcode.DUP2 /* 92 */:
                return IDENTD_UNREACHABLE;
            case Opcode.DUP2_X1 /* 93 */:
                return IDENTD_AUTH_FAILURE;
            default:
                return new Socks4CommandStatus(b);
        }
    }

    public Socks4CommandStatus(int byteValue) {
        this(byteValue, "UNKNOWN");
    }

    public Socks4CommandStatus(int byteValue, String name) {
        this.name = (String) ObjectUtil.checkNotNull(name, "name");
        this.byteValue = (byte) byteValue;
    }

    public byte byteValue() {
        return this.byteValue;
    }

    public boolean isSuccess() {
        return this.byteValue == 90;
    }

    public int hashCode() {
        return this.byteValue;
    }

    public boolean equals(Object obj) {
        return (obj instanceof Socks4CommandStatus) && this.byteValue == ((Socks4CommandStatus) obj).byteValue;
    }

    @Override // java.lang.Comparable
    public int compareTo(Socks4CommandStatus o) {
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
