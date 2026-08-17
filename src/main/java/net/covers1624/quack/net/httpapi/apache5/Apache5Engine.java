/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.apache5;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.httpapi.EngineRequest;
import net.covers1624.quack.net.httpapi.HttpEngine;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

/**
 * An Apache HTTPClient implementation for {@link HttpEngine}.
 * <p>
 * Created by covers1624 on 17/8/26.
 */
@Requires ("org.apache.httpcomponents.client5:httpclient5")
public abstract class Apache5Engine implements HttpEngine {

    public static Apache5Engine create() {
        return create(HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(StandardCookieSpec.RELAXED)
                        .build()
                )
                .setDefaultCookieStore(new BasicCookieStore())
                .build()
        );
    }

    public static Apache5Engine create(CloseableHttpClient client) {
        return new Apache5Engine() {
            @Override
            protected CloseableHttpClient getClient() {
                return client;
            }
        };
    }

    @Override
    public EngineRequest newRequest() {
        return new Apache5EngineRequest(this);
    }

    protected abstract CloseableHttpClient getClient();
}
