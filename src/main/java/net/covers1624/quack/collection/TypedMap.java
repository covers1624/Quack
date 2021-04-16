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

package net.covers1624.quack.collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 12/8/19.
 */
public class TypedMap implements Map<Object, Object> {

    private final Map<Object, Object> delegate;

    public TypedMap() {
        this(new HashMap<>());
    }

    public TypedMap(Map<Object, Object> delegate) {
        this.delegate = delegate;
    }

    @Nullable
    public <T> T put(Key<T> key, T value) {
        return unsafeCast(delegate.put(key, value));
    }

    @Nullable
    public <T> T get(Key<T> key) {
        return unsafeCast(delegate.get(key));
    }

    //@formatter:off
    @Override public int size() { return delegate.size(); }
    @Override public boolean isEmpty() { return delegate.isEmpty(); }
    @Override public boolean containsKey(Object key) { return delegate.containsKey(key); }
    @Override public boolean containsValue(Object value) { return delegate.containsValue(value); }
    @Override public Object get(Object key) { return delegate.get(key); }
    @Nullable @Override public Object put(Object key, Object value) { return delegate.put(key, value); }
    @Override public Object remove(Object key) { return delegate.remove(key); }
    @Override public void putAll(@NotNull Map<?, ?> m) { delegate.putAll(m); }
    @Override public void clear() { delegate.clear(); }
    @NotNull @Override public Set<Object> keySet() { return delegate.keySet(); }
    @NotNull @Override public Collection<Object> values() { return delegate.values(); }
    @NotNull @Override public Set<Entry<Object, Object>> entrySet() { return delegate.entrySet(); }
    //@formatter:on

    public static class Key<T> {

        private final String name;

        public Key(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj)) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            Key<?> other = (Key<?>) obj;
            return other.name.equals(name);
        }

        @Override
        public int hashCode() {
            int i = 0;
            i = 31 * i + name.hashCode();
            return i;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
