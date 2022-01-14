/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.download.DownloadListener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;

/**
 * Created by covers1624 on 16/12/21.
 */
@Requires ("org.slf4j:slf4j-api")
public abstract class AbstractDownloadAction implements DownloadAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDownloadAction.class);

    protected static final SimpleDateFormat FORMAT_RFC1123 = parseFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
    protected static final SimpleDateFormat FORMAT_RFC1036 = parseFormat("EEE, dd-MMM-yy HH:mm:ss zzz");
    protected static final SimpleDateFormat FORMAT_ASCTIME = parseFormat("EEE MMM d HH:mm:ss yyyy");

    protected static final SimpleDateFormat[] PATTERNS = new SimpleDateFormat[] {
            FORMAT_RFC1123,
            FORMAT_RFC1036,
            FORMAT_ASCTIME
    };

    private static final Date TWO_DIGIT_YEAR_START;

    static {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        TWO_DIGIT_YEAR_START = cal.getTime();
    }

    @Nullable
    protected String url;
    @Nullable
    protected Dest dest;
    protected boolean onlyIfModified;
    protected boolean useETag;
    protected boolean quiet = true;
    @Nullable
    protected String userAgent;
    @Nullable
    protected DownloadListener downloadListener;

    protected final Map<String, List<String>> headers = new HashMap<>();

    protected boolean upToDate;

    protected boolean expectNotModified() {
        return useETag || onlyIfModified;
    }

    protected void validateCode(int code, String reasonPhrase) throws HttpResponseException {
        if ((code < 200 || code > 299) && (!expectNotModified() || code != HTTP_NOT_MODIFIED)) {
            throw new HttpResponseException(code, reasonPhrase);
        }
    }

    protected boolean calcUpToDate(int code, long lastModifiedDisk, @Nullable Date lastModifiedHeader) {
        boolean timestampNotModified = onlyIfModified && lastModifiedHeader != null && lastModifiedDisk >= lastModifiedHeader.getTime();
        boolean notModified = expectNotModified() && code == HTTP_NOT_MODIFIED;
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
            return true;
        }
        return false;
    }

    @Override
    public AbstractDownloadAction setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public AbstractDownloadAction setDest(Dest dest) {
        this.dest = dest;
        return this;
    }

    @Override
    public AbstractDownloadAction setDest(StringWriter sw) {
        return setDest(Dest.string(sw));
    }

    @Override
    public AbstractDownloadAction setDest(OutputStream os) {
        return setDest(Dest.stream(os));
    }

    @Override
    public AbstractDownloadAction setDest(File file) {
        return setDest(Dest.file(file));
    }

    @Override
    public AbstractDownloadAction setDest(Path path) {
        return setDest(Dest.path(path));
    }

    @Override
    public AbstractDownloadAction setOnlyIfModified(boolean onlyIfModified) {
        this.onlyIfModified = onlyIfModified;
        return this;
    }

    @Override
    public AbstractDownloadAction setUseETag(boolean useETag) {
        this.useETag = useETag;
        return this;
    }

    @Override
    public AbstractDownloadAction setQuiet(boolean quiet) {
        this.quiet = quiet;
        return this;
    }

    @Override
    public AbstractDownloadAction setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    @Override
    public DownloadAction addRequestHeader(String key, String value) {
        headers.computeIfAbsent(key, k -> new LinkedList<>()).add(value);
        return this;
    }

    @Override
    public AbstractDownloadAction setDownloadListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
        return this;
    }

    @Override
    public boolean isUpToDate() {
        return upToDate;
    }

    @Nullable
    protected static Date parseDate(@Nullable String str) {
        if (str == null) return null;
        if (str.length() > 1 && str.startsWith("'") && str.endsWith("'")) {
            str = str.substring(1, str.length() - 1);
        }
        for (SimpleDateFormat pattern : PATTERNS) {
            pattern.set2DigitYearStart(TWO_DIGIT_YEAR_START);
            ParsePosition parsePosition = new ParsePosition(0);
            Date date = pattern.parse(str, parsePosition);
            if (parsePosition.getIndex() != 0) {
                return date;
            }
        }
        return null;
    }

    //@formatter:off
    @Override @Nullable public String getUrl() { return url; }
    @Override @Nullable public Dest getDest() { return dest; }
    @Override public boolean getOnlyIfModified() { return onlyIfModified; }
    @Override public boolean getUseETag() { return useETag; }
    @Override public boolean getQuiet() { return quiet; }
    @Override @Nullable public String getUserAgent() { return userAgent; }
    @Override @Nullable public DownloadListener getDownloadListener() { return downloadListener; }
    //@formatter:on

    private static SimpleDateFormat parseFormat(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format;
    }
}
