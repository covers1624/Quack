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

package net.covers1624.quack.net.okhttp;

import net.covers1624.quack.annotation.Requires;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

import java.util.function.Function;

/**
 * A ResponseBody capable of wrapping the parent's {@link Source}.
 * <p>
 * Created by covers1624 on 15/2/21.
 */
@Requires ("com.squareup.okhttp3:okhttp")
public abstract class SniffingResponseBody extends ResponseBody {

    private final ResponseBody parent;
    private BufferedSource source;

    public SniffingResponseBody(ResponseBody parent) {
        this.parent = parent;
    }

    /**
     * Creates a new {@link SniffingResponseBody} from the given body, with the {@link Source}
     * wrapped using the given Function.
     *
     * @param body The body to wrap.
     * @param func The Function to wrap the {@link Source}.
     * @return The wrapped {@link ResponseBody}.
     */
    public static ResponseBody ofFunction(ResponseBody body, Function<Source, Source> func) {
        return new SniffingResponseBody(body) {
            @Override
            protected Source wrapSource(Source other) {
                return func.apply(other);
            }
        };
    }

    @Override
    public long contentLength() {
        return parent.contentLength();
    }

    @Override
    public MediaType contentType() {
        return parent.contentType();
    }

    @Override
    public BufferedSource source() {
        if (source == null) {
            source = Okio.buffer(wrapSource(parent.source()));
        }
        return source;
    }

    /**
     * Wrap the given {@link Source} into another {@link Source}.
     * This is usually performed with something such as {@link ForwardingSource}
     *
     * @param other The Source to wrap.
     * @return The wrapped source.
     */
    protected abstract Source wrapSource(Source other);
}
