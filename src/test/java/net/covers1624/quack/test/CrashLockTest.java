/*
 * MIT License
 *
 * Copyright (c) 2018-2021 covers1624
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.covers1624.quack.test;

import net.covers1624.quack.util.CrashLock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by covers1624 on 22/1/21.
 */
public class CrashLockTest {

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

}
