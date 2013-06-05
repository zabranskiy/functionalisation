package com.sdc.java;

import java.util.*;

public class Frame {
    private String myStackedVariableType = "";
    private int myStackedVariableIndex = 0;
    private boolean myStackChecked = false;

    private Map<Integer, String> myLocalVariableNames = new HashMap<Integer, String>();
    private Map<Integer, String> myLocalVariableTypes = new HashMap<Integer, String>();
    private List<Integer> myDeclaredVariables = new ArrayList<Integer>();
    private Set<Integer> myLocalVariablesFromDebugInfo = new HashSet<Integer>();

    private Frame myParent = this;
    private List<Frame> myChildren = new ArrayList<Frame>();

    public String getStackedVariableType() {
        return myStackedVariableType;
    }

    public void setStackedVariableType(String stackedVariableType) {
        this.myStackedVariableType = stackedVariableType;
    }

    public void setStackedVariableIndex(int stackedVariableIndex) {
        this.myStackedVariableIndex = stackedVariableIndex;
    }

    public int getStackedVariableIndex() {
        return myStackedVariableIndex;
    }

    public Frame getParent() {
        return myParent;
    }

    public void setParent(final Frame parent) {
        this.myParent = parent;
    }

    public boolean checkStack() {
        if (!myStackChecked && !myStackedVariableType.isEmpty()) {
            myStackChecked = true;
            return true;
        }
        return false;
    }

    public boolean hasStack() {
        return !myStackedVariableType.isEmpty();
    }

    public void addChild(final Frame child) {
        myChildren.add(child);
    }

    public void addLocalVariableName(final int index, final String name) {
        myLocalVariableNames.put(index, name);
    }

    public void addLocalVariableType(final int index, final String type) {
        myLocalVariableTypes.put(index, type);
    }

    public boolean addLocalVariableFromDebugInfo(final int index, final String name, final String type) {
        if (!containsIndex(index)) {
            return false;
        }

        if (myLocalVariablesFromDebugInfo.contains(index)) {
            for (final Frame frame : myChildren) {
                if (frame.addLocalVariableFromDebugInfo(index, name, type)) {
                    return true;
                }
            }
            return false;
        } else {
            myLocalVariablesFromDebugInfo.add(index);
            addLocalVariableName(index, name);
            addLocalVariableType(index, type);
            return true;
        }
    }

    public String getLocalVariableName(final int index) {
        if (containsIndex(index)) {
            if (myDeclaredVariables.contains(index)) {
                return myLocalVariableNames.get(index);
            } else {
                myDeclaredVariables.add(index);
                return myLocalVariableTypes.get(index) + myLocalVariableNames.get(index);
            }
        } else {
            return myParent.getLocalVariableName(index);
        }
    }

    public String getExtendLocalVariableName(final int index) {
        if (containsIndex(index)) {
            myDeclaredVariables.add(index);
            return myLocalVariableTypes.get(index) + myLocalVariableNames.get(index);
        } else {
            return myParent.getExtendLocalVariableName(index);
        }
    }

    public void removeDeclaredVariables() {
        myDeclaredVariables.clear();
    }

    public String getLocalVariableNameOnly(final int index) {
        return myLocalVariableNames.get(index);
    }

    public boolean containsIndex(final int index) {
        return myLocalVariableTypes.containsKey(index);
    }

    public boolean DVcontainIndex(final int index) {
        return myDeclaredVariables.contains(index);
    }
}
