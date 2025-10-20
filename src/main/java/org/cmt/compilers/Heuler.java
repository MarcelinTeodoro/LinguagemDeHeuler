// Arquivo: Heuler.java
package main.java.org.cmt.compilers;

import main.java.org.cmt.compilers.expressions.Expr;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Heuler {

    static boolean hadError = false;

    public static void main(String[] args) throws IOException {

        runFile("src/main/recursos/teste2.txt");
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
    }

    private static void run(String source) {
        // Fase 1: Análise Léxica (Scanner)
        Lexer lexer = new Lexer();
        TokenStream tokenStream = lexer.scanTokens(source);
        List<Token> tokens = tokenStream.getTokens();
        if (hadError) return;

        // Fase 2: Análise Sintática (Parser)
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        if (hadError) return;

        // Fase 3: Visualização da AST
        System.out.println(new AstPrinter().print(statements));
    }

    // Sistema de notificação de erros...
    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[linha " + line + "] Erro" + where + ": " + message);
        hadError = true;
    }
}