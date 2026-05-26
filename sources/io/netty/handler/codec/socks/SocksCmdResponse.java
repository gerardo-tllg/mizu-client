package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import io.netty.util.internal.ObjectUtil;
import java.net.IDN;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socks/SocksCmdResponse.class */
public final class SocksCmdResponse extends SocksResponse {
    private final SocksCmdStatus cmdStatus;
    private final SocksAddressType addressType;
    private final String host;
    private final int port;
    private static final byte[] DOMAIN_ZEROED = {0};
    private static final byte[] IPv4_HOSTNAME_ZEROED = {0, 0, 0, 0};
    private static final byte[] IPv6_HOSTNAME_ZEROED = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public SocksCmdResponse(SocksCmdStatus cmdStatus, SocksAddressType addressType) {
        this(cmdStatus, addressType, null, 0);
    }

    public SocksCmdResponse(SocksCmdStatus cmdStatus, SocksAddressType addressType, String host, int port) {
        super(SocksResponseType.CMD);
        ObjectUtil.checkNotNull(cmdStatus, "cmdStatus");
        ObjectUtil.checkNotNull(addressType, "addressType");
        if (host != null) {
            switch (addressType) {
                case IPv4:
                    if (!NetUtil.isValidIpV4Address(host)) {
                        throw new IllegalArgumentException(host + " is not a valid IPv4 address");
                    }
                    break;
                case DOMAIN:
                    String asciiHost = IDN.toASCII(host);
                    if (asciiHost.length() > 255) {
                        throw new IllegalArgumentException(host + " IDN: " + asciiHost + " exceeds 255 char limit");
                    }
                    host = asciiHost;
                    break;
                    break;
                case IPv6:
                    if (!NetUtil.isValidIpV6Address(host)) {
                        throw new IllegalArgumentException(host + " is not a valid IPv6 address");
                    }
                    break;
            }
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException(port + " is not in bounds 0 <= x <= 65535");
        }
        this.cmdStatus = cmdStatus;
        this.addressType = addressType;
        this.host = host;
        this.port = port;
    }

    public SocksCmdStatus cmdStatus() {
        return this.cmdStatus;
    }

    public SocksAddressType addressType() {
        return this.addressType;
    }

    public String host() {
        return (this.host == null || this.addressType != SocksAddressType.DOMAIN) ? this.host : IDN.toUnicode(this.host);
    }

    public int port() {
        return this.port;
    }

    @Override // io.netty.handler.codec.socks.SocksMessage
    public void encodeAsByteBuf(ByteBuf byteBuf) {
        byteBuf.writeByte(protocolVersion().byteValue());
        byteBuf.writeByte(this.cmdStatus.byteValue());
        byteBuf.writeByte(0);
        byteBuf.writeByte(this.addressType.byteValue());
        switch (this.addressType) {
            case IPv4:
                byte[] hostContent = this.host == null ? IPv4_HOSTNAME_ZEROED : NetUtil.createByteArrayFromIpAddressString(this.host);
                byteBuf.writeBytes(hostContent);
                ByteBufUtil.writeShortBE(byteBuf, this.port);
                break;
            case DOMAIN:
                if (this.host != null) {
                    byteBuf.writeByte(this.host.length());
                    byteBuf.writeCharSequence(this.host, CharsetUtil.US_ASCII);
                } else {
                    byteBuf.writeByte(DOMAIN_ZEROED.length);
                    byteBuf.writeBytes(DOMAIN_ZEROED);
                }
                ByteBufUtil.writeShortBE(byteBuf, this.port);
                break;
            case IPv6:
                byte[] hostContent2 = this.host == null ? IPv6_HOSTNAME_ZEROED : NetUtil.createByteArrayFromIpAddressString(this.host);
                byteBuf.writeBytes(hostContent2);
                ByteBufUtil.writeShortBE(byteBuf, this.port);
                break;
        }
    }
}
