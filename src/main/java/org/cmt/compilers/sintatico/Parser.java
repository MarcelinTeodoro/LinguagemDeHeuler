package main.java.org.cmt.compilers.sintatico;

import main.java.org.cmt.compilers.lexico.Token;
import main.java.org.cmt.compilers.lexico.TokenType;
import main.java.org.cmt.compilers.sintatico.expressions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.EnumMap;
import static main.java.org.cmt.compilers.lexico.TokenType.*;

/**
 * Parser implementado no estilo Pratt para expressões, com parsing manual para
 * declarações e comandos. Produz uma lista de `Stmt` (programa) a partir da
 * lista de tokens fornecida pelo lexer.
 *
 * Estrutura principal:
 * - tabela `rules` (ParseRule) para resolver prefix/infix por precedência
 * - métodos top-level para declarações/comandos (declaration, statement)
 * - método `parsePrecedence` para análise de expressões por precedência
 */
public class Parser {

    /** Exceção interna usada para controle de fluxo em erros de parsing. */
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;
    private final EnumMap<TokenType, ParseRule> rules;
    private boolean panicMode = false; // usado para evitar mensagens de erro repetidas


    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.rules = new EnumMap<>(TokenType.class);
        initializeRules();
    }

    /**
     * Entry point: percorre os tokens e constrói a lista de declarações/stmt.
     */
    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    // --- EXPRESSÕES (nível de expressão) ---

    /** Inicia o parsing de uma expressão (atribuições têm a menor precedência). */
    private Expr expression() {
        return assignment(); // A atribuição é o nível mais baixo de precedência
    }

    // --- DECLARAÇÕES E COMANDOS ---

    /**
     * Parsing de declaração. Em caso de erro, sincroniza e continua.
     */
    private Stmt declaration() {
        try {
            if (match(Var)) return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null; // Retorna null para o laço principal saber que houve um erro.
        }
    }

    /** Dispatch para diferentes tipos de statements. */
    private Stmt statement() {
        if (match(Print)) return printStatement();
        if (match(LeftBrace)) return new Stmt.Block(block());
        if (match(If)) return ifStatement();
        if (match(While)) return whileStatement();
        if (match(For)) return forStatement();
        return expressionStatement();
    }

    /**
     * Trata atribuições com associatividade à direita: a = b = 3
     */
    private Expr assignment() {
        Expr expr = parsePrecedence(Precedence.OR);

        if (match(Equal)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Alvo de atribuição inválido.");
        }

        return expr;
    }

    /** Parse de declaração `var` (var nome = expr;). */
    private Stmt varDeclaration() {
        // Suporta sintaxe opcional de anotação de tipo: var <type> name = expr;
        Token typeToken = null;
        if (check(TokenType.Int) || check(TokenType.Float) || check(TokenType.Bool)) {
            typeToken = advance();
        }
        Token name = consume(Identifier, "Esperava um nome de variável.");
        Expr initializer = null;
        if (match(Equal)) {
            initializer = expression();
        }
        consume(Semicolon, "Esperava ';' depois da declaração da variável.");
        return new Stmt.Var(name, typeToken, initializer);
    }

    /** Parse de for: for identifier in expr scope */
    private Stmt forStatement() {
        Token iterator = consume(Identifier, "Esperava identificador depois de 'for'.");
        // aceita a palavra-chave 'in' como identificador ou token - verificamos lexema
        if (!check(Identifier) || !peek().lexeme.equals("in")) {
            error(peek(), "Esperava 'in' depois do identificador do for.");
        }
        // consome 'in'
        advance();
        Expr iterable = expression();
        Stmt body = statement();
        return new Stmt.For(iterator, iterable, body);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(Semicolon, "Esperava ';' depois do valor.");
        return new Stmt.Print(value);
    }

    /** if (cond) thenBranch [else elseBranch] */
    private Stmt ifStatement() {
        consume(LeftParen, "Esperava '(' depois de 'if'.");
        Expr condition = expression();
        consume(RightParen, "Esperava ')' depois da condição do if.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(Else)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        consume(LeftParen, "Esperava '(' depois de 'while'.");
        Expr condition = expression();
        consume(RightParen, "Esperava ')' depois da condição do while.");
        Stmt body = statement();

        return new Stmt.While(body, condition);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(Semicolon, "Esperava ';' depois da expressão.");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RightBrace) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RightBrace, "Esperava '}' para fechar o bloco.");
        return statements;
    }

    // --- PRATT: parsing de expressões por precedência ---

    /**
     * Núcleo do Pratt parser: consome uma função prefix e depois aplica as
     * regras infix enquanto a precedência permitir.
     */
    private Expr parsePrecedence(Precedence precedence) {
        advance();
        PrefixParseFn prefixRule = getRule(previous().type).prefix;
        if (prefixRule == null) {
            throw error(previous(), "Esperava uma expressão.");
        }

        Expr left = prefixRule.call();

        while (!isAtEnd()) {
            ParseRule rule = getRule(peek().type);
            if (rule == null || rule.infix == null || precedence.ordinal() > rule.precedence.ordinal()) {
                break;
            }

            advance();
            InfixParseFn infixRule = rule.infix;
            left = infixRule.call(left);
        }

        return left;
    }

    // Funções construtoras de nós de expressão (numeros, strings, variáveis, agrupamento, etc.)
    private Expr number()     {
        double value = Double.parseDouble(previous().lexeme);
        return new Expr.Literal(value);
    }

    private Expr variable() {
        return new Expr.Variable(previous());
    }

    private Expr string() {
        return new Expr.Literal(previous().literal);
    }

    private Expr literal() {
        switch (previous().type) {
            case True: return new Expr.Literal(true);
            case False: return new Expr.Literal(false);
            case Nil: return new Expr.Literal(null);
            case STRING:
                return new Expr.Literal(previous().literal);
            default:
                return null;
        }
    }

    private Expr grouping()   {
        Expr expression = expression();
        consume(RightParen, "Esperava ')' depois da expressão.");
        return new Expr.Grouping(expression);
    }
    private Expr unary()     {
        Token operator = previous();
        Expr right = parsePrecedence(Precedence.UNARY);
        return new Expr.Unary(operator, right);
    }
    private Expr binary(Expr left) {
        Token operator = previous();
        ParseRule rule = getRule(operator.type);
        Expr right = parsePrecedence(Precedence.values()[rule.precedence.ordinal() + 1]);
        return new Expr.Binary(left, operator, right);
    }
    private Expr logical(Expr left) {
        Token operator = previous();
        ParseRule rule = getRule(operator.type);
        Expr right = parsePrecedence(Precedence.values()[rule.precedence.ordinal() + 1]);
        return new Expr.Logical(left, operator, right);
    }

    // --- TABELA DE REGRAS E MÉTODOS AUXILIARES ---

    /** Inicializa a tabela de ParseRule que mapeia tokens para funções prefix/infix. */
    private void initializeRules() {
        rules.put(TokenType.EndOfFile, new ParseRule(null, null, Precedence.NONE));

        rules.put(TokenType.LeftParen,  new ParseRule(this::grouping, null, Precedence.NONE));
        rules.put(TokenType.RightParen, new ParseRule(null, null, Precedence.NONE));
        rules.put(TokenType.LeftBrace,  new ParseRule(null, null, Precedence.NONE));
        rules.put(TokenType.RightBrace, new ParseRule(null, null, Precedence.NONE));
        rules.put(TokenType.Comma,      new ParseRule(null, null, Precedence.NONE));
        rules.put(TokenType.Dot,        new ParseRule(null, null, Precedence.NONE));
        rules.put(TokenType.Semicolon,  new ParseRule(null, null, Precedence.NONE));
        rules.put(TokenType.Equal,      new ParseRule(null, null, Precedence.NONE));

        rules.put(TokenType.Minus, new ParseRule(this::unary, this::binary, Precedence.TERM));
        rules.put(TokenType.Plus,  new ParseRule(null, this::binary, Precedence.TERM));
        rules.put(TokenType.Star,  new ParseRule(null, this::binary, Precedence.FACTOR));
        rules.put(TokenType.Slash, new ParseRule(null, this::binary, Precedence.FACTOR));
        rules.put(TokenType.And, new ParseRule(null, this::logical, Precedence.AND));
        rules.put(TokenType.Or,  new ParseRule(null, this::logical, Precedence.OR));

        rules.put(TokenType.Bang, new ParseRule(this::unary, null, Precedence.NONE));

        rules.put(TokenType.BangEqual,    new ParseRule(null, this::binary, Precedence.EQUALITY));
        rules.put(TokenType.EqualEqual,   new ParseRule(null, this::binary, Precedence.EQUALITY));
        rules.put(TokenType.Greater,      new ParseRule(null, this::binary, Precedence.COMPARISON));
        rules.put(TokenType.GreaterEqual, new ParseRule(null, this::binary, Precedence.COMPARISON));
        rules.put(TokenType.Less,         new ParseRule(null, this::binary, Precedence.COMPARISON));
        rules.put(TokenType.LessEqual,    new ParseRule(null, this::binary, Precedence.COMPARISON));

        rules.put(TokenType.Identifier, new ParseRule(this::variable, null, Precedence.NONE));
        rules.put(TokenType.STRING,     new ParseRule(this::string,   null, Precedence.NONE));
        rules.put(TokenType.Number,     new ParseRule(this::number,   null, Precedence.NONE));
        rules.put(TokenType.True,       new ParseRule(this::literal,  null, Precedence.NONE));
        rules.put(TokenType.False,      new ParseRule(this::literal,  null, Precedence.NONE));
        rules.put(TokenType.Nil,        new ParseRule(this::literal,  null, Precedence.NONE));

    }

    private ParseRule getRule(TokenType type) { return rules.get(type); }

    /** Verifica se o token atual tem o tipo informado (sem consumi-lo). */
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    /** Consome um token do tipo esperado ou lança erro com a mensagem informada. */
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    /** Reporta erro de parsing formatado e entra em modo de pânico para evitar ruído. */
    private ParseError error(Token token, String message) {
        if (!panicMode) {  
            main.java.org.cmt.compilers.Heuler.error(token, message);
            panicMode = true; 
        }
        return new ParseError();
    }

    /** Tenta recuperar do erro atual avançando até um ponto seguro (synchronize). */
    private void synchronize() {
        panicMode = false; 

        while (!isAtEnd()) {
            if (previous().type == TokenType.Semicolon) return;

            switch (peek().type) {
                case Class:
                case Fun:
                case Var:
                case For:
                case If:
                case While:
                case Print:
                case Return:
                    return;
            }

            advance();
        }
    }


    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    /** Consome e retorna o token atual, movendo o cursor adiante. */
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous(); 
    }

    private boolean isAtEnd() { return peek().type == TokenType.EndOfFile; }
    private Token peek() { return tokens.get(current); }
    private Token previous() { return tokens.get(current - 1); }
}
