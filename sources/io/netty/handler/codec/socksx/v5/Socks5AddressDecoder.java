package io.netty.handler.codec.socksx.v5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/Socks5AddressDecoder.class */
public interface Socks5AddressDecoder {
    public static final Socks5AddressDecoder DEFAULT = new Socks5AddressDecoder() { // from class: io.netty.handler.codec.socksx.v5.Socks5AddressDecoder.1
        private static final int IPv6_LEN = 16;

        /* JADX INFO: Thrown type has an unknown type hierarchy: io.netty.handler.codec.DecoderException */
        @Override // io.netty.handler.codec.socksx.v5.Socks5AddressDecoder
        public String decodeAddress(Socks5AddressType addrType, ByteBuf in) throws Exception {
            if (addrType == Socks5AddressType.IPv4) {
                return NetUtil.intToIpAddress(ByteBufUtil.readIntBE(in));
            }
            if (addrType == Socks5AddressType.DOMAIN) {
                int length = in.readUnsignedByte();
                String domain = in.toString(in.readerIndex(), length, CharsetUtil.US_ASCII);
                in.skipBytes(length);
                return domain;
            }
            if (addrType == Socks5AddressType.IPv6) {
                if (in.hasArray()) {
                    int readerIdx = in.readerIndex();
                    in.readerIndex(readerIdx + 16);
                    return NetUtil.bytesToIpAddress(in.array(), in.arrayOffset() + readerIdx, 16);
                }
                byte[] tmp = new byte[16];
                in.readBytes(tmp);
                return NetUtil.bytesToIpAddress(tmp);
            }
            throw new DecoderException("unsupported address type: " + (addrType.byteValue() & 255));
        }
    };

    String decodeAddress(Socks5AddressType socks5AddressType, ByteBuf byteBuf) throws Exception;
}
