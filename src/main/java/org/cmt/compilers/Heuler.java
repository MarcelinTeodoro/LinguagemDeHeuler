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
import main.java.org.cmt.compilers.bytecode.Compiler;
import main.java.org.cmt.compilers.bytecode.VM;
import main.java.org.cmt.compilers.bytecode.Chunk;
import main.java.org.cmt.compilers.sintatico.Resolver;

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
    static VM vm = new VM(); // Crie a VM uma vez


    public static void main(String[] args) throws IOException {

        // Se um caminho de arquivo for fornecido, usa-o; caso contrário usa o recurso de teste padrão.
        if (args != null && args.length > 0) {
            runFile(args[0]);
        } else {
            runFile("src/main/recursos/testeFibo.heuler");
        }
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
        if (hadError) return;

        // Fase 2: Análise Sintática (Parser)
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        if (hadError) return;

        // --- NOVA FASE 3: Análise Semântica (Resolver) ---
        Resolver resolver = new Resolver();
        resolver.resolve(statements);

        // Se o resolver encontrou erros (ex: var a = a;), paramos aqui.
        if (hadError) return;

        // Fase 4: Compilação (AST -> Bytecode)
        Compiler compiler = new Compiler(vm);
        boolean success = compiler.compile(statements);
        if (!success) return;

        // Fase 5: Execução (VM)
        Chunk chunk = compiler.getCompiledChunk();
        vm.interpret(chunk);
    }


    // Sistema de notificação de erros: fornece mensagens com número de linha
    // e lexema (quando disponível). Marca `hadError` para controle externo.
    public static void error(Token token, String message) {
        if (token.type == TokenType.EndOfFile) {
            report(token.line, token.column, " no final", message);
        } else {
            report(token.line, token.column, " em '" + token.lexeme + "'", message);
        }
    }

    public static void error(int line, int column, String message) {
        report(line, column, "", message);
    }

    public static void error(int line, String message) {
        report(line, 0, "", message);
    }

    private static void report(int line, int column, String where, String message) {
        System.err.println("[linha " + line + ":" + column + "] Erro" + where + ": " + message);
        hadError = true;
    }
}