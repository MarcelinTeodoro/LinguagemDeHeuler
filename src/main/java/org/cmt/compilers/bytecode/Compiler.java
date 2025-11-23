// Arquivo: Compiler.java
package main.java.org.cmt.compilers.bytecode;

import main.java.org.cmt.compilers.Heuler;
import main.java.org.cmt.compilers.lexico.Token;
import main.java.org.cmt.compilers.lexico.TokenType;
import main.java.org.cmt.compilers.sintatico.Stmt;
import main.java.org.cmt.compilers.sintatico.expressions.Expr;

import java.util.List;

/**
 * O Compilador.
 * Percorre a AST (árvore sintática) e emite o bytecode correspondente.
 * Implementa o padrão Visitor para traduzir cada nó da árvore.
 */
public class Compiler implements Expr.Visitor<Void>, Stmt.Visitor<Void> {



    private Chunk compilingChunk;
    private VM vm; // Precisamos da VM para o resultado

    public Compiler(VM vm) {
        this.vm = vm;
    }
    // Array para rastrear as locais ativas (simula a pilha)
    private Local[] locals = new Local[256];
    private int localCount = 0;
    private int scopeDepth = 0; // 0 = Global, > 0 = Local

    private static class Local {
        final Token name;
        final int depth; // Profundidade do escopo (0 = global, 1 = bloco, etc.)

        Local(Token name, int depth) {
            this.name = name;
            this.depth = depth;
        }
    }
    // 1. Adicionar uma local à lista do compilador
    private void addLocal(Token name) {
        if (localCount == 256) {
            // Erro: muitas variáveis locais (limitação simples)
            return;
        }
        locals[localCount++] = new Local(name, scopeDepth);
    }

    // 2. Tentar encontrar o índice de uma local (resolveLocal)
    private int resolveLocal(Token name) {
        // Procura do fim para o começo (para garantir o shadowing correto)
        for (int i = localCount - 1; i >= 0; i--) {
            Local local = locals[i];
            if (name.lexeme.equals(local.name.lexeme)) {
                return i; // Encontrou! Retorna o índice da pilha.
            }
        }
        return -1; // Não é local (provavelmente é global)
    }

    /**
     * Ponto de entrada principal do Compilador.
     * @param statements A lista de comandos (AST) vinda do Parser.
     * @return true se a compilação foi bem-sucedida.
     */
    public boolean compile(List<Stmt> statements) {
        this.compilingChunk = new Chunk();

        try {
            for (Stmt statement : statements) {
                if (statement != null) { // Ignora 'null's de erros de parsing
                    compile(statement);
                }
            }

            // No final, emitimos uma instrução de retorno para terminar a execução.
            emitReturn();
            return true;

        } catch (CompileError error) {
            // (Vamos adicionar um tratamento de erro de compilação mais tarde)
            return false;
        }
    }

    // Método auxiliar para obter o chunk atual
    private Chunk currentChunk() {
        return this.compilingChunk;
    }

    // --- Métodos de Emissão de Bytecode ---

    private void emitByte(byte b) {
        // (Assume-se que a linha é a do último token processado - vamos refinar isto)
        currentChunk().write(b, 1); // Linha temporária
    }


    private void emitReturn() {
        emitByte((byte) OpCode.OP_NIL.ordinal()); // Coloca um 'nil' padrão na pilha
        emitByte((byte) OpCode.OP_RETURN.ordinal());
    }
    private void beginScope() {
        scopeDepth++;
    }

    private void endScope() {
        scopeDepth--;

        // Descarta as variáveis que saíram de escopo
        // Emitimos OP_POP para cada variável que estava neste nível
        while (localCount > 0 && locals[localCount - 1].depth > scopeDepth) {
            emitByte((byte)OpCode.OP_POP.ordinal());
            localCount--;
        }
    }
    // Emite um salto para frente e retorna o índice do placeholder para ser remendado depois
    private int emitJump(OpCode instruction) {
        emitByte((byte)instruction.ordinal());
        emitByte((byte) 0xff); // Placeholder byte 1
        emitByte((byte) 0xff); // Placeholder byte 2
        return currentChunk().getCode().size() - 2;
    }

