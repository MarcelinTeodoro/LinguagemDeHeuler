package main.java.org.cmt.compilers.sintatico;

import main.java.org.cmt.compilers.sintatico.expressions.Expr;

@FunctionalInterface
interface PrefixParseFn {
    Expr call();
}
