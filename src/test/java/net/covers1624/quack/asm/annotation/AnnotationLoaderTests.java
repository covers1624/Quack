/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.asm.annotation;

import net.covers1624.quack.asm.annotation.AnnotationParserTests.BasicAnnotation;
import net.covers1624.quack.asm.annotation.AnnotationParserTests.MoreThanOne;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by covers1624 on 22/6/23.
 */
public class AnnotationLoaderTests extends AnnotationTests {

    @Test
    public void testBasicLoader() {
        AnnotationLoader loader = new AnnotationLoader();
        getClassNode(BasicAnnotation.class).accept(loader.forClass());

        BasicAnnotation ann = loader.getAnnotation(BasicAnnotation.class, false);

        assertNotNull(ann);
        assertEquals(BasicAnnotation.class, ann.annotationType());
        assertArrayEquals(new String[] { "asdf" }, ann.strings());
        assertArrayEquals(new int[] { 1 }, ann.ints());
        AnnotationParserTests.InnerAnnotation inner = ann.inner()[0];
        assertEquals(AnnotationParserTests.SomeEnum.A, inner.value());
        assertEquals("I am defaulted!", ann.defaultValue());
    }

    @Test
    public void testRepeatableExpandLoader() {
        AnnotationLoader loader = new AnnotationLoader();
        getClassNode(MoreThanOne.class).accept(loader.forClass());

        {
            MoreThanOne.MoreThanOneList ann = loader.getAnnotation(MoreThanOne.MoreThanOneList.class, false);
            assertNotNull(ann);
            MoreThanOne[] annArr = ann.value();
            assertEquals(2, annArr.length);
            assertEquals("a", annArr[0].value());
            assertEquals("b", annArr[1].value());
        }

        {
            MoreThanOne[] annArr = loader.getAnnotations(MoreThanOne.class, false);
            assertEquals(2, annArr.length);
            assertEquals("a", annArr[0].value());
            assertEquals("b", annArr[1].value());
        }
    }
}