    // Volta ao 'offset' e escreve a distância correta até o ponto atual
    private void patchJump(int offset) {
        // -2 para ajustar o próprio tamanho do offset do salto
        int jump = currentChunk().getCode().size() - offset - 2;

        if (jump > 65535) {
            // Erro simples se o código for grande demais
            throw new CompileError();
        }

        // Escreve os dois bytes do short
        currentChunk().getCode().set(offset, (byte)((jump >> 8) & 0xff));
        currentChunk().getCode().set(offset + 1, (byte)(jump & 0xff));
    }

    // Emite um salto para trás (loop)
    private void emitLoop(int loopStart) {
        emitByte((byte) OpCode.OP_LOOP.ordinal());

        int offset = currentChunk().getCode().size() - loopStart + 2;
        if (offset > 65535) throw new CompileError();

        emitByte((byte)((offset >> 8) & 0xff));
        emitByte((byte)(offset & 0xff));
    }

    // --- Compilando Comandos (Stmt.Visitor) ---

    // Método de despacho genérico
    private void compile(Stmt stmt) {
        stmt.accept(this);
    }

    // Método de despacho genérico
    private void compile(Expr expr) {
        expr.accept(this);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        compile(stmt.expression);
        emitByte((byte) OpCode.OP_POP.ordinal()); // Descarta o valor da expressão
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        compile(stmt.expression);
        emitByte((byte)OpCode.OP_PRINT.ordinal());
        return null;
    }

