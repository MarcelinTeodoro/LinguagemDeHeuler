package main.java.org.cmt.compilers.bytecode;

/**
 * Define os códigos de operação (Opcodes) que a nossa VM executará.
 * Cada instrução terá um byte que a representa.
 */
public enum OpCode {
    // --- Opcodes Essenciais ---
    OP_RETURN,     // Retorna de uma função (ou termina o script)
    OP_CONSTANT,   // Carrega uma constante (ex: número, string) na pilha

    // --- Opcodes Aritméticos ---
    OP_NEGATE,     // Inverte o sinal de um número (ex: -5)
    OP_ADD,        // Soma os dois valores no topo da pilha
    OP_SUBTRACT,   // Subtrai os dois valores no topo da pilha
    OP_MULTIPLY,   // Multiplica os dois valores no topo da pilha
    OP_DIVIDE,     // Divide os dois valores no topo da pilha

    // --- Manipulação de Pilha e Saída ---
    OP_PRINT,      // Imprime o valor no topo da pilha
    OP_POP,        // Desempilha/Descarta o valor do topo (usado após expressões sem efeito colateral)
    OP_NIL,        // Empilha o valor literal 'nil' (nulo)

    // --- Variáveis ---
    OP_DEFINE_GLOBAL, // Cria uma nova variável global
    OP_GET_GLOBAL,    // Lê o valor de uma variável global
    OP_SET_GLOBAL,    // Atualiza o valor de uma variável global
    OP_GET_LOCAL,     // Lê da pilha num índice específico (variável local)
    OP_SET_LOCAL,     // Escreve na pilha num índice específico (variável local)

    // --- CONTROLE DE FLUXO ---
    OP_JUMP_IF_FALSE, // Salta para a frente se o topo da pilha for falso (if/while)
    OP_JUMP,          // Salta para a frente incondicionalmente (usado no else)
    OP_LOOP,          // Salta para trás (usado para repetir o while)

    // --- Comparação e Lógica ---
    OP_EQUAL,         // Compara igualdade (==) entre os dois valores do topo
    OP_GREATER,       // Operação relacional: Maior que (>)
    OP_LESS,          // Operação relacional: Menor que (<)
    OP_NOT,           // Negação lógica (!). Inverte o booleano no topo.

    // --- Otimizações de Booleanos ---
    OP_TRUE,          // Empilha o valor literal 'true' (otimização de espaço)
    OP_FALSE          // Empilha o valor literal 'false' (otimização de espaço)
}