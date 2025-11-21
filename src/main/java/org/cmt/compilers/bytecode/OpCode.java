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
    OP_ADD,        // Soma
    OP_SUBTRACT,   // Subtração
    OP_MULTIPLY,   // Multiplicação
    OP_DIVIDE,     // Divisão
    OP_PRINT,      //
    OP_POP,        //
    OP_NIL,
    OP_DEFINE_GLOBAL, // Cria uma nova variável global
    OP_GET_GLOBAL,    // Lê o valor de uma variável global
    OP_SET_GLOBAL,    // Atualiza o valor de uma variável global
    OP_GET_LOCAL, // Lê da pilha num índice específico
    OP_SET_LOCAL,  // Escreve na pilha num índice específico

    // --- CONTROLE DE FLUXO ---
    OP_JUMP_IF_FALSE, // Salta para a frente se o topo da pilha for falso
    OP_JUMP,          // Salta para a frente incondicionalmente (usado no else)
    OP_LOOP,           // Salta para trás (usado no while)

    OP_EQUAL,
    OP_GREATER,
    OP_LESS,
    OP_NOT,
    // à medida que avançamos no roadmap.
}
