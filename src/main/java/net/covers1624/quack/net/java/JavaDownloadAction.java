/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.java;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.io.IOUtils;
import net.covers1624.quack.io.ProgressInputStream;
import net.covers1624.quack.net.AbstractDownloadAction;
import net.covers1624.quack.net.DownloadAction;
import net.covers1624.quack.net.download.DownloadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static java.util.Objects.requireNonNull;

/**
 * An implementation of {@link DownloadAction} which uses
 * java's builtin HttpURLConnection.
 * <p>
 * Created by covers1624 on 16/12/21.
 */
@Requires ("org.slf4j:slf4j-api")
public class JavaDownloadAction extends AbstractDownloadAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaDownloadAction.class);
    private static final int MAX_REDIRECTS = Integer.getInteger("quack.JavaDownloadAction.max_redirects", 5);

    @Override
    public void execute() throws IOException {
        String url = requireNonNull(this.url, "URL not set");
        Dest dest = requireNonNull(this.dest, "Dest not set");

        String etag = dest.getEtag();
        long lastModified = dest.getLastModified();

        for (int i = 0; i < MAX_REDIRECTS; i++) {
            URL res = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) res.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setInstanceFollowRedirects(false);
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                for (String value : entry.getValue()) {
                    conn.setRequestProperty(entry.getKey(), value);
                }
            }
            if (userAgent != null) {
                conn.setRequestProperty("User-Agent", userAgent);
            }

            if (useETag && etag != null) {
                conn.setRequestProperty("If-None-Match", etag.trim());
            }

            if (onlyIfModified && lastModified != -1) {
                conn.setRequestProperty("If-Modified-Since", FORMAT_RFC1123.format(new Date(lastModified)));
            }

            if (downloadListener != null) {
                downloadListener.connecting();
            }

            if (!quiet) {
                LOGGER.info("Connecting to {}.", url);
            }

            int code = conn.getResponseCode();
            String locHeader = conn.getHeaderField("Location");
            if (shouldFollowRedirect(code) && locHeader != null) {
                locHeader = URLDecoder.decode(locHeader, "UTF-8");
                url = new URL(new URL(url), locHeader).toExternalForm();
                LOGGER.info("Following redirect to {}.", url);
                try {
                    conn.getInputStream().close();
                } catch (Throwable ignored) {
                }
                continue;
            }

            validateCode(code, conn.getResponseMessage());

            InputStream stream = conn.getInputStream();

            String eTagHeader = conn.getHeaderField("ETag");
            Date lastModifiedHeader = parseDate(conn.getHeaderField("Last-Modified"));
            long contentLength = conn.getHeaderFieldLong("Content-Length", -1);

            upToDate = calcUpToDate(code, lastModified, lastModifiedHeader);
            if (upToDate) {
                return;
            }

            if (downloadListener != null) {
                downloadListener.start(contentLength);
            }

            InputStream content = stream;
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
            if (onlyIfModified && lastModifiedHeader != null) {
                dest.setLastModified(lastModifiedHeader.getTime());
            }

            if (useETag && eTagHeader != null) {
                dest.setEtag(eTagHeader);
            }
            return;
        }
    }

    private static boolean shouldFollowRedirect(int code) {
        return code >= 300 && code <= 307 && code != 306 && code != HTTP_NOT_MODIFIED;
    }

    //@formatter:off
    @Override public JavaDownloadAction setUrl(String url) { super.setUrl(url); return this; }
    @Override public JavaDownloadAction setDest(Dest dest) { super.setDest(dest); return this; }
    @Override public JavaDownloadAction setDest(StringWriter sw) { super.setDest(sw); return this; }
    @Override public JavaDownloadAction setDest(OutputStream os) { super.setDest(os); return this; }
    @Override public JavaDownloadAction setDest(File file) { super.setDest(file); return this; }
    @Override public JavaDownloadAction setDest(Path path) { super.setDest(path); return this; }
    @Override public JavaDownloadAction setOnlyIfModified(boolean onlyIfModified) { super.setOnlyIfModified(onlyIfModified); return this; }
    @Override public JavaDownloadAction setUseETag(boolean useETag) { super.setUseETag(useETag); return this; }
    @Override public JavaDownloadAction setQuiet(boolean quiet) { super.setQuiet(quiet); return this; }
    @Override public JavaDownloadAction setUserAgent(String userAgent) { super.setUserAgent(userAgent); return this; }
    @Override public JavaDownloadAction addRequestHeader(String key, String value) { super.addRequestHeader(key, value); return this; }
    @Override public JavaDownloadAction setDownloadListener(DownloadListener downloadListener) { super.setDownloadListener(downloadListener); return this; }
    //@formatter:on
}
