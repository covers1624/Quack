/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.collection.redblack;

import net.covers1624.quack.collection.Object2IntPair;
import net.covers1624.quack.util.SneakyUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Based off Chicken-Bones C# RedBlack Tree library: https://github.com/Chicken-Bones/RedBlack
 * <p>
 * Created by covers1624 on 16/5/21.
 */
public abstract class ComparableRedBlackTree<T, N extends RedBlackNode<N> & Comparable<N>> extends CollectionRedBlackTree<T, N> {

    @Override
    protected Entries makeEntriesCollection() {
        return new ComparableEntries();
    }

    public Object2IntPair<N> closest(N node) {
        return closest(node::compareTo);
    }

    @Override
    protected void orderConsistencyCheck(@Nullable N left, @Nullable N right) {
        if (left != null && right != null && left.compareTo(right) >= 0) {
            throw new IllegalArgumentException("Comparison contract violated by supplied arguments '" + left + "' < '" + right + "'");
        }
    }

    public void insertRange(Iterable<N> nodes) {
        insertRangeAt(null, nodes);
    }

    public void insertRangeAt(@Nullable N loc, Iterable<N> nodes) {
        for (N node : nodes) {
            if (loc == null) { //find insertion location
                Object2IntPair<N> c = closest(node);
                loc = c.getKey();
                if (c.getValue() < 0) {
                    if (loc != null) {
                        loc = loc.getPrev();
                    }
                }
            }

            orderConsistencyCheck(loc, node);
            insertAt(loc, true, node);
            loc = node;
        }
        assert loc != null;
        orderConsistencyCheck(loc, loc.getNext());
    }

    protected class ComparableEntries extends Entries {

        @Override
        public boolean add(N t) {
            if (getRoot() == null) {
                insertAt(null, false, t);
                return true;
            }

            Object2IntPair<N> locPair = closest(t);
            if (locPair.getValue() == 0) return false;

            insertAt(locPair.getKey(), locPair.getValue() > 0, t);
            return true;
        }

        @Override
        public boolean contains(Object o) {
            return o instanceof RedBlackNode
                    && o instanceof Comparable
                    && find(SneakyUtils.<Comparable<N>>unsafeCast(o)::compareTo) != null;
        }
    }
}
