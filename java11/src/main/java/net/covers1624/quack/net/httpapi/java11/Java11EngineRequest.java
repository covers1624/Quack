package net.covers1624.quack.net.httpapi.java11;

import net.covers1624.quack.net.httpapi.*;
import net.covers1624.quack.util.SneakyUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow;

/**
 * Created by covers1624 on 11/1/24.
 */
public class Java11EngineRequest extends AbstractEngineRequest {

    private final Java11HttpEngine engine;
    private @Nullable String method;
    private @Nullable WebBody body;

    Java11EngineRequest(Java11HttpEngine engine) {
        this.engine = engine;
    }

    @Override
    public EngineRequest method(String method, @Nullable WebBody body) {
        this.method = method;
        this.body = body;
        return this;
    }

    @Override
    protected void assertState() {
        super.assertState();
        if (method == null) {
            throw new IllegalStateException("method(String, Body) must be called first");
        }
    }

    @Override
    public EngineResponse execute() throws IOException {
        assertState();
        if (url == null) throw new IllegalStateException("Url not set.");

        if (body != null && headers.get("Content-Type") == null) {
            headers.add("Content-Type", body.contentType());
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.uri(URI.create(url));
        builder.method(method, body != null ? toPublisher(body) : BodyPublishers.noBody());
        if (!headers.isEmpty()) {
            builder.headers(headers.toArray());
        }

        try {
            return new Java11EngineResponse(
                    this,
                    engine.getClient().send(builder.build(), HttpResponse.BodyHandlers.ofInputStream())
            );
        } catch (InterruptedException ex) {
            throw new IOException("Request failed. Interrupted.", ex);
        }
    }

    private static BodyPublisher toPublisher(WebBody body) {
        if (body instanceof MultipartBody) throw new UnsupportedOperationException("Multipart requests are not supported by this engine yet.");

        return new BodyPublisher() {
            @Override
            public long contentLength() {
                return body.length();
            }

            @Override
            public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
                BodyPublishers.ofInputStream(SneakyUtils.sneak(body::open))
                        .subscribe(subscriber);
            }
        };
    }
}
