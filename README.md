
# Linguagem De Heuler — Documentação

Este repositório contém uma implementação didática de componentes iniciais de um compilador/interpreter para a linguagem "Heuler". O projeto fornece implementações do analisador léxico (lexer), analisador sintático (parser) e estruturas de dados para representar a Árvore de Sintaxe Abstrata (AST).

## Estrutura do projeto

- `src/main/java/main/java/org/cmt/compilers/` (código-fonte Java)
  - `Heuler.java` — ponto de entrada que lê um arquivo de entrada, executa o lexer e o parser, e imprime a representação da AST.
  - `AstPrinter.java` — visitante que converte a AST em uma representação textual.
  - `lexico/` — pacote que contém os arquivos relacionados ao analisador léxico:
    - `Lexer.java`
    - `Token.java`
    - `TokenType.java`
    - `TokenStream.java`
  - `sintatico/` — pacote que contém os arquivos relacionados ao analisador sintático:
    - `Parser.java`
    - `ParseRule.java`
    - `Precedence.java`
    - `PrefixParseFn.java`
    - `InfixParseFn.java`
    - `expressions/Expr.java` — hierarquia de nós de expressão
    - `Stmt.java` — hierarquia de instruções/comandos

## Como executar (Windows)

Requisitos: JDK 8 ou superior.

Exemplo mínimo (prompt do Windows):

```bat
cd "d:\Área de Trabalho\Trabalhos escola\Facul\6º Período\Compiladores\LinguagemDeHeuler"
javac -d out -sourcepath src src\main\java\main\java\org\cmt\compilers\Heuler.java
java -cp out main.java.org.cmt.compilers.Heuler
```

Observação: ajuste o comando `javac`/`java` caso a estrutura de packages seja alterada.

## Arquivos de recurso

- `src/main/recursos/` contém arquivos de exemplo e a gramática (EBNF).

## Componentes principais (visão geral)

- Lexer (`lexico`): tokeniza o texto de entrada, produzindo uma sequência de tokens com tipo, lexema, literal e número de linha.
- Parser (`sintatico`): consome a sequência de tokens e constrói nós de expressão (`Expr`) e instruções (`Stmt`) formando a AST.
- AST e impressão: `AstPrinter` implementa visitors para `Expr` e `Stmt` e converte a AST para uma representação textual legível.

## Observações de uso

- O ponto de entrada padrão em `Heuler.main` lê um arquivo na pasta `src/main/recursos/`.
- O projeto está organizado por pacotes (lexical/sintático) para separar responsabilidades.
