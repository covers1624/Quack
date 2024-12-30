/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.curl4j;

import net.covers1624.quack.annotation.ReplaceWith;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

/**
 * Created by covers1624 on 10/1/24.
 */
@Deprecated // Moved to curl4j.
@ReplaceWith ("net.covers1624.curl4j.httpapi")
@ApiStatus.ScheduledForRemoval (inVersion = "0.5.0")
public class Curl4jHttpException extends IOException {

    Curl4jHttpException(String message) {
        super(message);
    }

    Curl4jHttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
