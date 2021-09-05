/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.collection;

import net.covers1624.quack.annotation.ReplaceWith;
import net.covers1624.quack.annotation.ReplaceWithExpr;
import net.covers1624.quack.util.Copyable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;
import static org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

/**
 * Various Collection Utilities for Iterables and Arrays.
 * <p>
 * Created by covers1624 on 5/08/18.
 */
public class ColUtils {

    //region Deprecated
    @Deprecated
    @ReplaceWith ("String#join()")
    @ScheduledForRemoval (inVersion = "0.4.0")
    public static String toString(Iterable<?> col) {
        return mkString(col, "[ ", ", ", " ]", ColUtils::toString_);
    }

    @Deprecated
    @ReplaceWith ("String#join()")
    @ScheduledForRemoval (inVersion = "0.4.0")
    public static String toString(Object[] col) {
        return mkString(col, "[ ", ", ", " ]", ColUtils::toString_);
    }

    @Deprecated
    @ScheduledForRemoval (inVersion = "0.4.0")
    private static String toString_(Object obj) {
        if (obj instanceof Iterable) {
            return toString((Iterable<?>) obj);
        } else if (obj instanceof Object[]) {
            return toString((Object[]) obj);
        } else {
            return String.valueOf(obj);
        }
    }

    @Deprecated
    @ScheduledForRemoval (inVersion = "0.4.0")
    @ReplaceWithExpr ("java.util.Arrays.stream(col).collect(Collectors.joining())")
    public static String mkString(String[] array) {
        return mkString(array, "");
    }

    @Deprecated
    @ScheduledForRemoval (inVersion = "0.4.0")
    @ReplaceWithExpr ("java.util.Arrays.stream(col).collect(Collectors.joining(sep))")
    public static String mkString(String[] array, String sep) {
        return mkString(array, "", sep, "");
    }

    @Deprecated
    @ScheduledForRemoval (inVersion = "0.4.0")
    @ReplaceWithExpr ("java.util.Arrays.stream(col).collect(Collectors.joining(sep, start, end))")
    public static String mkString(String[] array, String start, String sep, String end) {
        return mkString(Arrays.asList(array), start, sep, end);
    }

    @Deprecated
    @ReplaceWith ("String#join()")
    @ScheduledForRemoval (inVersion = "0.4.0")
    public static String mkString(Iterable<String> col) {
        return mkString(col, "");
    }

    @Deprecated
    @ReplaceWith ("String#join()")
    @ScheduledForRemoval (inVersion = "0.4.0")
    @ReplaceWithExpr ("java.util.Arrays.stream(col).collect(Collectors.joining(sep, start, end))")
    public static String mkString(Iterable<String> col, String sep) {
        return mkString(col, "", sep, "");
    }

    @Deprecated
    @ReplaceWith ("String#join()")
    @ScheduledForRemoval (inVersion = "0.4.0")
    @ReplaceWithExpr ("ColUtils.stream(col).collect(Collectors.joining(sep, start, end))")
    public static String mkString(Iterable<String> col, String start, String sep, String end) {
        StringBuilder builder = new StringBuilder(start);
        boolean isFirst = true;
        for (String s : col) {
            if (!isFirst) {
                builder.append(sep);
            }
            isFirst = false;
            builder.append(s);
        }
        builder.append(end);
        return builder.toString();
    }

    @Deprecated
    @ReplaceWith ("String#join()")
    @ScheduledForRemoval (inVersion = "0.4.0")
    @ReplaceWithExpr ("java.util.Arrays.stream(col).map(func).collect(Collectors.joining(sep, start, end))")
    public static String mkString(Object[] col, String start, String sep, String end, Function<Object, String> func) {
        return mkString(Arrays.asList(col), start, sep, end, func);
    }

