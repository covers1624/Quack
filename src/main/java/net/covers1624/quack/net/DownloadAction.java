/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net;

import net.covers1624.quack.io.IOUtils;
import net.covers1624.quack.io.WriterOutputStream;
import net.covers1624.quack.net.apache.ApacheHttpClientDownloadAction;
import net.covers1624.quack.net.download.DownloadListener;
import net.covers1624.quack.net.java.JavaDownloadAction;
import net.covers1624.quack.net.okhttp.OkHttpDownloadAction;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;

/**
 * A simple action to download a single file.
 * <p>
 * Download Actions support ETag and OnlyIfModified Http headers. As well as downloading
 * the file to an in-memory String/byte array.
 * <p>
 * Several implementations of this exist:
 * Java HttpURLConnection: {@link JavaDownloadAction}.
 * OkHttp: {@link OkHttpDownloadAction}.
 * Apache HttpClient: {@link ApacheHttpClientDownloadAction}.
 * <p>
 * Created by covers1624 on 22/11/21.
 */
public interface DownloadAction {

    /**
     * Execute the download action.
     *
     * @throws IOException           If an IO error occurs whilst downloading.
     * @throws HttpResponseException If the response code was not expected.
     */
    void execute() throws IOException;

    /**
     * Set the URL to download from.
     *
     * @param url The URL.
     * @return The same download action.
     */
    DownloadAction setUrl(String url);

    /**
     * Set the Destination to download content to.
     *
     * @param dest The Destination.
     * @return The same download action.
     */
    DownloadAction setDest(Dest dest);

    /**
     * Set the {@link StringWriter} to download content to.
     *
     * @param sw The {@link StringWriter}.
     * @return The same download action.
     */
    DownloadAction setDest(StringWriter sw);

    /**
     * Set the {@link OutputStream} to download content to.
     *
     * @param os The {@link OutputStream}.
     * @return The same download action.
     */
    DownloadAction setDest(OutputStream os);

    /**
     * Set the {@link File} to download content to.
     *
     * @param file The file.
     * @return The same download action.
     */
    DownloadAction setDest(File file);

    /**
     * Set the {@link Path} to download content to.
     *
     * @param path The path.
     * @return The same download action.
     */
    DownloadAction setDest(Path path);

    /**
     * If this Download action should use <code>If-Modified-Since</code>
     * HTTP request header.
     *
     * @param onlyIfModified If <code>If-Modified-Since</code> should be used.
     * @return The same download action.
     */
    DownloadAction setOnlyIfModified(boolean onlyIfModified);

    /**
     * If this download action should use <code>If-None-Match</code>
     * HTTP request header.
     *
     * @param useETag If <code>If-None-Match</code> should be used.
     * @return The same download action.
     */
    DownloadAction setUseETag(boolean useETag);

    /**
     * If this download action should not log things.
     *
     * @param quiet If the download action should be quiet.
     * @return The same download action.
     */
    DownloadAction setQuiet(boolean quiet);

    /**
     * Sets the <code>User-Agent</code> HTTP request header.
     *
     * @param userAgent The <code>User-Agent</code> request header.
     * @return The same download action.
     */
    DownloadAction setUserAgent(String userAgent);

    /**
     * Adds an arbitrary Http header to the action.
     *
     * @param key   The key.
     * @param value The value.
     * @return The same download action.
     */
    DownloadAction addRequestHeader(String key, String value);

    /**
     * Set the {@link DownloadListener} to use.
     *
     * @param downloadListener The {@link DownloadListener}.
     * @return The same download action.
     */
    DownloadAction setDownloadListener(DownloadListener downloadListener);

    @Nullable
    String getUrl();

    @Nullable
    Dest getDest();

    boolean getOnlyIfModified();

    boolean getUseETag();

    boolean getQuiet();

    @Nullable
    String getUserAgent();

    @Nullable
    DownloadListener getDownloadListener();

    boolean isUpToDate();

    /**
     * Interface for consuming the output of an HTTP response.
     */
    interface Dest {

        /**
         * Get the Okio {@link OutputStream} to push the data to.
         *
         * @return The OutputStream.
         * @throws IOException If an error occurs opening the output.
         */
        OutputStream getOutputStream() throws IOException;

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
            return stream(new WriterOutputStream(sw, charset));
        }

        /**
         * Returns a {@link Dest}which writes its content to a {@link OutputStream}.
         * <p>
         * The returned {@link Dest} object does not support ETag or LastModified.
         *
         * @param os The {@link OutputStream}.
         * @return The {@link Dest}.
         */
        static Dest stream(OutputStream os) {
            return new Dest() {
                @Override
                public OutputStream getOutputStream() {
                    return os;
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
                public OutputStream getOutputStream() throws IOException {
                    return Files.newOutputStream(IOUtils.makeParents(tempFile));
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
