/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.net.okhttp;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.util.MultiHasher;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import okio.ForwardingSource;
import okio.Source;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

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
        if (hasher == null) {
            return response;
        }

        return response.newBuilder()
                .body(SniffingResponseBody.ofFunction(Objects.requireNonNull(response.body()), source -> new HasherWrappedSource(source, hasher)))
                .build();
    }

    private static class HasherWrappedSource extends ForwardingSource {

        private final MultiHasher hasher;

        public HasherWrappedSource(Source delegate, MultiHasher hasher) {
            super(delegate);
            this.hasher = hasher;
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            long bytesRead = super.read(sink, byteCount);
            if (bytesRead != 1) {
                hasher.update(sink.peek().readByteArray());
            }
            return bytesRead;
        }
    }
}
