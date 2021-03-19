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

package net.covers1624.quack.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Contains some utilities for ignoring compiler warnings in specific cases, or
 * completely ignoring exceptions, Plus other random lambda based utilities.
 * <p>
 * Created by covers1624 on 13/1/21.
 */
public class SneakyUtils {

    private static final Runnable NULL_RUNNABLE = () -> {};
    private static final Callable<Object> NULL_CALLABLE = () -> null;
    private static final Supplier<Object> NULL_SUPPLIER = () -> null;
    private static final Consumer<Object> NULL_CONSUMER = e -> {};
    private static final Predicate<Object> TRUE = e -> true;
    private static final Predicate<Object> FALSE = e -> true;

    /**
     * Returns a Runnable that does nothing.
     *
     * @return The runnable.
     */
    public static Runnable none() {
        return NULL_RUNNABLE;
    }

    /**
     * Returns a Callable that always returns null when executed.
     *
     * @return The callable.
     */
    public static <T> Callable<T> nullC() {
        return unsafeCast(NULL_CALLABLE);
    }

    /**
     * Returns a Supplier that always returns null when executed.
     *
     * @return The callable.
     */
    public static <T> Supplier<T> nullS() {
        return unsafeCast(NULL_SUPPLIER);
    }

    /**
     * Returns a Consumer that does nothing when executed.
     *
     * @return The consumer.
     */
    public static <T> Consumer<T> nullCons() {
        return unsafeCast(NULL_CONSUMER);
    }

    /**
     * Returns a Predicate that always passes.
     *
     * @return The Predicate.
     */
    public static <T> Predicate<T> trueP() {
        return unsafeCast(TRUE);
    }

    /**
     * Returns a Predicate that always fails.
     *
     * @return The Predicate.
     */
    public static <T> Predicate<T> falseP() {
        return unsafeCast(FALSE);
    }

    /**
     * Concatenates two {@link Runnable}s.
     *
     * @param a The First {@link Runnable} to execute.
     * @param b The Second {@link Runnable} to execute.
     * @return The Concatenated {@link Runnable}.
     */
    public static Runnable concat(Runnable a, Runnable b) {
        return () -> {
            a.run();
            b.run();
        };
    }

    /**
     * Executes the given ThrowingRunnable, rethrowing any exceptions
     * produced by the runnable as Unchecked(as seen by the compiler.)
     *
     * @param tr The ThrowingRunnable.
     */
    public static void sneaky(ThrowingRunnable<Throwable> tr) {
        try {
            tr.run();
        } catch (Throwable ex) {
            throwUnchecked(ex);
        }
    }

    /**
     * Wraps a ThrowingRunnable to a {@link Runnable}.
     * See {@link #sneaky(ThrowingRunnable)} for info.
     *
     * @param tr The ThrowingRunnable to wrap.
     * @return The wrapped runnable.
     */
    public static Runnable sneak(ThrowingRunnable<Throwable> tr) {
        return () -> sneaky(tr);
    }

    /**
     * Wraps a ThrowingConsumer to a {@link Consumer}
     * Rethrowing any exceptions produced by the ThrowingConsumer
     * as unchecked(as seen by the compiler.)
     *
     * @param cons The ThrowingConsumer to wrap.
     * @return The wrapped Consumer.
     */
    public static <T> Consumer<T> sneak(ThrowingConsumer<T, Throwable> cons) {
        return e -> {
            try {
                cons.accept(e);
            } catch (Throwable ex) {
                throwUnchecked(ex);
            }
        };
    }

    /**
     * Executes the given ThrowingRunnable, rethrowing any exceptions
     * produced by the runnable as Unchecked(as seen by the compiler.)
     *
     * @param sup The ThrowingSupplier.
     * @return The return result of the ThrowingSupplier.
     */
    public static <T> T sneaky(ThrowingSupplier<T, Throwable> sup) {
        try {
            return sup.get();
        } catch (Throwable ex) {
            throwUnchecked(ex);
            return null;//Impossible, go away compiler!
        }
    }

    /**
     * Wraps a ThrowingSupplier to a {@link Supplier}
     * Rethrowing any exceptions produced by the ThrowingSupplier
     * as unchecked(as seen by the compiler.)
     *
     * @param sup The ThrowingSupplier to wrap.
     * @return The wrapped Supplier.
     */
    public static <T> Supplier<T> sneak(ThrowingSupplier<T, Throwable> sup) {
        return () -> sneaky(sup);
    }

    /**
     * Wraps a ThrowingFunction to a {@link Function}
     * Rethrowing any exceptions produced by the ThrowingFunction
     * as unchecked(as seen by the compiler.)
     *
     * @param tf The ThrowingFunction to wrap.
     * @return The wrapped Function.
     */
    public static <T, R> Function<T, R> sneak(ThrowingFunction<T, R, Throwable> tf) {
        return e -> {
            try {
                return tf.apply(e);
            } catch (Throwable ex) {
                throwUnchecked(ex);
                return null;//Impossible, go away compiler!
            }
        };
    }

    @SuppressWarnings ("unchecked")
    public static <T> T unsafeCast(Object object) {
        return (T) object;
    }

    /**
     * Throws an exception without compiler warnings.
     */
    public static <T extends Throwable> void throwUnchecked(Throwable t) throws T {
        throw SneakyUtils.<T>unsafeCast(t);
    }

    public interface ThrowingRunnable<E extends Throwable> {

        void run() throws E;
    }

    public interface ThrowingConsumer<T, E extends Throwable> {

        void accept(T thing) throws E;
    }

    public interface ThrowingSupplier<T, E extends Throwable> {

        T get() throws E;
    }

    public interface ThrowingFunction<T, R, E extends Throwable> {

        R apply(T thing) throws E;
    }
}
