/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.collection.redblack;

/**
 * Based off Chicken-Bones C# RedBlack Tree library: https://github.com/Chicken-Bones/RedBlack
 * <p>
 * Created by covers1624 on 16/5/21.
 */
public class ContainerNode<T extends Comparable<T>, N extends ContainerNode<T, N>> extends RedBlackNode<N> implements Comparable<N> {

    public T value;

    public ContainerNode(T value) {
        this.value = value;
    }

    @Override
    public int compareTo(N o) {
        return value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return "{" + (isRed() ? "R" : "B") + "}: '" + value + "'";
    }
}
