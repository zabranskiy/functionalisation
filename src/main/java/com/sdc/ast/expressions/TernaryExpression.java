package com.sdc.ast.expressions;

public class TernaryExpression extends Expression {
    private final Expression myCondition;
    private final Expression myLeft;
    private final Expression myRight;

    public TernaryExpression(Expression myCondition, Expression myLeft, Expression myRight) {
        this.myCondition = myCondition;
        this.myLeft = myLeft;
        this.myRight = myRight;
    }

    public Expression getLeft() {
        return myLeft;
    }

    public Expression getRight() {
        return myRight;
    }

    public Expression getCondition() {
        return myCondition;
    }

}
