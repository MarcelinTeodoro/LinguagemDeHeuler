// Arquivo: Heuler.java
package main.java.org.cmt.compilers;

import main.java.org.cmt.compilers.lexico.Lexer;
import main.java.org.cmt.compilers.lexico.TokenStream;
import main.java.org.cmt.compilers.lexico.Token;
import main.java.org.cmt.compilers.lexico.TokenType;
import main.java.org.cmt.compilers.sintatico.Parser;
import main.java.org.cmt.compilers.sintatico.Stmt;
import main.java.org.cmt.compilers.AstPrinter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Ponto de entrada do programa. Coordena as três fases simples demonstradas
 * neste projeto:
 * 1) Análise léxica (lexer/scan)
 * 2) Análise sintática (parser -> AST)
 * 3) Impressão/visualização da AST (AstPrinter)
 *
 * Também contém um mecanismo simples de report de erros (linha + mensagem).
 */
public class Heuler {

    static boolean hadError = false;

    public static void main(String[] args) throws IOException {

        // Para depuração, o runner padrão lê o arquivo de recursos de teste.
        runFile("src/main/recursos/teste3.txt");
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
    }

    /**
     * Fluxo principal: tokeniza, parseia e imprime a AST.
     */
    private static void run(String source) {
        // Fase 1: Análise Léxica (Scanner)
        Lexer lexer = new Lexer();
        TokenStream tokenStream = lexer.scanTokens(source);
        List<Token> tokens = tokenStream.getTokens();

        // Exibe tabela de tokens (útil para debugging)
        System.out.println("--- Tabela de Tokens ---");
        for (Token token : tokens) {
            // Imprime linha, tipo e lexema de forma legível
            System.out.printf("Linha %-4d | %-15s | Lexema: '%s'\n",
                    token.line,
                    token.type,
                    token.lexeme);
            if (token.type == TokenType.EndOfFile) {
                break;
            }
        }
        System.out.println("------------------------\n");

        // Fase 2: Análise Sintática (Parser)
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Fase 3: Visualização da AST
        System.out.println("--- Árvore Sintática Gerada ---");
        System.out.println(new AstPrinter().print(statements));
    }

    // Sistema de notificação de erros: fornece mensagens com número de linha
    // e lexema (quando disponível). Marca `hadError` para controle externo.
    public static void error(Token token, String message) {
        if (token.type == TokenType.EndOfFile) {
            report(token.line, " no final", message);
        } else {
            report(token.line, " em '" + token.lexeme + "'", message);
        }
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[linha " + line + "] Erro" + where + ": " + message);
        hadError = true;
    }
}