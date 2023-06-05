/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.collection;

/**
 * Created by covers1624 on 30/1/23.
 */
class ForEachAbort extends RuntimeException {

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
