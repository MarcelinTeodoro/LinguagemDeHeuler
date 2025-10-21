package main.java.org.cmt.compilers.sintatico;

import main.java.org.cmt.compilers.sintatico.expressions.Expr;

/**
 * Representa uma função de parsing quando um token aparece em posição infix
 * (após uma expressão). Recebe o nó à esquerda e retorna o nó combinado.
 */
@FunctionalInterface
interface InfixParseFn {
    Expr call(Expr left);
}
