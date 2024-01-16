/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.curl4j;

import net.covers1624.curl4j.CURL;
import net.covers1624.curl4j.util.*;
import net.covers1624.curl4j.util.CurlMimeBody.Builder.PartBuilder;
import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.httpapi.AbstractEngineRequest;
import net.covers1624.quack.net.httpapi.HeaderList;
import net.covers1624.quack.net.httpapi.MultipartBody;
import net.covers1624.quack.net.httpapi.WebBody;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static net.covers1624.curl4j.CURL.*;

/**
 * Created by covers1624 on 1/11/23.
 */
@Requires ("net.covers1624:curl4j")
public class Curl4jEngineRequest extends AbstractEngineRequest {

    private final Curl4jHttpEngine engine;

    private final List<Consumer<CurlHandle>> customOptions = new LinkedList<>();

    private boolean followRedirects = true;
    private @Nullable Path destFile;
    private @Nullable String unixSocket;

    private @Nullable String method;
    private @Nullable WebBody body;

    public Curl4jEngineRequest(Curl4jHttpEngine engine) {
        this.engine = engine;
    }

    @Override
    public Curl4jEngineRequest method(String method, @Nullable WebBody body) {
        this.method = method;
        this.body = body;
        return this;
    }

    /**
     * Called to tell this request to not follow redirects.
     *
     * @return The same request.
     */
    public Curl4jEngineRequest dontFollowRedirects() {
        assertState();
        followRedirects = false;
        return this;
    }

    /**
     * Tell this Request to store the body in the given file. Otherwise,
     * the request will return an incremental response, only downloading as fast
     * as the client pulls data from the body.
     * <p>
     * This is a hot-path for downloading files.
     * <p>
     * If a path is provided, it can be assumed that a {@link WebBody.PathBody}
     * is used on the response.
     *
     * @param destFile The destination file.
     * @return The same request.
     */
    public Curl4jEngineRequest useFileOutput(Path destFile) {
        assertState();
        this.destFile = destFile;
        return this;
    }

    /**
     * Set the {@link CURL#CURLOPT_UNIX_SOCKET_PATH} option.
     *
     * @param unixSocket The socket.
     * @return The same request.
     */
    public Curl4jEngineRequest unixSocket(String unixSocket) {
        this.unixSocket = unixSocket;
        return this;
    }

    /**
     * Provide custom configuration to curl.
     * <p>
     * These callbacks are called after all default state has been set, but prior to
     * executing the curl operation.
     *
     * @param action The action.
     * @return The same request.
     */
    public Curl4jEngineRequest addCustomOption(Consumer<CurlHandle> action) {
        customOptions.add(action);
        return this;
    }

    // @formatter:off
    @Override public Curl4jEngineRequest url(String url) { super.url(url); return this; }
    @Override public Curl4jEngineRequest header(String key, String value) { super.header(key, value); return this; }
    @Override public Curl4jEngineRequest headers(Map<String, String> headers) { super.headers(headers); return this; }
    @Override public Curl4jEngineRequest headers(HeaderList headers) { super.headers(headers); return this; }
    @Override public Curl4jEngineRequest removeHeader(String key) { super.removeHeader(key); return this; }
    // @formatter:on

    @Override
    protected void assertState() {
        super.assertState();
        if (method == null) {
            throw new IllegalStateException("method(String, Body) must be called first");
        }
    }

