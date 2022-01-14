/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.download;

/**
 * Created by covers1624 on 13/1/21.
 */
public interface DownloadListener {

    /**
     * Indicates that the download is attempting to connect.
     */
    void connecting();

    /**
     * The expected length for the download.
     * -1 to indicate that there is no expected length.
     *
     * @param expectedLen The expected length or -1.
     */
    void start(long expectedLen);

    /**
     * Updates the listener on how many bytes have been processed.
     *
     * @param processedBytes The progress.
     */
    void update(long processedBytes);

    /**
     * Marks the download as finished.
     *
     * @param totalProcessed The total number of bytes downloaded.
     */
    void finish(long totalProcessed);
}
