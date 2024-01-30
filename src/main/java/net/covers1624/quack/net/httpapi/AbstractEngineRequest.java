/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by covers1624 on 1/8/23.
 */
public abstract class AbstractEngineRequest implements EngineRequest {

    @Nullable
    protected String url;
    protected final HeaderList headers = new HeaderList();
    protected boolean executed = false;

    protected void assertState() {
        if (executed) {
            throw new IllegalStateException("Already executed.");
        }
    }

    @Override
    public EngineRequest url(String url) {
        assertState();
        this.url = url;
        return this;
    }

    @Override
    public EngineRequest header(String key, String value) {
        assertState();
        headers.add(key, value);
        return this;
    }

    @Override
    public EngineRequest headers(Map<String, String> headers) {
        assertState();
        this.headers.addAll(headers);
        return this;
    }

    @Override
    public EngineRequest headers(HeaderList headers) {
        assertState();
        this.headers.addAll(headers);
        return this;
    }

    @Override
    public EngineRequest removeHeader(String key) {
        assertState();
        headers.removeAll(key);
        return this;
    }

    @Override
    public String getUrl() {
        if (url == null) throw new IllegalStateException("URL not set");
        return url;
    }

    @Override
    public HeaderList getHeaders() {
        return headers;
    }
}
