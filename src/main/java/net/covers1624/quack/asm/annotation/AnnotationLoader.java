/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.asm.annotation;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.asm.annotation.AnnotationParser.AnnotationParseException;
import net.covers1624.quack.asm.annotation.AnnotationParser.ClassLookup;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Utility for converting annotations loaded from ASM into class instances.
 * <p>
 * Created by covers1624 on 21/6/23.
 */
@Requires ("org.ow2.asm:asm")
public class AnnotationLoader {

    private final ClassLookup lookup;
    private final boolean ignoreMissing;
    private final Map<Class<? extends Annotation>, List<Annotation>> annotations = new LinkedHashMap<>();
    private final Map<Class<? extends Annotation>, List<Annotation>> invisibleAnnotations = new LinkedHashMap<>();

    public AnnotationLoader() {
        this(ClassLookup.DEFAULT);
    }

    public AnnotationLoader(boolean ignoreMissing) {
        this(ClassLookup.DEFAULT, ignoreMissing);
    }

    public AnnotationLoader(ClassLookup lookup) {
        this(lookup, false);
    }

    public AnnotationLoader(ClassLookup lookup, boolean ignoreMissing) {
        this.lookup = lookup;
        this.ignoreMissing = ignoreMissing;
    }

    /**
     * Returns a {@link ClassVisitor} which will consume class annotations into
     * this {@link AnnotationLoader}.
     *
     * @return The visitor.
     */
    public ClassVisitor forClass() {
        return forClass(null);
    }

    /**
     * Returns a {@link ClassVisitor} which will consume class annotations into
     * this {@link AnnotationLoader}.
     *
     * @return The visitor.
     */
    public ClassVisitor forClass(@Nullable ClassVisitor delegate) {
        return new ClassVisitor(Opcodes.ASM9, delegate) {
            @Nullable
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                return newVisitor(super.visitAnnotation(descriptor, visible), descriptor, visible);
            }
        };
    }

    /**
     * Returns a {@link MethodVisitor} which will consume method annotations into
     * this {@link AnnotationLoader}.
     *
     * @return The visitor.
     */
    public MethodVisitor forMethod() {
        return forMethod(null);
    }

    /**
     * Returns a {@link MethodVisitor} which will consume method annotations into
     * this {@link AnnotationLoader}.
     *
     * @param delegate Additionally chain visited data to the given delegate.
     * @return The visitor.
     */
    public MethodVisitor forMethod(@Nullable MethodVisitor delegate) {
        return new MethodVisitor(Opcodes.ASM9, delegate) {
            @Nullable
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                return newVisitor(super.visitAnnotation(descriptor, visible), descriptor, visible);
            }
        };
    }

    /**
     * Overload of {@link #forField(FieldVisitor)}.
     *
     * @return The visitor.
     */
    public FieldVisitor forField() {
        return forField(null);
    }

    /**
     * Returns a {@link FieldVisitor} which will consume field annotations into
     * this {@link AnnotationLoader}.
     *
     * @param delegate Additionally chain visited data to the given delegate.
     * @return The visitor.
     */
    public FieldVisitor forField(@Nullable FieldVisitor delegate) {
        return new FieldVisitor(Opcodes.ASM9, delegate) {
            @Nullable
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                return newVisitor(super.visitAnnotation(descriptor, visible), descriptor, visible);
            }
        };
    }

    /**
     * Get a single annotation of the specified type.
     * <p>
     * If there is more than one annotation, the first is returned.
     *
     * @param clazz The annotation class.
     * @return The annotation instance.
     */
    @Nullable
    public <T extends Annotation> T getAnnotation(Class<T> clazz) {
        return getAnnotation(clazz, true);
    }

    /**
     * Get a single annotation of the specified type.
     * <p>
     * If there is more than one annotation, the first is returned.
     *
     * @param clazz   The annotation class.
     * @param visible If the runtime visible or invisible annotation list should be queried.
     * @return The annotation instance.
     */
    @Nullable
    @SuppressWarnings ("unchecked")
    public <T extends Annotation> T getAnnotation(Class<T> clazz, boolean visible) {
        List<Annotation> annotations = getMap(visible).get(clazz);
        if (annotations == null || annotations.isEmpty()) return null;
        return (T) annotations.get(0);
    }

    /**
     * Get all annotation of the specified type.
     *
     * @param clazz The annotation class.
     * @return The annotation instance.
     */
    public <T extends Annotation> T @Nullable [] getAnnotations(Class<T> clazz) {
        return getAnnotations(clazz, true);
    }

    /**
     * Get all annotation of the specified type.
     *
     * @param clazz   The annotation class.
     * @param visible If the runtime visible or invisible annotation list should be queried.
     * @return The annotation instance.
     */
    @SuppressWarnings ("unchecked")
    public <T extends Annotation> T @Nullable [] getAnnotations(Class<T> clazz, boolean visible) {
        List<Annotation> annotations = getMap(visible).get(clazz);
        if (annotations == null || annotations.isEmpty()) return null;
        return annotations.toArray((T[]) Array.newInstance(clazz, 0));
    }

    /**
     * Get all annotation types..
     *
     * @return The types.
     */
    public Set<Class<? extends Annotation>> getAllTypes() {
        return getAllTypes(true);
    }

    /**
     * Get all annotation types..
     *
     * @param visible If the runtime visible or invisible annotation list should be queried.
     * @return The types.
     */
    public Set<Class<? extends Annotation>> getAllTypes(boolean visible) {
        return Collections.unmodifiableSet(getMap(visible).keySet());
    }

    private Map<Class<? extends Annotation>, List<Annotation>> getMap(boolean visible) {
        return visible ? annotations : invisibleAnnotations;
    }

    @Nullable
    private AnnotationParser newVisitor(AnnotationVisitor delegate, String descriptor, boolean visible) {
        try {
            return AnnotationParser.newVisitor(
                    delegate,
                    lookup,
                    descriptor,
                    value -> addToMap(value, visible)
            );
        } catch (AnnotationParseException ex) {
            if (!ignoreMissing) {
                throw ex;
            }
            return null;
        }
    }

    private void addToMap(Annotation annotation, boolean visible) {
        getMap(visible)
                .computeIfAbsent(annotation.annotationType(), e -> new ArrayList<>())
                .add(annotation);
        // Also expand repeatable annotations and add separately.
        Annotation[] expanded = expandRepeatable(annotation);
        if (expanded != null) {
            for (Annotation ann : expanded) {
                addToMap(ann, visible);
            }
        }
    }

    /**
     * If the given annotation is a {@link Repeatable} annotation, expand it into
     * the contained annotations.
     *
     * @param ann The annotation to expand.
     * @return The expanded annotations, or {@code null} if the annotation is not
     * repeatable and no expansion is possible.
     */
    @SuppressWarnings ("unchecked")
    public static <T extends Annotation> T @Nullable [] expandRepeatable(Annotation ann) {
        try {
            Class<?> clazz = ann.annotationType();
            for (Method method : clazz.getMethods()) {
                Class<?> returnType = method.getReturnType();
                if (!method.getName().equals("value") || !returnType.isArray()) continue;
                Repeatable r = returnType.getComponentType().getAnnotation(Repeatable.class);
                if (r != null && r.value().equals(clazz)) {
                    return (T[]) method.invoke(ann);
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
