package main.java.org.cmt.compilers.expressions;

import main.java.org.cmt.compilers.Token;

public class UnaryExpression extends Expression {
    public Token operator;
    public Expression expression;

    public UnaryExpression(Token operator, Expression expression) {
        this.operator = operator;
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "UnaryExpression{" +
                "operator=" + operator +
                ", expression=" + expression +
                '}';
    }
}
