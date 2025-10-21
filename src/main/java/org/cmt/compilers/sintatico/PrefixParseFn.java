package main.java.org.cmt.compilers.sintatico;

import main.java.org.cmt.compilers.sintatico.expressions.Expr;

/**
 * Representa uma função de parsing quando um token aparece em posição prefix
 * (início de expressão). Retorna o nó de expressão correspondente.
 */
@FunctionalInterface
interface PrefixParseFn {
    Expr call();
}
