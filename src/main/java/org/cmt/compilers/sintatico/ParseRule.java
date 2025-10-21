package main.java.org.cmt.compilers.sintatico;

import main.java.org.cmt.compilers.sintatico.expressions.Expr;

/**
 * Associação de um TokenType a sua função prefix e infix e ao nível de precedência.
 * Usado pelo Pratt parser para decidir como interpretar um token quando ele
 * aparece em posição prefix (início de expressão) ou infix (após uma expressão).
 */
class ParseRule {
    final PrefixParseFn prefix;
    final InfixParseFn infix;
    final Precedence precedence;

    ParseRule(PrefixParseFn prefix, InfixParseFn infix, Precedence precedence) {
        this.prefix = prefix;
        this.infix = infix;
        this.precedence = precedence;
    }
}
