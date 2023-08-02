/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.apache;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.io.IOUtils;
import net.covers1624.quack.net.httpapi.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by covers1624 on 21/6/22.
 */
@Requires ("org.apache.httpcomponents:httpclient")
@Requires (value = "org.apache.httpcomponents:httpmime", optional = "Required if using MultipartBody.")
public class ApacheEngineRequest extends AbstractEngineRequest {

    private final ApacheEngine engine;

    @Nullable
    private RequestBuilder builder;
    private final HeaderList headers = new HeaderList();

    public ApacheEngineRequest(ApacheEngine engine) {
        this.engine = engine;
    }

    @Override
    public EngineRequest method(String method, @Nullable WebBody body) {
        assert builder == null : "Method already set";
        builder = RequestBuilder.create(method);
        if (body != null) {
            if (!(body instanceof MultipartBody)) {
                builder.setEntity(new AbstractHttpEntity() {
                    {
                        setContentType(body.contentType());
                    }

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
            ContentType contentType = null;
            String contentTypeString = part.body.contentType();
            if (contentTypeString != null) {
                contentType = ContentType.parse(contentTypeString);
            }
            multipartBuilder.addPart(part.name, new AbstractContentBody(contentType) {
                @Override
                public String getTransferEncoding() {
                    return MIME.ENC_BINARY;
                }

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

        HttpUriRequest uriRequest = builder.build();
        CloseableHttpResponse response = engine.getClient().execute(uriRequest);

        return new ApacheEngineResponse(this, response);
    }
}
