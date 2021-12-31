/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.io;

import org.junit.jupiter.api.Test;

import java.nio.file.attribute.PosixFilePermissions;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by covers1624 on 31/12/21.
 */
public class IOUtilsTests {

    @Test
    public void testParseMode() {
        assertEquals(PosixFilePermissions.fromString("---------"), IOUtils.parseMode(0));
        assertEquals(PosixFilePermissions.fromString("r--------"), IOUtils.parseMode(400));
        assertEquals(PosixFilePermissions.fromString("-w-------"), IOUtils.parseMode(200));
        assertEquals(PosixFilePermissions.fromString("--x------"), IOUtils.parseMode(100));
        assertEquals(PosixFilePermissions.fromString("---r-----"), IOUtils.parseMode(40));
        assertEquals(PosixFilePermissions.fromString("----w----"), IOUtils.parseMode(20));
        assertEquals(PosixFilePermissions.fromString("-----x---"), IOUtils.parseMode(10));
        assertEquals(PosixFilePermissions.fromString("------r--"), IOUtils.parseMode(4));
        assertEquals(PosixFilePermissions.fromString("-------w-"), IOUtils.parseMode(2));
        assertEquals(PosixFilePermissions.fromString("--------x"), IOUtils.parseMode(1));
        assertEquals(PosixFilePermissions.fromString("rwxr-xr-x"), IOUtils.parseMode(755));
        assertEquals(PosixFilePermissions.fromString("rwxrwxrwx"), IOUtils.parseMode(777));
    }

    @Test
    public void testWriteMode() {
        assertEquals(0, IOUtils.writeMode(PosixFilePermissions.fromString("---------")));
        assertEquals(400, IOUtils.writeMode(PosixFilePermissions.fromString("r--------")));
        assertEquals(200, IOUtils.writeMode(PosixFilePermissions.fromString("-w-------")));
        assertEquals(100, IOUtils.writeMode(PosixFilePermissions.fromString("--x------")));
        assertEquals(40, IOUtils.writeMode(PosixFilePermissions.fromString("---r-----")));
        assertEquals(20, IOUtils.writeMode(PosixFilePermissions.fromString("----w----")));
        assertEquals(10, IOUtils.writeMode(PosixFilePermissions.fromString("-----x---")));
        assertEquals(4, IOUtils.writeMode(PosixFilePermissions.fromString("------r--")));
        assertEquals(2, IOUtils.writeMode(PosixFilePermissions.fromString("-------w-")));
        assertEquals(1, IOUtils.writeMode(PosixFilePermissions.fromString("--------x")));
        assertEquals(755, IOUtils.writeMode(PosixFilePermissions.fromString("rwxr-xr-x")));
        assertEquals(777, IOUtils.writeMode(PosixFilePermissions.fromString("rwxrwxrwx")));
    }
}
