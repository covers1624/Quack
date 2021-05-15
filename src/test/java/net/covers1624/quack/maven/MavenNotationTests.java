/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.maven;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by covers1624 on 2/5/21.
 */
public class MavenNotationTests {

    @Test
    public void testParsing() {
        MavenNotation a = MavenNotation.parse("group:module:1.0");
        assertComponents(a, "group", "module", "1.0", null, "jar");

        MavenNotation b = MavenNotation.parse("group:module");
        assertComponents(b, "group", "module", null, null, "jar");

        MavenNotation c = MavenNotation.parse("group:module@zip");
        assertComponents(c, "group", "module", null, null, "zip");

        MavenNotation d = MavenNotation.parse("group:module:1.0:classifier@zip");
        assertComponents(d, "group", "module", "1.0", "classifier", "zip");

        MavenNotation e = MavenNotation.parse("group:module:1.0:classifier");
        assertComponents(e, "group", "module", "1.0", "classifier", "jar");
    }

    public static void assertComponents(MavenNotation notation, String group, String module, @Nullable String version, @Nullable String classifier, String extension) {
        assertEquals(group, notation.group);
        assertEquals(module, notation.module);
        assertEquals(version, notation.version);
        assertEquals(classifier, notation.classifier);
        assertEquals(extension, notation.extension);
    }
}
