/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.net.okhttp;

import net.covers1624.quack.annotation.Requires;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Throttler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * An {@link Interceptor} which applies a {@link Throttler} to the response body from a request tag.
 * <p>
 * Created by covers1624 on 19/11/21.
 */
@Requires ("com.squareup.okhttp3:okhttp")
public class ThrottlerInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        Throttler throttler = request.tag(Throttler.class);

        Response response = chain.proceed(request);
        if (throttler == null) return response;

        ResponseBody body = response.body();
        if (body == null) return response;

        return response.newBuilder()
                .body(SniffingResponseBody.ofFunction(body, throttler::source))
                .build();
    }
}
