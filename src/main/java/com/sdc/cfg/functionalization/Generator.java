package com.sdc.cfg.functionalization;

import com.sdc.ast.controlflow.Assignment;
import com.sdc.ast.controlflow.Statement;
import com.sdc.ast.expressions.identifiers.Variable;
import com.sdc.cfg.Node;
import com.sdc.cfg.Switch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Generator {
    private final List<Node> myNodes;
    private final AnonymousClass aClass = new AnonymousClass();
//    private List<Variable> myVars = new ArrayList<Variable>();

    public Generator(final List<Node> nodes) {
        this.myNodes = nodes;
    }

    public AnonymousClass genAnonymousClass() {
        final HashSet<Variable> set = new HashSet<Variable>();
        for (Node node : myNodes) {
            final FNode.FType ftype = (node instanceof Switch) ? FNode.FType.SWITCH
                    : (node.getCondition() != null) ? FNode.FType.IF :
                    FNode.FType.SIMPLE;

            ArrayList<Variable> list = new ArrayList<Variable>();
            list.addAll(set);
            final FNode fnode = new FNode(ftype, myNodes.indexOf(node), list);

            fnode.setStatements(new ArrayList<Statement>(node.getStatements()));
            for (Statement statement : node.getStatements()) {
                if ((statement instanceof Assignment) && (((Assignment) statement).getLeft() instanceof Variable)) {
                    final Variable variable = ((Variable) ((Assignment) statement).getLeft());
                    if (variable.isFrameContainIndex()) {
                        boolean fl = true;
                        for (Variable var : set) {
                            if (var.getIndex() == variable.getIndex()) {
                                fl = false;
                            }
                        }
                        if (fl) set.add(variable);
                    }
                }
            }
            for (Node tail : node.getListOfTails()) {
                fnode.addBranch(myNodes.indexOf(tail), set);
            }
            aClass.addFNode(fnode);
        }
        for (Variable var : set) {
            var.removeVariableFromDV();
        }
        return aClass;
    }
}
