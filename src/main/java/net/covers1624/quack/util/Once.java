package net.covers1624.quack.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple single use latch.
 * Example:
 * <pre>
 *     StringBuilder b = new StringBuilder();
 *     Once once = new Once();
 *     for (String s : strings) {
 *         if (once.once()) {
 *             b.append(", ");
 *         }
 *         b.append(s);
 *     }
 * </pre>
 * <p>
 * Created by covers1624 on 5/9/21.
 */
public class Once {

    private final AtomicBoolean bool = new AtomicBoolean();

    public boolean once() {
        return bool.getAndSet(true);
    }

    public void reset() {
        bool.getAndSet(false);
    }
}
