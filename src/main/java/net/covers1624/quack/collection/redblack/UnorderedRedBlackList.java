/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.collection.redblack;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * Based off Chicken-Bones C# RedBlack Tree library: https://github.com/Chicken-Bones/RedBlack
 * <p>
 * Created by covers1624 on 16/5/21.
 */
public class UnorderedRedBlackList<T> extends CollectionRedBlackTree<T, UnorderedRedBlackList<T>.Node> implements List<T> {

    public Node nodeAt(int index) {
        return RedBlackList.getByIndex(this, index, Node::getCount);
    }

    public int indexOf(Node node) {
        return RedBlackList.indexOf(this, node, Node::getCount);
    }

    @Override
    protected Node newNode(T value) {
        return new Node(value);
    }

    @Override
    protected T getValue(Node node) {
        return node.value;
    }

    @Override
    public boolean add(T t) {
        insertAt(null, true, newNode(t));
        return true;
    }

    @Override
    public T get(int index) {
        return nodeAt(index).value;
    }

    @Override
    public T set(int index, T element) {
        Node at = nodeAt(index);
        replace(at, new Node(element));
        return at.value;
    }

    @Override
    public void add(int index, T element) {
        if (index == size()) {
            insertAt(null, true, new Node(element));
        } else {
            insertAt(nodeAt(index), false, new Node(element));
        }
    }

    @Override
    public T remove(int index) {
        Node thing = nodeAt(index);
        entries().remove(thing);
        return thing.value;
    }

    //region Not supported
    //@formatter:off
    @Override public boolean contains(Object o) { throw new UnsupportedOperationException("contains(Object) would be O(n)"); }
    @Override public boolean remove(Object o) { throw new UnsupportedOperationException("remove(Object) would be O(n)"); }
    @Override public boolean addAll(int index, Collection<? extends T> c) { throw new UnsupportedOperationException("Not Yet Implemented"); }
    @Override public int indexOf(Object o) { throw new UnsupportedOperationException("indexOf(Object) would be O(n)"); }
    @Override public int lastIndexOf(Object o) { throw new UnsupportedOperationException("lastIndexOf(Object) would be O(n)"); }
    @Override public ListIterator<T> listIterator() { throw new UnsupportedOperationException("Not Yet Implemented"); }
    @Override public ListIterator<T> listIterator(int index) { throw new UnsupportedOperationException("Not Yet Implemented"); }
    @Override public List<T> subList(int fromIndex, int toIndex) { throw new UnsupportedOperationException("Not Yet Implemented"); }
    //@formatter:on
    //endregion

    public class Node extends RedBlackNode<Node> {

        public T value;
        public int count = 1;

        public Node(T value) {
            this.value = value;
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

