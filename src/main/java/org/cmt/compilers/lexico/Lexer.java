package main.java.org.cmt.compilers.lexico;

import main.java.org.cmt.compilers.Heuler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private int start;
    private int end;
    private int line;
    private String source;
    private List<Token> tokens;

    private static Map<String, TokenType> keywords = new HashMap<>();

    static {
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

    public TokenStream scanTokens(String source) {
        this.line = 1;
        this.source = source;

        while (!isAtEnd()) {
            this.start = this.end;
            this.scanToken();
        }

        makeToken(TokenType.EndOfFile);
        return new TokenStream(this.tokens);
    }

    public void scanToken() {
        char c = this.advance();

        switch (c) {
            case ' ':
            case '\t':
            case '\r':
                start = end;
                break;

            case '\n':
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
                string();
                break;
            }

            default: {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Heuler.error(line, "Caractere inválido.");
                }
            }
        }
    }

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

        advance();

        String lexeme = source.substring(start + 1, end - 1);
        makeToken(TokenType.STRING, lexeme, lexeme);
    }

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