    @Override
    public Curl4jEngineResponse execute() throws IOException {
        assertState();

        if (url == null) throw new IllegalStateException("Url not set.");
        executed = true;

        // If we are not writing into a file, we must take the incremental path
        // to give the user control over where the data is going.
        if (destFile == null) {
            HandlePool<CurlMultiHandle>.Entry handleEntry = engine.getMultiHandle();
            setupHandle(handleEntry.handle);
            return new IncrementalCurl4jResponse(this, handleEntry);
        }

        HandlePool<CurlHandle>.Entry handleEntry = engine.getHandle();
        CurlHandle handle = handleEntry.handle;

        setupHandle(handle);

        // Files get a hot path, we don't curl_multi it, just blast it right to the file.
        try (HandlePool<CurlHandle>.Entry ignored = handleEntry;
             CurlOutput output = CurlOutput.toFile(destFile);
             CurlInput input = makeInput();
             CurlMimeBody mimeBody = buildMime(handle);
             SListHeaderWrapper headers = new SListHeaderWrapper(this.headers.toStrings());
             HeaderCollector headerCollector = new HeaderCollector()) {

            output.apply(handle);
            headerCollector.apply(handle);

            if (input != null) {
                input.apply(handle);
            } else if (mimeBody != null) {
                mimeBody.apply(handle);
            }

            headers.apply(handle);

            for (Consumer<CurlHandle> customOption : customOptions) {
                customOption.accept(handle);
            }

            int result = curl_easy_perform(handle.curl);
            if (result != CURLE_OK) {
                throw new IOException("Curl returned error: " + curl_easy_strerror(result));
            }

            HeaderList responseHeaders = new HeaderList();
            responseHeaders.addAllMulti(headerCollector.getHeaders());

            long responseCode = curl_easy_getinfo_long(handle.curl, CURLINFO_RESPONSE_CODE);
            String contentType = responseHeaders.get("Content-Type");
            WebBody respBody = new WebBody.PathBody(destFile, contentType);
            return new Curl4jEngineResponse() {
                // @formatter:off
                @Override public Curl4jEngineRequest request() { return Curl4jEngineRequest.this; }
                @Override public int statusCode() { return (int) responseCode; }
                @Override public String message() { return ""; }
                @Override public HeaderList headers() { return responseHeaders; }
                @Override public WebBody body() { return respBody; }
                @Override public void close() { }
                // @formatter:on
            };
        }
    }

    private void setupHandle(CurlHandle handle) {
        curl_easy_reset(handle.curl);

        String impersonate = engine.getImpersonate();
        if (impersonate != null) {
            curl_easy_impersonate(handle.curl, impersonate, true);
        }

        curl_easy_setopt(handle.curl, CURLOPT_URL, url);
        curl_easy_setopt(handle.curl, CURLOPT_CUSTOMREQUEST, method);

        if (followRedirects) {
            curl_easy_setopt(handle.curl, CURLOPT_FOLLOWLOCATION, true);
        }

        if (unixSocket != null) {
            curl_easy_setopt(handle.curl, CURLOPT_UNIX_SOCKET_PATH, unixSocket);
        }

        if (body != null && !(body instanceof MultipartBody)) {
            String contentType = body.contentType();
            if (contentType != null) {
                headers.add("Content-Type", contentType);
            }
        }
    }

    HeaderList headers() {
        return headers;
    }

    List<Consumer<CurlHandle>> customOptions() {
        return customOptions;
    }

    @Nullable CurlInput makeInput() {
        if (body == null || body instanceof MultipartBody) return null;

        return inputFromBody(body);
    }

    @Nullable CurlMimeBody buildMime(CurlHandle handle) {
        if (!(body instanceof MultipartBody)) return null;

        CurlMimeBody.Builder builder = CurlMimeBody.builder(handle);
        for (MultipartBody.Part part : ((MultipartBody) body).getParts()) {
            PartBuilder partBuilder = builder.addPart(part.name);
            if (part.fileName != null) {
                partBuilder.fileName(part.fileName);
            }
            partBuilder.body(inputFromBody(part.body));
        }
        return builder.build();
    }

    private static CurlInput inputFromBody(WebBody body) {
        return new CurlInput() {
            private @Nullable InputStream is;

            @Override
            protected ReadableByteChannel open() throws IOException {
                return Channels.newChannel(getStream());
            }

            @Override
            public long availableBytes() throws IOException {
                long len = body.length();
                if (len == -1) {
                    len = getStream().available();
                }
                return len;
            }

            private InputStream getStream() throws IOException {
                if (is == null) {
                    is = body.open();
                }
                return is;
            }
        };
    }
}
