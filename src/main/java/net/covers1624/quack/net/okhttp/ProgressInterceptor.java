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
import net.covers1624.quack.net.download.DownloadListener;
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
 * An interceptor capable of handling a {@link ProgressTag} {@link Request} tag.
 * <p>
 * Created by covers1624 on 15/2/21.
 */
@Requires ("com.squareup.okhttp3:okhttp")
public class ProgressInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        ProgressTag tag = request.tag(ProgressTag.class);

        if (tag == null) {
            return chain.proceed(request);
        }

        tag.listener.connecting();
        Response response = chain.proceed(request);
        return response.newBuilder()
                .body(SniffingResponseBody.ofFunction(Objects.requireNonNull(response.body()), e -> new ProgressForwardingSource(e, tag)))
                .build();
    }

    public static class ProgressTag {

        /**
         * Initial already downloaded length.
         */
        public final long existingLen;

        public final DownloadListener listener;

        public ProgressTag(DownloadListener listener) {
            this(0, listener);
        }

        public ProgressTag(long existingLen, DownloadListener listener) {
            this.existingLen = existingLen;
            this.listener = listener;
        }
    }

    private static class ProgressForwardingSource extends ForwardingSource {

        private final ProgressTag tag;
        private long totalLen;

        public ProgressForwardingSource(Source delegate, ProgressTag tag) {
            super(delegate);
            this.tag = tag;
            totalLen = tag.existingLen;
        }

        @Override
        public long read(@NotNull Buffer sink, long byteCount) throws IOException {
            long len = super.read(sink, byteCount);
            if (len == -1) {
                tag.listener.finish(totalLen);
            } else {
                totalLen += len;
                tag.listener.update(totalLen);
            }
            return len;
        }
    }
}
