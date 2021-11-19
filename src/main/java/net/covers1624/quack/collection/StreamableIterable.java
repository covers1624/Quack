/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.collection;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.covers1624.quack.annotation.Requires;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static net.covers1624.quack.util.SneakyUtils.first;

/**
 * An iterable capable of stream-like operations.
 * <p>
 * Whilst the {@link Stream} API is nice for some operations. There
 * are some use cases where a simpler wrapped Iterable approach would
 * be faster and more memory efficient, that's what this class aims to solve.
 * <p>
 * Created by covers1624 on 1/10/21.
 */
@Requires ("com.google.guava:guava")
public interface StreamableIterable<T> extends Iterable<T> {

    static <T> StreamableIterable<T> empty() {
        return of(Collections.emptyList());
    }

    static <T> StreamableIterable<T> of(Iterable<T> itr) {
        return itr::iterator;
    }

    /**
     * Filter the elements in this {@link StreamableIterable} by matching a {@link Predicate}.
     * <p>
     * All elements which match this Predicate will be in the resulting {@link StreamableIterable}.
     *
     * @param pred The predicate.
     * @return A wrapped {@link StreamableIterable} with the filter applied.
     */
    default StreamableIterable<T> filter(Predicate<? super T> pred) {

        return () -> new AbstractIterator<T>() {
            private final Iterator<T> itr = iterator();

            @Override
            protected T computeNext() {
                while (itr.hasNext()) {
                    T e = itr.next();
                    if (pred.test(e)) {
                        return e;
                    }
                }
                return endOfData();
            }
        };
    }

    /**
     * Filter the elements in this {@link StreamableIterable} by matching a {@link Predicate}.
     * <p>
     * All elements which do not match this Predicate will be in the resulting {@link StreamableIterable}.
     *
     * @param pred The predicate.
     * @return A wrapped {@link StreamableIterable} with the filter applied.
     */
    default StreamableIterable<T> filterNot(Predicate<? super T> pred) {
        return filter(pred.negate());
    }

    /**
     * Transform the elements in this {@link StreamableIterable}.
     *
     * @param func The {@link Function} to transform with.
     * @return A wrapped {@link StreamableIterable} with the mapped elements.
     */
    default <R> StreamableIterable<R> map(Function<? super T, ? extends R> func) {

        return () -> new Iterator<R>() {
            private final Iterator<T> itr = iterator();

            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public R next() {
                return func.apply(itr.next());
            }

            @Override
            public void remove() {
                itr.remove();
            }
        };
    }

