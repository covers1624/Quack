/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.asm.annotation;

import net.covers1624.quack.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by covers1624 on 22/6/23.
 */
public class AnnotationTests {

    protected static ClassNode getClassNode(Class<?> clazz) {
        ClassNode cNode = new ClassNode();
        ClassReader reader = new ClassReader(getClassBytes(clazz));
        reader.accept(cNode, 0);
        return cNode;
    }

    protected static byte[] getClassBytes(Class<?> clazz) {
        try (InputStream is = AnnotationParserTests.class.getResourceAsStream("/" + clazz.getName().replace('.', '/') + ".class")) {
            return IOUtils.toBytes(is);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
