/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.apache;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.io.IOUtils;
import net.covers1624.quack.io.ProgressInputStream;
import net.covers1624.quack.net.AbstractDownloadAction;
import net.covers1624.quack.net.DownloadAction;
import net.covers1624.quack.net.download.DownloadListener;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * An Apache HttpClient implementation of {@link DownloadAction}.
 * <p>
 * Created by covers1624 on 22/11/21.
 */
@Requires ("org.slf4j:slf4j-api")
@Requires ("org.apache.httpcomponents:httpclient")
public class ApacheHttpClientDownloadAction extends AbstractDownloadAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApacheHttpClientDownloadAction.class);

    private static final CloseableHttpClient CLIENT = HttpClientBuilder.create().build();

    private CloseableHttpClient client = CLIENT;

    @Override
    public void execute() throws IOException {
        String url = requireNonNull(this.url, "URL not set");
        Dest dest = requireNonNull(this.dest, "Dest not set");

        HttpGet get = new HttpGet(url);
        headerList.forEach((name, value) -> get.addHeader(name, value));
        if (userAgent != null && !headerList.contains("User-Agent")) {
            get.addHeader("User-Agent", userAgent);
        }
        String etag = dest.getEtag();
        if (useETag && etag != null) {
            get.addHeader("If-None-Match", etag.trim());
        }

        long lastModifiedDisk = dest.getLastModified();
        if (onlyIfModified && lastModifiedDisk != -1) {
            get.addHeader("If-Modified-Since", DateUtils.formatDate(new Date(lastModifiedDisk)));
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
            validateCode(code, line.getReasonPhrase());

            Header lastModifiedHeader = response.getLastHeader("Last-Modified");
            Date lastModifiedHeaderDate = null;
            if (lastModifiedHeader != null) {
                lastModifiedHeaderDate = DateUtils.parseDate(lastModifiedHeader.getValue());
            }

            upToDate = calcUpToDate(code, lastModifiedDisk, lastModifiedHeaderDate);

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

    //@formatter:off
    @Override public ApacheHttpClientDownloadAction setUrl(String url) { super.setUrl(url); return this; }
    @Override public ApacheHttpClientDownloadAction setDest(Dest dest) { super.setDest(dest); return this; }
    @Override public ApacheHttpClientDownloadAction setDest(StringWriter sw) { super.setDest(sw); return this; }
    @Override public ApacheHttpClientDownloadAction setDest(OutputStream os) { super.setDest(os); return this; }
    @Override public ApacheHttpClientDownloadAction setDest(File file) { super.setDest(file); return this; }
    @Override public ApacheHttpClientDownloadAction setDest(Path path) { super.setDest(path); return this; }
    @Override public ApacheHttpClientDownloadAction setOnlyIfModified(boolean onlyIfModified) { super.setOnlyIfModified(onlyIfModified); return this; }
    @Override public ApacheHttpClientDownloadAction setUseETag(boolean useETag) { super.setUseETag(useETag); return this; }
    @Override public ApacheHttpClientDownloadAction setQuiet(boolean quiet) { super.setQuiet(quiet); return this; }
    @Override public ApacheHttpClientDownloadAction addRequestHeader(String key, String value) { super.addRequestHeader(key, value); return this; }
    @Override public ApacheHttpClientDownloadAction setUserAgent(String userAgent) { super.setUserAgent(userAgent); return this; }
    @Override public ApacheHttpClientDownloadAction setDownloadListener(DownloadListener downloadListener) { super.setDownloadListener(downloadListener); return this; }
    public CloseableHttpClient getClient() { return client; }
    //@formatter:on
}
