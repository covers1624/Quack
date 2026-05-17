/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.io;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * A simple OutputStream treating all input as Text,
 * delegating each line of text to the provided consumer.
 * <p>
 * This consumer does not support Mac OS classic CR line endings.
 * <p>
 * Created by covers1624 on 1/4/21.
 */
public class ConsumingOutputStream extends WriterOutputStream {

    public ConsumingOutputStream(Consumer<String> consumer) {
        this(StandardCharsets.UTF_8, consumer);
    }

    public ConsumingOutputStream(Charset charset, Consumer<String> consumer) {
        super(new LineConsumingWriter(consumer), charset);
    }
}
