/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.apache5;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.httpapi.EngineRequest;
import net.covers1624.quack.net.httpapi.EngineResponse;
import net.covers1624.quack.net.httpapi.HeaderList;
import net.covers1624.quack.net.httpapi.WebBody;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by covers1624 on 17/8/26.
 */
@Requires ("org.apache.httpcomponents.client5:httpclient5")
public class Apache5EngineResponse implements EngineResponse {

    private final EngineRequest request;
    private final ClassicHttpResponse response;
    private final HeaderList headers = new HeaderList();
    @Nullable
    private final WebBody body;

    public Apache5EngineResponse(EngineRequest request, ClassicHttpResponse response) {
        this.request = request;
        this.response = response;
        for (Header header : response.getHeaders()) {
            headers.add(header.getName(), header.getValue());
        }
        HttpEntity entity = response.getEntity();
        body = entity != null ? new ResponseBodyWrapper(entity) : null;
    }

    @Override
    public EngineRequest request() {
        return request;
    }

    @Override
    public int statusCode() {
        return response.getCode();
    }

    @Override
    public String message() {
        return response.getReasonPhrase();
    }

    @Override
    public HeaderList headers() {
        return headers;
    }

    @Override
    @Nullable
    public WebBody body() {
        return body;
    }

    @Override
    public void close() throws IOException {
        response.close();
    }

    private static class ResponseBodyWrapper implements WebBody {

        private final HttpEntity entity;
        @Nullable
        private final String contentType;

        private ResponseBodyWrapper(HttpEntity entity) {
            this.entity = entity;
            this.contentType = entity.getContentType();
        }

        @Override
        public InputStream open() throws IOException {
            return entity.getContent();
        }

        @Override
        public boolean multiOpenAllowed() {
            return entity.isRepeatable();
        }

        @Override
        public long length() {
            return entity.getContentLength();
        }

        @Nullable
        @Override
        public String contentType() {
            return contentType;
        }
    }
}
