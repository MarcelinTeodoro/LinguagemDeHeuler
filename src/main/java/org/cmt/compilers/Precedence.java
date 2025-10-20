// Arquivo: Precedence.java
package main.java.org.cmt.compilers;

// Define os níveis de precedência, do mais baixo para o mais alto.
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