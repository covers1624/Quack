/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net;

import java.io.IOException;

/**
 * Created by covers1624 on 22/11/21.
 */
public class HttpResponseException extends IOException {

    public final int code;
    public final String reasonPhrase;

    public HttpResponseException(int code, String reasonPhrase) {
        super("status code: " + code + " , reason phrase: " + reasonPhrase);
        this.code = code;
        this.reasonPhrase = reasonPhrase;
    }
}
