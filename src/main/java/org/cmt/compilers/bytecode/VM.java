// Arquivo: VM.java
package main.java.org.cmt.compilers.bytecode;

import main.java.org.cmt.compilers.Heuler;

import java.util.HashMap;
import java.util.Map;

/**
 * A Máquina Virtual (VM) da Heuler.
 * Executa o bytecode gerado pelo Compilador.
 * É uma VM baseada em pilha (stack-based).
 */
public class VM {

    private final Map<String, Object> globals = new HashMap<>();
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
                    pop(); // Apenas descarta o valor de retorno final (o 'nil')
                    return InterpretResult.INTERPRET_OK; // Termina silenciosamente
                }
                case OP_NIL: {
                    push(null); // 'null' do Java representa 'nil' da Heuler
                    break;
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

                case OP_PRINT: {
                    Object value = pop();
                    System.out.println(value);
                    break;
                }
                case OP_POP: {
                    pop(); // Apenas descarta o valor do topo da pilha
                    break;
                }
                case OP_DEFINE_GLOBAL: {
                    // O nome da variável vem da tabela de constantes
                    String name = readString();
                    // O valor está no topo da pilha (resultado da expressão inicializadora)
                    globals.put(name, pop());
                    break;
                }

                case OP_GET_GLOBAL: {
                    String name = readString();
                    if (!globals.containsKey(name)) {
                        // Erro de tempo de execução: Variável não definida
                        Heuler.error(chunk.getLine(ip), "Variável indefinida '" + name + "'.");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    push(globals.get(name));
                    break;
                }

                case OP_SET_GLOBAL: {
                    String name = readString();
                    if (!globals.containsKey(name)) {
                        Heuler.error(chunk.getLine(ip), "Variável indefinida '" + name + "'.");
                        return InterpretResult.INTERPRET_RUNTIME_ERROR;
                    }
                    Object value = peek(0); // Pega o valor sem remover (para permitir a = b = 1)
                    globals.put(name, value);
                    break;
                }
                case OP_GET_LOCAL: {
                    // O operando é o índice na pilha (slot) onde a variável está
                    int slot = readByte() & 0xFF;
                    push(stack[slot]); // Apenas copia o valor daquele slot para o topo
                    break;
                }

                case OP_SET_LOCAL: {
                    int slot = readByte() & 0xFF;
                    Object value = peek(0); // O novo valor está no topo
                    stack[slot] = value;    // Atualiza o slot específico
                    break;
                }
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
    private String readString() {
        return (String) readConstant();
    }

    // Helper para espreitar a pilha sem remover
    private Object peek(int distance) {
        return stack[stackTop - 1 - distance];
    }
}