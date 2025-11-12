// Arquivo: VM.java
package main.java.org.cmt.compilers.bytecode;

import main.java.org.cmt.compilers.Heuler;

/**
 * A Máquina Virtual (VM) da Heuler.
 * Executa o bytecode gerado pelo Compilador.
 * É uma VM baseada em pilha (stack-based).
 */
public class VM {

    private static final int STACK_MAX = 256; // Tamanho máximo da pilha
    private Chunk chunk; // O bytecode que estamos a executar
    private int ip;      // Instruction Pointer: aponta para a próxima instrução

    private Object[] stack = new Object[STACK_MAX]; // A pilha de valores
    private int stackTop; // Aponta para o topo da pilha (logo acima do último item)

    public VM() {
        this.stackTop = 0;
    }

    /**
     * O ponto de entrada principal para a VM.
     * @param chunk O bytecode a ser executado.
     * @return O resultado da interpretação.
     */
    public InterpretResult interpret(Chunk chunk) {
        this.chunk = chunk;
        this.ip = 0; // Começa na primeira instrução
        return run();
    }

    /**
     * O loop principal de execução (despacho de bytecode).
     * Lê e executa instruções até terminar.
     */
    private InterpretResult run() {
        for (;;) {
            byte instruction = readByte();

            // Converte o byte para um OpCode
            OpCode op = OpCode.values()[instruction];

            switch (op) {
                case OP_RETURN: {
                    // Por enquanto, OP_RETURN termina a VM e imprime o resultado
                    Object result = pop();
                    System.out.println("Resultado da Execução: " + result);
                    return InterpretResult.INTERPRET_OK;
                }

                case OP_CONSTANT: {
                    Object constant = readConstant();
                    push(constant);
                    break;
                }

                case OP_NEGATE: {
                    Object value = pop();
                    push(-(double)value); // Assume que é um número
                    break;
                }

                case OP_ADD:      binaryOp('+'); break;
                case OP_SUBTRACT: binaryOp('-'); break;
                case OP_MULTIPLY: binaryOp('*'); break;
                case OP_DIVIDE:   binaryOp('/'); break;
            }
        }
    }

    // --- Funções Auxiliares da VM ---

    private void binaryOp(char op) {
        // Lógica LIFO: o operando da direita é 'b', o da esquerda é 'a'
        Object b = pop();
        Object a = pop();

        // Verificação de tipo simples (vamos melhorar depois)
        if (!(a instanceof Double) || !(b instanceof Double)) {
            Heuler.error(0, "Operandos devem ser números."); // Linha de erro temporária
            return; // Precisamos de um InterpretResult de erro aqui
        }

        switch (op) {
            case '+': push((double)a + (double)b); break;
            case '-': push((double)a - (double)b); break;
            case '*': push((double)a * (double)b); break;
            case '/': push((double)a / (double)b); break;
        }
    }

    // Lê o próximo byte e avança o ponteiro de instrução
    private byte readByte() {
        return this.chunk.getCode().get(ip++);
    }

    // Lê uma constante da tabela de constantes
    private Object readConstant() {
        // Lê o índice da constante (que é o próximo byte após OP_CONSTANT)
        int constantIndex = readByte() & 0xFF; // Converte byte para int (0-255)
        return this.chunk.getConstants().get(constantIndex);
    }

    // --- Funções da Pilha (Stack) ---

    private void push(Object value) {
        if (stackTop == STACK_MAX) {
            Heuler.error(0, "Stack overflow!"); // Linha de erro temporária
            return;
        }
        this.stack[stackTop] = value;
        this.stackTop++;
    }

    private Object pop() {
        if (stackTop == 0) {
            Heuler.error(0, "Stack underflow!"); // Linha de erro temporária
            return null;
        }
        this.stackTop--;
        return this.stack[stackTop];
    }
}