package main.java.org.cmt.compilers.bytecode;

public class Debug {

    // Imprime todo o Chunk (cabeçalho + instruções)
    public static void disassembleChunk(Chunk chunk, String name) {
        System.out.println("== " + name + " ==");

        for (int offset = 0; offset < chunk.getCode().size(); ) {
            offset = disassembleInstruction(chunk, offset);
        }
        System.out.println("== Fim do Chunk ==\n");
    }

    // Imprime uma única instrução e retorna o offset da próxima
    public static int disassembleInstruction(Chunk chunk, int offset) {
        System.out.printf("%04d ", offset); // Imprime o índice do byte (ex: 0005)

        // Imprime a linha do código fonte
        int line = chunk.getLine(offset);
        if (offset > 0 && line == chunk.getLine(offset - 1)) {
            System.out.print("   | "); // Mesma linha da instrução anterior
        } else {
            System.out.printf("%4d ", line);
        }

        byte instruction = chunk.getCode().get(offset);
        if (instruction < 0 || instruction >= OpCode.values().length) {
            System.out.println("Opcode desconhecido " + instruction);
            return offset + 1;
        }

        OpCode op = OpCode.values()[instruction];

        switch (op) {
            // Instruções simples (sem operandos)
            case OP_RETURN:
            case OP_NIL:
            case OP_TRUE:
            case OP_FALSE:
            case OP_POP:
            case OP_PRINT:
            case OP_NEGATE:
            case OP_NOT:
            case OP_ADD:
            case OP_SUBTRACT:
            case OP_MULTIPLY:
            case OP_DIVIDE:
            case OP_EQUAL:
            case OP_GREATER:
            case OP_LESS:
                return simpleInstruction(op.name(), offset);

            // Instruções com 1 operando (índice de constante ou slot de variável)
            case OP_CONSTANT:
            case OP_DEFINE_GLOBAL:
            case OP_GET_GLOBAL:
            case OP_SET_GLOBAL:
                return constantInstruction(op.name(), chunk, offset);

            case OP_GET_LOCAL:
            case OP_SET_LOCAL:
                return byteInstruction(op.name(), chunk, offset);

            // Instruções de Salto (2 bytes de operando)
            case OP_JUMP:
            case OP_JUMP_IF_FALSE:
            case OP_LOOP:
                return jumpInstruction(op.name(), 1, chunk, offset);

            default:
                System.out.println("Opcode desconhecido " + instruction);
                return offset + 1;
        }
    }

    // --- Formatadores ---

    private static int simpleInstruction(String name, int offset) {
        System.out.println(name);
        return offset + 1;
    }

    private static int constantInstruction(String name, Chunk chunk, int offset) {
        int constantIndex = chunk.getCode().get(offset + 1) & 0xFF; // Pega o operando (índice)
        System.out.printf("%-16s %4d '", name, constantIndex);
        Object value = chunk.getConstants().get(constantIndex);
        System.out.print(value);
        System.out.println("'");
        return offset + 2; // Opcode + 1 byte de operando
    }

    private static int byteInstruction(String name, Chunk chunk, int offset) {
        int slot = chunk.getCode().get(offset + 1) & 0xFF;
        System.out.printf("%-16s %4d\n", name, slot);
        return offset + 2;
    }

    private static int jumpInstruction(String name, int sign, Chunk chunk, int offset) {
        int jump = (chunk.getCode().get(offset + 1) & 0xFF) << 8 |
                (chunk.getCode().get(offset + 2) & 0xFF);
        System.out.printf("%-16s %4d -> %d\n", name, offset,
                offset + 3 + sign * jump);
        return offset + 3; // Opcode + 2 bytes de operando
    }
}