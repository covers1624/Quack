/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.asm.annotation;

import net.covers1624.quack.annotation.Requires;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * An {@link AnnotationVisitor} implementation, which produces annotation
 * instances, similar to Java Reflection.
 * <p>
 * Created by covers1624 on 22/6/23.
 */
@Requires ("org.ow2.asm:asm")
public abstract class AnnotationParser extends AnnotationVisitor {

    protected final ClassLookup abstractions;

    @Deprecated
    @ScheduledForRemoval (inVersion = "0.5")
    protected AnnotationParser(ClassLookup abstractions) {
        this(null, abstractions);
    }

    protected AnnotationParser(@Nullable AnnotationVisitor delegate, ClassLookup abstractions) {
        super(Opcodes.ASM9, delegate);
        this.abstractions = abstractions;
    }

    /**
     * Create a visitor.
     * <p>
     * The visitor when piped annotation data will provide the loaded annotation object
     * and the class it was loaded from via the provided consumer.
     *
     * @param lookup The class lookup.
     * @param desc   The descriptor of the annotation.
     * @param cons   The consumer to accept the parsed annotations.
     * @return The {@link AnnotationVisitor} implementation.
     */
    public static AnnotationParser newVisitor(ClassLookup lookup, String desc, Consumer<Annotation> cons) throws AnnotationParseException {
        return newVisitor(null, lookup, desc, cons);
    }

    /**
     * Create a visitor.
     * <p>
     * The visitor when piped annotation data will provide the loaded annotation object
     * and the class it was loaded from via the provided consumer.
     *
     * @param delegate The AnnotationVisitor to delegate calls to.
     * @param lookup   The class lookup.
     * @param desc     The descriptor of the annotation.
     * @param cons     The consumer to accept the parsed annotations.
     * @return The {@link AnnotationVisitor} implementation.
     */
    public static AnnotationParser newVisitor(@Nullable AnnotationVisitor delegate, ClassLookup lookup, String desc, Consumer<Annotation> cons) throws AnnotationParseException {
        Class<? extends Annotation> clazz;
        try {
            clazz = lookup.getAnnotationClass(Type.getType(desc));
        } catch (Throwable ex) {
            throw new AnnotationParseException("Failed to load annotation.", ex);
        }
        return new AnnotationParser(delegate, lookup) {

            private final Map<String, Object> values = new HashMap<>();

            @Override
            public void visitValue(@Nullable String name, Object value) {
                assert name != null;
                values.put(name, value);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                cons.accept(
                        (Annotation) Proxy.newProxyInstance(
                                clazz.getClassLoader(),
                                new Class[] { clazz },
                                new AnnotationInvocationHandler(clazz, values)
                        )
                );
            }
        };
    }

    public abstract void visitValue(@Nullable String name, Object value);

    @Override
    public void visit(@Nullable String name, Object value) {
        super.visit(name, value);
        if (value instanceof Type) {
            try {
                value = abstractions.getClass((Type) value);
            } catch (ClassNotFoundException ex) {
                throw new AnnotationParseException("Failed to load Type value.", ex);
            }
        }
        visitValue(name, value);
    }

