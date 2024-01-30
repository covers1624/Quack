/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.okhttp;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.httpapi.*;
import net.covers1624.quack.net.okhttp.ProgressForwardingSource;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.net.ProgressListener;

import java.io.IOException;

/**
 * Created by covers1624 on 21/4/23.
 */
@Requires ("com.squareup.okhttp3:okhttp")
public class OkHttpEngineRequest extends AbstractEngineRequest {

    private final OkHttpEngine engine;
    @Nullable
    private Request.Builder builder;

    public OkHttpEngineRequest(OkHttpEngine engine) {
        this.engine = engine;
    }

    @Override
    public EngineRequest method(String method, @Nullable WebBody body) {
        assert builder == null : "Method already set.";
        builder = new Request.Builder();
        RequestBody reqBody = null;
        if (body != null) {
            if (!(body instanceof MultipartBody)) {
                reqBody = toOkhttp(body, listener);
            } else {
                okhttp3.MultipartBody.Builder bodyBuilder = new okhttp3.MultipartBody.Builder();
                bodyBuilder.setType(okhttp3.MultipartBody.FORM);
                for (MultipartBody.Part part : ((MultipartBody) body).getParts()) {
                    bodyBuilder.addFormDataPart(part.name, part.fileName, toOkhttp(part.body, listener));
                }
                reqBody = bodyBuilder.build();
            }
        }
        builder.method(method, reqBody);
        return this;
    }

    @Override
    protected void assertState() {
        super.assertState();
        if (builder == null) {
            throw new IllegalStateException("method(String, Body) must be called first");
        }
    }

    @Override
    public EngineRequest url(String url) {
        super.url(url);
        builder.url(url);
        return this;
    }

    @Override
    public EngineResponse execute() throws IOException {
        assertState();
        if (url == null) {
            throw new IllegalStateException("Url not set.");
        }
        assert builder != null;
        executed = true;

        for (HeaderList.Entry header : headers) {
            builder.addHeader(header.name, header.value);
        }

        if (listener != null) {
            listener.start(builder.getBody$okhttp() != null ? RequestListener.Direction.UPLOAD : RequestListener.Direction.DOWNLOAD);
        }

        Response response = engine.getClient().newCall(builder.build()).execute();
        return new OkHttpEngineResponse(this, response, listener);
    }

    private static RequestBody toOkhttp(WebBody body, @Nullable RequestListener listener) {
        MediaType contentType;
        String contentTypeStr = body.contentType();
        if (contentTypeStr != null) {
            contentType = MediaType.parse(contentTypeStr);
        } else {
            contentType = null;
        }
        return new RequestBody() {
            @Override
            public void writeTo(BufferedSink dst) throws IOException {
                try (Source src = wrapWithProgress(Okio.source(body.open()), body.length(), listener)) {
                    dst.writeAll(src);
                }
            }

            // @formatter:off
            @Nullable @Override public MediaType contentType() { return contentType; }
            @Override public long contentLength() { return body.length(); }
            @Override public boolean isOneShot() { return true; }
            // @formatter:on
        };
    }

    private static Source wrapWithProgress(Source source, long total, @Nullable RequestListener listener) {
        if (listener == null) return source;

        return new ForwardingSource(source) {
            private long curr;

            @Override
            public long read(@NotNull Buffer sink, long byteCount) throws IOException {
                long now = super.read(sink, byteCount);
                if (now != -1) {
                    curr += now;
                    listener.onUpload(total, curr);
                }
                return now;
            }
        };
    }
}
