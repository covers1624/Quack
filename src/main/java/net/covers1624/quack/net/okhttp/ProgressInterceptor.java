/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.okhttp;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.download.DownloadListener;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

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
                .body(SniffingResponseBody.ofFunction(Objects.requireNonNull(response.body()), e -> new ProgressForwardingSource(e, tag.listener, tag.existingLen)))
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
}
