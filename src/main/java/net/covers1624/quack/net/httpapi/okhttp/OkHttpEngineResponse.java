/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.okhttp;

import kotlin.Pair;
import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.httpapi.EngineRequest;
import net.covers1624.quack.net.httpapi.EngineResponse;
import net.covers1624.quack.net.httpapi.HeaderList;
import net.covers1624.quack.net.httpapi.WebBody;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by covers1624 on 21/4/23.
 */
@Requires ("com.squareup.okhttp3:okhttp")
public class OkHttpEngineResponse implements EngineResponse {

    private final EngineRequest request;
    private final Response response;
    private final HeaderList headers = new HeaderList();
    @Nullable
    private final WebBody body;

    public OkHttpEngineResponse(EngineRequest request, Response response) {
        this.request = request;
        this.response = response;
        for (Pair<? extends String, ? extends String> header : response.headers()) {
            headers.add(header.component1(), header.component2());
        }
        ResponseBody respBody = response.body();
        body = respBody != null ? new ResponseBodyWrapper(respBody) : null;
    }

    @Override
    public EngineRequest request() {
        return request;
    }

    @Override
    public int statusCode() {
        return response.code();
    }

    @Override
    public String message() {
        return response.message();
    }

    @Override
    public HeaderList headers() {
        return headers;
    }

    @Override
    public @Nullable WebBody body() {
        return body;
    }

    @Override
    public void close() throws IOException {
        ResponseBody body = response.body();
        if (body != null) {
            body.close();
        }
    }

    private static class ResponseBodyWrapper implements WebBody {

        private final ResponseBody body;

        @Nullable
        private final String contentType;

        public ResponseBodyWrapper(ResponseBody body) {
            this.body = body;
            MediaType contentType = body.contentType();
            this.contentType = contentType != null ? contentType.toString() : null;
        }

        // @formatter:off
        @Override public InputStream open() { return body.byteStream(); }
        @Override public ReadableByteChannel openChannel() { return body.source(); }
        @Override public boolean multiOpenAllowed() { return false; }
        @Override public long length() { return body.contentLength(); }
        @Override public @Nullable String contentType() { return contentType; }
        // @formatter:on
    }
}
