/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.asm.annotation;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.lang.annotation.Repeatable;

import static net.covers1624.quack.asm.annotation.AnnotationParser.ClassLookup.DEFAULT;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by covers1624 on 22/6/23.
 */
public class AnnotationParserTests extends AnnotationTests {

    @Test
    public void testBasics() {
        ClassNode cNode = getClassNode(BasicAnnotation.class);
        AnnotationNode aNode = cNode.invisibleAnnotations.get(0);
        BasicAnnotation[] result = new BasicAnnotation[1];
        aNode.accept(AnnotationParser.newVisitor(DEFAULT, aNode.desc, v -> result[0] = (BasicAnnotation) v));
        BasicAnnotation res = result[0];

        assertEquals(BasicAnnotation.class, res.annotationType());
        assertArrayEquals(new String[] { "asdf" }, res.strings());
        assertArrayEquals(new int[] { 1 }, res.ints());
        InnerAnnotation inner = res.inner()[0];
        assertEquals(SomeEnum.A, inner.value());
        assertEquals("I am defaulted!", res.defaultValue());
    }

    @Test
    public void testBasicsFromClassReader() {
        ClassReader reader = new ClassReader(getClassBytes(BasicAnnotation.class));
        BasicAnnotation[] result = new BasicAnnotation[1];
        reader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                return AnnotationParser.newVisitor(DEFAULT, descriptor, v -> result[0] = (BasicAnnotation) v);
            }
        }, 0);

        BasicAnnotation res = result[0];

        assertEquals(BasicAnnotation.class, res.annotationType());
        assertArrayEquals(new String[] { "asdf" }, res.strings());
        assertArrayEquals(new int[] { 1 }, res.ints());
        InnerAnnotation inner = res.inner()[0];
        assertEquals(SomeEnum.A, inner.value());
        assertEquals("I am defaulted!", res.defaultValue());
    }

    @Test
    public void testRepeatableExpand() {
        ClassNode cNode = getClassNode(MoreThanOne.class);
        AnnotationNode aNode = cNode.invisibleAnnotations.get(0);
        MoreThanOne.MoreThanOneList[] result = new MoreThanOne.MoreThanOneList[1];
        aNode.accept(AnnotationParser.newVisitor(DEFAULT, aNode.desc, v -> result[0] = (MoreThanOne.MoreThanOneList) v));
        MoreThanOne[] r = AnnotationLoader.expandRepeatable(result[0]);
        assertNotNull(r);
        assertEquals(2, r.length);
        assertEquals("a", r[0].value());
        assertEquals("b", r[1].value());
    }

    @BasicAnnotation (
            strings = "asdf",
            ints = 1,
            inner = @InnerAnnotation (
                    value = SomeEnum.A
            )
    )
    public @interface BasicAnnotation {

        String[] strings();

        int[] ints();

        InnerAnnotation[] inner();

        String defaultValue() default "I am defaulted!";
    }

    public @interface InnerAnnotation {

        SomeEnum value();
    }

    @MoreThanOne ("a")
    @MoreThanOne ("b")
    @Repeatable (MoreThanOne.MoreThanOneList.class)
    public @interface MoreThanOne {

        String value();

        @interface MoreThanOneList {

            MoreThanOne[] value();
        }
    }

    public enum SomeEnum {
        A,
        B
    }
}
