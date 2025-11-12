package main.java.org.cmt.compilers.bytecode;
import java.util.ArrayList;
import java.util.List;

/**
 * Um "Chunk" armazena o bytecode compilado.
 * Contém a sequência de instruções (Opcodes) e a "tabela de constantes"
 * para os literais usados pelo código.
 */
public class Chunk {

    private final List<Byte> code;       // O array dinâmico de instruções (bytecode)
    private final List<Object> constants;  // A tabela de constantes
    private final List<Integer> lines;     // Mapeia o bytecode para as linhas do código-fonte

    public Chunk() {
        this.code = new ArrayList<>();
        this.constants = new ArrayList<>();
        this.lines = new ArrayList<>();
    }

    /**
     * Adiciona um byte (seja um OpCode ou um operando) ao chunk.
     * Também armazena a linha do código-fonte correspondente.
     */
    public void write(byte b, int line) {
        this.code.add(b);
        this.lines.add(line);
    }

    /**
     * Adiciona um valor (ex: número, string) à tabela de constantes.
     * Retorna o índice onde o valor foi adicionado, para que a
     * instrução OP_CONSTANT possa referenciá-lo.
     *
     * @return O índice da constante.
     */
    public int addConstant(Object value) {
        this.constants.add(value);
        return this.constants.size() - 1; // Retorna o índice do item recém-adicionado
    }

    // --- Getters que a VM usará ---

    public List<Byte> getCode() {
        return code;
    }

    public List<Object> getConstants() {
        return constants;
    }

    public int getLine(int offset) {
        return lines.get(offset);
    }
}
