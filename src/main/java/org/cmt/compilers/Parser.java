package main.java.org.cmt.compilers;

import main.java.org.cmt.compilers.expressions.*;
import java.util.ArrayList;
import java.util.List;
import static main.java.org.cmt.compilers.TokenType.*;

public class Parser {

    private int current;
    private TokenStream tokens;

    public List<Expression> parseTokens(TokenStream tokens) {
        current = 0;
        this.tokens = tokens;

        List<Expression> expressions = new ArrayList<>();

        while (!isAtEnd()) {
            expressions.add(expression());
        }

        return expressions;
    }

    Expression expression() {
        return factor();
    }

    private Expression factor() {
        Expression left = term();

        if(match(Plus, Minus)) {
            Token operator = previous();
            Expression right = factor();
            left = new BinaryExpression(left, operator, right);
        }

        return left;
    }

    private Expression term() {
        Expression left = unary();

        if(match(Star, Slash)) {
            Token operator = previous();
            Expression right = term();
            left = new BinaryExpression(left, operator, right);
        }

        return left;
    }

    private Expression unary() {
        Expression expr = null;

        if(match(Minus)) {
            Token operator = previous();
            expr = new UnaryExpression(operator, unary());
        } else {
            return literal();
        }

        return expr;
    }

    private Expression literal() {
        if(match(STRING, Number)) {
            return new LiteralExpression(previous());
        }

        if(match(LeftParen)) {
            return group();
        }

        throw new RuntimeException("Invalid literal kind: '" + previous().lexeme + "'. At line " + previous().line);
    }

    private Expression group() {
        Expression expr = expression();

        consume(RightParen, "Expect ')' after group expression.");

        return new GroupExpression(expr);
    }

    boolean consume(TokenType type, String message) {
        if(match(type)) {
            return true;
        }

        throw new RuntimeException(message);
    }

    Token previous() {
        return this.tokens.getTokens().get(current - 1);
    }

    boolean match(TokenType... types) {
        for(TokenType t: types) {
            if(peek().type == t) {
                advance();
                return true;
            }
        }

        return false;
    }

    Token peek() {
        return this.tokens.getTokens().get(current);
    }

    Token advance() {
        Token current = peek();
        this.current++;
        return current;
    }

    boolean isAtEnd() {
        return this.current >= this.tokens.getTokens().size() - 1;
    }
}
