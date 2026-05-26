package io.netty.handler.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpHeadersFactory;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/proxy/HttpProxyHandler.class */
public final class HttpProxyHandler extends ProxyHandler {
    private static final String PROTOCOL = "http";
    private static final String AUTH_BASIC = "basic";
    private final HttpClientCodecWrapper codecWrapper;
    private final String username;
    private final String password;
    private final CharSequence authorization;
    private final HttpHeaders outboundHeaders;
    private final boolean ignoreDefaultPortsInConnectHostHeader;
    private HttpResponseStatus status;
    private HttpHeaders inboundHeaders;

    public HttpProxyHandler(SocketAddress proxyAddress) {
        this(proxyAddress, null);
    }

    public HttpProxyHandler(SocketAddress proxyAddress, HttpHeaders headers) {
        this(proxyAddress, headers, false);
    }

    public HttpProxyHandler(SocketAddress proxyAddress, HttpHeaders headers, boolean ignoreDefaultPortsInConnectHostHeader) {
        super(proxyAddress);
        this.codecWrapper = new HttpClientCodecWrapper();
        this.username = null;
        this.password = null;
        this.authorization = null;
        this.outboundHeaders = headers;
        this.ignoreDefaultPortsInConnectHostHeader = ignoreDefaultPortsInConnectHostHeader;
    }

    public HttpProxyHandler(SocketAddress proxyAddress, String username, String password) {
        this(proxyAddress, username, password, null);
    }

    public HttpProxyHandler(SocketAddress proxyAddress, String username, String password, HttpHeaders headers) {
        this(proxyAddress, username, password, headers, false);
    }

    public HttpProxyHandler(SocketAddress proxyAddress, String username, String password, HttpHeaders headers, boolean ignoreDefaultPortsInConnectHostHeader) {
        super(proxyAddress);
        this.codecWrapper = new HttpClientCodecWrapper();
        this.username = (String) ObjectUtil.checkNotNull(username, "username");
        this.password = (String) ObjectUtil.checkNotNull(password, "password");
        ByteBuf authz = Unpooled.copiedBuffer(username + ':' + password, CharsetUtil.UTF_8);
        try {
            ByteBuf authzBase64 = Base64.encode(authz, false);
            authz.release();
            try {
                this.authorization = new AsciiString("Basic " + authzBase64.toString(CharsetUtil.US_ASCII));
                authzBase64.release();
                this.outboundHeaders = headers;
                this.ignoreDefaultPortsInConnectHostHeader = ignoreDefaultPortsInConnectHostHeader;
            } catch (Throwable th) {
                authzBase64.release();
                throw th;
            }
        } catch (Throwable th2) {
            authz.release();
            throw th2;
        }
    }

    @Override // io.netty.handler.proxy.ProxyHandler
    public String protocol() {
        return PROTOCOL;
    }

    @Override // io.netty.handler.proxy.ProxyHandler
    public String authScheme() {
        return this.authorization != null ? AUTH_BASIC : "none";
    }

    public String username() {
        return this.username;
    }

    public String password() {
        return this.password;
    }

    @Override // io.netty.handler.proxy.ProxyHandler
    protected void addCodec(ChannelHandlerContext ctx) throws Exception {
        ChannelPipeline p = ctx.pipeline();
        String name = ctx.name();
        p.addBefore(name, (String) null, this.codecWrapper);
    }

    @Override // io.netty.handler.proxy.ProxyHandler
    protected void removeEncoder(ChannelHandlerContext ctx) throws Exception {
        this.codecWrapper.codec.removeOutboundHandler();
    }

    @Override // io.netty.handler.proxy.ProxyHandler
    protected void removeDecoder(ChannelHandlerContext ctx) throws Exception {
        this.codecWrapper.codec.removeInboundHandler();
    }

