/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi;

/**
 * Represents a request listener.
 * <p>
 * This monitors both Request and Response bodies.
 * <p>
 * Created by covers1624 on 29/1/24.
 */
public interface RequestListener {

    /**
     * The primary direction of this request.
     * <p>
     * At the moment, this is UPLOAD if the request has a body,
     * otherwise DOWNLOAD.
     *
     * @param type The request direction.
     */
    void start(Direction type);

    /**
     * Called with progress data on the request body upload.
     *
     * @param total The total expected. May be -1 if the total is unknown.
     * @param now   The current value.
     */
    void onUpload(long total, long now);

    /**
     * Called with progress data on the response body download.
     *
     * @param total The total expected. May be -1 if the total is unknown.
     * @param now   The current value.
     */
    void onDownload(long total, long now);

    /**
     * Called when the transfer is complete.
     */
    void end();

    enum Direction {
        UPLOAD,
        DOWNLOAD
    }
}
