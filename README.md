
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

## Lacunas em relação aos requisitos da atividade (resumo claro e plano de ação)

Abaixo estão todas as diferenças entre o que a atividade pede (conforme as imagens fornecidas) e o que o código atualmente implementa. Cada item está descrito de forma simples, por que importa, e o que pode ser feito (passos concretos) para corrigir ou completar.

1) Léxico — tokens e palavras-reservadas incompletos
 - O que falta: o `Lexer` não emite token para vírgula (`,`), embora `TokenType` contenha `Comma`. Além disso, várias palavras-reservadas presentes em `TokenType` (por exemplo: `for`, `return`, `class`, `fun`, `true`, `false`, `nil`) não foram adicionadas ao mapa `keywords` do `Lexer`.
 - Por que importa: o parser pode esperar tokens/keywords que o lexer não produz; isso causa falhas ao analisar código que use `for`, `true`/`false`, `nil`, etc.
 - Plano de ação prático:
   1. Adicionar tratamento de `,` em `Lexer.scanToken()` emitindo `TokenType.Comma`.
   2. Completar o mapa `keywords` com todas as palavras do enum `TokenType` que a linguagem deve reconhecer.

2) Léxico — identificadores e caracteres permitidos
 - O que falta: identificadores não aceitam underscore (`_`) — `isAlpha` só considera letras A-Z/a-z.
 - Por que importa: convenções comuns de identificadores (por exemplo `my_var`) são usadas em exemplos e testes; sem `_` o lexer classifica esses identificadores incorretamente.
 - Plano de ação prático:
   1. Alterar `isAlpha` ou `isAlphanumeric` para permitir `_` como caractere inicial e subsequente.

3) Léxico — posição de token incompleta (linha apenas)
 - O que falta: `Token` armazena apenas `line`, não armazena coluna/offset.
 - Por que importa: mensagens de erro mais úteis (linha+coluna) tornam debugging e correção muito mais simples.
 - Plano de ação prático:
   1. Adicionar um campo `column` (ou `startOffset`) em `Token`.
   2. Atualizar `Lexer` para calcular/registrar coluna ao avançar (manter `startColumn` e atualizar em `advance()` e em `\n`).

4) Léxico — tratamento de erros numéricos
 - O que falta: `number()` usa `Double.parseDouble` sem capturar exceções; números mal formatados podem causar crash.
 - Por que importa: entradas mal formatadas devem produzir erro léxico controlado, não exceção não tratada.
 - Plano de ação prático:
   1. Envolver `Double.parseDouble` em try/catch; ao capturar NumberFormatException reportar erro léxico com `Heuler.error(line, ...)` e produzir um token de erro ou pule o lexema.

5) Léxico — comentários de bloco (opcional)
 - O que falta: suporte a `/* ... */` (comentários de bloco) caso isso seja desejado.
 - Por que importa: muitos códigos e testes usam blocos de comentários; sem suporte, o lexer pode interpretar `/*` como `/'` e `*` tokens.
 - Plano de ação prático:
   1. Implementar detecção de `/*` e consumir até `*/`, atualizando `line` para quebras internas.

6) Sintático — declarações tipadas (int/float/bool) não implementadas
 - O que falta: a gramática solicitada pede declarações do tipo `tipo id ;` (ex.: `int x;`) enquanto o parser atual só aceita `var id` (declaration com `Var`).
 - Por que importa: se a atividade exige tipos explícitos, a implementação atual não atende ao enunciado.
 - Plano de ação prático:
   1. Decidir o conjunto de tokens para tipos (`int`, `float`, `bool`) e adicioná-los ao `TokenType` e ao `keywords` do lexer.
   2. Atualizar `Parser.declaration()` para aceitar a forma `Type Identifier ;` além de (ou no lugar de) `var`.

7) Sintático — `for` statement ausente
 - O que falta: `TokenType` e `synchronize()` mencionam `For`, mas não há parsing para `for`.
 - Por que importa: a gramática da atividade costuma incluir `for`/loops como parte das estruturas de controle; não implementá-lo reduz a cobertura do trabalho.
 - Plano de ação prático:
   1. Implementar método `forStatement()` no `Parser` para traduzir a sintaxe `for (init; cond; inc) body` para as construções já existentes (ou implementar nativamente se preferir).

8) Sintático — alinhamento entre `TokenType` e lexer/parser
 - O que falta: discrepâncias entre o enum `TokenType` e os tokens realmente emitidos pelo `Lexer` (por exemplo `Comma` existe no enum mas não é produzido). Também há keywords listadas no enum e não mapeadas.
 - Por que importa: inconsistências dificultam depuração e causam erros de parsing inesperados.
 - Plano de ação prático:
   1. Fazer uma auditoria: gerar a lista de TokenType e assegurar que o Lexer gere cada token necessário.
   2. Remover tokens não usados ou documentar por que estão presentes.

9) Sintático — mensagens de erro e posição
 - O que falta: o parser reporta erros com `Heuler.error(token, message)`, mas `Token` só tem `line` — sem coluna/offset. Mensagens às vezes não incluem o token recebido na mensagem de erro.
 - Por que importa: mensagens claras (token esperado vs token recebido + posição exata) aceleram correções.
 - Plano de ação prático:
   1. Adicionar coluna/offset aos `Token` (ver item 3) e usar essa informação em `Heuler.error(token, message)`.
   2. Padronizar mensagens do parser para sempre incluir token esperado e token recebido quando aplicável.

10) Documentação e exemplos de aceitação/rejeição
 - O que falta: embora exista `gramatica.ebnf` e `teste*.txt`, o repositório não tem um documento único com exemplos claros de código aceito vs rejeitado e instruções para testes automáticos.
 - Por que importa: trabalho em equipe e correção por pares ficam mais fáceis com exemplos concretos e testes.
 - Plano de ação prático:
   1. Criar `examples/accepted/` e `examples/rejected/` com 6–10 amostras cada.
   2. Escrever um README de testes com comandos `javac`/`java` e um script `run_tests.bat` simples que executa Heuler sobre os exemplos e verifica saídas/erros esperados.

Prioridade sugerida (ordem recomendada de implementação)
 1. Alinhar `keywords` e adicionar `Comma` no lexer (corrige discrepâncias básicas).
 2. Permitir underscore `_` em identificadores.
 3. Adicionar coluna/offset em `Token` e propagar para mensagens de erro.
 4. Tratar `NumberFormatException` em `number()`.
 5. Implementar `for`-statement e declarações tipadas (se a gramática exigir).
 6. Adicionar exemplos aceitos/rejeitados e script de teste.

Notas finais rápidas
 - A maior divergência em relação aos requisitos é o modelo de declaração (tipado vs `var`) e a ausência de `for` se a atividade realmente exigir essas formas. O restante são melhorias de robustez e usabilidade (mensagens de erro, posição, cobertura de tokens).
 - Posso começar a implementar esses itens sequencialmente (faça sua escolha da prioridade) e criar PRs pequenos para cada mudança — recomendo começar pelo alinhamento lexer/TokenType e pelo campo coluna no Token.

