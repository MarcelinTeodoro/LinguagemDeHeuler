package main.java.org.cmt.compilers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private int start;
    private int end;
    private char current;
    private int line;
    private String source;
    private List<Token> tokens;

    private static Map<String, TokenType> keywords = new HashMap<String, TokenType>();

    static {
        keywords.put("let", TokenType.Let);
        keywords.put("def", TokenType.Def);
        keywords.put("if", TokenType.If);
        keywords.put("else", TokenType.Else);
    }

    public Lexer() {
        this.tokens = new ArrayList<Token>();
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
            case '=': {
                makeToken(TokenType.Equal);
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

            case '(': {
                makeToken(TokenType.LeftParen);
                break;
            }

            case ')': {
                makeToken(TokenType.RightParen);
                break;
            }

            case '-': {
                makeToken(TokenType.Minus);
                break;
            }

            case '+': {
                makeToken(TokenType.Plus);
                break;
            }

            case '*': {
                makeToken(TokenType.Star);
                break;
            }

            case '/': {
                makeToken(TokenType.Slash);
                break;
            }

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
                    throw new RuntimeException("Invalid character '" + c + "', at line " + line + ".");
                }
            }
        }
    }

    private void string() {
        while(!isAtEnd() && peek() != '"') {
            if(peek() == '\n') {
                line++;
            }
            advance();
        }

        if(peek() != '"') {
            throw new RuntimeException("Unterminated string at line " + line);
        }

        advance();

        String lexeme = source.substring(start + 1, end - 1);
        makeToken(TokenType.STRING, lexeme, lexeme);
    }

    private void number() {
        while (!isAtEnd() && isDigit(peek()))
            advance();

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
        Token token = new Token(type, lexeme, literal, line);
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

    char peek() {
        if(isAtEnd()) return '\0';
        return this.source.charAt(this.end);
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
