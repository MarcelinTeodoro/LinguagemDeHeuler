package main.java.org.cmt.compilers.expressions;


import main.java.org.cmt.compilers.Token;

public class LiteralExpression extends Expression {
    public Token literal;

    public LiteralExpression(Token literal) {
        this.literal = literal;
    }

    @Override
    public String toString() {
        return "LiteralExpression{" +
                "literal=" + literal +
                '}';
    }
}