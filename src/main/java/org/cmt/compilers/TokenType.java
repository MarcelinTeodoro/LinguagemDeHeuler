package main.java.org.cmt.compilers;
// Arquivo: TokenType.java (versão completa)
public enum TokenType {
    // Tokens de um único caractere
    LeftParen, RightParen, LeftBrace, RightBrace,
    Comma, Dot, Minus, Plus, Semicolon, Slash, Star,

    // Tokens de um ou dois caracteres
    Bang, BangEqual,
    Equal, EqualEqual,
    Greater, GreaterEqual,
    Less, LessEqual,

    // Literais
    Identifier, STRING, Number,

    // Palavras-reservadas
    And, Class, Else, False, Fun, For, If, Nil, Or,
    Print, Return, Super, This, True, Var, While,

    // Suas palavras-reservadas
    Def, Let,

    // Fim do arquivo
    EndOfFile
}