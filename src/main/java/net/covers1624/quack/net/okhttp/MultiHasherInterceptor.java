/*
 * MIT License
 *
 * Copyright (c) 2018-2021 covers1624
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
