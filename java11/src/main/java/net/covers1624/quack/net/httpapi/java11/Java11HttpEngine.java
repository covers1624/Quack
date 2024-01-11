package net.covers1624.quack.net.httpapi.java11;

import net.covers1624.quack.net.httpapi.EngineRequest;
import net.covers1624.quack.net.httpapi.HttpEngine;

import java.net.CookieManager;
import java.net.http.HttpClient;

/**
 * Created by covers1624 on 11/1/24.
 */
public abstract class Java11HttpEngine implements HttpEngine {

    public static Java11HttpEngine create() {
        return create(HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL) // Yes, follow redirects from http -> https
                .cookieHandler(new CookieManager()) // in-memory cookies please
                .build()
        );
    }

    public static Java11HttpEngine create(HttpClient client) {
        return new Java11HttpEngine() {
            @Override
            protected HttpClient getClient() {
                return client;
            }
        };
    }

    protected abstract HttpClient getClient();

    @Override
    public EngineRequest newRequest() {
        return new Java11EngineRequest(this);
    }
}
