/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.asm;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.collection.FastStream;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.IntFunction;

import static org.objectweb.asm.Opcodes.V1_8;

/**
 * A very simple builder system around ObjectWeb ASM.
 * <p>
 * Created by covers1624 on 17/9/22.
 */
@Requires ("org.ow2.asm:asm")
public class ClassBuilder {

    private static final Type OBJECT_TYPE = Type.getType(Object.class);

    private int classVersion = V1_8;
    private final int access;
    private final Type name;
    private @Nullable String signature;
    private Type parent = OBJECT_TYPE;
    private final List<Type> interfaces = new LinkedList<>();

    private final List<FieldBuilder> fields = new LinkedList<>();
    private final List<MethodBuilder> methods = new LinkedList<>();

    public ClassBuilder(int access, Type name) {
        this.access = access;
        this.name = name;
    }

    public ClassBuilder withClassVersion(int classVersion) {
        this.classVersion = classVersion;
        return this;
    }

    public ClassBuilder withSignature(String signature) {
        this.signature = signature;
        return this;
    }

    public ClassBuilder withParent(Type parent) {
        this.parent = parent;
        return this;
    }

    public ClassBuilder withInterface(Type iFace) {
        interfaces.add(iFace);
        return this;
    }

    public FieldBuilder addField(int access, String name, Type desc) {
        FieldBuilder field = new FieldBuilder(access, this, name, desc);
        fields.add(field);
        return field;
    }

    public MethodBuilder addMethod(int access, Method method) {
        return addMethod(access, method.getName(), Type.getType(method));
    }

    public MethodBuilder addMethod(int access, String name, Type desc) {
        MethodBuilder method = new MethodBuilder(access, this, name, desc);
        methods.add(method);
        return method;
    }

    /**
     * Creates a method cloned from the given {@link MethodNode}.
     *
     * @param mNode The node to clone.
     * @return The builder.
     */
    @Requires ("org.ow2.asm:asm-tree")
    public MethodBuilder addMethod(MethodNode mNode) {
        MethodBuilder method = new MethodBuilder(mNode.access, this, mNode.name, Type.getMethodType(mNode.desc));
        method.withBodyRaw(mNode::accept);
        methods.add(method);
        return method;
    }

    public byte[] build() {
        return build(ClassWriter::new);
    }

    public byte[] build(IntFunction<ClassWriter> classWriterFunc) {
        ClassWriter cw = classWriterFunc.apply(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        String[] iFaces = FastStream.of(interfaces).map(Type::getInternalName).toArray(new String[0]);
        cw.visit(classVersion, access, name.getInternalName(), signature, parent.getInternalName(), iFaces);

        for (FieldBuilder field : fields) {
            field.build(cw);
        }

        for (MethodBuilder method : methods) {
            method.build(cw);
        }

        cw.visitEnd();
        return cw.toByteArray();
    }

    // @formatter:off
    public int classVersion() { return classVersion; }
    public @Nullable String signature() { return signature; }
    public int access() { return access; }
    public Type name() { return name; }
    public Type parent() { return parent; }
    public List<Type> interfaces() { return Collections.unmodifiableList(interfaces); }
    // @formatter:on

    public static class FieldBuilder {

        private final int access;
        private final ClassBuilder owner;
        private final String name;
        private final Type desc;

        private @Nullable String signature;
        private @Nullable Object value;

        public FieldBuilder(int access, ClassBuilder owner, String name, Type desc) {
            this.access = access;
            this.owner = owner;
            this.name = name;
            this.desc = desc;
        }

        public FieldBuilder withSignature(String signature) {
            this.signature = signature;
            return this;
        }

        public FieldBuilder withValue(Object value) {
            this.value = value;
            return this;
        }

        private void build(ClassVisitor cv) {
            FieldVisitor fv = cv.visitField(access, name, desc.getDescriptor(), signature, value);
            fv.visitEnd();
        }

        // @formatter:off
        public int access() { return access; }
        public ClassBuilder owner() { return owner; }
        public String name() { return name; }
        public Type desc() { return desc; }
        public @Nullable String getSignature() { return signature; }
        public @Nullable Object getValue() { return value; }
        // @formatter:on
    }
}