    /**
     * Flat map the elements in this {@link StreamableIterable}.
     *
     * @param func The function to get/create the iterable for a given element.
     * @return A wrapped {@link StreamableIterable} with the flat mapped elements.
     */
    default <R> StreamableIterable<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> func) {

        return () -> new AbstractIterator<R>() {
            private final Iterator<T> itr = iterator();
            @Nullable
            Iterator<? extends R> working = null;

            @Override
            protected R computeNext() {
                while (true) {
                    if (working == null) {
                        if (itr.hasNext()) {
                            working = func.apply(itr.next()).iterator();
                        } else {
                            break;
                        }
                    }
                    if (working.hasNext()) {
                        return working.next();
                    }
                    working = null;
                }
                return endOfData();
            }
        };
    }

    /**
     * Filter the elements in this {@link StreamableIterable} by their hashcode/equals.
     *
     * @return A wrapped {@link StreamableIterable} with the filtered elements.
     */
    default StreamableIterable<T> distinct() {
        Set<T> set = new HashSet<>();
        return filter(set::add);
    }

    /**
     * Peek the elements of this {@link StreamableIterable} as they are consumed.
     *
     * @param action The consumer.
     * @return A wrapped {@link StreamableIterable} with the peek function applied.
     */
    default StreamableIterable<T> peek(Consumer<T> action) {

        return () -> new Iterator<T>() {
            private final Iterator<T> itr = iterator();

            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public T next() {
                T n = itr.next();
                action.accept(n);
                return n;
            }
        };
    }

    /**
     * Limit the number of elements returned by this {@link StreamableIterable} to the given value.
     *
     * @param max The limit. -1 for infinite.
     * @return A wrapped {@link StreamableIterable} with the max filter applied.
     * In the event <code>-1<</code> is supplied, the same {@link StreamableIterable} will be provided.
     * In the event <code>0</code> is supplied, an empty {@link StreamableIterable} will be provided.
     */
    default StreamableIterable<T> limit(@Range (from = -1, to = Integer.MAX_VALUE) int max) {
        // No point doing filtering.
        if (max == -1) return this;
        if (max == 0) return Collections::emptyIterator;

        return () -> new AbstractIterator<T>() {
            private final Iterator<T> itr = iterator();
            private int count = 0;

            @Override
            protected T computeNext() {
                if (!itr.hasNext()) return endOfData();
                if (count++ >= max) return endOfData();
                return itr.next();
            }
        };
    }

    /**
     * Skip n number of elements in this {@link StreamableIterable}.
     *
     * @param n The number of elements to skip.
     * @return A wrapped {@link StreamableIterable} with the max filter applied.
     * In the event <code>0</code> is supplied, the same {@link StreamableIterable} will be provided.
     */
    default StreamableIterable<T> skip(@Range (from = 0, to = Integer.MAX_VALUE) int n) {
        if (n == 0) return this;


        return () -> new AbstractIterator<T>() {
            private final Iterator<T> itr = iterator();
            private int count = 0;

            @Override
            protected T computeNext() {
                while (itr.hasNext()) {
                    T next = itr.next();
                    if (count++ < n) {
                        continue;
                    }
                    return next;
                }
                return endOfData();
            }
        };
    }

    /**
     * Collect this {@link StreamableIterable} to an array.
     *
     * @return The array.
     */
    default Object[] toArray() {
        return toList().toArray();
    }

    /**
     * Collect this {@link StreamableIterable} to an array.
     *
     * @param arr The array to add the elements to.
     *            If all elements to not fit in this array a new array
     *            will be returned of appropriate size.
     * @return Either the array passed in or one of appropriate size
     * with all elements of the Iterable.
     */
    default T[] toArray(T[] arr) {
        return toList().toArray(arr);
    }

    /**
     * Folds each element of this {@link StreamableIterable} into the previous.
     *
     * @param identity    The starting element.
     * @param accumulator The function to fold elements with.
     * @return The result.
     */
    @Nullable
    @Contract ("null,_->null")
    default T fold(@Nullable T identity, BinaryOperator<@Nullable T> accumulator) {
        T ret = identity;
        for (T t : this) {
            ret = accumulator.apply(ret, t);
        }
        return ret;
    }

    /**
     * Folds each element of this {@link StreamableIterable} into the previous.
     *
     * @param accumulator The function to fold elements with.
     * @return The result. Empty if no elements exist.
     */
    default Optional<T> fold(BinaryOperator<T> accumulator) {
        boolean found = false;
        T ret = null;
        for (T t : this) {
            if (found) {
                ret = accumulator.apply(ret, t);
            } else {
                ret = t;
            }
            found = true;
        }
        return found ? Optional.ofNullable(ret) : Optional.empty();
    }

    /**
     * Count the number of elements in this {@link StreamableIterable}.
     *
     * @return The number of elements.
     */
    default int count() {
        int i = 0;
        for (T ignored : this) {
            i++;
        }
        return i;
    }

    /**
     * Check if any elements in this {@link StreamableIterable} match the given predicate.
     *
     * @param test The predicate.
     * @return The result.
     */
    default boolean anyMatch(Predicate<? super T> test) {
        for (T t : this) {
            if (test.test(t)) return true;
        }
        return false;
    }

    /**
     * Check if all elements in this {@link StreamableIterable} match the given predicate.
     *
     * @param test The predicate.
     * @return The result.
     */
    default boolean allMatch(Predicate<? super T> test) {
        for (T t : this) {
            if (!test.test(t)) return false;
        }
        return true;
    }

    /**
     * Check if no elements in this {@link StreamableIterable} match the given predicate.
     *
     * @param test The predicate.
     * @return The result.
     */
    default boolean noneMatch(Predicate<? super T> test) {
        for (T t : this) {
            if (test.test(t)) return false;
        }
        return true;
    }

    /**
     * Optionally get the first element within this {@link StreamableIterable}.
     *
     * @return The last element.
     */
    default Optional<T> findFirst() {
        return ColUtils.headOption(this);
    }

    /**
     * Optionally get the first element within this {@link StreamableIterable}.
     *
     * @return The last element.
     */
    default Optional<T> findLast() {
        return ColUtils.tailOption(this);
    }

    /**
     * Collect this {@link StreamableIterable} to an {@link ArrayList}.
     *
     * @return The {@link ArrayList}.
     */
    default ArrayList<T> toList() {
        return Lists.newArrayList(this);
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link LinkedList}.
     *
     * @return The {@link LinkedList}.
     */
    default LinkedList<T> toLinkedList() {
        return Lists.newLinkedList(this);
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link HashSet}.
     *
     * @return The {@link HashSet}.
     */
    default HashSet<T> toSet() {
        return Sets.newHashSet(this);
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link HashMap}.
     * This method will always resolve the existing element on collision.
     *
     * @param keyFunc   The function for extracting the key.
     * @param valueFunc The function for extracting the value.
     * @return The {@link Map}.
     */
    default <K, V> HashMap<K, V> toMap(Function<T, K> keyFunc, Function<T, V> valueFunc) {
        return toMap(new HashMap<>(), keyFunc, valueFunc);
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link HashMap}.
     *
     * @param keyFunc   The function for extracting the key.
     * @param valueFunc The function for extracting the value.
     * @param mergeFunc The function for merging 2 values on collision. (Left existing, Right toAdd)
     * @return The {@link Map}.
     */
    default <K, V> HashMap<K, V> toMap(Function<T, K> keyFunc, Function<T, V> valueFunc, BinaryOperator<V> mergeFunc) {
        return toMap(new HashMap<>(), keyFunc, valueFunc, mergeFunc);
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link LinkedHashMap}.
     * This method will always resolve the existing element on collision.
     *
     * @param keyFunc   The function for extracting the key.
     * @param valueFunc The function for extracting the value.
     * @return The {@link Map}.
     */
    default <K, V> HashMap<K, V> toLinkedHashMap(Function<T, K> keyFunc, Function<T, V> valueFunc) {
        return toMap(new LinkedHashMap<>(), keyFunc, valueFunc);
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link LinkedHashMap}.
     *
     * @param keyFunc   The function for extracting the key.
     * @param valueFunc The function for extracting the value.
     * @param mergeFunc The function for merging 2 values on collision. (Left existing, Right toAdd)
     * @return The {@link Map}.
     */
    default <K, V> HashMap<K, V> toLinkedHashMap(Function<T, K> keyFunc, Function<T, V> valueFunc, BinaryOperator<V> mergeFunc) {
        return toMap(new LinkedHashMap<>(), keyFunc, valueFunc, mergeFunc);
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link Map}.
     * This method will always resolve the existing element on collision.
     *
     * @param map       The map to add the elements to.
     * @param keyFunc   The function for extracting the key.
     * @param valueFunc The function for extracting the value.
     * @return The same map that was passed in.
     */
    default <K, V, M extends Map<K, V>> M toMap(M map, Function<T, K> keyFunc, Function<T, V> valueFunc) {
        return toMap(map, keyFunc, valueFunc, first());
    }

    /**
     * Collect this {@link StreamableIterable} to a {@link Map}.
     *
     * @param map       The map to add the elements to.
     * @param keyFunc   The function for extracting the key.
     * @param valueFunc The function for extracting the value.
     * @param mergeFunc The function for merging 2 values on collision. (Left existing, Right toAdd)
     * @return The same map that was passed in.
     */
    default <K, V, M extends Map<K, V>> M toMap(M map, Function<T, K> keyFunc, Function<T, V> valueFunc, BinaryOperator<V> mergeFunc) {
        for (T t : this) {
            K key = keyFunc.apply(t);
            V value = valueFunc.apply(t);
            V existing = map.get(key);
            if (existing == null) {
                map.put(key, valueFunc.apply(t));
            } else {
                map.put(key, mergeFunc.apply(existing, value));
            }
        }
        return map;
    }

    /**
     * Convert this {@link StreamableIterable} to a {@link Stream}.
     *
     * @return The {@link Stream}
     */
    default Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Convert this {@link StreamableIterable} to a parallel {@link Stream}.
     *
     * @return The {@link Stream}
     */
    default Stream<T> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }
}
