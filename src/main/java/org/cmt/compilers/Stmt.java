
package main.java.org.cmt.compilers;

import main.java.org.cmt.compilers.expressions.Expr;
import java.util.List;

public abstract class Stmt {

    public interface Visitor<R> {
        R visitBlockStmt(Block stmt);
        R visitExpressionStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
        R visitVarStmt(Var stmt);
        R visitIfStmt(If stmt);       // <-- ADICIONE ESTA LINHA
        R visitWhileStmt(While stmt);
        // Adicionaremos IfStmt, WhileStmt, etc., aqui mais tarde.
    }

    public abstract <R> R accept(Visitor<R> visitor);

    // --- CLASSES ANINHADAS PARA CADA TIPO DE COMANDO ---

    // Para um bloco de código: { ... }
    public static class Block extends Stmt {
        public final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    // Para um comando de expressão: expressao;
    public static class Expression extends Stmt {
        public final Expr expression;

        public Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    // Para o comando de impressão (vamos manter o 'print' por enquanto)
    public static class Print extends Stmt {
        public final Expr expression;

        public Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    // Para a declaração de uma variável: tipo id; ou var id = expr;
    public static class Var extends Stmt {
        public final Token name;
        public final Expr initializer; // Pode ser null se não houver inicializador

        public Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }
    // Para o comando condicional: if (cond) ... else ...
    public static class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch; // Pode ser null

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    // Para o comando de repetição: while (cond) ...
    public static class While extends Stmt {
        public final Expr condition;
        public final Stmt body;

        public While(Stmt body, Expr condition) {
            this.body = body;
            this.condition = condition;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }
}
