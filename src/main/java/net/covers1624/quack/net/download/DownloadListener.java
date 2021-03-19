/*
 * MIT License
 *
 * Copyright (c) 2018-2021 covers1624
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
