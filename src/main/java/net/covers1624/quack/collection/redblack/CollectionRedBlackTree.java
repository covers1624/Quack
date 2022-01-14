/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.collection.redblack;

import com.google.common.collect.Iterators;
import net.covers1624.quack.collection.ColUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Contains the base Collection overrides for a Collection based RedBlackTree.
 * TODO, this can be cleaned up and moved to an Interface, if we move BaseRedBlackTree to have a parent public interface.
 * <p>
 * Created by covers1624 on 17/5/21.
 */
public abstract class CollectionRedBlackTree<T, N extends RedBlackNode<N>> extends BaseRedBlackTree<N> implements Collection<T> {

    protected abstract N newNode(T value);

    protected abstract T getValue(N node);

    @Override
    public int size() {
        return entries().size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return Iterators.transform(entries().iterator(), this::getValue);
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return toArray(new Object[0]);
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        T1[] array = ColUtils.createNewArray(a, size());
        int i = 0;
        for (T t : this) {
            if (i == array.length) {
                //Double the size, we hit a case where the expected size of the tree does not match the iterator size..
                // This should never happen, but regardless we do this.
                array = Arrays.copyOf(array, array.length * 2);
            }
            array[i] = unsafeCast(t);
            i++;
        }
        if (i != array.length) {
            //Trim array in the event that it was resized, or the iterator returned less elements than the expected size.
            array = Arrays.copyOf(array, i);
        }
        return array;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        boolean ret = false;
        for (T t : c) {
            ret |= add(t);
        }
        return ret;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean ret = false;
        for (Object t : c) {
            ret |= remove(t);
        }
        return ret;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        //this requires re-starting the iterator each time we _dont_ find an element in c, as the underlying
        // iterator does not support continued node iteration on removals.
        throw new UnsupportedOperationException("Not implemented for performance reasons. TODO");
    }

    @Override
    public void clear() {
        entries().clear();
    }
}
