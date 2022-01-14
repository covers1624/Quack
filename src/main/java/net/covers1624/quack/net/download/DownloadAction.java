/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.download;

import net.covers1624.quack.annotation.ReplaceWith;
import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.collection.ColUtils;
import net.covers1624.quack.util.DataUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.Date;
import java.util.function.Predicate;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Inspired and vaguely based off https://github.com/michel-kraemer/gradle-download-task
 * <pre>
 * Notable differences:
 *  Wayyy simpler implementation.
 *  Lazy evaluation of file and URL inputs.
 *  Single file downloads.
 *  External validation of file for up-to-date checking.
 *  UserAgent spoofing. (Thanks mojang!)
 *  Ability to set the ProgressLogger to use.
 * </pre>
 * <p>
 * This is split into an Action, Spec and Task.
 * <p>
 * The Spec {@link DownloadSpec}, Provides the specification for how things work.
 * <p>
 * The Action {@link DownloadAction}, What actually handles downloading
 * implements {@link DownloadSpec}, Useful for other tasks that need to download
 * something but not necessarily create an entire task to do said download.
 * <p>
 * The Task {@link DownloadTask} for gradle, Task wrapper for {@link DownloadAction},
 * implements {@link DownloadSpec} and hosts the Action as a task.
 * <p>
 * Created by covers1624 on 8/02/19.
 */
@Deprecated
@ReplaceWith ("net.covers1624.quack.net.apache.ApacheHttpClientDownloadAction")
@ScheduledForRemoval (inVersion = "0.5.0")
@Requires ("org.slf4j:slf4j-api")
@Requires ("org.apache.commons:commons-lang3")
@Requires ("org.apache.httpcomponents:httpclient")
public class DownloadAction implements DownloadSpec {

    private static final Logger LOGGER = LoggerFactory.getLogger("DownloadAction");

    private Object src;
    private Path dest;
    private boolean onlyIfModified;
    private UseETag useETag = UseETag.FALSE;
    private Path eTagFile;
    private String userAgent;
    private boolean quiet;
    private Predicate<Path> fileUpToDate = e -> true;

    private DownloadListener listener;

    private boolean upToDate;

    public DownloadAction() {
    }