    @Override
    @SuppressWarnings ({ "rawtypes", "unchecked" })
    public void visitEnum(String name, String descriptor, String value) {
        super.visitEnum(name, descriptor, value);
        try {
            // Thanks Java, must use raw explicit here..
            visitValue(name, Enum.<Enum>valueOf(abstractions.getEnumClass(Type.getType(descriptor)), value));
        } catch (AnnotationParseException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new AnnotationParseException("Failed to parse enum constant " + value + " for " + descriptor, ex);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        return newVisitor(super.visitAnnotation(name, descriptor), abstractions, descriptor, v -> visitValue(name, v));
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return new AnnotationParser(super.visitArray(name), abstractions) {

            private final ArrayList<Object> values = new ArrayList<>(1);

            @Override
            public void visitValue(@Nullable String name, Object value) {
                assert name == null;
                values.add(value);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                values.trimToSize();
                AnnotationParser.this.visitValue(name, values);
            }
        };
    }

    /**
     * Abstractions for class lookups, which may be different on different platforms.
     */
    public interface ClassLookup {

        /**
         * The default singleton instance using {@link Class#forName}
         */
        ClassLookup DEFAULT = new ClassLookup() { };

        /**
         * Lookup the provided ASM {@link Type} and produce a {@link Class} instance.
         *
         * @param type The type to lookup.
         * @return The class instance.
         */
        default Class<?> getClass(Type type) throws ClassNotFoundException {
            // TODO, maybe use thread context loader?
            return Class.forName(type.getClassName());
        }

        /**
         * Lookup the provided {@link Type} as an enum and produce a {@link Class} instance.
         *
         * @param type The type to lookup.
         * @return The class instance.
         */
        default <T extends Enum<T>> Class<T> getEnumClass(Type type) throws ClassNotFoundException {
            Class<?> clazz = getClass(type);
            if (!clazz.isEnum()) throw new IllegalArgumentException("Not an enum class. " + type);
            return unsafeCast(clazz);
        }

        /**
         * Lookup the provided {@link Type} as an annotation and produce a {@link Class} instance.
         *
         * @param type The type to lookup.
         * @return The class instance.
         */
        default Class<? extends Annotation> getAnnotationClass(Type type) throws ClassNotFoundException {
            Class<?> clazz = getClass(type);
            if (!clazz.isAnnotation()) throw new IllegalArgumentException("Not an annotation class. " + type);
            return unsafeCast(clazz);
        }
    }

    /**
     * Thrown if there is any error during the annotation parsing process.
     */
    public static class AnnotationParseException extends RuntimeException {

        public AnnotationParseException(String message) {
            super(message);
        }

        public AnnotationParseException(Throwable cause) {
            super(cause);
        }

        public AnnotationParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class AnnotationInvocationHandler implements InvocationHandler {

        private final Class<? extends Annotation> annotationType;

        private final Map<String, Object> members;

        AnnotationInvocationHandler(Class<? extends Annotation> annotationType, Map<String, Object> members) {
            this.annotationType = annotationType;
            this.members = members;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "hashCode":
                    // TODO may not be accurate. We should probably populate this map with the annotation defaults
                    //      then overwrite with user specified. This hashcode should also include the hash of the
                    //      annotation type, to differentiate between different empty annotations.
                    return members.hashCode();
                case "equals": {
                    Object arg = args[0];
                    if (arg == null || !Proxy.isProxyClass(arg.getClass())) return false;

                    InvocationHandler o = Proxy.getInvocationHandler(arg);
                    if (!(o instanceof AnnotationInvocationHandler)) return false;
                    AnnotationInvocationHandler other = (AnnotationInvocationHandler) o;

                    return members.equals(other.members) && annotationType.equals(other.annotationType);
                }
                case "toString":
                    // TODO this should mirror Java's annotation toString.
                    return annotationType.getName() + members;
                case "annotationType":
                    return annotationType;
                default:
                    return getMember(method);
            }
        }

        @SuppressWarnings ("unchecked")
        private Object getMember(Method method) {
            Object obj = members.get(method.getName());
            // Default value!
            if (obj == null) return method.getDefaultValue();

            Class<?> desiredType = method.getReturnType();

            // We parse as a List of Objects, convert into an array.
            if (desiredType.isArray() && obj instanceof List) {
                List<Object> objs = (List<Object>) obj;
                Object array;
                if (desiredType.equals(Object[].class)) {
                    array = new Object[objs.size()];
                } else {
                    array = Array.newInstance(desiredType.getComponentType(), objs.size());
                }
                for (int i = 0; i < objs.size(); i++) {
                    // Whack, auto unboxes for us.
                    Array.set(array, i, objs.get(i));
                }
                // Put it back in the map.
                obj = array;
                members.put(method.getName(), array);
            }

            // Always copy non-zero sized arrays to avoid mutation.
            if (desiredType.isArray() && Array.getLength(obj) != 0) {
                obj = cloneArray(obj);
            }

            return obj;
        }

        private Object cloneArray(Object array) {
            Class<?> type = array.getClass();

            if (type == boolean[].class) return ((boolean[]) array).clone();
            if (type == byte[].class) return ((byte[]) array).clone();
            if (type == char[].class) return ((char[]) array).clone();
            if (type == short[].class) return ((short[]) array).clone();
            if (type == int[].class) return ((int[]) array).clone();
            if (type == float[].class) return ((float[]) array).clone();
            if (type == long[].class) return ((long[]) array).clone();
            if (type == double[].class) return ((double[]) array).clone();
            return ((Object[]) array).clone();
        }
    }
}