    @Deprecated
    @ScheduledForRemoval (inVersion = "0.4.0")
    @ReplaceWithExpr ("ColUtils.stream(col).map(func).collect(Collectors.joining(sep, start, end))")
    public static String mkString(Iterable<?> col, String start, String sep, String end, Function<Object, String> func) {
        StringBuilder builder = new StringBuilder(start);
        boolean isFirst = true;
        for (Object s : col) {
            if (!isFirst) {
                builder.append(sep);
            }
            isFirst = false;
            builder.append(func.apply(s));
        }
        builder.append(end);
        return builder.toString();
    }

    @Deprecated
    @ReplaceWith ("#iterable(Enumeration)")
    @ScheduledForRemoval (inVersion = "0.4.0")
    public static <E> Iterable<E> toIterable(Enumeration<E> enumeration) {
        return iterable(enumeration);
    }

    @Deprecated
    @ReplaceWith ("#allMatch(T[], Predicate)")
    public static <T> boolean forAll(T[] col, Predicate<T> func) {
        return allMatch(col, func);
    }

    @Deprecated
    @ScheduledForRemoval (inVersion = "0.4.0")
    @ReplaceWith ("#allMatch(Iterable, Predicate)")
    public static <T> boolean forAll(Iterable<T> col, Predicate<T> func) {
        return allMatch(col, func);
    }

    @Deprecated
    @ScheduledForRemoval (inVersion = "0.4.0")
    @ReplaceWith ("#anyMatch(Iterable, Predicate)")
    public static <T> boolean exists(Iterable<T> col, Predicate<T> func) {
        return anyMatch(col, func);
    }

    @Deprecated
    @ScheduledForRemoval (inVersion = "0.4.0")
    @ReplaceWith ("#findFirst(Iterable, Predicate)")
    public static <T> Optional<T> find(Iterable<T> col, Predicate<T> func) {
        return findFirst(col, func);
    }

    @Deprecated
    @ScheduledForRemoval (inVersion = "0.4.0")
    @ReplaceWith ("#headOption(Iterable, Predicate)")
    public static <T> T head(Iterable<T> col) {
        Iterator<T> itr = col.iterator();
        if (itr.hasNext()) {
            return itr.next();
        }
        throw new RuntimeException("Empty Iterable.");
    }
    //endregion

    /**
     * Slices the given array.
     *
     * @param arr   The Array to slice.
     * @param from  The from index.
     * @param until The until index.
     * @return The sliced array.
     */
    @SuppressWarnings ("unchecked")
    public static <T> T[] slice(T[] arr, int from, int until) {
        int low = Math.max(from, 0);
        int high = Math.min(Math.max(until, 0), arr.length);
        int size = Math.max(high - low, 0);
        T[] result = (T[]) Array.newInstance(arr.getClass().getComponentType(), size);
        if (size > 0) {
            System.arraycopy(arr, low, result, 0, size);
        }
        return result;
    }

    /**
     * Returns the element in the array with the highest value returned by the supplied function.
     *
     * @param col  The array.
     * @param func The function.
     * @return The max element or null.
     */
    @Nullable
    public static <T> T maxBy(T[] col, ToIntFunction<T> func) {
        return maxBy(Arrays.asList(col), func);
    }

    /**
     * Returns the element in the Iterable with the highest value returned by the supplied function.
     *
     * @param col  The array.
     * @param func The function.
     * @return The max element or null.
     */
    @Nullable
    public static <T> T maxBy(Iterable<T> col, ToIntFunction<T> func) {
        int max = Integer.MIN_VALUE;
        T maxT = null;
        for (T t : col) {
            int x = func.applyAsInt(t);
            if (x > max) {
                maxT = t;
                max = x;
            }
        }
        return maxT;
    }

    /**
     * Returns true if all elements in the provided array match the
     * given predicate.
     *
     * @param col  The collection to match.
     * @param func The function to apply.
     * @return If all elements match.
     */
    public static <T> boolean allMatch(T[] col, Predicate<T> func) {
        return allMatch(Arrays.asList(col), func);
    }