    public void execute() throws IOException {
        if (src == null) {
            throw new IllegalArgumentException("Download source not provided");
        }
        if (dest == null) {
            throw new IllegalArgumentException("Download destination not provided.");
        }

        URL src = getSrc();
        Path dest = getDest();

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(src.toString());
            long timestamp = 0;
            if (Files.exists(dest)) {
                timestamp = Files.getLastModifiedTime(dest).toMillis();
            }
            if (onlyIfModified && Files.exists(dest)) {
                request.addHeader("If-Modified-Since", DateUtils.formatDate(new Date(timestamp)));
            }
            if (getUseETag().isEnabled()) {
                String etag = loadETag(src);
                if (!getUseETag().weak && StringUtils.startsWith(etag, "W/")) {
                    etag = null;
                }
                if (etag != null) {
                    request.addHeader("If-None-Match", etag);
                }
            }
            request.addHeader("Accept-Encoding", "gzip");
            if (getUserAgent() != null) {
                request.addHeader("User-Agent", getUserAgent());
            }

            try (CloseableHttpResponse response = client.execute(request)) {
                int code = response.getStatusLine().getStatusCode();
                if ((code < 200 || code > 299) && code != HttpStatus.SC_NOT_MODIFIED) {
                    throw new HttpResponseException(code, response.getStatusLine().getReasonPhrase());
                }
                long lastModified = 0;
                Header lastModifiedHeader = response.getLastHeader("Last-Modified");
                if (lastModifiedHeader != null) {
                    String val = lastModifiedHeader.getValue();
                    if (!StringUtils.isEmpty(val)) {
                        Date date = DateUtils.parseDate(val);
                        if (date != null) {
                            lastModified = date.getTime();
                        }
                    }
                }
                if ((code == HttpStatus.SC_NOT_MODIFIED || (lastModified != 0 && timestamp >= lastModified)) && fileUpToDate.test(dest)) {
                    if (!isQuiet()) {
                        LOGGER.info("Not Modified. Skipping '{}'.", src);
                    }
                    upToDate = true;
                    return;
                }
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return;//kden..
                }

                long contentLen = entity.getContentLength();
                long processed = 0;
                if (listener != null) {
                    listener.start(contentLen);
                }
                boolean finished = false;
                Path dstTmp = dest.resolveSibling("__tmp_" + dest.getFileName());
                if (Files.notExists(dstTmp.getParent())) {
                    Files.createDirectories(dstTmp.getParent());
                }
                try (InputStream is = entity.getContent()) {
                    try (OutputStream os = Files.newOutputStream(dstTmp, CREATE)) {
                        byte[] buffer = new byte[16384];
                        int len;
                        while ((len = is.read(buffer)) >= 0) {
                            os.write(buffer, 0, len);
                            processed += len;
                            if (listener != null) {
                                listener.update(processed);
                            }
                        }
                        os.flush();
                        finished = true;
                    }
                } finally {
                    if (!finished) {
                        Files.delete(dstTmp);
                    } else {
                        Files.move(dstTmp, dest, REPLACE_EXISTING);
                        if (Files.notExists(dest.getParent())) {
                            Files.createDirectories(dest.getParent());
                        }
                    }
                    if (listener != null) {
                        listener.finish(processed);
                    }
                }
                if (onlyIfModified && lastModified > 0) {
                    Files.setLastModifiedTime(dest, FileTime.fromMillis(lastModified));
                }
                if (getUseETag().isEnabled()) {
                    Header eTagHeader = response.getFirstHeader("ETag");
                    if (eTagHeader != null) {
                        String etag = eTagHeader.getValue();
                        boolean isWeak = StringUtils.startsWith(etag, "W/");
                        if (isWeak && getUseETag().warnOnWeak && !quiet) {
                            LOGGER.warn("Weak ETag found.");
                        }
                        if (!isWeak || getUseETag().weak) {
                            saveETag(src, etag);
                        }
                    }
                }
            }
        }
    }

    @Nullable
    protected String loadETag(URL url) {
        Path eTagFile = getETagFile();
        if (Files.notExists(eTagFile)) {
            return null;
        }
        try {
            return ColUtils.headOption(Files.readAllLines(eTagFile)).orElse(null);
        } catch (IOException e) {
            LOGGER.warn("Error reading ETag file '{}'.", eTagFile);
            return null;
        }
    }

    protected void saveETag(URL url, String eTag) {
        Path eTagFile = getETagFile();
        try {
            Path tmp = eTagFile.resolveSibling("__tmp_" + eTagFile.getFileName());
            Files.write(tmp, Collections.singleton(eTag), CREATE);
            Files.move(tmp, eTagFile, REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.warn("Error saving ETag file '{}'.", eTagFile, e);
        }
    }

    public static String toLengthText(long bytes) {
        return DataUtils.humanSize(bytes);
    }

    @Override
    public void fileUpToDateWhen(Predicate<Path> spec) {
        fileUpToDate = fileUpToDate.and(spec);
    }

    //@formatter:off
    @Override public URL getSrc() { return makeURL(src); }
    @Override public Path getDest() { return dest; }
    @Override public boolean getOnlyIfModified() { return onlyIfModified; }
    @Override public UseETag getUseETag() { return useETag; }
    @Override public Path getETagFile() { return getETagFile_(); }
    @Override public String getUserAgent() { return userAgent; }
    @Override public DownloadListener getListener() { return listener; }
    @Override public boolean isQuiet() { return quiet; }
    @Override public boolean isUpToDate() { return upToDate; }
    @Override public void setSrc(Object src) { this.src = src; }
    @Override public void setDest(Path dest) { this.dest = dest; }
    @Override public void setOnlyIfModified(boolean onlyIfModified) { this.onlyIfModified = onlyIfModified; }
    @Override public void setUseETag(Object eTag) { useETag = UseETag.parse(eTag); }
    @Override public void setETagFile(Path eTagFile) { this.eTagFile = eTagFile; }
    @Override public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    @Override public void setQuiet(boolean quiet) { this.quiet = quiet; }
    @Override public void setListener(DownloadListener listener) { this.listener = listener; }
    //@formatter:on

    private Path getETagFile_() {
        if (eTagFile == null) {
            Path dest = getDest();
            return dest.resolveSibling(dest.getFileName() + ".etag");
        }
        return eTagFile;
    }

    private URL makeURL(Object object) {
        if (object instanceof CharSequence) {
            try {
                return new URL(((CharSequence) object).toString());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Invalid URL " + object, e);
            }
        } else if (object instanceof URL) {
            return (URL) object;
        } else {
            throw new IllegalArgumentException("Expected CharSequence or URL. Got: " + object.getClass());
        }
    }

    public enum UseETag {
        FALSE(false, false),
        TRUE(true, true),
        ALL(true, false),
        STRONG(false, false);

        public final boolean weak;
        public final boolean warnOnWeak;

        UseETag(boolean weak, boolean warnOnWeak) {
            this.weak = weak;
            this.warnOnWeak = warnOnWeak;
        }

        public boolean isEnabled() {
            return this != FALSE;
        }

        public static UseETag parse(Object value) {
            if (value instanceof UseETag) {
                return (UseETag) value;
            } else if (value instanceof Boolean) {
                if ((Boolean) value) {
                    return TRUE;
                } else {
                    return FALSE;
                }
            } else if (value instanceof String) {
                switch ((String) value) {
                    case "true":
                        return TRUE;
                    case "false":
                        return FALSE;
                    case "all":
                        return ALL;
                    case "strong":
                        return STRONG;
                }
            }
            throw new IllegalArgumentException("Unable to parse ETag, Unknown value: " + value);
        }
    }

}
