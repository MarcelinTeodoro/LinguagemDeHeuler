// Arquivo: Resolver.java
package main.java.org.cmt.compilers.sintatico;

import main.java.org.cmt.compilers.Heuler;
import main.java.org.cmt.compilers.lexico.Token;
import main.java.org.cmt.compilers.sintatico.expressions.Expr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * O Resolver (Analisador Semântico) caminha pela AST para encontrar
 * erros semânticos (como uso de variáveis) e resolver a que escopo
 * cada variável pertence (local ou global).
 *
 * Ele implementa o Visitor para percorrer a AST.
 */
public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    // Usaremos uma Pilha de Mapas. Cada mapa representa um escopo.
    // O boolean indica se a variável já foi "definida" (inicializada).
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    // (Vamos precisar de uma referência ao Interpretador/VM mais tarde 
    // para saber se estamos em modo global ou local, mas começamos assim.)

    public Resolver() {
        // (Por enquanto, começamos com o escopo global implícito)
    }

    /**
     * Ponto de entrada: resolve uma lista de comandos (um programa).
     */
    public void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    // --- Métodos de Despacho (Helpers) ---

    private void resolve(Stmt stmt) {
        if (stmt != null) {
            stmt.accept(this);
        }
    }

    private void resolve(Expr expr) {
        if (expr != null) {
            expr.accept(this);
        }
    }

    // --- Métodos de Gestão de Escopo ---

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }


    // --- Visitantes (Implementação virá a seguir) ---
    // (Precisamos de implementar todos os métodos da interface, 
    //  mas vamos focar nos mais importantes primeiro)

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    // ... (Implementação dos outros Visitors virá nos próximos passos) ...

    /* Implementação de todos os outros métodos obrigatórios da interface */
    @Override public Void visitExpressionStmt(Stmt.Expression stmt) { resolve(stmt.expression); return null; }
    @Override public Void visitPrintStmt(Stmt.Print stmt) { resolve(stmt.expression); return null; }
    @Override public Void visitVarStmt(Stmt.Var stmt) { /* A lógica principal virá aqui */ return null; }
    @Override public Void visitIfStmt(Stmt.If stmt) { /* ... */ return null; }
    @Override public Void visitWhileStmt(Stmt.While stmt) { /* ... */ return null; }
    @Override public Void visitForStmt(Stmt.For stmt) { /* ... */ return null; }

    @Override public Void visitAssignExpr(Expr.Assign expr) { /* A lógica principal virá aqui */ return null; }
    @Override public Void visitVariableExpr(Expr.Variable expr) { /* A lógica principal virá aqui */ return null; }

    @Override public Void visitBinaryExpr(Expr.Binary expr) { resolve(expr.left); resolve(expr.right); return null; }
    @Override public Void visitGroupingExpr(Expr.Grouping expr) { resolve(expr.expression); return null; }
    @Override public Void visitLiteralExpr(Expr.Literal expr) { return null; } // Literais não fazem nada
    @Override public Void visitLogicalExpr(Expr.Logical expr) { resolve(expr.left); resolve(expr.right); return null; }
    @Override public Void visitUnaryExpr(Expr.Unary expr) { resolve(expr.right); return null; }

    // (Estes podem ficar vazios por enquanto)
    @Override public Void visitCallExpr(Expr.Call expr) { return null; }
    @Override public Void visitGetExpr(Expr.Get expr) { return null; }
    @Override public Void visitSetExpr(Expr.Set expr) { return null; }
    @Override public Void visitSuperExpr(Expr.Super expr) { return null; }
    @Override public Void visitThisExpr(Expr.This expr) { return null; }
}