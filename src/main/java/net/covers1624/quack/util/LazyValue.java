/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.util;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A simple Holder object to lazily compute some value.
 * <p>
 * This class is synchronized, meaning multi-thread access causing the value to
 * be resolved is properly handled.
 * <p>
 * This class is also explicitly NonNull.
 * <p>
 * Created by covers1624 on 15/4/21.
 */
public class LazyValue<T> implements Supplier<T> {

    private final Supplier<T> factory;
    @Nullable
    private T value;

    public LazyValue(Supplier<T> factory) {
        this.factory = factory;
    }

    /**
     * Get or compute the value if it doesn't exist.
     *
     * @return The value.
     */
    @Override
    public T get() {
        if (value != null) return value; //Already set
        synchronized (this) { //Lock whilst we compute
            if (value != null) return value; //Don't re-compute if already set.

            value = Objects.requireNonNull(factory.get());
        }
        return Objects.requireNonNull(value);
    }

    /**
     * If the value is present or not.
     *
     * @return If the value is present.
     */
    public boolean hasValue() {
        return value != null;
    }

}
