package meteordevelopment.meteorclient.utils.network;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.net.ssl.SSLSession;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/network/FailedHttpResponse.class */
public final class FailedHttpResponse<T> extends Record implements HttpResponse<T> {
    private final HttpRequest request;
    private final Exception exception;

    public FailedHttpResponse(HttpRequest request, Exception exception) {
        this.request = request;
        this.exception = exception;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, FailedHttpResponse.class), FailedHttpResponse.class, "request;exception", "FIELD:Lmeteordevelopment/meteorclient/utils/network/FailedHttpResponse;->request:Ljava/net/http/HttpRequest;", "FIELD:Lmeteordevelopment/meteorclient/utils/network/FailedHttpResponse;->exception:Ljava/lang/Exception;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, FailedHttpResponse.class), FailedHttpResponse.class, "request;exception", "FIELD:Lmeteordevelopment/meteorclient/utils/network/FailedHttpResponse;->request:Ljava/net/http/HttpRequest;", "FIELD:Lmeteordevelopment/meteorclient/utils/network/FailedHttpResponse;->exception:Ljava/lang/Exception;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, FailedHttpResponse.class, Object.class), FailedHttpResponse.class, "request;exception", "FIELD:Lmeteordevelopment/meteorclient/utils/network/FailedHttpResponse;->request:Ljava/net/http/HttpRequest;", "FIELD:Lmeteordevelopment/meteorclient/utils/network/FailedHttpResponse;->exception:Ljava/lang/Exception;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public HttpRequest request() {
        return this.request;
    }

    public Exception exception() {
        return this.exception;
    }

    public int statusCode() {
        return 400;
    }

    public Optional<HttpResponse<T>> previousResponse() {
        return Optional.empty();
    }

    public HttpHeaders headers() {
        return HttpHeaders.of(Map.of(), (s1, s2) -> {
            return true;
        });
    }

    public T body() {
        return null;
    }

    public Optional<SSLSession> sslSession() {
        return Optional.empty();
    }

    public URI uri() {
        return this.request.uri();
    }

    @Nullable
    public HttpClient.Version version() {
        return (HttpClient.Version) this.request.version().orElse(null);
    }
}
