package main.java.org.cmt.compilers.lexico;

import main.java.org.cmt.compilers.Heuler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scanner / Lexer simples que percorre o código-fonte e produz uma lista de tokens.
 *
 * Estratégia:
 * - Mantém índices `start` e `end` para delimitar o lexema atual.
 * - Avança caractere a caractere, reconhecendo tokens por meio de um switch
 *   e por funções auxiliares (string, number, identifier).
 * - Produz tokens com tipo, lexema, literal (quando aplicável) e número de linha.
 */
public class Lexer {
    private int start;
    private int end;
    private int line;
    /** Coluna atual (0-based) enquanto percorre a linha. */
    private int column;
    /** Coluna onde o lexema atual começou. */
    private int startColumn;
    private String source;
    private List<Token> tokens;

    /** Mapa de palavras-reservadas -> token type. */
    private static Map<String, TokenType> keywords = new HashMap<>();

    static {
        // Palavras-reservadas atualmente mapeadas.

        keywords.put("def", TokenType.Def);
        keywords.put("if", TokenType.If);
        keywords.put("else", TokenType.Else);
        keywords.put("var",    TokenType.Var);
        keywords.put("print",  TokenType.Print);
        keywords.put("while",  TokenType.While);
        keywords.put("and",  TokenType.And);
        keywords.put("or",  TokenType.Or);
        // Adiciona palavras-reservadas presentes no TokenType/gramática
        keywords.put("for", TokenType.For);
        keywords.put("true", TokenType.True);
        keywords.put("false", TokenType.False);
        keywords.put("nil", TokenType.Nil);
        keywords.put("return", TokenType.Return);
        keywords.put("class", TokenType.Class);
        keywords.put("fun", TokenType.Fun);
        keywords.put("super", TokenType.Super);
        keywords.put("this", TokenType.This);
    // Tipos primitivos e palavras auxiliares
    keywords.put("int", TokenType.Int);
    keywords.put("float", TokenType.Float);
    keywords.put("bool", TokenType.Bool);
    keywords.put("in", TokenType.Identifier); // 'in' treated as identifier or reserved in grammar; keep as Identifier for now
    }

    public Lexer() {
        this.tokens = new ArrayList<>();
    }

    /**
     * Ponto de entrada do scanner. Retorna um TokenStream com todos os tokens
     * encontrados no texto de entrada.
     */
    public TokenStream scanTokens(String source) {
        this.line = 1;
        this.column = 0;
        this.source = source;

        while (!isAtEnd()) {
            this.start = this.end; // marca início do próximo lexema
            this.startColumn = this.column;
            this.scanToken();
        }

        // Ao final, adiciona token de EOF
        makeToken(TokenType.EndOfFile);
        return new TokenStream(this.tokens);
    }

