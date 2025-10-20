package main.java.org.cmt.compilers;

import main.java.org.cmt.compilers.expressions.Expr;

// Interface para uma função de parsing infixa (ex: adição '+', multiplicação '*')
@FunctionalInterface
interface InfixParseFn {
    Expr call(Expr left);
}