/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.okhttp;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.httpapi.*;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import org.jetbrains.annotations.Nullable;

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
                reqBody = toOkhttp(body);
            } else {
                okhttp3.MultipartBody.Builder bodyBuilder = new okhttp3.MultipartBody.Builder();
                bodyBuilder.setType(okhttp3.MultipartBody.FORM);
                for (MultipartBody.Part part : ((MultipartBody) body).getParts()) {
                    bodyBuilder.addFormDataPart(part.name, part.fileName, toOkhttp(part.body));
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
        executed = true;

        for (HeaderList.Entry header : headers) {
            builder.addHeader(header.name, header.value);
        }

        Response response = engine.getClient().newCall(builder.build()).execute();
        return new OkHttpEngineResponse(this, response);
    }

    private static RequestBody toOkhttp(WebBody body) {
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
                try (Source src = Okio.source(body.open())) {
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
}
