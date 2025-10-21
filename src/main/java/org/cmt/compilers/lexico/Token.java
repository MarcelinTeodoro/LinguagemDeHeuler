package main.java.org.cmt.compilers.lexico;

/**
 * Representa um token produzido pelo analisador léxico.
 *
 * Campos:
 * - type: tipo do token (enum TokenType).
 * - lexeme: a sequência de caracteres do token conforme aparece no código-fonte.
 * - literal: valor literal associado (por exemplo, Double para números ou String para strings).
 * - line: número da linha onde o token foi encontrado (1-based).
 */
public class Token {
    public String lexeme;
    public TokenType type;
    public Object literal;
    public int line;
    /** Coluna (0-based) onde o token começa na linha. */
    public int column;

    public Token(TokenType type, String lexeme, Object literal, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.column = column;
    }

    /**
     * Representação simples útil para depuração: imprime lexema e tipo.
     */
    @Override
    public String toString() {
        return "<" + this.lexeme + ", " + this.type + ">";
    }
}
