
// Arquivo: Parser.java (versão final corrigida com atribuição)
package main.java.org.cmt.compilers;

import main.java.org.cmt.compilers.expressions.*;
        import java.util.ArrayList;
import java.util.List;
import java.util.EnumMap;
import static main.java.org.cmt.compilers.TokenType.*;

public class Parser {

    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;
    private final EnumMap<TokenType, ParseRule> rules;
    private boolean panicMode = false;


    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.rules = new EnumMap<>(TokenType.class);
        initializeRules();
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }


    // --- PONTO DE ENTRADA PARA EXPRESSÕES ---
    private Expr expression() {
        return assignment(); // A atribuição é o nível mais baixo de precedência
    }

    // --- LÓGICA DE PARSING ---

    private Stmt declaration() {
        try {
            if (match(Var)) return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null; // Retorna null para o laço principal saber que houve um erro.
        }
    }

    private Stmt statement() {
        if (match(Print)) return printStatement();
        if (match(LeftBrace)) return new Stmt.Block(block());
        if (match(If)) return ifStatement();
        if (match(While)) return whileStatement();
        return expressionStatement();
    }

    // NOVO MÉTODO DE ATRIBUIÇÃO
    private Expr assignment() {
        // Analisa o lado esquerdo da expressão.
        // Ele pode ser um simples nome de variável ou algo mais complexo.
        Expr expr = parsePrecedence(Precedence.OR); // Analisa uma expressão com precedência a partir de 'OR'

        if (match(Equal)) {
            Token equals = previous();
            // Chamada recursiva a 'assignment()' para garantir a associatividade à direita.
            Expr value = assignment();

            // Verifica se o lado esquerdo é um alvo válido para atribuição.
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            // Se não for, é um erro de sintaxe.
            error(equals, "Alvo de atribuição inválido.");
        }

        return expr;
    }

    private Stmt varDeclaration() {
        Token name = consume(Identifier, "Esperava um nome de variável.");
        Expr initializer = null;
        if (match(Equal)) {
            initializer = expression();
        }
        consume(Semicolon, "Esperava ';' depois da declaração da variável.");
        return new Stmt.Var(name, initializer);
    }
    // Para o comando de impressão: print expressao;
    private Stmt printStatement() {
        Expr value = expression();
        consume(Semicolon, "Esperava ';' depois do valor.");
        return new Stmt.Print(value);
    }
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
    // ... Seus outros métodos de statement (printStatement, ifStatement, etc.) estão corretos ...
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(Semicolon, "Esperava ';' depois da expressão.");
        return new Stmt.Expression(expr);
    }

    // Para um bloco de código: { ... }
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RightBrace) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RightBrace, "Esperava '}' para fechar o bloco.");
        return statements;
    }



    // --- PRATT PARSER E FUNÇÕES DE EXPRESSÃO ---

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
    // Métodos de parsing (por enquanto, podem ser esqueletos)
    private Expr number()     {
        double value = Double.parseDouble(previous().lexeme);
        return new Expr.Literal(value);
    }

    private Expr variable() {
        // A ser implementado: analisa uma variável.
        return new Expr.Variable(previous());
    }

    private Expr string() {
        // Analisa um literal de string.
        return new Expr.Literal(previous().literal);
    }

    private Expr literal() {
        switch (previous().type) {
            case True: return new Expr.Literal(true);
            case False: return new Expr.Literal(false);
            case Nil: return new Expr.Literal(null); // Usando null do Java para representar nil
            case STRING:
                // O valor literal já foi extraído pelo Lexer.
                // Se não, você precisaria extraí-lo aqui.
                return new Expr.Literal(previous().literal);
            default:
                // Inalcançável, mas bom para robustez
                return null;
        }
    }

    private Expr grouping()   {
        // Os parênteses de abertura já foram consumidos.
        // Agora, analisamos a expressão dentro deles.
        Expr expression = expression();

        // Exigimos que o próximo token seja um parêntese de fechamento.
        consume(RightParen, "Esperava ')' depois da expressão.");

        return new Expr.Grouping(expression);
    }
    private Expr unary()     {
        Token operator = previous();

        // Analisa o operando (a expressão à direita do operador).
        // Usamos a precedência UNARY para lidar com casos como "!!true".
        Expr right = parsePrecedence(Precedence.UNARY);

        return new Expr.Unary(operator, right);
    }
    private Expr binary(Expr left) {
        // O operador já foi consumido.
        Token operator = previous();

        // Pega a regra do operador para saber sua precedência.
        ParseRule rule = getRule(operator.type);

        // Analisa o operando da direita. A precedência passada é um nível acima
        // para lidar corretamente com a associatividade à esquerda.
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

    private void initializeRules() {
        // Adicione esta linha para evitar NullPointerException em tokens não mapeados
        rules.put(EndOfFile, new ParseRule(null, null, Precedence.NONE));

        // Parênteses e Símbolos
        rules.put(LeftParen,  new ParseRule(this::grouping, null, Precedence.NONE)); // 'call' seria a função infixa
        rules.put(RightParen, new ParseRule(null, null, Precedence.NONE));
        rules.put(LeftBrace,  new ParseRule(null, null, Precedence.NONE));
        rules.put(RightBrace, new ParseRule(null, null, Precedence.NONE));
        rules.put(Comma,      new ParseRule(null, null, Precedence.NONE));
        rules.put(Dot,        new ParseRule(null, null, Precedence.NONE)); // 'dot' seria a função infixa
        rules.put(Semicolon,  new ParseRule(null, null, Precedence.NONE));
        rules.put(Equal,      new ParseRule(null, null, Precedence.NONE)); // Tratado manualmente em 'assignment'

        // Operadores
        rules.put(Minus, new ParseRule(this::unary, this::binary, Precedence.TERM));
        rules.put(Plus,  new ParseRule(null, this::binary, Precedence.TERM));
        rules.put(Star,  new ParseRule(null, this::binary, Precedence.FACTOR));
        rules.put(Slash, new ParseRule(null, this::binary, Precedence.FACTOR));
        rules.put(And, new ParseRule(null, this::logical, Precedence.AND));
        rules.put(Or,  new ParseRule(null, this::logical, Precedence.OR));

        // Operador Unário
        rules.put(Bang, new ParseRule(this::unary, null, Precedence.NONE));

        // Operadores de Comparação e Igualdade
        rules.put(BangEqual,    new ParseRule(null, this::binary, Precedence.EQUALITY));
        rules.put(EqualEqual,   new ParseRule(null, this::binary, Precedence.EQUALITY));
        rules.put(Greater,      new ParseRule(null, this::binary, Precedence.COMPARISON));
        rules.put(GreaterEqual, new ParseRule(null, this::binary, Precedence.COMPARISON));
        rules.put(Less,         new ParseRule(null, this::binary, Precedence.COMPARISON));
        rules.put(LessEqual,    new ParseRule(null, this::binary, Precedence.COMPARISON));



        // Literais e Variáveis
        rules.put(Identifier, new ParseRule(this::variable, null, Precedence.NONE));
        rules.put(STRING,     new ParseRule(this::string,   null, Precedence.NONE));
        rules.put(Number,     new ParseRule(this::number,   null, Precedence.NONE));
        rules.put(True,       new ParseRule(this::literal,  null, Precedence.NONE));
        rules.put(False,      new ParseRule(this::literal,  null, Precedence.NONE));
        rules.put(Nil,        new ParseRule(this::literal,  null, Precedence.NONE));

    }

    private ParseRule getRule(TokenType type) { return rules.get(type); }
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    // MÉTODO 'consume' CORRIGIDO
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        if (!panicMode) {  // Evita repetir a mesma mensagem
            Heuler.error(token, message);
            panicMode = true; // Entra em modo de pânico
        }
        return new ParseError();
    }
    private void synchronize() {
        panicMode = false; // Sai do modo de pânico

        while (!isAtEnd()) {
            if (previous().type == Semicolon) return;

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

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous(); // Retorna o token anterior (consumido)
    }

    private boolean isAtEnd() { return peek().type == EndOfFile; }
    private Token peek() { return tokens.get(current); }
    private Token previous() { return tokens.get(current - 1); }
}
