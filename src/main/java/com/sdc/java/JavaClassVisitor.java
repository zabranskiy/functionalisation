package com.sdc.java;

import com.sdc.abstractLangauge.AbstractClassVisitor;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ASM4;

public class JavaClassVisitor extends AbstractClassVisitor {
    private JavaClass myDecompiledJavaClass = null;
    private final int myTextWidth;
    private final int myNestSize;

    public JavaClassVisitor(final int textWidth, final int nestSize) {
        super(ASM4);
        this.myTextWidth = textWidth;
        this.myNestSize = nestSize;
    }

    @Override
    public void visit(final int version, final int access, final String name
            , final String signature,final  String superName, final String[] interfaces) {
        final String modifier = getAccess(access & ~Opcodes.ACC_SUPER);
        String type = "";

        if ((access & Opcodes.ACC_ENUM) == 0
                && (access & Opcodes.ACC_INTERFACE) == 0
                && (access & Opcodes.ACC_ANNOTATION) == 0)
        {
            type = "class ";
        }

        final String className = getClassName(name);

        final String[] classParts = name.split("/");
        StringBuilder packageName = new StringBuilder("");
        for (int i = 0; i < classParts.length - 2; i++) {
            packageName.append(classParts[i]).append(".");
        }
        packageName.append(classParts[classParts.length - 2]);

        String superClass = "";
        String superClassImport = "";
        if (superName != null && !"java/lang/Object".equals(superName)) {
            superClass = getClassName(superName);
            superClassImport = getDecompiledFullClassName(superName);
        }

        List<String> implementedInterfaces = new ArrayList<String>();
        List<String> implementedInterfacesImports = new ArrayList<String>();
        if (interfaces != null && interfaces.length > 0) {
            for (final String implInterface : interfaces) {
                implementedInterfaces.add(getClassName(implInterface));
                implementedInterfacesImports.add(getDecompiledFullClassName(implInterface));
            }
        }

        myDecompiledJavaClass = new JavaClass(modifier, type, className, packageName.toString(), implementedInterfaces
                , superClass, myTextWidth, myNestSize);

        if (!superClassImport.isEmpty()) {
            myDecompiledJavaClass.appendImport(superClassImport);
        }

        myDecompiledJavaClass.appendImports(implementedInterfacesImports);
    }

    @Override
    public void visitSource(final String source, final String debug) {
    }

    @Override
    public void visitOuterClass(final String owner, final String name, final String desc) {
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        return null;
    }

    @Override
    public void visitAttribute(final Attribute attr) {
    }

    @Override
    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature
            , final Object value) {
        final JavaClassField cf = new JavaClassField(getAccess(access)
                , getDescriptor(desc, 0), name, myTextWidth, myNestSize);
        myDecompiledJavaClass.appendField(cf);
        return null;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc
            , final String signature, final String[] exceptions) {
        if (name.equals("<init>")) {
            return null;
        }

        final String modifier = getAccess(access);
        final int returnTypeIndex = desc.indexOf(')') + 1;
        final String returnType = getDescriptor(desc, returnTypeIndex);

        List<String> throwedExceptions = new ArrayList<String>();
        if (exceptions != null) {
            for (final String exception: exceptions) {
                throwedExceptions.add(getClassName(exception));
            }
        }

        final JavaClassMethod javaClassMethod = new JavaClassMethod(modifier, returnType, name
                , throwedExceptions.toArray(new String[throwedExceptions.size()])
                , myTextWidth, myNestSize);

        addInformationAboutParameters(desc.substring(1, desc.indexOf(')')), javaClassMethod);

        myDecompiledJavaClass.appendMethod(javaClassMethod);

        return new JavaMethodVisitor(javaClassMethod
                , myDecompiledJavaClass.getPackage() + "." + myDecompiledJavaClass.getName());
    }

    @Override
    public void visitEnd() {
        for (final JavaClassMethod method: myDecompiledJavaClass.getMethods()) {
            myDecompiledJavaClass.appendImports(method.getImports());
        }

        System.out.println(myDecompiledJavaClass.toString());
    }

    private String getAccess(final int access) {
        StringBuilder sb = new StringBuilder("");

        if ((access & Opcodes.ACC_PUBLIC) != 0) {
            sb.append("public ");
        }
        if ((access & Opcodes.ACC_PRIVATE) != 0) {
            sb.append("private ");
        }
        if ((access & Opcodes.ACC_PROTECTED) != 0) {
            sb.append("protected ");
        }
        if ((access & Opcodes.ACC_FINAL) != 0) {
            sb.append("final ");
        }
        if ((access & Opcodes.ACC_STATIC) != 0) {
            sb.append("static ");
        }
        if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) {
            sb.append("synchronized ");
        }
        if ((access & Opcodes.ACC_VOLATILE) != 0) {
            sb.append("volatile ");
        }
        if ((access & Opcodes.ACC_TRANSIENT) != 0) {
            sb.append("transient ");
        }
        if ((access & Opcodes.ACC_ABSTRACT) != 0) {
            sb.append("abstract ");
        }
        if ((access & Opcodes.ACC_STRICT) != 0) {
            sb.append("strictfp ");
        }
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
            sb.append("synthetic ");
        }
        if ((access & Opcodes.ACC_ENUM) != 0) {
            sb.append("enum ");
        }

        return sb.toString();
    }

    private String getDescriptor(final String descriptor, final int pos) {
        switch (descriptor.charAt(pos)) {
            case 'V':
                return "void ";
            case 'B':
                return "byte ";
            case 'J':
                return "long ";
            case 'Z':
                return "boolean ";
            case 'I':
                return "int ";
            case 'S':
                return "short ";
            case 'C':
                return "char ";
            case 'F':
                return "float ";
            case 'D':
                return "double ";
            //case "L":
            default:
                final String className = descriptor.substring(pos + 1, descriptor.indexOf(";", pos));
                myDecompiledJavaClass.appendImport(getDecompiledFullClassName(className));
                return getClassName(className) + " ";
        }
    }

    private void addInformationAboutParameters(final String descriptor, final JavaClassMethod javaClassMethod) {
        int count = 0;
        int pos = 0;

        while (pos < descriptor.length()) {
            final int backupPos = pos;

            switch (descriptor.charAt(pos)) {
                case 'B':
                    count++;
                    pos++;
                    break;
                case 'J':
                    count += 2;
                    pos++;
                    break;
                case 'Z':
                    count++;
                    pos++;
                    break;
                case 'I':
                    count++;
                    pos++;
                    break;
                case 'S':
                    count++;
                    pos++;
                    break;
                case 'C':
                    count++;
                    pos++;
                    break;
                case 'F':
                    count++;
                    pos++;
                    break;
                case 'D':
                    count += 2;
                    pos++;
                    break;
                case 'L':
                    count++;
                    pos = descriptor.indexOf(";", pos) + 1;
                    break;
            }

            javaClassMethod.addLocalVariableName(count, "x" + count);
            javaClassMethod.addLocalVariableType(count, getDescriptor(descriptor, backupPos));
        }

        javaClassMethod.setLastLocalVariableIndex(count);
    }

    private String getClassName(final String fullClassName) {
        final String[] classParts = fullClassName.split("/");

        return classParts[classParts.length - 1];
    }

    private String getDecompiledFullClassName(final String fullClassName) {
        return fullClassName.replace("/", ".");
    }
}