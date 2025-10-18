package main.java.org.cmt.compilers;

public enum TokenType {
    Def, LeftParen, RightParen, Identifier, EndOfFile, If, Else, LeftBrace, RightBrace, Equal, Let, Number, STRING, Minus, Plus, Star, Slash, Semicolon,
    Bang, BangEqual, // Adicionado para ! e !=
    EqualEqual,     // Adicionado para ==
    Less, LessEqual,   // Adicionado para < e <=
    Greater, GreaterEqual, // Adicionado para > e >=
    Point
}