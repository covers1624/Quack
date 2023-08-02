/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.okhttp;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.httpapi.EngineRequest;
import net.covers1624.quack.net.httpapi.HttpEngine;
import net.covers1624.quack.net.okhttp.SimpleCookieJar;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * Created by covers1624 on 21/4/23.
 */
@Requires ("com.squareup.okhttp3:okhttp")
public abstract class OkHttpEngine implements HttpEngine {

    public static OkHttpEngine create() {
        return create(new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .connectionPool(new ConnectionPool())
                .cookieJar(new SimpleCookieJar())
                .build()
        );
    }

    public static OkHttpEngine create(OkHttpClient client) {
        return new OkHttpEngine() {
            @Override
            protected OkHttpClient getClient() {
                return client;
            }
        };
    }

    protected abstract OkHttpClient getClient();

    @Override
    public EngineRequest newRequest() {
        return new OkHttpEngineRequest(this);
    }
}
