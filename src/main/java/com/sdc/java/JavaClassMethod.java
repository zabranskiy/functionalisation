package com.sdc.java;

import JavaClassPrinter.JavaClassPrinterPackage;

import com.sdc.abstractLangauge.AbstractClassMethod;
import com.sdc.ast.controlflow.Statement;
import com.sdc.cfg.GraphDrawer;
import com.sdc.cfg.Node;
import com.sdc.cfg.functionalization.AnonymousClass;
import com.sdc.cfg.functionalization.FNode;
import pretty.PrettyPackage;

import java.util.ArrayList;
import java.util.List;

public class JavaClassMethod extends AbstractClassMethod {
    private final String myModifier;
    private final String myReturnType;
    private final String myName;
    private final String[] myExceptions;

    private List<String> myImports = new ArrayList<String>();

    private int myLastLocalVariableIndex;

    private final Frame myRootFrame = new Frame();
    private Frame myCurrentFrame = myRootFrame;

    private List<Statement> myBody = null;
    private List<Node> myNodes = null;
    private AnonymousClass aClass;

    private final int myTextWidth;
    private final int myNestSize;

    public String getModifier() {
        return myModifier;
    }

    public String getReturnType() {
        return myReturnType;
    }

    public String getName() {
        return myName;
    }

    public String[] getExceptions() {
        return myExceptions;
    }

    public List<String> getImports() {
        return myImports;
    }

    public int getLastLocalVariableIndex() {
        return myLastLocalVariableIndex;
    }

    public int getNestSize() {
        return myNestSize;
    }

    public List<Statement> getBody() {
        return myBody;
    }

    public void setBody(final List<Statement> body) {
        this.myBody = body;
    }

    public void setLastLocalVariableIndex(int lastLocalVariableIndex) {
        this.myLastLocalVariableIndex = lastLocalVariableIndex;
    }

    public Frame getCurrentFrame() {
        return myCurrentFrame;
    }

    public void setCurrentFrame(final Frame currentFrame) {
        this.myCurrentFrame = currentFrame;
    }

    public JavaClassMethod(final String modifier, final String returnType, final String name, final String[] exceptions,
                           final int textWidth, final int nestSize) {
        this.myModifier = modifier;
        this.myReturnType = returnType;
        this.myName = name;
        this.myExceptions = exceptions;
        this.myTextWidth = textWidth;
        this.myNestSize = nestSize;
    }

    public void addImport(final String importClassName) {
        myImports.add(importClassName);
    }

    public void addLocalVariableName(final int index, final String name) {
        myCurrentFrame.addLocalVariableName(index, name);
    }

    public void addLocalVariableType(final int index, final String type) {
        myCurrentFrame.addLocalVariableType(index, type);
    }

    public void addLocalVariableFromDebugInfo(final int index, final String name, final String type) {
        myRootFrame.addLocalVariableFromDebugInfo(index, name, type);
    }

    public List<String> getParameters() {
        List<String> parameters = new ArrayList<String>();
        for (int variableIndex = 1; variableIndex <= myLastLocalVariableIndex; variableIndex++) {
            if (myRootFrame.containsIndex(variableIndex)) {
                parameters.add(myRootFrame.getLocalVariableName(variableIndex));
            }
        }
        return parameters;
    }

    public List<String> getParametersWithOnlyNames() {
        List<String> parameters = new ArrayList<String>();
        for (int variableIndex = 1; variableIndex <= myLastLocalVariableIndex; variableIndex++) {
            if (myRootFrame.containsIndex(variableIndex)) {
                parameters.add(myRootFrame.getLocalVariableNameOnly(variableIndex));
            }
        }
        return parameters;
    }

    public void setNodes(List<Node> myNodes) {
        this.myNodes = myNodes;
    }

    public List<Node> getNodes() {
        return myNodes;
    }

    public AnonymousClass getAnonymousClass() {
        return aClass;
    }

    public void setAnonymousClass(AnonymousClass aClass) {
        this.aClass = aClass;
    }

    public void drawCFG() {
        GraphDrawer graphDrawer = new GraphDrawer(myNodes, myNestSize, myTextWidth);
//        graphDrawer.draw();
        graphDrawer.simplyDraw();
    }

    @Override
    public String toString() {
        return PrettyPackage.pretty(myTextWidth, JavaClassPrinterPackage.printClassMethod(this));
    }
}
