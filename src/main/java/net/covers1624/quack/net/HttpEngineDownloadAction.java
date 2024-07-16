/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.io.IOUtils;
import net.covers1624.quack.io.ProgressInputStream;
import net.covers1624.quack.net.download.DownloadListener;
import net.covers1624.quack.net.httpapi.EngineRequest;
import net.covers1624.quack.net.httpapi.EngineResponse;
import net.covers1624.quack.net.httpapi.HttpEngine;
import net.covers1624.quack.net.httpapi.WebBody;
import net.covers1624.quack.util.TimeUtils;
import org.apache.http.client.utils.DateUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.Date;

import static java.util.Objects.requireNonNull;

/**
 * A {@link DownloadAction} backed by the {@link HttpEngine} abstraction.
 * <p>
 * Created by covers1624 on 16/7/24.
 */
@Requires ("org.slf4j:slf4j-api")
public class HttpEngineDownloadAction extends AbstractDownloadAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpEngineDownloadAction.class);

    private @Nullable HttpEngine engine;

    public HttpEngineDownloadAction() {
    }

    public HttpEngineDownloadAction(HttpEngine engine) {
        this.engine = engine;
    }

    @Override
    public void execute() throws IOException {
        HttpEngine engine = requireNonNull(this.engine, "HttpEngine not set.");
        String url = requireNonNull(this.url, "URL not set");
        Dest dest = requireNonNull(this.dest, "Dest not set");

        EngineRequest request = engine.newRequest()
                .method("GET", null)
                .url(url)
                .headers(headerList);
        if (userAgent != null && !headerList.contains("User-Agent")) {
            request.header("User-Agent", userAgent);
        }

        String eTag = dest.getEtag();
        if (useETag && eTag != null) {
            request.header("If-None-Match", eTag.trim());
        }

        long lastModifiedDisk = dest.getLastModified();
        if (onlyIfModified && lastModifiedDisk != -1) {
            request.header("If-Modified-Since", TimeUtils.FORMAT_RFC1123.format(new Date(lastModifiedDisk)));
        }

        if (downloadListener != null) downloadListener.connecting();

        if (!quiet) LOGGER.info("Connecting to {}.", url);

        try (EngineResponse response = request.execute()) {
            int code = response.statusCode();
            validateCode(code, response.message());

            String lastModifiedHeader = response.headers().get("Last-Modified");
            Date lastModifiedHeaderDate = null;
            if (lastModifiedHeader != null) {
                lastModifiedHeaderDate = TimeUtils.parseDate(lastModifiedHeader);
            }

            upToDate = calcUpToDate(code, lastModifiedDisk, lastModifiedHeaderDate);
            if (upToDate) return;

            WebBody body = response.body();
            if (body == null) return;

            long contentLen = body.length();
            if (downloadListener != null) downloadListener.start(contentLen);

            InputStream content = body.open();
            if (downloadListener != null) {
                content = new ProgressInputStream(content, downloadListener);
            }

            if (!quiet) LOGGER.info("Downloading '{}'.", url);

            boolean success = false;
            try (InputStream is = content;
                 OutputStream os = dest.getOutputStream()) {
                IOUtils.copy(is, os);
                success = true;
            } finally {
                if (!quiet) LOGGER.info("Finished '{}'. Success? {}", url, success);
                dest.onFinished(success);
            }

            if (onlyIfModified && lastModifiedHeaderDate != null) {
                dest.setLastModified(lastModifiedHeaderDate.getTime());
            }

            String eTagHeader = response.headers().get("ETag");
            if (useETag && eTagHeader != null) {
                dest.setEtag(eTagHeader);
            }
        }
    }

    public HttpEngineDownloadAction setEngine(HttpEngine engine) {
        this.engine = engine;
        return this;
    }

    //@formatter:off
    @Override public HttpEngineDownloadAction setUrl(String url) { super.setUrl(url); return this; }
    @Override public HttpEngineDownloadAction setDest(Dest dest) { super.setDest(dest); return this; }
    @Override public HttpEngineDownloadAction setDest(StringWriter sw) { super.setDest(sw); return this; }
    @Override public HttpEngineDownloadAction setDest(OutputStream os) { super.setDest(os); return this; }
    @Override public HttpEngineDownloadAction setDest(File file) { super.setDest(file); return this; }
    @Override public HttpEngineDownloadAction setDest(Path path) { super.setDest(path); return this; }
    @Override public HttpEngineDownloadAction setOnlyIfModified(boolean onlyIfModified) { super.setOnlyIfModified(onlyIfModified); return this; }
    @Override public HttpEngineDownloadAction setUseETag(boolean useETag) { super.setUseETag(useETag); return this; }
    @Override public HttpEngineDownloadAction setQuiet(boolean quiet) { super.setQuiet(quiet); return this; }
    @Override public HttpEngineDownloadAction addRequestHeader(String key, String value) { super.addRequestHeader(key, value); return this; }
    @Override public HttpEngineDownloadAction setUserAgent(String userAgent) { super.setUserAgent(userAgent); return this; }
    @Override public HttpEngineDownloadAction setDownloadListener(DownloadListener downloadListener) { super.setDownloadListener(downloadListener); return this; }
    //@formatter:on
}
