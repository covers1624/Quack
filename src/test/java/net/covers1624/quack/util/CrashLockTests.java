/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.util;

import net.covers1624.quack.util.CrashLock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by covers1624 on 22/1/21.
 */
public class CrashLockTests {

    @Test
    public void testLock() {
        CrashLock crashLock = new CrashLock("Locked.");
        crashLock.lock();
    }

    @Test
    public void testLockUnlock() {
        CrashLock crashLock = new CrashLock("Locked.");
        crashLock.lock();
        crashLock.unlock();
    }

    @Test
    public void testLockLock() {
        CrashLock crashLock = new CrashLock("Locked.");
        crashLock.lock();
        RuntimeException e = Assertions.assertThrows(RuntimeException.class, crashLock::lock);
        Assertions.assertEquals("Locked.", e.getMessage());
    }

    @Test
    public void testLockLockUnlockLock() {
        CrashLock crashLock = new CrashLock("Locked.");
        crashLock.lock();
        RuntimeException e = Assertions.assertThrows(RuntimeException.class, crashLock::lock);
        Assertions.assertEquals("Locked.", e.getMessage());
        crashLock.unlock();
        crashLock.lock();
    }

}
