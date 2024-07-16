/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi;

import net.covers1624.quack.net.DownloadAction;
import net.covers1624.quack.net.HttpEngineDownloadAction;

/**
 * Represents an abstract interface for making web requests.
 * <p>
 * {@link HttpEngine} may not support all features provided by the underlying
 * HTTP implementation. It makes no attempt to provide raw access to the underlying engine.
 * <p>
 * A specific {@link HttpEngine} may not support all features of this API. Such differences
 * should be noted by implementation in its documentation. Some features of the API may only
 * be available with additional dependencies, these should also be noted by the implementation
 * documentation.
 * <p>
 * Created by covers1624 on 1/8/23.
 */
public interface HttpEngine {

    /**
     * Creates a new {@link EngineRequest} builder.
     *
     * @return The new {@link EngineRequest} builder.
     */
    EngineRequest newRequest();

    /**
     * Creates a new {@link DownloadAction} backed by
     * this {@link HttpEngine}.
     *
     * @return The new {@link HttpEngineDownloadAction} instance.
     */
    default HttpEngineDownloadAction newDownloadAction() {
        return new HttpEngineDownloadAction(this);
    }
}
