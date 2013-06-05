package com.sdc.java;

import JavaClassPrinter.JavaClassPrinterPackage;
import pretty.PrettyPackage;
import com.sdc.abstractLangauge.AbstractClass;

import java.util.ArrayList;
import java.util.List;

public class JavaClass extends AbstractClass {
    private final String myModifier;
    private final String myType;
    private final String myName;
    private final String myPackage;

    private final String mySuperClass;
    private final List<String> myImplementedInterfaces;

    private List<JavaClassField> myFields = new ArrayList<JavaClassField>();
    private List<JavaClassMethod> myMethods = new ArrayList<JavaClassMethod>();

    private List<String> myImports = new ArrayList<String>();

    private final int myTextWidth;
    private final int myNestSize;

    public String getModifier() {
        return myModifier;
    }

    public String getType() {
        return myType;
    }

    public String getName() {
        return myName;
    }

    public String getPackage() {
        return myPackage;
    }

    public List<String> getImports() {
        return myImports;
    }

    public List<String> getImplementedInterfaces() {
        return myImplementedInterfaces;
    }

    public String getSuperClass() {
        return mySuperClass;
    }

    public List<JavaClassField> getFields() {
        return myFields;
    }

    public List<JavaClassMethod> getMethods() {
        return myMethods;
    }

    public int getNestSize() {
        return myNestSize;
    }

    public JavaClass(final String modifier, final String type, final String name, final String packageName,
                     final List<String> implementedInterfaces, final String superClass,
                     final int textWidth, final int nestSize) {
        this.myModifier = modifier;
        this.myType = type;
        this.myName = name;
        this.myPackage = packageName;
        this.myImplementedInterfaces = implementedInterfaces;
        this.mySuperClass = superClass;
        this.myTextWidth = textWidth;
        this.myNestSize = nestSize;
    }

    public void appendField(final JavaClassField field) {
        myFields.add(field);
    }

    public void appendMethod(final JavaClassMethod method) {
        myMethods.add(method);
    }

    public void appendImports(final List<String> imports) {
        for (final String importName : imports) {
            appendImport(importName);
        }
    }

    public void appendImport(final String importName) {
        if (!myImports.contains(importName)
                && (importName.indexOf(myPackage) != 0 || importName.lastIndexOf(".") != myPackage.length()))
        {
            myImports.add(importName);
        }
    }

    @Override
    public String toString() {
        return PrettyPackage.pretty(myTextWidth, JavaClassPrinterPackage.printJavaClass(this));
    }
}
