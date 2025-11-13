// Arquivo: Compiler.java
package main.java.org.cmt.compilers.bytecode;

import main.java.org.cmt.compilers.Heuler;
import main.java.org.cmt.compilers.lexico.Token;
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
        // Adiciona o valor literal (ex: 123.0) à tabela de constantes
        int index = currentChunk().addConstant(expr.value);
        // Emite a instrução OP_CONSTANT seguida do índice
        emitByte((byte)OpCode.OP_CONSTANT.ordinal());
        emitByte((byte)index);
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
            // (Adicionaremos operadores de comparação aqui)
        }
        return null;
    }
    public Chunk getCompiledChunk() {
        return compilingChunk;
    }

    // --- Métodos de Visitor Não Implementados (Ainda) ---
    // (Precisamos deles para o código compilar, mas a lógica virá depois)

    // Stmt
    @Override public Void visitBlockStmt(Stmt.Block stmt) { return null; }
    @Override public Void visitVarStmt(Stmt.Var stmt) { return null; }
    @Override public Void visitIfStmt(Stmt.If stmt) { return null; }
    @Override public Void visitWhileStmt(Stmt.While stmt) { return null; }
    @Override public Void visitForStmt(Stmt.For stmt) { return null; }

    // Expr
    @Override public Void visitVariableExpr(Expr.Variable expr) { return null; }
    @Override public Void visitAssignExpr(Expr.Assign expr) { return null; }
    @Override public Void visitLogicalExpr(Expr.Logical expr) { return null; }
    @Override public Void visitCallExpr(Expr.Call expr) { return null; }
    @Override public Void visitGetExpr(Expr.Get expr) { return null; }
    @Override public Void visitSetExpr(Expr.Set expr) { return null; }
    @Override public Void visitThisExpr(Expr.This expr) { return null; }
    @Override public Void visitSuperExpr(Expr.Super expr) { return null; }



    // Classe de erro interna
    public static class CompileError extends RuntimeException {}
}