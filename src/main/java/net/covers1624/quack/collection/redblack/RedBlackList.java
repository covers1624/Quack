/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.collection.redblack;

import net.covers1624.quack.util.Object2IntFunction;
import net.covers1624.quack.util.SneakyUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * Based off Chicken-Bones C# RedBlack Tree library: https://github.com/Chicken-Bones/RedBlack
 * <p>
 * Created by covers1624 on 16/5/21.
 */
public class RedBlackList<T extends Comparable<T>> extends SimpleRedBlackTree<T, RedBlackList<T>.Node> implements List<T> {

    @Override
    protected Node newNode(T value) {
        return new Node(value);
    }

    public Node nodeAt(int index) {
        return getByIndex(this, index, Node::getCount);
    }

    public int indexOf(@Nullable Node node) {
        return indexOf(this, node, Node::getCount);
    }

    @Override
    public T get(int index) {
        return nodeAt(index).value;
    }

    @Override
    public T set(int index, T element) {
        Node at = nodeAt(index);
        replace(at, newNode(element));
        return at.value;
    }

    @Override
    public void add(int index, T element) {
        if (index == size()) {
            insertAt(null, true, newNode(element));
        } else {
            insertAt(nodeAt(index), false, newNode(element));
        }
    }

    @Override
    public T remove(int index) {
        Node thing = nodeAt(index);
        entries().remove(thing);
        return thing.value;
    }

    @Override
    public int indexOf(Object o) {
        if (!(o instanceof Comparable)) return -1;
        return indexOf(find(SneakyUtils.<T>unsafeCast(o)));
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }

    public static <N extends RedBlackNode<N>> N getByIndex(BaseRedBlackTree<N> tree, int index, Object2IntFunction<N> countGetter) {
        if (index < 0 || index >= tree.count) {
            throw new IndexOutOfBoundsException("Got: " + index + ", Range: [0.." + tree.count + "]");
        }

        N node = tree.getRoot();
        assert node != null;
        while (true) {
            if (node.getLeft() != null && index < countGetter.apply(node.getLeft())) {
                node = node.getLeft();
                continue;
            }
            if (node.getRight() == null) return node;

            index -= countGetter.apply(node) - countGetter.apply(node.getRight());

            if (index < 0) return node;

            node = node.getRight();
        }
    }

    public static <N extends RedBlackNode<N>> int indexOf(BaseRedBlackTree<N> tree, @Nullable N node, Object2IntFunction<N> countGetter) {
        if (node == null) return -1;

        assert node.getRoot() == node;
        int index = node.getLeft() != null ? countGetter.apply(node.getLeft()) : 0;

        while (node.getParent() != null) {
            if (node.getSide()) {//if we're on the right side of a parent, add all the left sum
                index += countGetter.apply(node.getParent()) - countGetter.apply(node);
            }
            node = node.getParent();
        }
        return index;
    }

    public class Node extends ContainerNode<T, Node> {

        int count = 1;

        public Node(T value) {
            super(value);
        }

        @Override
        public void onChildrenChanged() {
            int count = 1;
            if (getLeft() != null) {
                count += getLeft().count;
            }
            if (getRight() != null) {
                count += getRight().count;
            }
            if (this.count != count) {
                this.count = count;
                if (getParent() != null) {
                    getParent().onChildrenChanged();
                }
            }
        }

        public int getCount() {
            return count;
        }
    }
}
