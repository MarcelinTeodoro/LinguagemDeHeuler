package main.java.org.cmt.compilers.lexico;

/**
 * Enumeração de todos os tipos de tokens que a linguagem reconhece.
 *
 * Os valores incluem:
 * - tokens simples (parênteses, chaves, operadores)
 * - tokens compostos (==, !=, <=, >=)
 * - literais (identificadores, strings, números)
 * - palavras-reservadas (and, or, if, while, etc.)
 * - token de fim de arquivo
 */
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

    // Tipos primitivos
    Int, Float, Bool,

    // Palavras específicas da linguagem
    Def,

    // Fim do arquivo
    EndOfFile
}
