package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.NetUtil;
import java.util.List;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socks/SocksCmdRequestDecoder.class */
public class SocksCmdRequestDecoder extends ReplayingDecoder<State> {
    private SocksCmdType cmdType;
    private SocksAddressType addressType;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socks/SocksCmdRequestDecoder$State.class */
    public enum State {
        CHECK_PROTOCOL_VERSION,
        READ_CMD_HEADER,
        READ_CMD_ADDRESS
    }

    public SocksCmdRequestDecoder() {
        super(State.CHECK_PROTOCOL_VERSION);
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        switch ((State) state()) {
            case CHECK_PROTOCOL_VERSION:
                if (byteBuf.readByte() != SocksProtocolVersion.SOCKS5.byteValue()) {
                    out.add(SocksCommonUtils.UNKNOWN_SOCKS_REQUEST);
                    ctx.pipeline().remove(this);
                    return;
                } else {
                    checkpoint(State.READ_CMD_HEADER);
                    break;
                }
            case READ_CMD_HEADER:
                this.cmdType = SocksCmdType.valueOf(byteBuf.readByte());
                byteBuf.skipBytes(1);
                this.addressType = SocksAddressType.valueOf(byteBuf.readByte());
                checkpoint(State.READ_CMD_ADDRESS);
            case READ_CMD_ADDRESS:
                switch (this.addressType) {
                    case IPv4:
                        String host = NetUtil.intToIpAddress(ByteBufUtil.readIntBE(byteBuf));
                        int port = ByteBufUtil.readUnsignedShortBE(byteBuf);
                        out.add(new SocksCmdRequest(this.cmdType, this.addressType, host, port));
                        break;
                    case DOMAIN:
                        int fieldLength = byteBuf.readByte();
                        String host2 = SocksCommonUtils.readUsAscii(byteBuf, fieldLength);
                        int port2 = ByteBufUtil.readUnsignedShortBE(byteBuf);
                        out.add(new SocksCmdRequest(this.cmdType, this.addressType, host2, port2));
                        break;
                    case IPv6:
                        byte[] bytes = new byte[16];
                        byteBuf.readBytes(bytes);
                        String host3 = SocksCommonUtils.ipv6toStr(bytes);
                        int port3 = ByteBufUtil.readUnsignedShortBE(byteBuf);
                        out.add(new SocksCmdRequest(this.cmdType, this.addressType, host3, port3));
                        break;
                    case UNKNOWN:
                        out.add(SocksCommonUtils.UNKNOWN_SOCKS_REQUEST);
                        break;
                    default:
                        throw new Error();
                }
                ctx.pipeline().remove(this);
                return;
            default:
                throw new Error();
        }
    }
}
