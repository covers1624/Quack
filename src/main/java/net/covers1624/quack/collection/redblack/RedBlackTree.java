/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.collection.redblack;

/**
 * Based off Chicken-Bones C# RedBlack Tree library: https://github.com/Chicken-Bones/RedBlack
 * <p>
 * Created by covers1624 on 16/5/21.
 */
public class RedBlackTree<T extends Comparable<T>> extends SimpleRedBlackTree<T, RedBlackTree<T>.Node> {

    @Override
    protected Node newNode(T value) {
        return new Node(value);
    }

    public class Node extends ContainerNode<T, Node> {

        public Node(T value) {
            super(value);
        }
    }
}
