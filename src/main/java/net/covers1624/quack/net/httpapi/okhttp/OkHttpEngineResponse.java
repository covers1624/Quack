/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.okhttp;

import kotlin.Pair;
import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.io.ForwardingReadableByteChannel;
import net.covers1624.quack.net.httpapi.*;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by covers1624 on 21/4/23.
 */
@Requires ("com.squareup.okhttp3:okhttp")
public class OkHttpEngineResponse implements EngineResponse {

    private final EngineRequest request;
    private final Response response;
    private final HeaderList headers = new HeaderList();
    private final @Nullable RequestListener listener;
    @Nullable
    private final WebBody body;

    public OkHttpEngineResponse(EngineRequest request, Response response, @Nullable RequestListener listener) {
        this.request = request;
        this.response = response;
        this.listener = listener;
        for (Pair<? extends String, ? extends String> header : response.headers()) {
            headers.add(header.component1(), header.component2());
        }
        body = wrapBody(response.body(), listener);
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
        if (listener != null) {
            listener.end();
        }
    }

    private static @Nullable WebBody wrapBody(@Nullable ResponseBody body, @Nullable RequestListener listener) {
        if (body == null) return null;

        MediaType mediaType = body.contentType();
        return new WebBody() {
            @Nullable
            private final String contentType = mediaType != null ? mediaType.toString() : null;

            // @formatter:off
            @Override public ReadableByteChannel openChannel() { return wrapProgress(body.source(), length(), listener); }
            @Override public InputStream open() { return Channels.newInputStream(openChannel()); }
            @Override public boolean multiOpenAllowed() { return false; }
            @Override public long length() { return body.contentLength(); }
            @Override public @Nullable String contentType() { return contentType; }
            // @formatter:on
        };
    }

    private static ReadableByteChannel wrapProgress(ReadableByteChannel channel, long total, @Nullable RequestListener listener) {
        if (listener == null) return channel;

        return new ForwardingReadableByteChannel(channel) {
            private long curr;

            @Override
            public int read(ByteBuffer dst) throws IOException {
                int now = super.read(dst);
                if (now != -1) {
                    curr += now;
                    listener.onDownload(total, curr);
                }
                return now;
            }
        };
    }
}
