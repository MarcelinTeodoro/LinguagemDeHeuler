package main.java.org.cmt.compilers;
import main.java.org.cmt.compilers.expressions.Expr;
// Interface para uma função de parsing prefixa (ex: negação '-', literais)
@FunctionalInterface
interface PrefixParseFn {
    Expr call();
}