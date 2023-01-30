package net.covers1624.quack.collection;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by covers1624 on 19/1/23.
 */
public abstract class AbstractIterator<T> implements Iterator<T> {

    private static final int CONSUMED = 0;
    private static final int HAS_NEXT = 1;
    private static final int END_OF_DATA = 2;

    private int state;
    @Nullable
    private T next;

    @Nullable
    protected abstract T computeNext();

    @Override
    public final boolean hasNext() {
        if (state == END_OF_DATA) return false;
        if (state == CONSUMED) {
            state = HAS_NEXT;
            next = computeNext();
        }
        return state == HAS_NEXT;
    }

    @Override
    public final T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        state = CONSUMED;
        return next;
    }

    @Nullable
    protected final T endOfData() {
        state = END_OF_DATA;
        return null;
    }
}
