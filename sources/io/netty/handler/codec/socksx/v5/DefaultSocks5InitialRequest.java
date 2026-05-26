package io.netty.handler.codec.socksx.v5;

import io.netty.handler.codec.DecoderResult;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/codec/socksx/v5/DefaultSocks5InitialRequest.class */
public class DefaultSocks5InitialRequest extends AbstractSocks5Message implements Socks5InitialRequest {
    private final List<Socks5AuthMethod> authMethods;

    public DefaultSocks5InitialRequest(Socks5AuthMethod... authMethods) {
        Socks5AuthMethod m;
        ObjectUtil.checkNotNull(authMethods, "authMethods");
        List<Socks5AuthMethod> list = new ArrayList<>(authMethods.length);
        int length = authMethods.length;
        for (int i = 0; i < length && (m = authMethods[i]) != null; i++) {
            list.add(m);
        }
        this.authMethods = Collections.unmodifiableList((List) ObjectUtil.checkNonEmpty(list, "list"));
    }

    public DefaultSocks5InitialRequest(Iterable<Socks5AuthMethod> authMethods) {
        Socks5AuthMethod m;
        ObjectUtil.checkNotNull(authMethods, "authSchemes");
        List<Socks5AuthMethod> list = new ArrayList<>();
        Iterator<Socks5AuthMethod> it = authMethods.iterator();
        while (it.hasNext() && (m = it.next()) != null) {
            list.add(m);
        }
        this.authMethods = Collections.unmodifiableList((List) ObjectUtil.checkNonEmpty(list, "list"));
    }

    @Override // io.netty.handler.codec.socksx.v5.Socks5InitialRequest
    public List<Socks5AuthMethod> authMethods() {
        return this.authMethods;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(StringUtil.simpleClassName(this));
        DecoderResult decoderResult = decoderResult();
        if (!decoderResult.isSuccess()) {
            buf.append("(decoderResult: ");
            buf.append(decoderResult);
            buf.append(", authMethods: ");
        } else {
            buf.append("(authMethods: ");
        }
        buf.append(authMethods());
        buf.append(')');
        return buf.toString();
    }
}
