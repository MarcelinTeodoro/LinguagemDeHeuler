// Arquivo: ParseRule.java
package main.java.org.cmt.compilers;

import main.java.org.cmt.compilers.expressions.Expr;

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