package org.shcarr;

import java.io.IOException;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

public class CustomClassWriter {

    AddFieldAdapter addFieldAdapter;
    AddInterfaceAdapter addInterfaceAdapter;
    PublicizeMethodAdapter pubMethAdapter;
    final static String CLASSNAME = "java.lang.Integer";
    final static String CLONEABLE = "java/lang/Cloneable";
    // adding a cloneable interface to integer
    static String className = "java.lang.Integer";
    static String cloneableInterface = "java/lang/Cloneable";
    ClassReader reader;
    ClassWriter writer;

    public CustomClassWriter() throws IOException {
        reader = new ClassReader(className);
        writer = new ClassWriter(reader, 0);
    }

    // aNewBooleanField, we'll make it public.
    public byte[] addField() {
        addFieldAdapter = new AddFieldAdapter(
                "aNewBooleanField",
                ACC_PUBLIC,
                writer);
        reader.accept(addFieldAdapter, 0);
        return writer.toByteArray();
    }

    // will add a field and a method

    // this is the field "visitor"
    public class AddFieldAdapter extends ClassVisitor {
        private final String fieldName;
        private String fieldDefault;
        private int access = ACC_PUBLIC;
        private boolean isFieldPresent;

        public AddFieldAdapter(
                String fieldName, int fieldAccess, ClassVisitor cv) {
            super(ASM4, cv);
            this.cv = cv;
            this.fieldName = fieldName;
            this.access = fieldAccess;
        }

        @Override
        public FieldVisitor visitField(
                int access, String name, String desc, String signature, Object value) {
            if (name.equals(fieldName)) {
                isFieldPresent = true;
            }
            return cv.visitField(access, name, desc, signature, value);
        }

        @Override
        public void visitEnd() {
            if (!isFieldPresent) {
                FieldVisitor fv = cv.visitField(
                        access, fieldName, fieldDefault, null, null);
                if (fv != null) {
                    fv.visitEnd();
                }
            }
            cv.visitEnd();
        }
    }

    public byte[] publicizeMethod() {
        pubMethAdapter = new PublicizeMethodAdapter(writer);
        reader.accept(pubMethAdapter, 0);
        return writer.toByteArray();
    }


    public class AddInterfaceAdapter extends ClassVisitor {

        public AddInterfaceAdapter(ClassVisitor cv) {
            super(ASM4, cv);
        }

        @Override
        public void visit(
                int version,
                int access,
                String name,
                String signature,
                String superName, String[] interfaces) {
            String[] holding = new String[interfaces.length + 1];
            holding[holding.length - 1] = cloneableInterface;
            System.arraycopy(interfaces, 0, holding, 0, interfaces.length);
            cv.visit(V1_8, access, name, signature, superName, holding);
        }
    }

    public class PublicizeMethodAdapter extends ClassVisitor {
        public PublicizeMethodAdapter(int api, ClassVisitor cv) {
            super(ASM4, cv);
            this.cv = cv;
        }
        public MethodVisitor visitMethod(
                int access,
                String name,
                String desc,
                String signature,
                String[] exceptions) {
            if (name.equals("toUnsignedString0")) {
                return cv.visitMethod(
                        ACC_PUBLIC + ACC_STATIC,
                        name,
                        desc,
                        signature,
                        exceptions);
            }
            return cv.visitMethod(
                    access, name, desc, signature, exceptions);
        }
    }
}
