package com.sdc.cfg.functionalization;

import com.sdc.ast.controlflow.Return;
import com.sdc.ast.controlflow.Statement;
import com.sdc.ast.expressions.Expression;
import com.sdc.ast.expressions.Invocation;
import com.sdc.ast.expressions.identifiers.Variable;
import com.sdc.cfg.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FNode extends Node {
    private final List<Statement> myBranches = new ArrayList<Statement>();
    private List<Variable> myArguments = new ArrayList<Variable>();
    private final FType ftype;
    private final int index;

    public enum FType {
        SWITCH, IF, SIMPLE
    }

    public FNode(final FType ftype, final int index, final ArrayList<Variable> list) {
        this.index = index;
        this.ftype = ftype;
        this.myArguments = list;
    }

    public void addBranch(final int index, HashSet<Variable> set) {
        List list = new ArrayList();
        list.addAll(set);
        myBranches.add(new Return(new Invocation((index == 0) ? "start" : "fnode_" + index, list)));
    }

    public FType getType() {
        return ftype;
    }

    public int getIndex() {
        return index;
    }

    public List<Statement> getBranches() {
        return myBranches;
    }

    public List<Variable> getArguments() {
        return myArguments;
    }
}