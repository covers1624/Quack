/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.apache5;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.io.IOUtils;
import net.covers1624.quack.net.httpapi.*;
import org.apache.hc.client5.http.entity.mime.AbstractContentBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by covers1624 on 17/8/26.
 */
@Requires ("org.apache.httpcomponents.client5:httpclient5")
public class Apache5EngineRequest extends AbstractEngineRequest {

    private final Apache5Engine engine;

    @Nullable
    private ClassicRequestBuilder builder;

    public Apache5EngineRequest(Apache5Engine engine) {
        this.engine = engine;
    }

    @Override
    public EngineRequest method(String method, @Nullable WebBody body) {
        assert builder == null : "Method already set";
        builder = ClassicRequestBuilder.create(method);
        if (body != null) {
            if (!(body instanceof MultipartBody)) {
                builder.setEntity(new AbstractHttpEntity(body.contentType(), null) {

                    @Override
                    public void writeTo(OutputStream os) throws IOException {
                        try (InputStream is = getContent()) {
                            IOUtils.copy(is, os);
                        }
                    }

                    // @formatter:off
                    @Override public boolean isRepeatable() { return false; }
                    @Override public long getContentLength() { return body.length(); }
                    @Override public InputStream getContent() throws IOException, UnsupportedOperationException { return body.open(); }
                    @Override public boolean isStreaming() { return true; }
                    @Override public void close() { }
                    // @formatter:on
                });
            } else {
                builder.setEntity(multipartBody((MultipartBody) body));
            }
        }
        return this;
    }

    private static HttpEntity multipartBody(MultipartBody body) {
        MultipartEntityBuilder multipartBuilder = MultipartEntityBuilder.create();
        for (MultipartBody.Part part : body.getParts()) {
            String contentTypeString = part.body.contentType();
            ContentType contentType = contentTypeString != null ? ContentType.parse(contentTypeString) : ContentType.APPLICATION_OCTET_STREAM;
            multipartBuilder.addPart(part.name, new AbstractContentBody(contentType) {
                @Override
                public long getContentLength() {
                    return part.body.length();
                }

                @Override
                public String getFilename() {
                    return part.fileName;
                }

                @Override
                public void writeTo(OutputStream os) throws IOException {
                    try (InputStream is = part.body.open()) {
                        IOUtils.copy(is, os);
                    }
                }
            });
        }
        return multipartBuilder.build();
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
        builder.setUri(url);
        return this;
    }

    @Override
    public EngineResponse execute() throws IOException {
        assertState();
        if (url == null) {
            throw new IllegalStateException("Url not set.");
        }

        for (HeaderList.Entry header : headers) {
            builder.addHeader(header.name, header.value);
        }
        executed = true;

        ClassicHttpResponse response = engine.getClient().executeOpen(null, builder.build(), null);
        try {
            return new Apache5EngineResponse(this, response);
        } catch (Throwable ex) {
            try {
                response.close();
            } catch (Throwable suppressed) {
                ex.addSuppressed(suppressed);
            }
            throw ex;
        }
    }
}
