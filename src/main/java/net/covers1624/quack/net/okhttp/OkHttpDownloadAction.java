/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.net.okhttp;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.io.IOUtils;
import net.covers1624.quack.io.WriterOutputStream;
import net.covers1624.quack.net.download.DownloadListener;
import net.covers1624.quack.util.SneakyUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import okio.Source;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static java.util.Objects.requireNonNull;
import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * A Simple action to download files using {@link OkHttpClient}.
 * <p>
 * Created by covers1624 on 20/11/21.
 */
@Requires ("org.slf4j:slf4j-api")
@Requires ("com.squareup.okhttp3:okhttp")
public class OkHttpDownloadAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(OkHttpDownloadAction.class);

    private static final SimpleDateFormat FORMAT_RFC1123 = SneakyUtils.sneaky(() -> {
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format;
    });

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .build();

    private OkHttpClient client = CLIENT;
    @Nullable
    private String url;
    @Nullable
    private Dest dest;
    private boolean onlyIfModified;
    private boolean useETag;
    private boolean quiet = true;
    @Nullable
    private String userAgent;
    @Nullable
    private DownloadListener downloadListener;
    private final Map<Class<?>, Object> tags = new HashMap<>();

    private boolean upToDate;

    /**
     * Execute the download action.
     *
     * @throws IOException           If an IO error occurs whilst downloading.
     * @throws HttpResponseException If the response code was not expected.
     */
    public void execute() throws IOException {
        String url = requireNonNull(this.url, "URL not set");
        Dest dest = requireNonNull(this.dest, "Dest not set");

        Request.Builder builder = new Request.Builder()
                .url(url);
        if (userAgent != null) {
            builder.addHeader("User-Agent", userAgent);
        }
        String etag = dest.getEtag();
        if (useETag && etag != null) {
            builder.addHeader("If-None-Match", etag.trim());
        }

        long lastModified = dest.getLastModified();
        if (onlyIfModified && lastModified != -1) {
            builder.addHeader("If-Modified-Since", FORMAT_RFC1123.format(new Date(lastModified)));
        }
        for (Map.Entry<Class<?>, Object> entry : tags.entrySet()) {
            builder.tag(unsafeCast(entry.getKey()), entry.getValue());
        }

        if (downloadListener != null) {
            downloadListener.connecting();
        }
        if (!quiet) {
            LOGGER.info("Connecting to {}.", url);
        }
        try (Response response = client.newCall(builder.build()).execute()) {
            int code = response.code();
            if ((code < 200 || code > 299) && code != HTTP_NOT_MODIFIED) {
                throw new HttpResponseException(code, response.message());
            }
            Date lastModifiedHeader = response.headers().getDate("Last-Modified");

            boolean eTagNotModified = useETag && code == HTTP_NOT_MODIFIED;
            boolean timestampNotModified = onlyIfModified && lastModifiedHeader != null && lastModified >= lastModifiedHeader.getTime();
            if (eTagNotModified || timestampNotModified) {
                if (!quiet) {
                    String reason = "";
                    if (code == HTTP_NOT_MODIFIED) {
                        reason += "304 not modified ";
                    }
                    if (timestampNotModified) {
                        reason += "Last-Modified header";
                    }
                    LOGGER.info("Not Modified ({}). Skipping '{}'.", reason.trim(), url);
                }
                upToDate = true;
                return;
            }
            ResponseBody body = response.body();
            if (body == null) return; // Okay...

            long contentLen = body.contentLength();
            if (downloadListener != null) {
                downloadListener.start(contentLen);
            }

            Source s = body.source();
            if (downloadListener != null) {
                s = new ProgressForwardingSource(s, downloadListener);
            }
            if (!quiet) {
                LOGGER.info("Downloading '{}'.", url);
            }
            boolean success = false;
            try (Source source = s) {
                try (BufferedSink sink = Okio.buffer(dest.getSink())) {
                    sink.writeAll(source);
                    success = true;
                }
            } finally {
                if (!quiet) {
                    LOGGER.info("Finished '{}'. Success? {}", url, success);
                }
                dest.onFinished(success);
            }
            if (onlyIfModified && lastModifiedHeader != null) {
                dest.setLastModified(lastModifiedHeader.getTime());
            }
            String eTagHeader = response.header("ETag");
            if (useETag && eTagHeader != null) {
                dest.setEtag(eTagHeader);
            }
        }
    }

    /**
     * Set the {@link OkHttpClient} client to use.
     *
     * @param client The {@link OkHttpClient}.
     * @return The same download action.
     */
    public OkHttpDownloadAction setClient(OkHttpClient client) {
        this.client = client;
        return this;
    }

    /**
     * Set the URL to download from.
     *
     * @param url The URL.
     * @return The same download action.
     */
    public OkHttpDownloadAction setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Set the Destination to download content to.
     *
     * @param dest The Destination.
     * @return The same download action.
     */
    public OkHttpDownloadAction setDest(Dest dest) {
        this.dest = dest;
        return this;
    }

    /**
     * Set the {@link StringWriter} to download content to.
     *
     * @param sw The {@link StringWriter}.
     * @return The same download action.
     */
    public OkHttpDownloadAction setDest(StringWriter sw) {
        this.dest = Dest.string(sw);
        return this;
    }

    /**
     * Set the {@link File} to download content to.
     *
     * @param file The file.
     * @return The same download action.
     */
    public OkHttpDownloadAction setDest(File file) {
        this.dest = Dest.file(file);
        return this;
    }

    /**
     * Set the {@link Path} to download content to.
     *
     * @param path The path.
     * @return The same download action.
     */
    public OkHttpDownloadAction setDest(Path path) {
        this.dest = Dest.path(path);
        return this;
    }

    /**
     * If this Download action should use <code>If-Modified-Since</code>
     * HTTP request header.
     *
     * @param onlyIfModified If <code>If-Modified-Since</code> should be used.
     * @return The same download action.
     */
    public OkHttpDownloadAction setOnlyIfModified(boolean onlyIfModified) {
        this.onlyIfModified = onlyIfModified;
        return this;
    }

    /**
     * If this download action should use <code>If-None-Match</code>
     * HTTP request header.
     *
     * @param useETag If <code>If-None-Match</code> should be used.
     * @return The same download action.
     */
    public OkHttpDownloadAction setUseETag(boolean useETag) {
        this.useETag = useETag;
        return this;
    }

    /**
     * If this download action should not log things.
     *
     * @param quiet If the download action should be quiet.
     * @return The same download action.
     */
    public OkHttpDownloadAction setQuiet(boolean quiet) {
        this.quiet = quiet;
        return this;
    }

    /**
     * Sets the <code>User-Agent</code> HTTP request header.
     *
     * @param userAgent The <code>User-Agent</code> request header.
     * @return The same download action.
     */
    public OkHttpDownloadAction setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Set the {@link DownloadListener} to use.
     *
     * @param downloadListener The {@link DownloadListener}.
     * @return The same download action.
     */
    public OkHttpDownloadAction setDownloadListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
        return this;
    }

    /**
     * Add a Tag to the OkHttp {@link Request}.
     *
     * @param clazz The Tag type.
     * @param tag   The tag.
     * @return The same download action.
     */
    public OkHttpDownloadAction addTag(Class<?> clazz, Object tag) {
        tags.put(clazz, tag);
        return this;
    }

    /**
     * Set if the download action exits successfully to indicate
     * weather this action completed without any operations.
     *
     * @return If the file was up-to-date.
     */
    public boolean isUpToDate() {
        return upToDate;
    }

    //@formatter:off
    public OkHttpClient getClient() { return client; }
    @Nullable public String getUrl() { return url; }
    @Nullable public Dest getDest() { return dest; }
    public boolean getOnlyIfModified() { return onlyIfModified; }
    public boolean getUseETag() { return useETag; }
    public boolean getQuiet() { return quiet; }
    @Nullable public String getUserAgent() { return userAgent; }
    @Nullable public DownloadListener getDownloadListener() { return downloadListener; }
    public Map<Class<?>, Object> getTags() { return Collections.unmodifiableMap(tags); }
    //@formatter:on

    public static class HttpResponseException extends IOException {

        public final int code;
        public final String reasonPhrase;

        public HttpResponseException(int code, String reasonPhrase) {
            super("status code: " + code + " , reason phrase: " + reasonPhrase);
            this.code = code;
            this.reasonPhrase = reasonPhrase;
        }
    }

    /**
     * Interface for consuming the output of an HTTP response.
     */
    public interface Dest {

        /**
         * Get the Okio {@link Sink} to push the data to.
         *
         * @return The Sink.
         * @throws IOException If an error occurs opening the output.
         */
        Sink getSink() throws IOException;

        /**
         * Gets the ETag content for the resource.
         *
         * @return The ETag content.
         * @throws IOException If an IO Error occurs whilst reading the ETag content.
         */
        @Nullable
        String getEtag() throws IOException;

        /**
         * Set the ETag content for the current resource.
         *
         * @param etag The ETag content.
         * @throws IOException If an IO Error occurs whilst writing the ETag content.
         */
        void setEtag(String etag) throws IOException;

        /**
         * Gets the LastModified timestamp millis for the current resource.
         *
         * @return The LastModified timestamp. Otherwise <code>-1</code>.
         * @throws IOException If an IO Error occurs whilst reading the timestamp.
         */
        long getLastModified() throws IOException;

        /**
         * Sets the LastModified timestamp millis for the current resource.
         *
         * @param time The LastModified millis.
         * @throws IOException If an IO Error occurs whilst reading the timestamp.
         */
        void setLastModified(long time) throws IOException;

        /**
         * Called when the file has finished downloading.
         *
         * @param success If the operation finished successfully.
         * @throws IOException If an IO Error occurs.
         */
        void onFinished(boolean success) throws IOException;

        /**
         * Returns a {@link Dest} which writes its content to a {@link StringWriter}.
         * <p>
         * This method assumes {@link StandardCharsets#UTF_8}.
         *
         * @param sw The {@link StringWriter}.
         * @return The {@link Dest}.
         */
        static Dest string(StringWriter sw) {
            return string(sw, StandardCharsets.UTF_8);
        }

        /**
         * Returns a {@link Dest} which writes its content to a {@link StringWriter}.
         * <p>
         * The returned {@link Dest} object does not support ETag or LastModified.
         *
         * @param sw      The {@link StringWriter}.
         * @param charset The charset to use.
         * @return The {@link Dest}.
         */
        static Dest string(StringWriter sw, Charset charset) {
            return new Dest() {
                @Override
                public Sink getSink() throws IOException {
                    return Okio.sink(new WriterOutputStream(sw, charset));
                }

                //@formatter:off
                @Nullable @Override public String getEtag() { return null; }
                @Override public void setEtag(String etag) { }
                @Override public long getLastModified() { return -1; }
                @Override public void setLastModified(long time) { }
                @Override public void onFinished(boolean success) { }
                //@formatter:on
            };
        }

        /**
         * Returns a {@link Dest} which writes its content to a {@link File}.
         *
         * @param file The file.
         * @return The {@link Dest}.
         */
        static Dest file(File file) {
            return path(file.toPath());
        }

        /**
         * Returns a {@link Dest} which writes its content to a {@link Path}.
         *
         * @param path The path.
         * @return The {@link Dest}.
         */
        static Dest path(Path path) {
            return new Dest() {

                private final Path tempFile = path.resolveSibling("__tmp_" + path.getFileName());
                private final Path eTagFile = path.resolveSibling(path.getFileName() + ".etag");

                @Override
                public Sink getSink() throws IOException {
                    return Okio.sink(IOUtils.makeParents(tempFile));
                }

                @Nullable
                @Override
                public String getEtag() throws IOException {
                    if (Files.notExists(eTagFile)) {
                        return null;
                    }
                    return new String(Files.readAllBytes(eTagFile), StandardCharsets.UTF_8);
                }

                @Override
                public void setEtag(String etag) throws IOException {
                    Files.write(IOUtils.makeParents(eTagFile), etag.getBytes(StandardCharsets.UTF_8));
                }

                @Override
                public long getLastModified() throws IOException {
                    if (Files.notExists(path)) {
                        return -1;
                    }
                    return Files.getLastModifiedTime(path).toMillis();
                }

                @Override
                public void setLastModified(long time) throws IOException {
                    Files.setLastModifiedTime(path, FileTime.fromMillis(time));
                }

                @Override
                public void onFinished(boolean success) throws IOException {
                    if (success) {
                        Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        Files.deleteIfExists(tempFile);
                    }
                }
            };
        }
    }

}
