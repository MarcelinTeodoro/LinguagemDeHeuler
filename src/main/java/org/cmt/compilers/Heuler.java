package main.java.org.cmt.compilers;



import main.java.org.cmt.compilers.expressions.Expression;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Heuler {
    public static void main(String[] args) {
        String sourceFilepath = "src/main/recursos/teste.txt";

        String source = readFile(sourceFilepath);
        Lexer lexer = new Lexer();
        TokenStream tokenStream = lexer.scanTokens(source);

        Parser parser = new Parser();
        List<Expression> expressions = parser.parseTokens(tokenStream);

        for(Expression expr : expressions) {
            System.out.println(expr.toString());
        }
    }

    static String readFile(String filepath) {
        StringBuilder content = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            String line = "";

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }

        return content.toString();
    }
}
