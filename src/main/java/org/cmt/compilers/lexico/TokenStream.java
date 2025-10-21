package main.java.org.cmt.compilers.lexico;

import java.util.List;

/**
 * Wrapper simples para a lista de tokens produzida pelo Lexer.
 * Mantém a coleção para que outras partes do compilador (parser, impressor)
 * possam consumi-la de forma explícita.
 */
public class TokenStream {
    private List<Token> tokens;

    public TokenStream(List<Token> tokens) {
        this.tokens = tokens;
    }

    /** Retorna a lista interna de tokens. */
    public List<Token> getTokens() {
        return tokens;
    }
}
