/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.collection.redblack;

import net.covers1624.quack.util.SneakyUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Based off Chicken-Bones C# RedBlack Tree library: https://github.com/Chicken-Bones/RedBlack
 * <p>
 * Created by covers1624 on 16/5/21.
 */
public abstract class SimpleRedBlackTree<T extends Comparable<T>, N extends ContainerNode<T, N>> extends ComparableRedBlackTree<T, N> implements Collection<T> {

    @Override
    protected T getValue(N node) {
        return node.value;
    }

    @Nullable
    public N find(T value) {
        return find(n -> value.compareTo(n.value));
    }

    public void buildFromValues(List<T> list) {
        buildFrom(list.stream().map(this::newNode).collect(Collectors.toList()));
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Comparable)) return false;
        return find(SneakyUtils.<T>unsafeCast(o)) != null;
    }

    @Override
    public boolean add(T t) {
        return entries().add(newNode(t));
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Comparable)) return false;
        return entries().remove(find(SneakyUtils.<T>unsafeCast(o)));
    }
}
