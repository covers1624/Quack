/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.apache;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.httpapi.EngineRequest;
import net.covers1624.quack.net.httpapi.HttpEngine;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * An Apache HTTPClient implementation for {@link HttpEngine}.
 * <p>
 * Created by covers1624 on 20/6/22.
 */
@Requires ("org.apache.httpcomponents:httpclient")
@Requires (value = "org.apache.httpcomponents:httpmime", optional = "Required if using MultipartBody.")
public abstract class ApacheEngine implements HttpEngine {

    public static ApacheEngine create() {
        return create(HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD)
                        .build()
                )
                .setDefaultCookieStore(new BasicCookieStore())
                .build()
        );
    }

    public static ApacheEngine create(CloseableHttpClient client) {
        return new ApacheEngine() {
            @Override
            protected CloseableHttpClient getClient() {
                return client;
            }
        };
    }

    @Override
    public EngineRequest newRequest() {
        return new ApacheEngineRequest(this);
    }

    protected abstract CloseableHttpClient getClient();
}
