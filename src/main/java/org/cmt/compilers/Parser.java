/**
 * ---------------------------------------------------------------------------------
 * GUIA DE IMPLEMENTAÇÃO DO PARSER (ANALISADOR SINTÁTICO) - LINGUAGEM HEULER
 * ---------------------------------------------------------------------------------
 *
 * Olá, equipa! Este é o guia para continuarmos o desenvolvimento do nosso Parser.
 *
 * ## 1. Visão Geral
 * A missão desta classe (`Parser`) é receber uma sequência de tokens do `Lexer` e
 * transformá-la em uma estrutura de dados que represente a gramática do nosso
 * código. Essa estrutura é chamada de Árvore Sintática Abstrata (AST - Abstract Syntax Tree).
 *
 * ## 2. Estratégia de Implementação
 * Estamos a seguir a abordagem de um **Parser Descendente Recursivo** escrito manualmente.
 * Esta técnica é didática e nos dá total controle sobre o tratamento de erros.
 *
 * Nossa estratégia é dividida em duas partes, baseada no livro "Crafting Interpreters":
 *
 * a) Para ANÁLISE DE EXPRESSÕES (matemáticas, lógicas, etc.):
 * Usaremos a técnica de **"Top-Down Operator Precedence Parsing"**, também conhecida como
 * **Pratt Parser**. Esta abordagem é extremamente elegante para lidar com a
 * precedência de operadores (+, -, *, /) e associatividade, evitando a complexidade
 * de uma longa cadeia de funções para cada nível de precedência.
 * (Referência: "Crafting Interpreters", Capítulo 6 e 17).
 *
 * b) Para ANÁLISE DE DECLARAÇÕES E COMANDOS (`var`, `if`, `while`, etc.):
 * Usaremos o **Recursive Descent** padrão, onde cada tipo de declaração ou comando
 * terá sua própria função de parsing (ex: `declaration()`, `ifStatement()`, etc.).
 * (Referência: "Crafting Interpreters", Capítulos 8 e 9).
 *
 * ## 3. Gramática da Linguagem (Alvo)
 * Abaixo está a gramática que precisamos implementar, no formato EBNF:
 *
 * programa     → ( declaracao )* ( comando )* EOF ;
 *
 * // Declarações
 * declaracao   → varDecl | ... ;
 * varDecl      → tipo IDENTIFICADOR ";" ;
 * tipo         → "int" | "float" | "bool" ;
 *
 * // Comandos
 * comando      → atribuicao | condicional | repeticao | bloco | exprStmt ;
 * atribuicao   → IDENTIFICADOR "=" expressao ";" ;
 * condicional  → "if" "(" expressao ")" comando ( "else" comando )? ;
 * repeticao    → "while" "(" expressao ")" comando ;
 * bloco        → "{" ( declaracao | comando )* "}" ;
 * exprStmt     → expressao ";" ;
 *
 * // Expressões (com níveis de precedência do Pratt Parser)
 * expressao    → atribuicao ;
 * atribuicao   → IDENTIFICADOR "=" atribuicao | logica_ou ; // Associatividade à direita
 * logica_ou    → logica_e ( "or" logica_e )* ;
 * logica_e     → igualdade ( "and" igualdade )* ;
 * igualdade    → comparacao ( ( "!=" | "==" ) comparacao )* ;
 * comparacao   → termo ( ( ">" | ">=" | "<" | "<=" ) termo )* ;
 * termo        → fator ( ( "-" | "+" ) fator )* ;
 * fator        → unario ( ( "/" | "*" ) unario )* ;
 * unario       → ( "!" | "-" ) unario | primario ;
 * primario     → NUMERO | STRING | "true" | "false" | "nil" | IDENTIFICADOR
 * | "(" expressao ")" ;
 *
 * ## 4. Roadmap de Implementação
 *
 * 1. [FEITO] Estrutura base do Parser com `peek()`, `advance()`, `match()`.
 *
 * 2. [A FAZER] Implementar o Pratt Parser para expressões:
 * - Criar a `ParseRule`, uma estrutura ou classe para armazenar as funções de parsing
 * (prefix e infix) e a precedência de cada token.
 * - Criar a tabela de regras (`rules[]`).
 * - Implementar a função principal `parsePrecedence()`.
 * - Criar as funções de parsing para cada tipo de expressão (ex: `number()`, `grouping()`,
 * `unary()`, `binary()`).
 *
 * 3. [A FAZER] Implementar as funções de parsing para declarações e comandos:
 * - Criar as classes da AST para cada comando (ex: `IfStmt`, `WhileStmt`, `VarDecl`).
 * - Criar os métodos `declaration()` e `statement()`.
 * - Dentro de `statement()`, criar `ifStatement()`, `whileStatement()`, etc.
 *
 * 4. [A FAZER] Melhorar o tratamento de erros com "Panic Mode" (sincronização):
 * - Implementar um método `synchronize()` que avança os tokens até encontrar um
 * ponto seguro para recomeçar o parsing (ex: depois de um `;` ou antes de uma
 * palavra-chave como `if` ou `var`).
 *
 * Lembrem-se de consultar o "Crafting Interpreters" para exemplos de código e explicações
 * detalhadas. Bom trabalho!
 */
package main.java.org.cmt.compilers;

import main.java.org.cmt.compilers.expressions.*;
import java.util.ArrayList;
import java.util.List;
import static main.java.org.cmt.compilers.TokenType.*;

public class Parser {

    private int current;
    private TokenStream tokens;

    public List<Expression> parseTokens(TokenStream tokens) {
        current = 0;
        this.tokens = tokens;

        List<Expression> expressions = new ArrayList<>();

        while (!isAtEnd()) {
            expressions.add(expression());
        }

        return expressions;
    }

    Expression expression() {
        return factor();
    }

    private Expression factor() {
        Expression left = term();

        if(match(Plus, Minus)) {
            Token operator = previous();
            Expression right = factor();
            left = new BinaryExpression(left, operator, right);
        }

        return left;
    }

    private Expression term() {
        Expression left = unary();

        if(match(Star, Slash)) {
            Token operator = previous();
            Expression right = term();
            left = new BinaryExpression(left, operator, right);
        }

        return left;
    }

    private Expression unary() {
        Expression expr = null;

        if(match(Minus)) {
            Token operator = previous();
            expr = new UnaryExpression(operator, unary());
        } else {
            return literal();
        }

        return expr;
    }

    private Expression literal() {
        if(match(STRING, Number)) {
            return new LiteralExpression(previous());
        }

        if(match(LeftParen)) {
            return group();
        }

        throw new RuntimeException("Invalid literal kind: '" + previous().lexeme + "'. At line " + previous().line);
    }

    private Expression group() {
        Expression expr = expression();

        consume(RightParen, "Expect ')' after group expression.");

        return new GroupExpression(expr);
    }

    boolean consume(TokenType type, String message) {
        if(match(type)) {
            return true;
        }

        throw new RuntimeException(message);
    }

    Token previous() {
        return this.tokens.getTokens().get(current - 1);
    }

    boolean match(TokenType... types) {
        for(TokenType t: types) {
            if(peek().type == t) {
                advance();
                return true;
            }
        }

        return false;
    }

    Token peek() {
        return this.tokens.getTokens().get(current);
    }

    Token advance() {
        Token current = peek();
        this.current++;
        return current;
    }

    boolean isAtEnd() {
        return this.current >= this.tokens.getTokens().size() - 1;
    }
}
