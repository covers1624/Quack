/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi.curl4j;

import net.covers1624.curl4j.CURL;
import net.covers1624.curl4j.util.CurlHandle;
import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.httpapi.HttpEngine;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 1/11/23.
 */
@Requires ("net.covers1624:curl4j")
public class Curl4jHttpEngine implements HttpEngine {

    private static boolean CURL_GLOBAL_INIT = false;

    private final ThreadLocal<CurlHandle> CURL_HANDLES = CurlHandle.newThreadLocal();

    public final @Nullable String impersonate;

    public Curl4jHttpEngine() {
        this(null);
    }

    public Curl4jHttpEngine(@Nullable String impersonate) {
        this.impersonate = impersonate;
        if (!CURL.isCurlImpersonateSupported() && impersonate != null) {
            throw new IllegalArgumentException("Current CURL instance does not support impersonation.");
        }

        synchronized (Curl4jHttpEngine.class) {
            if (!CURL_GLOBAL_INIT) {
                CURL.curl_global_init(CURL.CURL_GLOBAL_DEFAULT);
                CURL_GLOBAL_INIT = true;
            }
        }
    }

    @Override
    public Curl4jEngineRequest newRequest() {
        return new Curl4jEngineRequest(this);
    }

    @Nullable String getImpersonate() {
        return impersonate;
    }

    CurlHandle getHandle() {
        return CURL_HANDLES.get();
    }
}
