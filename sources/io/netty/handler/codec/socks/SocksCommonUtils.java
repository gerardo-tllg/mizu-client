package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socks/SocksCommonUtils.class */
final class SocksCommonUtils {
    public static final SocksRequest UNKNOWN_SOCKS_REQUEST;
    public static final SocksResponse UNKNOWN_SOCKS_RESPONSE;
    private static final char ipv6hextetSeparator = ':';
    static final /* synthetic */ boolean $assertionsDisabled;

    static {
        $assertionsDisabled = !SocksCommonUtils.class.desiredAssertionStatus();
        UNKNOWN_SOCKS_REQUEST = new UnknownSocksRequest();
        UNKNOWN_SOCKS_RESPONSE = new UnknownSocksResponse();
    }

    private SocksCommonUtils() {
    }

    public static String ipv6toStr(byte[] src) {
        if (!$assertionsDisabled && src.length != 16) {
            throw new AssertionError();
        }
        StringBuilder sb = new StringBuilder(39);
        ipv6toStr(sb, src, 0, 8);
        return sb.toString();
    }

    private static void ipv6toStr(StringBuilder sb, byte[] src, int fromHextet, int toHextet) {
        int toHextet2 = toHextet - 1;
        int i = fromHextet;
        while (i < toHextet2) {
            appendHextet(sb, src, i);
            sb.append(':');
            i++;
        }
        appendHextet(sb, src, i);
    }

    private static void appendHextet(StringBuilder sb, byte[] src, int i) {
        StringUtil.toHexString(sb, src, i << 1, 2);
    }

    static String readUsAscii(ByteBuf buffer, int length) {
        String s = buffer.toString(buffer.readerIndex(), length, CharsetUtil.US_ASCII);
        buffer.skipBytes(length);
        return s;
    }
}
