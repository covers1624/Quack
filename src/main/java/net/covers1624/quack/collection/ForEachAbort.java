package net.covers1624.quack.collection;

/**
 * Created by covers1624 on 30/1/23.
 */
class ForEachAbort extends RuntimeException {

    public static final ForEachAbort INSTANCE = new ForEachAbort();

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