    /**
     * Lê um token a partir da posição atual. Método central com um switch
     * que reconhece tokens simples e delega para funções auxiliares quando
     * necessário (números, strings, identificadores).
     */
    public void scanToken() {
        char c = this.advance();

        switch (c) {
            case ' ':
            case '\t':
            case '\r':
                // espaços em branco são ignorados entre tokens
                start = end;
                break;

            case '\n':
                // nova linha -> incrementar contador de linha
                line++;
                start = end;
                // reinicia coluna ao começar nova linha
                this.column = 0;
                break;

            case ';': {
                makeToken(TokenType.Semicolon);
                break;
            }

            case '.': {
                makeToken(TokenType.Dot);
                break;
            }

            case '{': {
                makeToken(TokenType.LeftBrace);
                break;
            }

            case '}': {
                makeToken(TokenType.RightBrace);
                break;
            }

            case '(':
                makeToken(TokenType.LeftParen);
                break;

            case ')':
                makeToken(TokenType.RightParen);
                break;

            case ',': {
                makeToken(TokenType.Comma);
                break;
            }

            case '-':
                makeToken(TokenType.Minus);
                break;

            case '+':
                makeToken(TokenType.Plus);
                break;

            case '*':
                makeToken(TokenType.Star);
                break;

            case '/':
                // comentário de linha: consome até fim da linha
                if (peek() == '/') {
                    while (!isAtEnd() && peek() != '\n') {
                        advance();
                    }
                } else {
                    makeToken(TokenType.Slash);
                }
                break;

            case '!':
                makeToken(match('=') ? TokenType.BangEqual : TokenType.Bang);
                break;
            case '=':
                makeToken(match('=') ? TokenType.EqualEqual : TokenType.Equal);
                break;
            case '<':
                makeToken(match('=') ? TokenType.LessEqual : TokenType.Less);
                break;
            case '>':
                makeToken(match('=') ? TokenType.GreaterEqual : TokenType.Greater);
                break;

            case '"': {
                // string literal
                string();
                break;
            }

            default: {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    // caractere desconhecido -> reporta erro léxico
                    Heuler.error(line, "Caractere inválido.");
                }
            }
        }
    }

    /**
     * Lê uma string até a próxima aspa. Atualiza linha se encontrar quebras de
     * linha no interior da string. Ao encontrar erro (EOF antes da '"') reporta.
     */
    private void string() {
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') {
                line++;
                this.column = 0;
            }
            advance();
        }

        if (isAtEnd()) {
            Heuler.error(line, "String não terminada.");
            return;
        }

        // consome a aspa final
        advance();

        String lexeme = source.substring(start + 1, end - 1);
        makeToken(TokenType.STRING, lexeme, lexeme, this.startColumn);
    }

    /**
     * Lê um número (inteiro ou com ponto decimal). Converte para Double e cria
     * o token correspondente.
     */
    private void number() {
        while (!isAtEnd() && isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance();

            while (!isAtEnd() && isDigit(peek())) {
                advance();
            }
        }

        String lexeme = source.substring(this.start, this.end);
        Object literal = Double.parseDouble(lexeme);
        makeToken(TokenType.Number, lexeme, literal, this.startColumn);
    }

    /**
     * Lê um identificador ou palavra-chave. Se for palavra-reservada, usa o
     * tipo correspondente do mapa `keywords`, caso contrário gera IDENTIFIER.
     */
    void identifier() {
        while (!isAtEnd() && isAlphanumeric(peek())) {
            advance();
        }

        String lexeme = this.source.substring(start, end);

        if (keywords.containsKey(lexeme)) {
            makeToken(keywords.get(lexeme), lexeme, null, this.startColumn);
            return;
        }
        makeToken(TokenType.Identifier, lexeme, null, this.startColumn);
    }

    /* Helpers para criação e manipulação de tokens */
    Token makeToken(TokenType type) {
        String lexeme = this.source.substring(this.start, this.end);
        return this.makeToken(type, lexeme, null, this.startColumn);
    }

    Token makeToken(TokenType type, String lexeme) {
        return this.makeToken(type, lexeme, null, this.startColumn);
    }

    Token makeToken(TokenType type, String lexeme, Object literal, int column) {
        Token token = new Token(type, lexeme, literal, this.line, column);
        this.tokens.add(token);
        return token;
    }

    /* Funções utilitárias de leitura de caracteres */
    boolean isAtEnd() {
        return this.end >= source.length();
    }

    char advance() {
        char c = this.peek();
        this.end++;

        // atualiza coluna: considera que advance consome exatamente um caractere
        this.column++;
        return c;
    }
    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(end) != expected) {
            return false;
        }

        end++;
        return true;
    }

    char peek() {
        if(isAtEnd()) return '\0';
        return this.source.charAt(this.end);
    }

    private char peekNext() {
        if (end + 1 >= source.length()) return '\0';
        return source.charAt(end + 1);
    }

    boolean isAlphanumeric(char c) {
        return isAlpha(c) || isDigit(c) || c == '_';
    }

    boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
