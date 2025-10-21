package main.java.org.cmt.compilers.sintatico;

import main.java.org.cmt.compilers.sintatico.expressions.Expr;

@FunctionalInterface
interface InfixParseFn {
    Expr call(Expr left);
}
