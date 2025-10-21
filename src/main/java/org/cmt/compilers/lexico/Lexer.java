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
    private String source;
    private List<Token> tokens;

    /** Mapa de palavras-reservadas -> token type. */
    private static Map<String, TokenType> keywords = new HashMap<>();

    static {
        // Palavras-reservadas atualmente mapeadas.
        keywords.put("let", TokenType.Let);
        keywords.put("def", TokenType.Def);
        keywords.put("if", TokenType.If);
        keywords.put("else", TokenType.Else);
        keywords.put("var",    TokenType.Var);
        keywords.put("print",  TokenType.Print);
        keywords.put("while",  TokenType.While);
        keywords.put("and",  TokenType.And);
        keywords.put("or",  TokenType.Or);
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
        this.source = source;

        while (!isAtEnd()) {
            this.start = this.end; // marca início do próximo lexema
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
        makeToken(TokenType.STRING, lexeme, lexeme);
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
        makeToken(TokenType.Number, lexeme, literal);
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
            makeToken(keywords.get(lexeme));
            return;
        }

        makeToken(TokenType.Identifier, lexeme);
    }

    /* Helpers para criação e manipulação de tokens */
    Token makeToken(TokenType type) {
        String lexeme = this.source.substring(this.start, this.end);
        return this.makeToken(type, lexeme, null);
    }

    Token makeToken(TokenType type, String lexeme) {
        return this.makeToken(type, lexeme, null);
    }

    Token makeToken(TokenType type, String lexeme, Object literal) {
        Token token = new Token(type, lexeme, literal, this.line);
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
        return isAlpha(c) || isDigit(c);
    }

    boolean isAlpha(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
