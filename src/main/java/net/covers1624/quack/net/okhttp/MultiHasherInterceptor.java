/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.okhttp;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.util.MultiHasher;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * An {@link Interceptor} capable of handling a {@link MultiHasher} {@link Request} tag.
 * <p>
 * Created by covers1624 on 15/2/21.
 */
@Requires ("com.google.guava:guava")
@Requires ("com.squareup.okhttp3:okhttp")
public class MultiHasherInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        MultiHasher hasher = request.tag(MultiHasher.class);

        Response response = chain.proceed(request);
        if (hasher == null) return response;

        ResponseBody body = response.body();
        if (body == null) return response;

        return response.newBuilder()
                .body(SniffingResponseBody.ofFunction(body, source -> new HasherWrappedSource(source, hasher)))
                .build();
    }

}
