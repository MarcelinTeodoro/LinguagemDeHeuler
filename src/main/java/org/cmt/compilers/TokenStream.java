package main.java.org.cmt.compilers;

import java.util.List;

public class TokenStream {
    private List<Token> tokens;

    public TokenStream(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Token> getTokens() {
        return tokens;
    }
}
