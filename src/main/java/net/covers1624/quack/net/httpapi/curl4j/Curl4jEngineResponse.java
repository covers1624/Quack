/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.curl4j;

import net.covers1624.quack.annotation.ReplaceWith;
import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.httpapi.EngineResponse;
import net.covers1624.quack.net.httpapi.HeaderList;
import net.covers1624.quack.net.httpapi.WebBody;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Created by covers1624 on 1/11/23.
 */
@Deprecated // Moved to curl4j.
@ReplaceWith ("net.covers1624.curl4j.httpapi")
@ApiStatus.ScheduledForRemoval (inVersion = "0.5.0")
@Requires ("net.covers1624:curl4j")
public abstract class Curl4jEngineResponse implements EngineResponse {

    @Override
    public abstract Curl4jEngineRequest request();
}
