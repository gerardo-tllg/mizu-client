package io.netty.handler.codec.socksx.v5;

import io.netty.handler.codec.DecoderResult;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/DefaultSocks5InitialResponse.class */
public class DefaultSocks5InitialResponse extends AbstractSocks5Message implements Socks5InitialResponse {
    private final Socks5AuthMethod authMethod;

    public DefaultSocks5InitialResponse(Socks5AuthMethod authMethod) {
        this.authMethod = (Socks5AuthMethod) ObjectUtil.checkNotNull(authMethod, "authMethod");
    }

    @Override // io.netty.handler.codec.socksx.v5.Socks5InitialResponse
    public Socks5AuthMethod authMethod() {
        return this.authMethod;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(StringUtil.simpleClassName(this));
        DecoderResult decoderResult = decoderResult();
        if (!decoderResult.isSuccess()) {
            buf.append("(decoderResult: ");
            buf.append(decoderResult);
            buf.append(", authMethod: ");
        } else {
            buf.append("(authMethod: ");
        }
        buf.append(authMethod());
        buf.append(')');
        return buf.toString();
    }
}
