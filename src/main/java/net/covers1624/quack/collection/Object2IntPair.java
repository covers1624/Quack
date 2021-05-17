/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.collection;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Created by covers1624 on 4/19/20.
 */
public class Object2IntPair<T> {

    @Nullable
    private T key;
    private int value;

    public Object2IntPair() {
    }

    public Object2IntPair(@Nullable T key, int value) {
        this();
        this.key = key;
        this.value = value;
    }

    @Nullable
    public T getKey() {
        return key;
    }

    public void setKey(@Nullable T key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Object2IntPair<?> that = (Object2IntPair<?>) o;
        return getValue() == that.getValue() && Objects.equals(getKey(), that.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getValue());
    }
}
