/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.util;

/**
 * A simple crash lock.
 * <p>
 * Created by covers1624 on 13/10/20.
 */
public class CrashLock {

    private final String message;
    private boolean locked = false;

    public CrashLock(String message) {
        this.message = message;
    }

    public void lock() {
        if (locked) {
            throw new RuntimeException(message);
        }
        locked = true;
    }

    public void unlock() {
        locked = false;
    }
}
