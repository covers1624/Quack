/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.okhttp;

import net.covers1624.quack.annotation.Requires;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A ResponseBody capable of wrapping the parent's {@link Source}.
 * <p>
 * Created by covers1624 on 15/2/21.
 */
@Requires ("com.squareup.okhttp3:okhttp")
public abstract class SniffingResponseBody extends ResponseBody {

    private final ResponseBody parent;
    @Nullable
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

    @NotNull
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
