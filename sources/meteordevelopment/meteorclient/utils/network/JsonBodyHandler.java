package meteordevelopment.meteorclient.utils.network;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;
import java.lang.runtime.ObjectMethods;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/network/JsonBodyHandler.class */
public final class JsonBodyHandler<T> extends Record implements HttpResponse.BodySubscriber<T> {
    private final HttpResponse.BodySubscriber<InputStream> delegate;
    private final Gson gson;
    private final Type type;

    public JsonBodyHandler(HttpResponse.BodySubscriber<InputStream> delegate, Gson gson, Type type) {
        this.delegate = delegate;
        this.gson = gson;
        this.type = type;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, JsonBodyHandler.class), JsonBodyHandler.class, "delegate;gson;type", "FIELD:Lmeteordevelopment/meteorclient/utils/network/JsonBodyHandler;->delegate:Ljava/net/http/HttpResponse$BodySubscriber;", "FIELD:Lmeteordevelopment/meteorclient/utils/network/JsonBodyHandler;->gson:Lcom/google/gson/Gson;", "FIELD:Lmeteordevelopment/meteorclient/utils/network/JsonBodyHandler;->type:Ljava/lang/reflect/Type;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, JsonBodyHandler.class), JsonBodyHandler.class, "delegate;gson;type", "FIELD:Lmeteordevelopment/meteorclient/utils/network/JsonBodyHandler;->delegate:Ljava/net/http/HttpResponse$BodySubscriber;", "FIELD:Lmeteordevelopment/meteorclient/utils/network/JsonBodyHandler;->gson:Lcom/google/gson/Gson;", "FIELD:Lmeteordevelopment/meteorclient/utils/network/JsonBodyHandler;->type:Ljava/lang/reflect/Type;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, JsonBodyHandler.class, Object.class), JsonBodyHandler.class, "delegate;gson;type", "FIELD:Lmeteordevelopment/meteorclient/utils/network/JsonBodyHandler;->delegate:Ljava/net/http/HttpResponse$BodySubscriber;", "FIELD:Lmeteordevelopment/meteorclient/utils/network/JsonBodyHandler;->gson:Lcom/google/gson/Gson;", "FIELD:Lmeteordevelopment/meteorclient/utils/network/JsonBodyHandler;->type:Ljava/lang/reflect/Type;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public HttpResponse.BodySubscriber<InputStream> delegate() {
        return this.delegate;
    }

    public Gson gson() {
        return this.gson;
    }

    public Type type() {
        return this.type;
    }

    public static <T> HttpResponse.BodyHandler<T> ofJson(Gson gson, Type type) {
        return responseInfo -> {
            return new JsonBodyHandler(HttpResponse.BodySubscribers.ofInputStream(), gson, type);
        };
    }

    public CompletionStage<T> getBody() {
        return this.delegate.getBody().thenApply(in -> {
            if (in == null) {
                return null;
            }
            return this.gson.fromJson(new InputStreamReader(in), this.type);
        });
    }

    public void onSubscribe(Flow.Subscription subscription) {
        this.delegate.onSubscribe(subscription);
    }

    public void onNext(List<ByteBuffer> item) {
        this.delegate.onNext(item);
    }

    public void onError(Throwable throwable) {
        this.delegate.onError(throwable);
    }

    public void onComplete() {
        this.delegate.onComplete();
    }
}
