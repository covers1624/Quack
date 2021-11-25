/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.net.okhttp;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.DownloadAction;
import net.covers1624.quack.net.HttpResponseException;
import net.covers1624.quack.net.download.DownloadListener;
import net.covers1624.quack.util.SneakyUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static java.util.Objects.requireNonNull;
import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * An OkHttp implementation of {@link DownloadAction}.
 * <p>
 * Created by covers1624 on 20/11/21.
 */
@Requires ("org.slf4j:slf4j-api")
@Requires ("com.squareup.okhttp3:okhttp")
public class OkHttpDownloadAction implements DownloadAction {

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
    @Override
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
            boolean expectNotModified = useETag || onlyIfModified;
            if ((code < 200 || code > 299) && (!expectNotModified || code != HTTP_NOT_MODIFIED)) {
                throw new HttpResponseException(code, response.message());
            }
            Date lastModifiedHeader = response.headers().getDate("Last-Modified");

            boolean notModified = expectNotModified && code == HTTP_NOT_MODIFIED;
            boolean timestampNotModified = onlyIfModified && lastModifiedHeader != null && lastModified >= lastModifiedHeader.getTime();
            if (notModified || timestampNotModified) {
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
                try (BufferedSink sink = Okio.buffer(Okio.sink(dest.getOutputStream()))) {
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

    @Override
    public OkHttpDownloadAction setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public OkHttpDownloadAction setDest(Dest dest) {
        this.dest = dest;
        return this;
    }

    @Override
    public OkHttpDownloadAction setDest(StringWriter sw) {
        return setDest(Dest.string(sw));
    }

    @Override
    public DownloadAction setDest(OutputStream os) {
        return setDest(Dest.stream(os));
    }

    @Override
    public OkHttpDownloadAction setDest(File file) {
        return setDest(Dest.file(file));
    }

    @Override
    public OkHttpDownloadAction setDest(Path path) {
        return setDest(Dest.path(path));
    }

    @Override
    public OkHttpDownloadAction setOnlyIfModified(boolean onlyIfModified) {
        this.onlyIfModified = onlyIfModified;
        return this;
    }

    @Override
    public OkHttpDownloadAction setUseETag(boolean useETag) {
        this.useETag = useETag;
        return this;
    }

    @Override
    public OkHttpDownloadAction setQuiet(boolean quiet) {
        this.quiet = quiet;
        return this;
    }

    @Override
    public OkHttpDownloadAction setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    @Override
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

    @Override
    public boolean isUpToDate() {
        return upToDate;
    }

    //@formatter:off
    public OkHttpClient getClient() { return client; }
    @Override @Nullable public String getUrl() { return url; }
    @Override @Nullable public Dest getDest() { return dest; }
    @Override public boolean getOnlyIfModified() { return onlyIfModified; }
    @Override public boolean getUseETag() { return useETag; }
    @Override public boolean getQuiet() { return quiet; }
    @Override @Nullable public String getUserAgent() { return userAgent; }
    @Override @Nullable public DownloadListener getDownloadListener() { return downloadListener; }
    public Map<Class<?>, Object> getTags() { return Collections.unmodifiableMap(tags); }
    //@formatter:on
}