    /**
     * Returns true if all elements in the provided Iterable match the
     * given predicate.
     *
     * @param col  The collection to match.
     * @param func The function to apply.
     * @return If all elements match.
     */
    public static <T> boolean allMatch(Iterable<T> col, Predicate<T> func) {
        for (T t : col) {
            if (!func.test(t)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if any element in the provided Iterable matches the
     * given predicate.
     *
     * @param col  The collection to match.
     * @param func The function to apply.
     * @return If any elements match.
     */
    public static <T> boolean anyMatch(Iterable<T> col, Predicate<T> func) {
        for (T t : col) {
            if (func.test(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the first element in the Iterable that matches the given predicate.
     *
     * @param col  The collection.
     * @param func The predicate.
     * @return The Optional result.
     */
    public static <T> Optional<T> findFirst(Iterable<T> col, Predicate<T> func) {
        for (T t : col) {
            if (func.test(t)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    /**
     * Attempts to get the first element of the Iterable.
     *
     * @param col The collection.
     * @return The optional result.
     */
    public static <T> Optional<T> headOption(Iterable<T> col) {
        Iterator<T> itr = col.iterator();
        if (itr.hasNext()) {
            return Optional.of(itr.next());
        }
        return Optional.empty();
    }

    /**
     * Checks if a map contains all keys passed in.
     *
     * @param map  Map to check.
     * @param keys Keys that must exist.
     * @param <T>  The type of data in the map key.
     * @return False if fail.
     */
    @SafeVarargs
    public static <T> boolean containsKeys(Map<T, ?> map, T... keys) {
        for (T object : keys) {
            if (!map.containsKey(object)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds the value at the first null index in the array.
     *
     * @param array Array to add to.
     * @param value Value to add.
     * @param <T>   Type of value.
     * @return Returns a new array in the event the input was expanded.
     */
    public static <T> T[] addToArrayFirstNull(T[] array, T value) {
        int nullIndex = -1;
        for (int i = 0; i < array.length; i++) {
            T v = array[i];
            if (v == null) {
                nullIndex = i;
                break;
            }
        }
        if (nullIndex == -1) {
            T[] copy = createNewArray(array, array.length + 1);
            System.arraycopy(array, 0, copy, 0, array.length);
            nullIndex = array.length;
            array = copy;
        }
        array[nullIndex] = value;
        return array;
    }

    /**
     * Adds all elements from the array that are not null to the list.
     *
     * @param array Array to grab from.
     * @param list  List to add to.
     * @param <T>   What we are dealing with.
     * @return The modified list.
     */
    public static <T> List<T> addAllNoNull(T[] array, List<T> list) {
        for (T value : array) {
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    /**
     * Checks if the array is all null.
     *
     * @param array The array to check.
     * @param <T>   What we are dealing with.
     * @return True if the array only contains nulls.
     */
    public static <T> boolean isEmpty(T[] array) {
        for (T value : array) {
            if (value != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Counts the elements in the array that are not null.
     *
     * @param array The array to check.
     * @param <T>   What we are dealing with.
     * @return The count of non-null objects in the array.
     */
    public static <T> int countNonNull(T[] array) {
        return count(array, Objects::nonNull);
    }

    /**
     * Counts elements in the array that conform to the Function check.
     *
     * @param array The array to check.
     * @param check The Function to apply to each element.
     * @param <T>   What we are dealing with.
     * @return The count.
     */
    public static <T> int count(T[] array, Function<T, Boolean> check) {
        int counter = 0;
        for (T value : array) {
            if (check.apply(value)) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * Fills the array with the specified value.
     * If the value is an instance of Copyable it will call copy.
     *
     * @param array Array to fill.
     * @param value Value to fill with.
     * @param <T>   What we are dealing with.
     */
    public static <T> T[] fill(T[] array, T value) {
        for (int i = 0; i < array.length; i++) {
            T newValue = value;
            if (value instanceof Copyable) {
                Copyable<T> copyable = unsafeCast(value);
                newValue = copyable.copy();
            }
            array[i] = newValue;
        }
        return array;
    }

    /**
     * Fills the array with the specified value.
     * A Function is used to check if the value should be replaced.
     * If the value is an instance of Copyable it will call copy.
     *
     * @param array Array to fill.
     * @param value Value to fill with.
     * @param check Called to decide if the value should be replaced.
     * @param <T>   What we are dealing with.
     */
    public static <T> void fillArray(T[] array, T value, Function<T, Boolean> check) {
        for (int i = 0; i < array.length; i++) {
            if (check.apply(array[i])) {
                T newValue = value;
                if (value instanceof Copyable) {
                    Copyable<T> copyable = unsafeCast(value);
                    newValue = copyable.copy();
                }
                array[i] = newValue;
            }
        }
    }

    /**
     * Basically a wrapper for System.arraycopy with support for Copyable's
     *
     * @param src     The source array.
     * @param srcPos  Starting position in the source array.
     * @param dst     The destination array.
     * @param destPos Starting position in the destination array.
     * @param length  The number of elements to copy.
     */
    public static void arrayCopy(Object src, int srcPos, Object dst, int destPos, int length) {
        System.arraycopy(src, srcPos, dst, destPos, length);
        if (dst instanceof Copyable[]) {
            Object[] oa = (Object[]) dst;
            Copyable<Object>[] c = unsafeCast(dst);
            for (int i = destPos; i < destPos + length; i++) {
                if (c[i] != null) {
                    oa[i] = c[i].copy();
                }
            }
        }
    }

    /**
     * Returns the index of the first occurrence of the specified element in the array.
     * Will return -1 if the element is non existent in the array.
     *
     * @param array  The array to search.
     * @param object Element to find.
     * @param <T>    What we are dealing with.
     * @return The index in the array of the object.
     */
    public static <T> int indexOf(T[] array, T object) {
        if (object == null) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < array.length; i++) {
                if (object.equals(array[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns the index of the first occurrence of the specified element in the array
     * with the same identity. (Ref compare).
     * Will return -1 if the element is non existent in the array.
     *
     * @param array  The array to search.
     * @param object Element to find.
     * @return The index in the array of the object.
     */
    public static <T> int indexOfRef(T[] array, T object) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == object) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Create a new array using the provided array as a template for both type and length.
     *
     * @param array The template.
     * @param <T>   The type.
     * @return The new array.
     */
    public static <T> T[] createNewArray(T[] array) {
        return createNewArray(array, array.length);
    }

    /**
     * Create a new array using the provided array as a template for the type and with the provided length.
     *
     * @param array  The type template.
     * @param length The new array's length.
     * @param <T>    The type.
     * @return The new array.
     */
    public static <T> T[] createNewArray(T[] array, int length) {
        Class<? extends T[]> newType = unsafeCast(array.getClass());
        T[] copy;
        if (newType.equals(Object[].class)) {
            copy = unsafeCast(new Object[length]);
        } else {
            copy = unsafeCast(newArray(newType.getComponentType(), length));
        }
        return copy;
    }

    /**
     * Creates a new array form its component class.
     *
     * @param arrayClass The component class.
     * @param length     The length.
     * @param <T>        The thing.
     * @return The new array.
     */
    public static <T> T[] newArray(Class<T> arrayClass, int length) {
        return unsafeCast(Array.newInstance(arrayClass, length));
    }

    /**
     * Rolls the array based on the shift.
     * Positive shift means the array will roll to the right.
     * Negative shift means the array will roll to the left.
     *
     * @param input The input array.
     * @param shift The shift amount.
     * @param <T>   The thing.
     * @return The new array.
     */
    public static <T> T[] rollArray(T[] input, int shift) {
        T[] newArray = createNewArray(input);

        for (int i = 0; i < input.length; i++) {
            int newPos = (i + shift) % input.length;

            if (newPos < 0) {
                newPos += input.length;
            }
            newArray[newPos] = input[i];
        }
        return newArray;
    }

    /**
     * Checks if an array contains any of the specified element.
     *
     * @param input   The input
     * @param element The thing to test against.
     * @param <T>     The thing.
     * @return If the element exists at all.
     */
    public static <T> boolean contains(T[] input, T element) {
        for (T test : input) {
            if (Objects.equals(test, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the inverse of an array.
     * If the input array does not contain an element from the allElements array,
     * then it is added to the output.
     *
     * @param input       The input.
     * @param allElements All possible values.
     * @param <T>         The thing.
     * @return The inverse array.
     */
    public static <T> T[] inverse(T[] input, T[] allElements) {
        List<T> list = new LinkedList<>();
        for (T e : allElements) {
            if (!contains(input, e)) {
                list.add(e);
            }
        }

        return list.toArray(createNewArray(input, list.size()));
    }

    /**
     * Checks if the specified array is empty or contains a null entry.
     *
     * @param input The input.
     * @param <T>   The thing.
     * @return If the array is null or contains null.
     */
    public static <T> boolean isNullOrContainsNull(T @Nullable [] input) {
        if (input == null) return true;
        for (T t : input) {
            if (t != null) continue;
            return true;
        }
        return false;
    }

    /**
     * Convert an int array to a list of Integers.
     *
     * @param arr in.
     * @return out.
     */
    public static List<Integer> toList(int[] arr) {
        List<Integer> list = new ArrayList<>();
        for (int i : arr) {
            list.add(i);
        }
        return list;
    }

    /**
     * Represents this Enumeration as an Iterable.
     *
     * @param enumeration The Enumeration.
     * @return The Iterable.
     */
    public static <E> Iterable<E> iterable(Enumeration<E> enumeration) {
        return () -> new Iterator<E>() {
            //@formatter:off
            @Override public boolean hasNext() { return enumeration.hasMoreElements(); }
            @Override public E next() { return enumeration.nextElement(); }
            //@formatter:on
        };
    }

    /**
     * Shorthand for converting a stream to an Iterable.
     * <p>
     * It is important to note, that a Stream can only be consumed once,
     * and thus, the returned Iterable can only be consumed once.
     *
     * @param stream The Stream.
     * @return The Iterable.
     */
    public static <E> Iterable<E> iterable(Stream<E> stream) {
        return stream::iterator;
    }

    /**
     * Shorthand util for streaming an Iterable.
     *
     * @param iter The iterable.
     * @return The Stream.
     */
    public static <E> Stream<E> stream(Iterable<E> iter) {
        return StreamSupport.stream(iter.spliterator(), false);
    }

    /**
     * Shorthand util for streaming an Iterable in parallel.
     *
     * @param iter The iterable.
     * @return The Parallel Stream.
     */
    public static <E> Stream<E> parallelStream(Iterable<E> iter) {
        return StreamSupport.stream(iter.spliterator(), true);
    }

    @Nullable
    public static <T> T onlyOrDefault(Stream<T> stream) {
        return onlyOrDefault(stream, null);
    }

    /**
     * Returns the first element found in the Stream if it is the only element in the stream,
     * otherwise the default value is returned.
     *
     * @param stream   The stream.
     * @param _default The default value, in the event the stream is empty, or has more than one element.
     * @return The first element or the default.
     */
    @Nullable
    @Contract ("_,!null -> !null")
    public static <T> T onlyOrDefault(Stream<T> stream, @Nullable T _default) {
        T thing = _default;
        boolean found = false;
        for (T t : iterable(stream)) {
            if (found) return _default;
            found = true;
            thing = t;
        }
        return thing;
    }

    /**
     * Assert the stream contains a single element, and return it.
     *
     * @param stream The stream.
     * @return The element.
     */
    public static <T> T only(Stream<T> stream) {
        T thing = null;
        boolean found = false;
        for (T t : iterable(stream)) {
            if (found) {
                throw new IllegalArgumentException("More than one element.");
            }
            found = true;
            thing = t;
        }
        if (!found) {
            throw new IllegalArgumentException("Not found.");
        }
        return thing;
    }
}
