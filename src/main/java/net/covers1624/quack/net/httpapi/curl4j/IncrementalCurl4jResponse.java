/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.curl4j;

import net.covers1624.curl4j.CURLMsg;
import net.covers1624.curl4j.CurlWriteCallback;
import net.covers1624.curl4j.core.Memory;
import net.covers1624.curl4j.core.Pointer;
import net.covers1624.curl4j.util.*;
import net.covers1624.quack.net.httpapi.HeaderList;
import net.covers1624.quack.net.httpapi.WebBody;
import net.covers1624.quack.util.SneakyUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static net.covers1624.curl4j.CURL.*;

/**
 * Created by covers1624 on 10/1/24.
 */
class IncrementalCurl4jResponse extends Curl4jEngineResponse {

    // 64k buffer.
    private long buf = Memory.malloc(64 * 1024);
    private ByteBuffer buffer = Memory.newDirectByteBuffer(buf, 64 * 1024);

    private boolean done;
    private boolean paused;

    private final Curl4jEngineRequest request;
    private final CurlMultiHandle handle;

    private final @Nullable CurlInput input;
    private final @Nullable CurlMimeBody mimeBody;
    private final SListHeaderWrapper headers;

    private final int statusCode;

    private final HeaderList responseHeaders = new HeaderList();

    private final CurlWriteCallback writeCallback = new CurlWriteCallback((ptr, size, nmemb, userdata) -> {
        int rs = (int) (size * nmemb);
        if (rs == 0) return rs;

        // If our buffer is too small to consume this piece of data.
        if (buffer.remaining() < rs) {
            // If we already have a chunk of data, pause the transfer.
            if (buffer.position() != 0) {
                paused = true;
                return CURL_WRITEFUNC_PAUSE;
            }
            // We must grow the buffer, we can't pause as we have no data,
            // and we can't partially consume.
            growBuffer(rs - buffer.remaining());
        }
        Memory.memcpy(ptr, buf + buffer.position(), rs);
        buffer.position(buffer.position() + rs);
        return rs;
    });
    private final InputStream is = new InputStream() {
        @Override
        public int read() throws IOException {
            if (buffer.remaining() == 0) {
                if (done) return -1;
                fillBuffer();
            }
            return buffer.get() & 0xFF;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (buffer.remaining() == 0) {
                if (done) return -1;
                fillBuffer();
            }
            int l = Math.min(len, buffer.remaining());
            buffer.get(b, off, l);
            return l;
        }
    };
    private final WebBody webBody;

    public IncrementalCurl4jResponse(Curl4jEngineRequest request, CurlMultiHandle handle) throws IOException {
        this.request = request;
        this.handle = handle;

        input = request.makeInput();
        mimeBody = request.buildMime(handle);
        headers = new SListHeaderWrapper(request.headers().toStrings());

        try (HeaderCollector headerCollector = new HeaderCollector()) {
            if (input != null) {
                input.apply(handle);
            } else if (mimeBody != null) {
                mimeBody.apply(handle);
            }
            headers.apply(handle);
            headerCollector.apply(handle);

            curl_easy_setopt(handle.curl, CURLOPT_WRITEFUNCTION, writeCallback.getFunctionAddress());

            for (Consumer<CurlHandle> customOption : request.customOptions()) {
                customOption.accept(handle);
            }

            curl_multi_add_handle(handle.multi, handle.curl);

            // Fill the buffer! This will populate all headers and response codes.
            fillBuffer();
            statusCode = (int) curl_easy_getinfo_long(handle.curl, CURLINFO_RESPONSE_CODE);

            responseHeaders.addAllMulti(headerCollector.getHeaders());
            String contentType = responseHeaders.get("Content-Type");
            String len = responseHeaders.get("Content-Length");
            long contentLength = len != null && !len.isEmpty() ? Long.parseLong(len) : -1;

            // @formatter:off
            webBody = new WebBody() {
                @Override public InputStream open() { return is; }
                @Override public boolean multiOpenAllowed() { return false; }
                @Override public long length() { return contentLength; }
                @Override public @Nullable String contentType() { return contentType; }
            };
            // @formatter:on
        }
    }

    private void fillBuffer() throws IOException {
        assert buffer.position() == 0 || buffer.position() == buffer.limit() : "Buffer must either be empty or fully consumed.";

        // Reset position to 0.
        buffer.position(0);
        // Reset limit to our capacity.
        buffer.limit(buffer.capacity());

        // If we were paused, unpause.
        if (paused) {
            paused = false;
            curl_easy_pause(handle.curl, CURLPAUSE_RECV_CONT);
        }

        try (Memory.Stack stack = Memory.pushStack()) {
            Pointer nHandles = stack.mallocPointer();
            // Do work until we are finished, or we paused.
            while (!done && !paused) {
                int ret = curl_multi_perform(handle.multi, nHandles);

                // curl multi is not healthy.
                if (ret != CURLM_OK) throw new Curl4jHttpException("cURL multi returned error: " + curl_multi_strerror(ret));

                // curl_multi_perform gives us an out pointer for the number of active curl requests.
                done = nHandles.readInt() == 0;
            }
            // We are done!
            if (done) {
                // curl multi puts the curl results into a consumable list of messages for us to process
                // currently (curl 8.2.1) only has a single CURLMSG result type.
                CURLMsg msg;
                while ((msg = curl_multi_info_read(handle.multi, nHandles)) != null) {
                    if (msg.msg() != CURLMSG_DONE) continue;
                    int ret = (int) msg.data();
                    if (ret != CURLE_OK) {
                        throw new Curl4jHttpException("cURL returned erorr: " + curl_easy_strerror(ret));
                    }
                }
            }
        }
        // Flip the buffer position and limit. l = p; p = 0
        buffer.flip();
    }

    private void growBuffer(int more) {
        // Calculate new buffer size.
        int newSize = buffer.limit() + more;
        // reallocate the buffer.
        buf = Memory.realloc(buf, newSize);

        // Create a new ByteBuffer wrapper around our memory block.
        ByteBuffer newBuf = Memory.newDirectByteBuffer(buf, newSize);
        newBuf.position(buffer.position());
        buffer = newBuf;
    }

    @Override
    public void close() throws IOException {
        // Removing the curl handle from the multi handle will abort the request
        // if one is still running.
        curl_multi_remove_handle(handle.multi, handle.curl);
        closeSafe(writeCallback, input, mimeBody, headers);
        Memory.free(buf);
    }

    private static void closeSafe(AutoCloseable... closeables) {
        Throwable exception = null;
        for (AutoCloseable closeable : closeables) {
            if (closeable == null) continue;

            try {
                closeable.close();
            } catch (Throwable ex) {
                if (exception == null) {
                    exception = ex;
                } else {
                    exception.addSuppressed(ex);
                }
            }
        }
        if (exception != null) {
            SneakyUtils.throwUnchecked(exception);
        }
    }

    // @formatter:off
    @Override public Curl4jEngineRequest request() { return request; }
    @Override public int statusCode() { return statusCode; }
    @Override public String message() { return ""; }
    @Override public HeaderList headers() { return responseHeaders; }
    @Override public WebBody body() { return webBody; }
    // @formatter:on
}