    // --- Compilando Expressões (Expr.Visitor) ---

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) {
            emitByte((byte)OpCode.OP_NIL.ordinal());
        } else if (Boolean.TRUE.equals(expr.value)) {
            emitByte((byte)OpCode.OP_TRUE.ordinal());
        } else if (Boolean.FALSE.equals(expr.value)) {
            emitByte((byte)OpCode.OP_FALSE.ordinal());
        } else {
            // Apenas números e strings vão para a tabela de constantes
            int index = currentChunk().addConstant(expr.value);
            emitByte((byte)OpCode.OP_CONSTANT.ordinal());
            emitByte((byte)index);
        }
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        compile(expr.expression); // Apenas compila a expressão interna
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        compile(expr.right); // Compila o operando primeiro

        // Emite a instrução unária
        switch (expr.operator.type) {
            case Minus: emitByte((byte) OpCode.OP_NEGATE.ordinal()); break;
            case Bang:  emitByte((byte) OpCode.OP_NOT.ordinal()); break;
            // (Adicionaremos OP_NOT para '!' aqui)
        }
        return null;
    }


    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        // Compila o operando esquerdo
        compile(expr.left);
        // Compila o operando direito
        compile(expr.right);

        // Emite a instrução binária
        switch (expr.operator.type) {
            case Plus:    emitByte((byte)OpCode.OP_ADD.ordinal()); break;
            case Minus:   emitByte((byte)OpCode.OP_SUBTRACT.ordinal()); break;
            case Star:    emitByte((byte)OpCode.OP_MULTIPLY.ordinal()); break;
            case Slash:   emitByte((byte)OpCode.OP_DIVIDE.ordinal()); break;
            // --- NOVOS OPERADORES ---
            case EqualEqual:   emitByte((byte)OpCode.OP_EQUAL.ordinal()); break;
            case Greater:      emitByte((byte)OpCode.OP_GREATER.ordinal()); break;
            case Less:         emitByte((byte)OpCode.OP_LESS.ordinal()); break;
            // Para >= usamos < e invertemos (not)
            case GreaterEqual: emitByte((byte)OpCode.OP_LESS.ordinal()); emitByte((byte)OpCode.OP_NOT.ordinal()); break;
            // Para <= usamos > e invertemos (not)
            case LessEqual:    emitByte((byte)OpCode.OP_GREATER.ordinal()); emitByte((byte)OpCode.OP_NOT.ordinal()); break;
            case BangEqual:    emitByte((byte)OpCode.OP_EQUAL.ordinal()); emitByte((byte)OpCode.OP_NOT.ordinal()); break;
        }
        return null;
    }
    public Chunk getCompiledChunk() {
        return compilingChunk;
    }

    // --- Métodos de Visitor Não Implementados (Ainda) ---
    // (Precisamos deles para o código compilar, mas a lógica virá depois)

    // Stmt
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        for (Stmt statement : stmt.statements) {
            compile(statement);
        }
        endScope();
        return null;
    }
    // --- Declaração de Variável (var a = 1;) ---

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        // Compila o inicializador (coloca o valor na pilha)
        if (stmt.initializer != null) {
            compile(stmt.initializer);
        } else {
            emitByte((byte)OpCode.OP_NIL.ordinal());
        }

        if (scopeDepth > 0) {
            // É LOCAL: Não emitimos código! O valor já está na pilha.
            // Apenas registramos que esse slot da pilha agora tem nome.
            addLocal(stmt.name);
        } else {
            // É GLOBAL
            int nameIndex = currentChunk().addConstant(stmt.name.lexeme);
            emitByte((byte)OpCode.OP_DEFINE_GLOBAL.ordinal());
            emitByte((byte)nameIndex);
        }
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        // 1. Compila a condição
        compile(stmt.condition);

        // 2. Emite salto: Se falso, salta para o 'else' (ou fim)
        // jumpToElse: guardamos a posição para remendar depois
        int jumpToElse = emitJump(OpCode.OP_JUMP_IF_FALSE);

        // 3. Retira a condição da pilha (já foi usada pelo JUMP_IF_FALSE se for true)
        emitByte((byte)OpCode.OP_POP.ordinal());

        // 4. Compila o bloco 'then'
        compile(stmt.thenBranch);

        // 5. Emite salto: Ao fim do 'then', salta para o fim total (pula o else)
        int jumpToEnd = emitJump(OpCode.OP_JUMP);

        // 6. Remenda o primeiro salto (agora sabemos onde o else começa)
        patchJump(jumpToElse);

        // 7. Retira a condição da pilha (se saltámos para cá, ela ainda estava lá)
        emitByte((byte) OpCode.OP_POP.ordinal());

        // 8. Compila o bloco 'else' (se existir)
        if (stmt.elseBranch != null) {
            compile(stmt.elseBranch);
        }

        // 9. Remenda o segundo salto (agora sabemos onde o fim é)
        patchJump(jumpToEnd);

        return null;
    }
    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        // 1. Marca o início do loop
        int loopStart = currentChunk().getCode().size();

        // 2. Compila a condição
        compile(stmt.condition);

        // 3. Emite salto de saída: Se falso, sai do loop
        int exitJump = emitJump(OpCode.OP_JUMP_IF_FALSE);

        // 4. Pop da condição (se for true)
        emitByte((byte)OpCode.OP_POP.ordinal());

        // 5. Compila o corpo
        compile(stmt.body);

        // 6. Emite salto de volta para o início
        emitLoop(loopStart);

        // 7. Remenda o salto de saída
        patchJump(exitJump);

        // 8. Pop da condição (se saltou para fora)
        emitByte((byte)OpCode.OP_POP.ordinal());

        return null;
    }
    @Override
    public Void visitForStmt(Stmt.For stmt) {
        // 1. Criar um escopo para as variáveis de controle do loop (o iterador e o limite)
        beginScope();

        // --- A. Definir o Limite ($limit) ---
        // Compila a expressão 'iterable' (ex: o número 5). O valor fica no topo da pilha.
        compile(stmt.iterable);

        // Criamos uma variável local oculta para armazenar esse limite.
        // Usamos um nome que o usuário não pode digitar ($) para evitar conflitos.
        Token limitVar = new Token(TokenType.Identifier, "$limit", null, 0, 0);
        addLocal(limitVar); // O compilador agora sabe que o slot X da pilha é o $limit

        // --- B. Definir o Iterador (i) ---
        // Coloca o valor inicial 0 na pilha.
        emitByte((byte)OpCode.OP_CONSTANT.ordinal());
        int zeroIndex = currentChunk().addConstant(0.0);
        emitByte((byte)zeroIndex);

        // Define a variável do usuário (ex: 'i') apontando para esse 0.
        addLocal(stmt.iterator);

        // --- C. Início do Loop ---
        int loopStart = currentChunk().getCode().size();

        // --- D. Condição (i < limit) ---
        // Precisamos ler as variáveis locais da pilha para comparar.
        int iterSlot = resolveLocal(stmt.iterator);
        int limitSlot = resolveLocal(limitVar);

        // Carrega i
        emitByte((byte)OpCode.OP_GET_LOCAL.ordinal());
        emitByte((byte)iterSlot);

        // Carrega limit
        emitByte((byte)OpCode.OP_GET_LOCAL.ordinal());
        emitByte((byte)limitSlot);

        // Verifica i < limit
        emitByte((byte)OpCode.OP_LESS.ordinal());

        // --- E. Saída ---
        int exitJump = emitJump(OpCode.OP_JUMP_IF_FALSE);
        emitByte((byte)OpCode.OP_POP.ordinal()); // Descarta o resultado da comparação (true)

        // --- F. Corpo ---
        compile(stmt.body);

        // --- G. Incremento (i = i + 1) ---
        // Carrega i
        emitByte((byte)OpCode.OP_GET_LOCAL.ordinal());
        emitByte((byte)iterSlot);

        // Carrega 1
        emitByte((byte)OpCode.OP_CONSTANT.ordinal());
        int oneIndex = currentChunk().addConstant(1.0);
        emitByte((byte)oneIndex);

        // Soma
        emitByte((byte)OpCode.OP_ADD.ordinal());

        // Atualiza i na pilha
        emitByte((byte)OpCode.OP_SET_LOCAL.ordinal());
        emitByte((byte)iterSlot);
        emitByte((byte)OpCode.OP_POP.ordinal()); // O SET deixa o valor na pilha, precisamos descartar

        // --- H. Loop Back ---
        emitLoop(loopStart);

        // --- I. Finalização ---
        patchJump(exitJump);
        emitByte((byte)OpCode.OP_POP.ordinal()); // Descarta o resultado da comparação (false)

        endScope(); // Descarta 'i' e '$limit' da pilha
        return null;
    }

    // Expr
    // --- Acesso a Variável (print a;) ---
    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        // Tenta resolver como local primeiro
        int arg = resolveLocal(expr.name);

        if (arg != -1) {
            // É LOCAL
            emitByte((byte)OpCode.OP_GET_LOCAL.ordinal());
            emitByte((byte)arg);
        } else {
            // É GLOBAL
            int nameIndex = currentChunk().addConstant(expr.name.lexeme);
            emitByte((byte)OpCode.OP_GET_GLOBAL.ordinal());
            emitByte((byte)nameIndex);
        }
        return null;
    }
    // --- Atribuição (a = 2;) ---
    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        compile(expr.value); // Valor na pilha

        int arg = resolveLocal(expr.name);

        if (arg != -1) {
            // É LOCAL
            emitByte((byte)OpCode.OP_SET_LOCAL.ordinal());
            emitByte((byte)arg);
        } else {
            // É GLOBAL
            int nameIndex = currentChunk().addConstant(expr.name.lexeme);
            emitByte((byte)OpCode.OP_SET_GLOBAL.ordinal());
            emitByte((byte)nameIndex);
        }
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        // 1. Compila o lado esquerdo
        compile(expr.left);

        // 2. Verifica se podemos fazer curto-circuito (short-circuit)
        // AND: Se a esquerda for false, todo o resultado é false -> salta para o fim.
        // OR:  Se a esquerda for true, todo o resultado é true  -> salta para o fim.

        int endJump = -1;

        if (expr.operator.type == TokenType.And) {
            endJump = emitJump(OpCode.OP_JUMP_IF_FALSE);
        } else {
            // Para o OR é um pouco mais subtil:
            // Queremos saltar se for VERDADEIRO.
            // Como só temos JUMP_IF_FALSE, vamos fazer:
            // JUMP_IF_FALSE (para o próximo passo)
            // JUMP (para o fim) -> Achou verdadeiro!
            // próximo passo: continua a avaliação

            int elseJump = emitJump(OpCode.OP_JUMP_IF_FALSE);
            int end = emitJump(OpCode.OP_JUMP);

            patchJump(elseJump);
            endJump = end;
        }

        // 3. Se não houve curto-circuito, descartamos o valor da esquerda
        // e avaliamos o da direita.
        emitByte((byte) OpCode.OP_POP.ordinal());

        compile(expr.right);

        // 4. Remenda o salto do curto-circuito
        patchJump(endJump);

        return null;
    }
    //implementações futuras
    @Override public Void visitCallExpr(Expr.Call expr) { return null; }
    @Override public Void visitGetExpr(Expr.Get expr) { return null; }
    @Override public Void visitSetExpr(Expr.Set expr) { return null; }
    @Override public Void visitThisExpr(Expr.This expr) { return null; }
    @Override public Void visitSuperExpr(Expr.Super expr) { return null; }



    // Classe de erro interna
    public static class CompileError extends RuntimeException {}
}