package meteordevelopment.meteorclient.utils.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import meteordevelopment.meteorclient.utils.other.JsonDateDeserializer;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/network/Http.class */
public class Http {
    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/network/Http$Method.class */
    private enum Method {
        GET,
        POST
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/network/Http$Request.class */
    public static class Request {
        private final HttpRequest.Builder builder;
        private Method method;
        private Consumer<Exception> exceptionHandler = (v0) -> {
            v0.printStackTrace();
        };

        private Request(Method method, String url) {
            try {
                this.builder = HttpRequest.newBuilder().uri(new URI(url)).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36");
                this.method = method;
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public Request header(String name, String value) {
            this.builder.header(name, value);
            return this;
        }

        public Request bearer(String token) {
            this.builder.header("Authorization", "Bearer " + token);
            return this;
        }

        public Request bodyString(String string) {
            this.builder.header("Content-Type", "text/plain");
            this.builder.method(this.method.name(), HttpRequest.BodyPublishers.ofString(string));
            this.method = null;
            return this;
        }

        public Request bodyForm(String string) {
            this.builder.header("Content-Type", "application/x-www-form-urlencoded");
            this.builder.method(this.method.name(), HttpRequest.BodyPublishers.ofString(string));
            this.method = null;
            return this;
        }

        public Request bodyJson(String string) {
            this.builder.header("Content-Type", "application/json");
            this.builder.method(this.method.name(), HttpRequest.BodyPublishers.ofString(string));
            this.method = null;
            return this;
        }

        public Request bodyJson(Object object) {
            this.builder.header("Content-Type", "application/json");
            this.builder.method(this.method.name(), HttpRequest.BodyPublishers.ofString(Http.GSON.toJson(object)));
            this.method = null;
            return this;
        }

        public Request ignoreExceptions() {
            this.exceptionHandler = e -> {
            };
            return this;
        }

        public Request exceptionHandler(Consumer<Exception> exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        private <T> HttpResponse<T> _sendResponse(String accept, HttpResponse.BodyHandler<T> responseBodyHandler) {
            this.builder.header("Accept", accept);
            if (this.method != null) {
                this.builder.method(this.method.name(), HttpRequest.BodyPublishers.noBody());
            }
            HttpRequest request = this.builder.build();
            try {
                return Http.CLIENT.send(request, responseBodyHandler);
            } catch (IOException | InterruptedException e) {
                this.exceptionHandler.accept(e);
                return new FailedHttpResponse(request, e);
            }
        }

        @Nullable
        private <T> T _send(String str, HttpResponse.BodyHandler<T> bodyHandler) {
            HttpResponse<T> httpResponse_sendResponse = _sendResponse(str, bodyHandler);
            if (httpResponse_sendResponse.statusCode() == 200) {
                return (T) httpResponse_sendResponse.body();
            }
            return null;
        }

        public void send() {
            _send("*/*", HttpResponse.BodyHandlers.discarding());
        }

        public HttpResponse<Void> sendResponse() {
            return _sendResponse("*/*", HttpResponse.BodyHandlers.discarding());
        }

        @Nullable
        public InputStream sendInputStream() {
            return (InputStream) _send("*/*", HttpResponse.BodyHandlers.ofInputStream());
        }

        public HttpResponse<InputStream> sendInputStreamResponse() {
            return _sendResponse("*/*", HttpResponse.BodyHandlers.ofInputStream());
        }

        @Nullable
        public String sendString() {
            return (String) _send("*/*", HttpResponse.BodyHandlers.ofString());
        }

        public HttpResponse<String> sendStringResponse() {
            return _sendResponse("*/*", HttpResponse.BodyHandlers.ofString());
        }

        @Nullable
        public Stream<String> sendLines() {
            return (Stream) _send("*/*", HttpResponse.BodyHandlers.ofLines());
        }

        public HttpResponse<Stream<String>> sendLinesResponse() {
            return _sendResponse("*/*", HttpResponse.BodyHandlers.ofLines());
        }

        @Nullable
        public <T> T sendJson(Type type) {
            return (T) _send("application/json", JsonBodyHandler.ofJson(Http.GSON, type));
        }

        public <T> HttpResponse<T> sendJsonResponse(Type type) {
            return _sendResponse("*/*", JsonBodyHandler.ofJson(Http.GSON, type));
        }
    }

    public static Request get(String url) {
        return new Request(Method.GET, url);
    }

    public static Request post(String url) {
        return new Request(Method.POST, url);
    }
}
