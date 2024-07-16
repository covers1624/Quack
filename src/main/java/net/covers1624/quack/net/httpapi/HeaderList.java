/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * A simple collection of name-value pair entries.
 * <p>
 * Names are compared case-insensitive.
 * <p>
 * All name characters must be within {@code u0021 .. u007e} (inclusive)<br>
 * All value characters must either be {@code \t} or within {@code u0020 .. u007e} (inclusive)
 * <p>
 * Created by covers1624 on 1/8/23.
 */
public final class HeaderList implements Iterable<HeaderList.Entry> {

    // Interleaved list of name-value pairs.
    private final List<String> headers = new ArrayList<>();

    /**
     * Checks if the collection is empty.
     *
     * @return If the collection is empty.
     */
    public boolean isEmpty() {
        return headers.isEmpty();
    }

    /**
     * Gets the number of entries in the collection.
     *
     * @return The number of entries.
     */
    public int size() {
        return headers.size() / 2;
    }

    /**
     * Adds name-value pair.
     *
     * @param name  The name.
     * @param value The value.
     */
    public void add(String name, String value) {
        if (name.isEmpty()) throw new IllegalStateException("Key name must not be empty!");
        checkNameChars(name);
        checkValueChars(name, value);
        headers.add(name);
        headers.add(value.trim());
    }

    /**
     * Adds name-value pair.
     *
     * @param name  The name.
     * @param value The value.
     */
    public void addFirst(String name, String value) {
        if (name.isEmpty()) throw new IllegalStateException("Key name must not be empty!");
        checkNameChars(name);
        checkValueChars(name, value);
        headers.add(0, value.trim());
        headers.add(0, name);
    }

    /**
     * Add all the entries from the specified {@link Map} to
     * this collection.
     *
     * @param entries The entries to add.
     */
    public void addAll(Map<String, String> entries) {
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Add all the entries from the specified {@link Map} to
     * this collection.
     *
     * @param entries The entries to add.
     */
    public void addAllMulti(Map<String, List<String>> entries) {
        for (Map.Entry<String, List<String>> entry : entries.entrySet()) {
            for (String value : entry.getValue()) {
                add(entry.getKey(), value);
            }
        }
    }

    /**
     * Add all the entries form the specified {@link HeaderList} to
     * this collection.
     *
     * @param other The entries to add.
     */
    public void addAll(HeaderList other) {
        headers.addAll(other.headers);
    }

    /**
     * Remove all existing values of a specified name, then insert a new value.
     *
     * @param name  The name.
     * @param value The value.
     */
    public void set(String name, String value) {
        checkNameChars(name);
        checkValueChars(name, value);
        removeAll(name);
        add(name, value);
    }

    /**
     * Checks if this collection contains the given name-value pair.
     *
     * @param name  The name.
     * @param value The value.
     * @return If the name-value pair exists.
     */
    public boolean contains(String name, String value) {
        for (int i = 0; i < headers.size(); i += 2) {
            String v = headers.get(i);
            if (v.equalsIgnoreCase(name) && headers.get(i + 1).equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this collection contains an entry with the given name and any value.
     *
     * @param name The name to check for.
     * @return If the name exists.
     */
    public boolean contains(String name) {
        for (int i = 0; i < headers.size(); i += 2) {
            String v = headers.get(i);
            if (v.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove all entries with the specified name.
     *
     * @param name The name to remove.
     */
    public void removeAll(String name) {
        for (int i = 0; i < headers.size(); i += 2) {
            if (headers.get(i).equalsIgnoreCase(name)) {
                headers.remove(i);
                headers.remove(i);
                i -= 2;
            }
        }
    }

    /**
     * Get the first value with the specified name.
     *
     * @param name The name to get the first value for.
     * @return The first value. {@code null} if it does not exist.
     */
    @Nullable
    public String get(String name) {
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).equalsIgnoreCase(name)) {
                return headers.get(i + 1);
            }
        }
        return null;
    }

    /**
     * Get all values with the specified name.
     *
     * @param name The name to get values for.
     * @return All values for the specified name.
     */
    public List<String> getValues(String name) {
        List<String> values = new LinkedList<>();
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).equalsIgnoreCase(name)) {
                values.add(headers.get(i + 1));
            }
        }
        return values;
    }

    /**
     * Delete all headers in this list.
     */
    public void clear() {
        headers.clear();
    }

    /**
     * Convert this header list into a list of assembled header strings.
     * <p>
     * For example {@code HeaderName: HeaderValue}
     *
     * @return The assembled header strings.
     */
    public String[] toStrings() {
        if (headers.isEmpty()) return new String[0];

        int i = 0;
        String[] strings = new String[headers.size() / 2];
        for (Entry entry : this) {
            strings[i++] = entry.name + ": " + entry.value;
        }
        return strings;
    }

    /**
     * Convert this header list into an interleaved array
     * of header entries.
     *
     * @return The interleaved array.
     */
    public String[] toArray() {
        return headers.toArray(new String[0]);
    }

    /**
     * Visit each header within this list.
     *
     * @param consumer The consumer for key, value entries.
     */
    public void forEach(BiConsumer<String, String> consumer) {
        for (int i = 0; i < headers.size(); i += 2) {
            consumer.accept(headers.get(i), headers.get(i + 1));
        }
    }

    @Override
    public Iterator<Entry> iterator() {
        Iterator<String> backing = headers.iterator();
        return new Iterator<Entry>() {
            @Override
            public boolean hasNext() {
                return backing.hasNext();
            }

            @Override
            public Entry next() {
                return new Entry(backing.next(), backing.next());
            }
        };
    }

    private static void checkNameChars(String name) {
        char[] charArray = name.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if ('\u0021' > c || c > '\u007e') {
                throw new IllegalArgumentException(String.format("Name '%s' Contains invalid character \\u%04X at %d", name, (int) c, i));
            }
        }
    }

    private static void checkValueChars(String name, String value) {
        char[] charArray = value.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c != '\t' && ('\u0020' > c || c > '\u007e')) {
                throw new IllegalArgumentException(String.format("Value for name '%s' Contains invalid character \\u%04X at %d", name, (int) c, i));
            }
        }
    }

    public static class Entry implements Map.Entry<String, String> {

        public final String name;
        public final String value;

        public Entry(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getKey() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue(String value) {
            throw new UnsupportedOperationException();
        }
    }
}
