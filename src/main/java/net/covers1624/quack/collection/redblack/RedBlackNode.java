/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.collection.redblack;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Based off Chicken-Bones C# RedBlack Tree library: https://github.com/Chicken-Bones/RedBlack
 * <p>
 * Created by covers1624 on 15/5/21.
 */
public abstract class RedBlackNode<T extends RedBlackNode<T>> {

    @Nullable
    private T left;
    @Nullable
    private T right;

    private boolean isRed;

    @Nullable
    private T parent;

    void makeRoot() {
        setParent(null);
    }

    public T getRoot() {
        if (parent == null) return unsafeCast(this);
        return parent.getRoot();
    }

    public boolean getSide() {
        return requireParent().getRight() == this;
    }

    @Nullable
    public T getSibling() {
        return requireParent().getChild(!getSide());
    }

    @Nullable
    public T getChild(boolean r) {
        return r ? right : left;
    }

    public void assign(boolean r, @Nullable T n) {
        if (r) {
            setRight(n);
        } else {
            setLeft(n);
        }
    }

    public T getLeftMost() {
        if (left == null) return unsafeCast(this);
        return left.getLeftMost();
    }

    public T getRightMost() {
        if (right == null) return unsafeCast(this);
        return right.getRightMost();
    }

    public T most(boolean r) {
        return r ? getRightMost() : getLeftMost();
    }

    public T getNext() {
        return closest(true);
    }

    public T getPrev() {
        return closest(false);
    }

    public T closest(boolean r) {
        T next = getChild(r);
        if (next != null) {
            next = next.most(!r);
        }

        if (next != null) return next;

        T curr = unsafeCast(this);
        while (curr.getParent() != null && curr.getSide() == r) {
            curr = curr.getParent();
        }
        return curr.getParent();
    }

    public Iterable<T> to(T last) {
        return () -> new Iterator<T>() {
            @Nullable
            private T p = unsafeCast(RedBlackNode.this);

            @Override
            public boolean hasNext() {
                return p != null;
            }

            @Override
            public T next() {
                assert p != null;
                T curr = p;
                if (curr == last) {
                    p = null;
                } else {
                    p = curr.getNext();
                }
                return curr;
            }
        };
    }

    public void onChildrenChanged() { }

    @Nullable
    public T getLeft() {
        return left;
    }

    void setLeft(@Nullable T left) {
        this.left = left;
        if (left != null) {
            left.setParent(unsafeCast(this));
        }
    }

    @Nullable
    public T getRight() {
        return right;
    }

    public void setRight(@Nullable T right) {
        this.right = right;
        if (right != null) {
            right.setParent(unsafeCast(this));
        }
    }

    public boolean isRed() {
        return isRed;
    }

    @Nullable
    public T getParent() {
        return parent;
    }

    public T requireParent() {
        return Objects.requireNonNull(parent);
    }

    public boolean isBlack() {
        return !isRed;
    }

    void setRed(boolean red) {
        isRed = red;
    }

    void setParent(@Nullable T parent) {
        this.parent = parent;
    }

    void setBlack(boolean black) {
        isRed = !black;
    }

}
