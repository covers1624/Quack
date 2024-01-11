package net.covers1624.quack.net.httpapi.java11;

import net.covers1624.quack.net.httpapi.EngineRequest;
import net.covers1624.quack.net.httpapi.EngineResponse;
import net.covers1624.quack.net.httpapi.HeaderList;
import net.covers1624.quack.net.httpapi.WebBody;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.net.http.HttpResponse;

/**
 * Created by covers1624 on 11/1/24.
 */
public class Java11EngineResponse implements EngineResponse {

    private final Java11EngineRequest request;
    private final HttpResponse<InputStream> response;
    private final HeaderList headers = new HeaderList();
    private final WebBody body;

    public Java11EngineResponse(Java11EngineRequest request, HttpResponse<InputStream> response) {
        this.request = request;
        this.response = response;
        headers.addAllMulti(response.headers().map());
        String contentLen = headers.get("Content-Length");
        body = new ResponseBodyWrapper(
                response,
                contentLen != null && !contentLen.isEmpty() ? Long.parseLong(contentLen) : -1,
                headers.get("Content-Type")
        );
    }

    // @formatter:off
    @Override public EngineRequest request() { return request; }
    @Override public int statusCode() { return response.statusCode(); }
    @Override public String message() { return ""; }
    @Override public HeaderList headers() { return headers; }
    @Override public @Nullable WebBody body() { return body; }
    @Override public void close() { }
    // @formatter:on

    private static final class ResponseBodyWrapper implements WebBody {

        private final HttpResponse<InputStream> response;
        private final long contentLength;
        private final @Nullable String contentType;

        private ResponseBodyWrapper(HttpResponse<InputStream> response, long contentLength, @Nullable String contentType) {
            this.response = response;
            this.contentLength = contentLength;
            this.contentType = contentType;
        }

        // @formatter:off
        @Override public InputStream open() { return response.body(); }
        @Override public boolean multiOpenAllowed() { return false; }
        @Override public long length() { return contentLength; }
        @Override public @Nullable String contentType() { return contentType; }
        // @formatter:on
    }
}
