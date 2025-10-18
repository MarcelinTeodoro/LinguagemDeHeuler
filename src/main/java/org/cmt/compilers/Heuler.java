/* main: Orquestra a execução, decidindo entre ler um arquivo (runFile)
         ou iniciar um prompt interativo (runPrompt). * runFile: Lê todo o conteúdo de um arquivo e o passa para
          a função run. * runPrompt: Executa o código linha por linha, permitindo uma interação direta com o interpretador.
          Note que hadError é reiniciado a cada linha para que um erro não termine a sessão inteira.

        run: É o coração que conecta as fases. Por enquanto, ela chama o Lexer e imprime os tokens resultantes.

        error e report: Formam o nosso sistema de notificação de erros. report é um auxiliar que formata a mensagem
        e ativa a flag hadError. Ter este sistema centralizado é uma ótima prática de engenharia.*/

// Salve este código como Heuler.java
package main.java.org.cmt.compilers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Heuler {

    // Flag para controlar se ocorreu um erro
    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        runFile("src/main/recursos/teste.txt");//correcao para ficar mais pratico neste ponto
        if (args.length > 1) {
            System.out.println("Uso: jheuler [script]");
            System.exit(64); // Código de erro para uso incorreto da linha de comando [cite: 391]
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Se encontrou um erro, termine com um código de erro.
        if (hadError) System.exit(65);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false; // Resetar o erro no modo interativo
        }
    }

    private static void run(String source) {
        Lexer lexer = new Lexer(); // Vamos conectar o Lexer aqui
        TokenStream tokenStream = lexer.scanTokens(source);
        List<Token> tokens = tokenStream.getTokens();

        // Por enquanto, apenas imprimimos os tokens para verificar
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    // --- Sistema de Notificação de Erros ---

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[linha " + line + "] Erro" + where + ": " + message);
        hadError = true;
    }
}
