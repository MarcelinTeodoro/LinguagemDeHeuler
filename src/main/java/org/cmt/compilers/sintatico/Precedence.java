package main.java.org.cmt.compilers.sintatico;

/**
 * Níveis de precedência usados pelo Pratt parser. Ordenados do mais baixo
 * (NONE) para o mais alto (PRIMARY). Utilizados para controlar quando a
 * análise deve retornar para níveis superiores.
 */
public enum Precedence {
    NONE,
    ASSIGNMENT,  // =
    OR,          // or
    AND,         // and
    EQUALITY,    // == !=
    COMPARISON,  // < > <= >=
    TERM,        // + -
    FACTOR,      // * /
    UNARY,       // ! -
    CALL,        // . ()
    PRIMARY
}
