package main.java.org.cmt.compilers.expressions;




import main.java.org.cmt.compilers.Token;

public class BinaryExpression extends Expression {
    public Expression left;
    public Token operator;
    public Expression right;

    public BinaryExpression(Expression left, Token operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public String toString() {
        return "BinaryExpression{" +
                "left=" + left +
                ", operator=" + operator +
                ", right=" + right +
                '}';
    }
}