/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.asm;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.asm.ClassBuilder.FieldBuilder;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by covers1624 on 10/11/22.
 */
@Requires ("org.ow2.asm:asm")
public final class MethodBuilder {

    private final int access;
    private final Type owner;
    private final String name;
    private final Type desc;
    @Nullable
    private String signature;
    private final List<String> exceptions = new LinkedList<>();

    private @Nullable ClassBuilder ownerBuilder;

    @Nullable
    private Consumer<BodyGenerator> bodyGenerator;

    MethodBuilder(int access, ClassBuilder owner, String name, Type desc) {
        this.access = access;
        this.owner = owner.name();
        this.name = name;
        this.desc = desc;

        ownerBuilder = owner;
    }

    private MethodBuilder(int access, Type owner, String name, Type desc) {
        this.access = access;
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

    public static MethodBuilder of(int access, Type owner, String name, Type desc) {
        return new MethodBuilder(access, owner, name, desc);
    }

    public MethodBuilder withSignature(String signature) {
        this.signature = signature;
        return this;
    }

    public MethodBuilder withException(String exception) {
        exceptions.add(exception);
        return this;
    }

    public MethodBuilder withBody(Consumer<BodyGenerator> func) {
        if (bodyGenerator != null) throw new RuntimeException("Unable to add more than one body generator.");

        bodyGenerator = func;
        return this;
    }

    public MethodVisitor build(ClassVisitor cv) {
        if ((access & Opcodes.ACC_ABSTRACT) == 0 && bodyGenerator == null) {
            throw new IllegalStateException("Attempted to generate a non-abstract method without a body.");
        }

        MethodVisitor mv = cv.visitMethod(
                access,
                name,
                desc.getDescriptor(),
                signature,
                exceptions.toArray(new String[0])
        );
        if (bodyGenerator != null) {
            BodyGenerator bodyGen = new BodyGenerator(mv, access, owner, desc);
            mv.visitCode();
            bodyGenerator.accept(bodyGen);
            mv.visitMaxs(-1, -1);
        }
        mv.visitEnd();
        return mv;
    }

    // @formatter:off
    public int access() { return access; }
    public Type owner() { return owner; }
    public String name() { return name; }
    public Type desc() { return desc; }
    public @Nullable String signature() { return signature; }
    public List<String> exceptions() { return Collections.unmodifiableList(exceptions); }
    // @formatter:on

    public static class BodyGenerator {

        private final MethodVisitor mv;
        private final Type desc;

        @Nullable
        private final Var thisVar;
        private final Var[] params;
        private final BitSet usedVars = new BitSet();

        public BodyGenerator(MethodVisitor mv, int access, Type owner, Type desc) {
            this.mv = mv;
            this.desc = desc;

            thisVar = (access & ACC_STATIC) == 0 ? pushVar(owner, false) : null;
            Type[] types = desc.getArgumentTypes();
            params = new Var[types.length];
            for (int i = 0; i < types.length; i++) {
                params[i] = pushVar(types[i], false);
            }
        }

        private Var pushVar(Type type, boolean canFree) {
            int index = 0;
            while (true) {
                index = usedVars.nextClearBit(index);
                if (type.getSize() != 2) break;
                if (usedVars.nextClearBit(index + 1) - 1 == index) {
                    break;
                }
            }
            usedVars.set(index, index + type.getSize());
            return new Var(this, index, type, canFree);
        }

        private void popVar(Var var) {
            usedVars.clear(var.index, var.index + var.type.getSize());
        }

        public Var getThis() {
            if (thisVar == null) throw new UnsupportedOperationException("Static methods don't have 'this'.");

            return thisVar;
        }

        public int numParams() {
            return params.length;
        }

        public Var param(int index) {
            return params[index];
        }

        public Var newVar(Type type) {
            return pushVar(type, true);
        }

        public void ret() {
            insn(desc.getReturnType().getOpcode(IRETURN));
        }

        public void insn(int opcode) {
            mv.visitInsn(opcode);
        }

        public void intInsn(int opcode, int operand) {
            mv.visitIntInsn(opcode, operand);
        }

        public void loadThis() {
            load(getThis());
        }

        public void loadParam(int index) {
            load(param(index));
        }

        public void load(Var var) {
            varInsn(var.type.getOpcode(ILOAD), var.getIndex());
        }

        public void storeParam(int index) {
            store(param(index));
        }

        public void store(Var var) {
            varInsn(var.type.getOpcode(ISTORE), var.getIndex());
        }

        public void varInsn(int opcode, int var) {
            mv.visitVarInsn(opcode, var);
        }

        public void typeInsn(int opcode, Type type) {
            assert type.getSort() == Type.OBJECT;

            mv.visitTypeInsn(opcode, type.getInternalName());
        }

        public void getField(FieldBuilder field) {
            fieldInsn((field.access() & ACC_STATIC) != 0 ? GETSTATIC : GETFIELD, field);
        }

        public void putField(FieldBuilder field) {
            fieldInsn((field.access() & ACC_STATIC) != 0 ? PUTSTATIC : PUTFIELD, field);
        }
        public void fieldInsn(int opcode, FieldBuilder field) {
            fieldInsn(opcode, field.owner().name(), field.name(), field.desc());
        }

        public void getField(Field field) {
            fieldInsn(Modifier.isStatic(field.getModifiers()) ? GETSTATIC : GETFIELD, field);
        }

        public void putField(Field field) {
            fieldInsn(Modifier.isStatic(field.getModifiers()) ? PUTSTATIC : PUTFIELD, field);
        }

        public void fieldInsn(int opcode, Field field) {
            fieldInsn(opcode, Type.getType(field.getDeclaringClass()), field.getName(), Type.getType(field.getType()));
        }

        @Requires ("org.ow2.asm:asm-tree")
        public void getField(Type owner, FieldNode fDesc) {
            fieldInsn((fDesc.access & ACC_STATIC) != 0 ? GETSTATIC : GETFIELD, owner, fDesc);
        }

        @Requires ("org.ow2.asm:asm-tree")
        public void putField(Type owner, FieldNode fDesc) {
            fieldInsn((fDesc.access & ACC_STATIC) != 0 ? PUTSTATIC : PUTFIELD, owner, fDesc);
        }

        @Requires ("org.ow2.asm:asm-tree")
        public void fieldInsn(int opcode, Type owner, FieldNode fNode) {
            fieldInsn(opcode, owner, fNode.name, Type.getType(fNode.desc));
        }

        public void fieldInsn(int opcode, Type owner, String name, Type descriptor) {
            assert owner.getSort() == Type.OBJECT;

            mv.visitFieldInsn(opcode, owner.getInternalName(), name, descriptor.getDescriptor());
        }

        public void methodInsn(Method method) {
            int opcode;
            // TODO, ctor/super calls
            if (Modifier.isStatic(method.getModifiers())) {
                opcode = INVOKESTATIC;
            } else if (Modifier.isInterface(method.getDeclaringClass().getModifiers())) {
                opcode = INVOKEINTERFACE;
            } else {
                opcode = INVOKEVIRTUAL;
            }
            methodInsn(opcode, method);
        }

        public void methodInsn(int opcode, Method method) {
            methodInsn(opcode, Type.getType(method.getDeclaringClass()), method.getName(), Type.getType(method), Modifier.isInterface(method.getDeclaringClass().getModifiers()));
        }

        public void methodInsn(int opcode, MethodBuilder method) {
            methodInsn(opcode, method, method.ownerBuilder != null && (method.ownerBuilder.access() & ACC_INTERFACE) != 0);
        }

        public void methodInsn(int opcode, MethodBuilder method, boolean isInterface) {
            methodInsn(opcode, method.owner, method.name, method.desc, isInterface);
        }

        public void methodInsn(int opcode, Type owner, String name, Type descriptor, boolean isInterface) {
            assert owner.getSort() == Type.OBJECT;

            mv.visitMethodInsn(opcode, owner.getInternalName(), name, descriptor.getDescriptor(), isInterface);
        }

        public void invokeDynamic(String name, Type descriptor, Handle boostrapMethod, Object... bsmArgs) {
            mv.visitInvokeDynamicInsn(name, descriptor.getDescriptor(), boostrapMethod, bsmArgs);
        }

        public void jump(int opcode, Label label) {
            mv.visitJumpInsn(opcode, label);
        }

        public void label(Label label) {
            mv.visitLabel(label);
        }

        public void ldcInt(int i) {
            ldc(i);
        }

        public void ldcFloat(float f) {
            ldc(f);
        }

        public void ldcLong(long l) {
            ldc(l);
        }

        public void ldcDouble(double d) {
            ldc(d);
        }

        public void ldcString(String str) {
            ldc(str);
        }

        public void ldcClass(Type type) {
            assert type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY;

            ldc(type);
        }

        public void ldc(Object obj) {
            mv.visitLdcInsn(obj);
        }

        public void iinc(int var, int increment) {
            mv.visitIincInsn(var, increment);
        }

        public void tableSwitch(int min, int max, Label default_, Label... labels) {
            mv.visitTableSwitchInsn(min, max, default_, labels);
        }

        public void lookupSwitch(Label default_, int[] keys, Label[] labels) {
            mv.visitLookupSwitchInsn(default_, keys, labels);
        }

        public void multiNewArray(Type descriptor, int numDimensions) {
            mv.visitMultiANewArrayInsn(descriptor.getDescriptor(), numDimensions);
        }

        public void tryCatchBlock(Label start, Label end, Label handler, @Nullable Type type) {
            mv.visitTryCatchBlock(start, end, handler, type != null ? type.getDescriptor() : null);
        }

        public static class Var {

            private final BodyGenerator gen;
            private final int index;
            private final Type type;
            private final boolean canFree;
            private boolean freed;

            public Var(BodyGenerator gen, int index, Type type, boolean canFree) {
                this.gen = gen;
                this.index = index;
                this.type = type;
                this.canFree = canFree;
            }

            public int getIndex() {
                if (freed) throw new IllegalStateException("Use after free.");

                return index;
            }

            public void free() {
                if (!canFree) throw new UnsupportedOperationException("Variable cannot be freed.");

                gen.popVar(this);
                freed = true;
            }
        }
    }
}
