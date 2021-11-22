/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.net.apache;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.io.IOUtils;
import net.covers1624.quack.io.ProgressInputStream;
import net.covers1624.quack.net.DownloadAction;
import net.covers1624.quack.net.HttpResponseException;
import net.covers1624.quack.net.download.DownloadListener;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.Date;

import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static java.util.Objects.requireNonNull;

/**
 * An Apache HttpClient implementation of {@link DownloadAction}.
 * <p>
 * Created by covers1624 on 22/11/21.
 */
@Requires ("org.slf4j:slf4j-api")
@Requires ("org.apache.httpcomponents:httpclient")
public class ApacheHttpClientDownloadAction implements DownloadAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApacheHttpClientDownloadAction.class);

    private static final CloseableHttpClient CLIENT = HttpClientBuilder.create().build();

    private CloseableHttpClient client = CLIENT;
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

    private boolean upToDate;

    @Override
    public void execute() throws IOException {
        String url = requireNonNull(this.url, "URL not set");
        Dest dest = requireNonNull(this.dest, "Dest not set");

        HttpGet get = new HttpGet(url);
        if (userAgent != null) {
            get.addHeader("User-Agent", userAgent);
        }
        String etag = dest.getEtag();
        if (useETag && etag != null) {
            get.addHeader("If-None-Match", etag.trim());
        }

        long lastModified = dest.getLastModified();
        if (onlyIfModified && lastModified != -1) {
            get.addHeader("If-Modified-Since", DateUtils.formatDate(new Date(lastModified)));
        }

        if (downloadListener != null) {
            downloadListener.connecting();
        }
        if (!quiet) {
            LOGGER.info("Connecting to {}.", url);
        }
        try (CloseableHttpResponse response = client.execute(get)) {
            StatusLine line = response.getStatusLine();
            int code = line.getStatusCode();
            boolean expectNotModified = useETag || onlyIfModified;
            if ((code < 200 || code > 299) && expectNotModified && code != HTTP_NOT_MODIFIED) {
                throw new HttpResponseException(code, line.getReasonPhrase());
            }
            Header lastModifiedHeader = response.getLastHeader("Last-Modified");
            Date lastModifiedHeaderDate = null;
            if (lastModifiedHeader != null) {
                lastModifiedHeaderDate = DateUtils.parseDate(lastModifiedHeader.getValue());
            }

            boolean notModified = expectNotModified && code == HTTP_NOT_MODIFIED;
            boolean timestampNotModified = onlyIfModified && lastModifiedHeader != null && lastModified >= lastModifiedHeaderDate.getTime();
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
            HttpEntity entity = response.getEntity();
            if (entity == null) return; // Okay...

            long contentLen = entity.getContentLength();
            if (downloadListener != null) {
                downloadListener.start(contentLen);
            }

            InputStream content = entity.getContent();
            if (downloadListener != null) {
                content = new ProgressInputStream(content, downloadListener);
            }
            if (!quiet) {
                LOGGER.info("Downloading '{}'.", url);
            }
            boolean success = false;
            try (InputStream is = content) {
                try (OutputStream os = dest.getOutputStream()) {
                    IOUtils.copy(is, os);
                    success = true;
                }
            } finally {
                if (!quiet) {
                    LOGGER.info("Finished '{}'. Success? {}", url, success);
                }
                dest.onFinished(success);
            }
            if (onlyIfModified && lastModifiedHeaderDate != null) {
                dest.setLastModified(lastModifiedHeaderDate.getTime());
            }
            Header eTagHeader = response.getFirstHeader("ETag");
            if (useETag && eTagHeader != null) {
                dest.setEtag(eTagHeader.getValue());
            }
        }
    }

    /**
     * Set the {@link CloseableHttpClient} client to use.
     *
     * @param client The {@link CloseableHttpClient}.
     * @return The same download action.
     */
    public ApacheHttpClientDownloadAction setClient(CloseableHttpClient client) {
        this.client = client;
        return this;
    }

    @Override
    public ApacheHttpClientDownloadAction setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public ApacheHttpClientDownloadAction setDest(Dest dest) {
        this.dest = dest;
        return this;
    }

    @Override
    public ApacheHttpClientDownloadAction setDest(StringWriter sw) {
        return setDest(Dest.string(sw));
    }

    @Override
    public DownloadAction setDest(OutputStream os) {
        return setDest(Dest.stream(os));
    }

    @Override
    public ApacheHttpClientDownloadAction setDest(File file) {
        return setDest(Dest.file(file));
    }

    @Override
    public ApacheHttpClientDownloadAction setDest(Path path) {
        return setDest(Dest.path(path));
    }

    @Override
    public ApacheHttpClientDownloadAction setOnlyIfModified(boolean onlyIfModified) {
        this.onlyIfModified = onlyIfModified;
        return this;
    }

    @Override
    public ApacheHttpClientDownloadAction setUseETag(boolean useETag) {
        this.useETag = useETag;
        return this;
    }

    @Override
    public ApacheHttpClientDownloadAction setQuiet(boolean quiet) {
        this.quiet = quiet;
        return this;
    }

    @Override
    public ApacheHttpClientDownloadAction setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    @Override
    public ApacheHttpClientDownloadAction setDownloadListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
        return this;
    }

    @Override
    public boolean isUpToDate() {
        return upToDate;
    }

    //@formatter:off
    public CloseableHttpClient getClient() { return client; }
    @Override @Nullable public String getUrl() { return url; }
    @Override @Nullable public Dest getDest() { return dest; }
    @Override public boolean getOnlyIfModified() { return onlyIfModified; }
    @Override public boolean getUseETag() { return useETag; }
    @Override public boolean getQuiet() { return quiet; }
    @Override @Nullable public String getUserAgent() { return userAgent; }
    @Override @Nullable public DownloadListener getDownloadListener() { return downloadListener; }
    //@formatter:on
}
