/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.curl4j;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.httpapi.EngineResponse;
import net.covers1624.quack.net.httpapi.HeaderList;
import net.covers1624.quack.net.httpapi.WebBody;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Created by covers1624 on 1/11/23.
 */
@Requires ("net.covers1624:curl4j")
public class Curl4jEngineResponse implements EngineResponse {

    private final Curl4jEngineRequest request;
    private final int statusCode;
    private final HeaderList headers;
    private final WebBody body;

    public Curl4jEngineResponse(Curl4jEngineRequest request, int statusCode, HeaderList headers, WebBody body) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public Curl4jEngineRequest request() {
        return request;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public String message() {
        return "";
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
    }
}