    @Override // io.netty.handler.proxy.ProxyHandler
    protected Object newInitialMessage(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress raddr = (InetSocketAddress) destinationAddress();
        String hostString = HttpUtil.formatHostnameForHttp(raddr);
        int port = raddr.getPort();
        String url = hostString + ":" + port;
        String hostHeader = (this.ignoreDefaultPortsInConnectHostHeader && (port == 80 || port == 443)) ? hostString : url;
        DefaultHttpHeadersFactory defaultHttpHeadersFactoryWithValidation = DefaultHttpHeadersFactory.headersFactory().withValidation(false);
        DefaultFullHttpRequest defaultFullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.CONNECT, url, Unpooled.EMPTY_BUFFER, defaultHttpHeadersFactoryWithValidation, defaultHttpHeadersFactoryWithValidation);
        defaultFullHttpRequest.headers().set(HttpHeaderNames.HOST, hostHeader);
        if (this.authorization != null) {
            defaultFullHttpRequest.headers().set(HttpHeaderNames.PROXY_AUTHORIZATION, this.authorization);
        }
        if (this.outboundHeaders != null) {
            defaultFullHttpRequest.headers().add(this.outboundHeaders);
        }
        return defaultFullHttpRequest;
    }

    @Override // io.netty.handler.proxy.ProxyHandler
    protected boolean handleResponse(ChannelHandlerContext ctx, Object response) throws Exception {
        if (response instanceof HttpResponse) {
            if (this.status != null) {
                throw new HttpProxyConnectException(exceptionMessage("too many responses"), null);
            }
            HttpResponse res = (HttpResponse) response;
            this.status = res.status();
            this.inboundHeaders = res.headers();
        }
        boolean finished = response instanceof LastHttpContent;
        if (finished) {
            if (this.status == null) {
                throw new HttpProxyConnectException(exceptionMessage("missing response"), this.inboundHeaders);
            }
            if (this.status.code() != 200) {
                throw new HttpProxyConnectException(exceptionMessage("status: " + this.status), this.inboundHeaders);
            }
        }
        return finished;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/proxy/HttpProxyHandler$HttpProxyConnectException.class */
    public static final class HttpProxyConnectException extends ProxyConnectException {
        private static final long serialVersionUID = -8824334609292146066L;
        private final HttpHeaders headers;

        public HttpProxyConnectException(String message, HttpHeaders headers) {
            super(message);
            this.headers = headers;
        }

        public HttpHeaders headers() {
            return this.headers;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:io/netty/handler/proxy/HttpProxyHandler$HttpClientCodecWrapper.class */
    private static final class HttpClientCodecWrapper implements ChannelInboundHandler, ChannelOutboundHandler {
        final HttpClientCodec codec;

        private HttpClientCodecWrapper() {
            this.codec = new HttpClientCodec();
        }

        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            this.codec.handlerAdded(ctx);
        }

        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            this.codec.handlerRemoved(ctx);
        }

        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            this.codec.exceptionCaught(ctx, cause);
        }

        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            this.codec.channelRegistered(ctx);
        }

        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            this.codec.channelUnregistered(ctx);
        }

        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            this.codec.channelActive(ctx);
        }

        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            this.codec.channelInactive(ctx);
        }

        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            this.codec.channelRead(ctx, msg);
        }

        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            this.codec.channelReadComplete(ctx);
        }

        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            this.codec.userEventTriggered(ctx, evt);
        }

        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            this.codec.channelWritabilityChanged(ctx);
        }

        public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            this.codec.bind(ctx, localAddress, promise);
        }

        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            this.codec.connect(ctx, remoteAddress, localAddress, promise);
        }

        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            this.codec.disconnect(ctx, promise);
        }

        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            this.codec.close(ctx, promise);
        }

        public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            this.codec.deregister(ctx, promise);
        }

        public void read(ChannelHandlerContext ctx) throws Exception {
            this.codec.read(ctx);
        }

        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            this.codec.write(ctx, msg, promise);
        }

        public void flush(ChannelHandlerContext ctx) throws Exception {
            this.codec.flush(ctx);
        }
    }
}
